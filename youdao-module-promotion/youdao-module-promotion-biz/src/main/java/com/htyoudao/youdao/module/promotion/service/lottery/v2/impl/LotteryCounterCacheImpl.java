package com.htyoudao.youdao.module.promotion.service.lottery.v2.impl;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.LOTTERY_SYSTEM_AGAIN;

/** 次数缓存仅用于展示；受理和任务机会仍由持久化账本条件更新。 */
@Service("lotteryCounterCache")
@Slf4j
public class LotteryCounterCacheImpl implements LotteryCounterCache {
    @Resource
    private JdbcTemplate jdbc;
    @Resource
    private StringRedisTemplate redis;
    @Resource
    private ObjectMapper json;
    @Resource
    private LotteryCacheExecutor executor;
    private final AtomicInteger refills=new AtomicInteger();
    private static final DefaultRedisScript<Long> HEAD=new DefaultRedisScript<>(
            "local old=redis.call('GET',KEYS[1]); if old and tonumber(old)>tonumber(ARGV[1]) then return 0 end; redis.call('SET',KEYS[1],ARGV[1]); redis.call('SADD',KEYS[2],KEYS[1]); return 1",Long.class);
    private static final DefaultRedisScript<Long> SNAPSHOT=new DefaultRedisScript<>(
            "local old=redis.call('GET',KEYS[1]); if old and tonumber(old)>tonumber(ARGV[1]) then return 0 end; redis.call('SET',KEYS[1],ARGV[1]); redis.call('SET',KEYS[2],ARGV[2],'EX',ARGV[3]); redis.call('SET',KEYS[3],ARGV[4]); redis.call('SADD',KEYS[4],KEYS[1],KEYS[2],KEYS[3]); return 1",Long.class);

