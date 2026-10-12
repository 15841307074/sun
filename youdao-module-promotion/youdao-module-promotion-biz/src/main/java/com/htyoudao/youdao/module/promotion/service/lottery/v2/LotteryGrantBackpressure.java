package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.LOTTERY_SYSTEM_AGAIN;

/** 八实例共享发奖压力快照；只在新抽奖请求到来时刷新，不新增空闲扫描。 */
@Component
@DS("master")
@Slf4j
public class LotteryGrantBackpressure {
    private static final int HIGH=100,LOW=30;
    private static final long REFRESH_MILLIS=2000;
    private static final String SNAPSHOT=LotteryKeys.key(0,0,"grant-pressure","snapshot");
    private static final String LOCK=LotteryKeys.key(0,0,"grant-pressure","refresh-lock");
    private static final DefaultRedisScript<Long> SAVE=new DefaultRedisScript<>(
            "local old=redis.call('GET',KEYS[1]); local paused=0; if old then paused=tonumber(string.match(old,'^%d+:([01]):%d+$')) or 1 end; local n=tonumber(ARGV[1]); if n>=tonumber(ARGV[3]) then paused=1 elseif n<=tonumber(ARGV[4]) then paused=0 end; redis.call('SET',KEYS[1],ARGV[1]..':'..paused..':'..ARGV[2],'EX',600); return paused",Long.class);
    private static final DefaultRedisScript<Long> UNLOCK=new DefaultRedisScript<>(
            "if redis.call('GET',KEYS[1])==ARGV[1] then return redis.call('DEL',KEYS[1]) end; return 0",Long.class);
    @Resource private StringRedisTemplate redis;
    @Resource private JdbcTemplate jdbc;
    private LongSupplier clock=System::currentTimeMillis;
    private final ThreadPoolExecutor refreshExecutor=new ThreadPoolExecutor(0,1,30,TimeUnit.SECONDS,
            new SynchronousQueue<>(),task->{Thread thread=new Thread(task,"lottery-grant-pressure");thread.setDaemon(true);return thread;});
    private record Snapshot(boolean paused,long at) {}

    public void checkDrawAllowed() {
        long now=clock.getAsLong();
        Snapshot snapshot=parse(redis.opsForValue().get(SNAPSHOT));
        if(snapshot!=null&&now>=snapshot.at()&&now-snapshot.at()<REFRESH_MILLIS){check(snapshot.paused());return;}
        String token=UUID.randomUUID().toString();
        if(Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(LOCK,token,Duration.ofSeconds(60)))) {
            try {refreshExecutor.execute(()->refresh(token));}
            catch(RejectedExecutionException busy){unlock(token);}
        }
        // 数据库刷新只在后台进行；请求最多使用五秒内的旧快照，过旧则拒绝。
        check(snapshot==null||now<snapshot.at()||now-snapshot.at()>5000||snapshot.paused());
    }

    private void refresh(String token) {
        DynamicDataSourceContextHolder.push("master");
        try {
            int count=pendingCount();
            Long paused=redis.execute(SAVE,List.of(SNAPSHOT),Integer.toString(count),Long.toString(clock.getAsLong()),Integer.toString(HIGH),Integer.toString(LOW));
            if(paused==null)throw new IllegalStateException("未写入发奖积压快照");
        } catch(RuntimeException e) {
            try {redis.opsForValue().set(SNAPSHOT,HIGH+":1:"+clock.getAsLong(),Duration.ofMinutes(10));}
            catch(RuntimeException redisUnavailable){log.warn("Lottery grant pressure snapshot unavailable: {}",redisUnavailable.getClass().getSimpleName());}
            log.warn("Lottery grant pressure unavailable: {}",e.getClass().getSimpleName());
        } finally {
            DynamicDataSourceContextHolder.poll();
            unlock(token);
        }
    }

    private void unlock(String token) {
        try {redis.execute(UNLOCK,List.of(LOCK),token);}catch(RuntimeException ignored){ /* 锁有期限。 */ }
    }

    @PreDestroy public void stop(){refreshExecutor.shutdownNow();}

    private int pendingCount() {
        JdbcTemplate read=new JdbcTemplate(jdbc.getDataSource());read.setQueryTimeout(2);
        Integer count=read.queryForObject("SELECT COUNT(*) FROM (SELECT j.request_pk FROM lottery_v2_job j JOIN lottery_v2_request r ON r.id=j.request_pk "
                +"WHERE j.state IN ('READY','RUNNING') AND r.draw_status='ACCEPTED' AND COALESCE(r.grant_status,'PENDING')<>'WAIT_USER_CONFIRM' LIMIT 100) pending",Integer.class);
        return count==null?HIGH:count;
    }

    private Snapshot parse(String value) {
        if(value==null)return null;
        try {
            String[] fields=value.split(":",-1);
            if(fields.length!=3||Integer.parseInt(fields[0])<0||(!"0".equals(fields[1])&&!"1".equals(fields[1])))return null;
            return new Snapshot("1".equals(fields[1]),Long.parseLong(fields[2]));
        } catch(RuntimeException invalid){return null;}
    }

    private void check(boolean paused) {
        if(paused){io.micrometer.core.instrument.Metrics.counter("lottery.grant.backpressure").increment();throw exception(LOTTERY_SYSTEM_AGAIN);}
    }
}
