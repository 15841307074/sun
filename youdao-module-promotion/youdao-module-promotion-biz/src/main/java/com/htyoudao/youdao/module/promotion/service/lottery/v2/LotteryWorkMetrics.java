package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.baomidou.dynamic.datasource.annotation.DS;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicLong;

@Component
@DS("master")
@Slf4j
public class LotteryWorkMetrics {
    @Resource
    private JdbcTemplate jdbc;
    @Resource
    private MeterRegistry registry;
    private final AtomicLong pending=new AtomicLong(),oldest=new AtomicLong(),cashUnknown=new AtomicLong(),cashAge=new AtomicLong();
    @PostConstruct public void register(){
        registry.gauge("lottery.jobs.pending",pending);registry.gauge("lottery.jobs.oldest.seconds",oldest);
        registry.gauge("lottery.cash.unknown",cashUnknown);registry.gauge("lottery.cash.oldest.seconds",cashAge);
    }
    @Scheduled(fixedDelay=15000,initialDelay=15000)
    public void sample(){
        try {
            var jobs=jdbc.queryForMap("SELECT COUNT(*) AS n,COALESCE(MAX(TIMESTAMPDIFF(SECOND,r.created_at,NOW())),0) AS age FROM lottery_v2_job j JOIN lottery_v2_request r ON r.id=j.request_pk WHERE j.state<>'DONE'");
            pending.set(((Number)jobs.get("n")).longValue());oldest.set(((Number)jobs.get("age")).longValue());
            var cash=jdbc.queryForMap("SELECT COUNT(*) AS n,COALESCE(MAX(TIMESTAMPDIFF(SECOND,created_at,NOW())),0) AS age FROM lottery_v2_request WHERE draw_status='ACCEPTED' AND grant_status IN ('PROCESSING','ACCEPTED','WAIT_USER_CONFIRM','TRANSFERING')");
            cashUnknown.set(((Number)cash.get("n")).longValue());cashAge.set(((Number)cash.get("age")).longValue());
        }catch(Exception e){registry.counter("lottery.monitor.failures").increment();log.debug("Lottery metric sample unavailable");}
    }
}