    @Override
    public void changed(long b,long a,long member) {
        long revision=jdbc.queryForObject("SELECT revision FROM lottery_v2_counter WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key='TOTAL' AND source=0",Long.class,b,a,member);
        jdbc.update("UPDATE lottery_v2_counter SET cache_dirty=1,cache_next_at=NOW(3) WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key='TOTAL' AND source=0",b,a,member);
        // 提交后先同步推进缓存版本，任务接口返回时下一次查询即使用新次数。
        // 清除脏标记在独立工作线程执行，避免 afterCommit 复用已提交事务的连接。
        Runnable publish=()->{try{
            publishHead(b,a,member,revision);
            executor.submit(()->{try{
                jdbc.update("UPDATE lottery_v2_counter SET cache_dirty=0 WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key='TOTAL' AND source=0 AND revision=?",b,a,member,revision);
            }catch(Exception e){log.warn("Lottery quota acknowledgment retained: activity={}, member={}, reason={}",a,member,e.getClass().getSimpleName());}});
        }catch(Exception e){log.warn("Lottery quota publication retained: activity={}, member={}, reason={}",a,member,e.getClass().getSimpleName());}};
        if(TransactionSynchronizationManager.isActualTransactionActive())TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(){
            @Override
            public void afterCommit(){publish.run();}
        });
        else publish.run();
    }
    private void publishHead(long b,long a,long m,long revision){redis.execute(HEAD,List.of(head(b,a,m),registry(b,a)),Long.toString(revision));}
    @Override
    public Map<String,long[]> read(long b,long a,long m,String period) {
        String head=head(b,a,m),version=redis.opsForValue().get(head);
        if(version!=null){String cached=redis.opsForValue().get(view(b,a,m,period,version));if(cached!=null)return decode(cached);}
        if(refills.incrementAndGet()>4){refills.decrementAndGet();throw exception(LOTTERY_SYSTEM_AGAIN);}
        String lock=head+":refill",token=UUID.randomUUID().toString();boolean locked=false;
        try {
            locked=Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(lock,token,Duration.ofSeconds(5)));
            if(!locked)throw exception(LOTTERY_SYSTEM_AGAIN);
            Map<String,long[]> data=new HashMap<>();long revision=0;
            var rows=jdbc.queryForList("SELECT c.scope_key,c.source,c.gained,c.consumed,c.finished,t.revision FROM lottery_v2_counter c JOIN lottery_v2_counter t ON t.business_id=c.business_id AND t.activity_id=c.activity_id AND t.member_id=c.member_id AND t.scope_key='TOTAL' AND t.source=0 WHERE c.business_id=? AND c.activity_id=? AND c.member_id=? AND c.scope_key IN ('TOTAL',?)",b,a,m,period);
            for(var row:rows){revision=((Number)row.get("revision")).longValue();data.put(row.get("scope_key")+":"+row.get("source"),new long[]{num(row,"gained"),num(row,"consumed"),num(row,"finished")});}
            Map<String,long[]> total=new HashMap<>();data.forEach((k,v)->{if(k.startsWith("TOTAL:"))total.put(k,v);});
            Long applied=redis.execute(SNAPSHOT,List.of(head,view(b,a,m,period,Long.toString(revision)),LotteryKeys.key(b,a,"quota-total",m),registry(b,a)),
                    Long.toString(revision),encode(data),Long.toString(rows.isEmpty()?15:ttl(period)),encode(total));
            if(!Objects.equals(applied,1L))throw exception(LOTTERY_SYSTEM_AGAIN);
            return data;
        }finally{refills.decrementAndGet();if(locked)redis.execute(new DefaultRedisScript<>("if redis.call('GET',KEYS[1])==ARGV[1] then return redis.call('DEL',KEYS[1]) end return 0",Long.class),List.of(lock),token);}
    }
    /** 仅供人工恢复调用；正常空闲时不扫描数据库。 */
    @Override
    public void retry() {
        try{for(var row:jdbc.queryForList("SELECT business_id,activity_id,member_id,revision FROM lottery_v2_counter WHERE scope_key='TOTAL' AND source=0 AND cache_dirty=1 AND cache_next_at<=NOW(3) ORDER BY cache_next_at LIMIT 64")){
            long b=num(row,"business_id"),a=num(row,"activity_id"),m=num(row,"member_id"),v=num(row,"revision");
            try{publishHead(b,a,m,v);jdbc.update("UPDATE lottery_v2_counter SET cache_dirty=0 WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key='TOTAL' AND source=0 AND revision=?",b,a,m,v);}
            catch(Exception e){jdbc.update("UPDATE lottery_v2_counter SET cache_next_at=DATE_ADD(NOW(3),INTERVAL 10 SECOND) WHERE business_id=? AND activity_id=? AND member_id=? AND scope_key='TOTAL' AND source=0 AND revision=?",b,a,m,v);}
        }}catch(Exception e){log.debug("Lottery quota publisher unavailable: {}",e.getClass().getSimpleName());}
    }
    private long ttl(String period){
        try {
            LocalDate day=LocalDate.parse(period.substring(0,8),DateTimeFormatter.BASIC_ISO_DATE);
            LocalDateTime end=day.plusDays(1).atStartOfDay();
            if(period.length()==17&&!period.endsWith("2400"))end=day.atTime(Integer.parseInt(period.substring(13,15)),Integer.parseInt(period.substring(15,17)));
            return Math.max(15,Duration.between(LocalDateTime.now(),end.plusDays(1)).getSeconds());
        }catch(Exception e){return 86400;}
    }
    private long num(Map<String,Object> row,String k){return ((Number)row.get(k)).longValue();}
    private String encode(Object value){try{return json.writeValueAsString(value);}catch(Exception e){throw new IllegalStateException(e);}}
    private Map<String,long[]> decode(String value){try{return json.readValue(value,new TypeReference<>(){});}catch(Exception e){throw new IllegalStateException(e);}}
    private String head(long b,long a,long m){return LotteryKeys.key(b,a,"quota-version",m);}
    private String view(long b,long a,long m,String period,String version){return LotteryKeys.key(b,a,"quota",m+":"+period+":"+version);}
    private static String registry(long b,long a){return LotteryKeys.mutableRegistry(b,a);}
}
