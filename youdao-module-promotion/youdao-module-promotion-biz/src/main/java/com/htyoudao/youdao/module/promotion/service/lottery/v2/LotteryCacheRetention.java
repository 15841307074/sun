package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.baomidou.dynamic.datasource.annotation.DS;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.*;

/** 活动结束且流水结清满七天后，只清理登记过的可变缓存键。 */
@Component
@DS("master")
@Slf4j
public class LotteryCacheRetention {
    @Resource
    private JdbcTemplate jdbc;
    @Resource
    private StringRedisTemplate redis;
    private static final DefaultRedisScript<Long> DELETE=new DefaultRedisScript<>(
            "if redis.call('GET',KEYS[1])~=ARGV[1] then return 0 end; for i=3,#KEYS do redis.call('DEL',KEYS[i]);redis.call('SREM',KEYS[2],KEYS[i]);end;return 1",Long.class);
    @Scheduled(fixedDelay=3600000,initialDelay=60000)
    public void cleanup() {
        try {
            var activities=jdbc.queryForList("SELECT s.business_id,s.activity_id,s.config_version FROM lottery_settings s JOIN activity a ON a.id=s.activity_id AND a.business_id=s.business_id "
                    +"WHERE s.runtime_version=2 AND s.cache_cleaned_version<s.config_version AND a.end_date<DATE_SUB(NOW(),INTERVAL 7 DAY) "
                    +"AND NOT EXISTS(SELECT 1 FROM lottery_v2_request r WHERE r.business_id=s.business_id AND r.activity_id=s.activity_id AND (r.draw_status='ACCEPTED' OR r.updated_at>=DATE_SUB(NOW(),INTERVAL 7 DAY))) "
                    +"AND NOT EXISTS(SELECT 1 FROM lottery_v2_job j JOIN lottery_v2_request r ON r.id=j.request_pk WHERE r.business_id=s.business_id AND r.activity_id=s.activity_id AND (j.state<>'DONE' OR j.updated_at>=DATE_SUB(NOW(),INTERVAL 7 DAY))) "
                    +"AND NOT EXISTS(SELECT 1 FROM lottery_v2_counter_cache_task c WHERE c.business_id=s.business_id AND c.activity_id=s.activity_id AND c.dirty=1) ORDER BY a.end_date LIMIT 100");
            for(var row:activities) {
                long b=((Number)row.get("business_id")).longValue(),a=((Number)row.get("activity_id")).longValue();
                String version=row.get("config_version").toString(),registry=LotteryKeys.mutableRegistry(b,a);
                // 只扫描活动自己的键集合，不做 Redis 全局扫描。
                try(Cursor<String> keys=redis.opsForSet().scan(registry,ScanOptions.scanOptions().count(128).build())) {
                    List<String> batch=new ArrayList<>(List.of(LotteryKeys.key(b,a,"config-version",0),registry));
                    int processed=0;
                    while(keys.hasNext()&&processed<2048) {
                        String key=keys.next();if(!key.startsWith(LotteryKeys.prefix(b,a)))continue;
                        batch.add(key);processed++;
                        if(batch.size()==130){if(!Objects.equals(redis.execute(DELETE,batch,version),1L))break;batch=new ArrayList<>(batch.subList(0,2));}
                    }
                    if(batch.size()>2)redis.execute(DELETE,batch,version);
                }
                if(Objects.equals(redis.opsForSet().size(registry),0L))
                    jdbc.update("UPDATE lottery_settings SET cache_cleaned_version=? WHERE business_id=? AND activity_id=? AND config_version=?",version,b,a,version);
            }
        }catch(Exception e){log.warn("Lottery cache cleanup deferred: {}",e.getClass().getSimpleName());}
    }
}
