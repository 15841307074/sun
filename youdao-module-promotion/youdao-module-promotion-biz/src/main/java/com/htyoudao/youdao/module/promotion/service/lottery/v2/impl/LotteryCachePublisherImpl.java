package com.htyoudao.youdao.module.promotion.service.lottery.v2.impl;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.*;
import static com.htyoudao.youdao.module.promotion.dal.redis.RedisKeyConstants.*;

/** 配置按版本持久化发布，不删除库存、次数和请求结果。 */
@Service("lotteryCachePublisher")
@DS("master")
@Slf4j
public class LotteryCachePublisherImpl implements LotteryCachePublisher {
    @Resource
    private JdbcTemplate jdbc;
    @Resource
    private StringRedisTemplate redis;
    @Resource
    private LotteryCacheExecutor executor;
    private static final DefaultRedisScript<Long> PUBLISH = new DefaultRedisScript<>(
            "local v=redis.call('GET',KEYS[1]); if v and tonumber(v)>tonumber(ARGV[1]) then return 0 end; " +
            "redis.call('SET',KEYS[1],ARGV[1]); return 1", Long.class);

    @Override
    public void enqueue(LotterySettingsDO settings) {
        long b = BusinessContextHolder.getRequiredBusinessId();
        jdbc.update("INSERT INTO lottery_v2_cache_task(business_id,activity_id,settings_id,config_version) VALUES(?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE settings_id=VALUES(settings_id),config_version=GREATEST(config_version,VALUES(config_version)),dirty=1,next_at=NOW(3)",
                b, settings.getActivityId(), settings.getId(), settings.getConfigVersion());
        // 提交后只发布固定数量的键；旧门店缓存由后台任务分批清理。
        Runnable publish = () -> { try {
            redis.execute(PUBLISH, List.of(LotteryKeys.key(b,settings.getActivityId(),"config-version",0)), Long.toString(settings.getConfigVersion()));
            redis.opsForValue().increment(LotteryKeys.key(b,0,"store-list-version",0));
        } catch (Exception e) {
            log.warn("Lottery cache publication queued for retry: activity={}", settings.getActivityId());
        }};
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() { executor.submit(publish); }
            });
        } else publish.run();
    }

    @Override
    public void assertNoPending(long activityId) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM lottery_v2_request WHERE business_id=? AND activity_id=? AND draw_status='ACCEPTED'", Long.class,
                BusinessContextHolder.getRequiredBusinessId(), activityId);
        if (count != null && count > 0) throw new IllegalArgumentException("存在处理中抽奖，暂不能切换奖池模式");
    }
    @Override
    public void assertPrizeNoPending(long activityId, String code) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM lottery_v2_request WHERE business_id=? AND activity_id=? AND prize_code=? AND draw_status='ACCEPTED'", Long.class,
                BusinessContextHolder.getRequiredBusinessId(), activityId, code);
        if (count != null && count > 0) throw new IllegalArgumentException("奖品存在处理中抽奖，暂不能删除");
    }
    @Override
    public void resizeStock(LotterySettingsDO settings, LotteryPrizeDO prize, int total) {
        long b = BusinessContextHolder.getRequiredBusinessId();
        long pool = prize.getStoreId() == null ? 0 : prize.getStoreId();
        Long invalid = jdbc.queryForObject("SELECT COUNT(*) FROM lottery_v2_stock WHERE business_id=? AND activity_id=? AND stock_epoch=? AND pool_store_id=? AND prize_code=? AND reserved+issued>?", Long.class,
                b, settings.getActivityId(), settings.getStockEpoch(), pool, prize.getCode(), total);
        if (invalid != null && invalid > 0) throw new IllegalArgumentException("奖品总量不能小于已发放及预占数量");
        int changed = jdbc.update("UPDATE lottery_v2_stock SET total=?,revision=revision+1 WHERE business_id=? AND activity_id=? AND stock_epoch=? AND pool_store_id=? AND prize_code=? AND reserved+issued<=?",
                total, b, settings.getActivityId(), settings.getStockEpoch(), pool, prize.getCode(), total);
        if (Objects.equals(settings.getRuntimeVersion(), 2) && !Objects.equals(prize.getIsGuarantees(), 1) && changed != 1)
            throw new IllegalArgumentException("库存账本缺失或总量小于已发放及预占数量");
    }

    /** 仅用于新建奖品，不用于缓存丢失后的库存恢复。 */
    @Override
    public void initializeNewStock(LotterySettingsDO settings, LotteryPrizeDO prize) {
        if (!Objects.equals(settings.getRuntimeVersion(), 2) || Objects.equals(prize.getIsGuarantees(), 1)) return;
        jdbc.update("INSERT INTO lottery_v2_stock(business_id,activity_id,stock_epoch,pool_store_id,prize_code,prize_id,total) VALUES(?,?,?,?,?,?,?)",
                BusinessContextHolder.getRequiredBusinessId(), settings.getActivityId(), settings.getStockEpoch(),
                prize.getStoreId() == null ? 0 : prize.getStoreId(), prize.getCode(), prize.getId(), prize.getPrizeNum());
    }

    @Scheduled(fixedDelay = 3000)
    @Override
    public void retry() {
        try {
            for (Map<String,Object> row : jdbc.queryForList("SELECT business_id,activity_id FROM lottery_v2_cache_task WHERE dirty=1 AND next_at<=NOW(3) LIMIT 32")) {
                long b=((Number)row.get("business_id")).longValue(), a=((Number)row.get("activity_id")).longValue();
                try { publish(b,a); } catch (Exception e) {
                    jdbc.update("UPDATE lottery_v2_cache_task SET attempts=attempts+1,next_at=DATE_ADD(NOW(3),INTERVAL 10 SECOND) WHERE business_id=? AND activity_id=?",b,a);
                    log.warn("Lottery cache retry failed: activity={}",a);
                }
            }
        } catch (Exception e) { log.warn("Lottery cache poll unavailable: {}", e.getClass().getSimpleName()); }
    }

    @Override
    public void publish(long b, long a) {
        List<Map<String,Object>> rows = jdbc.queryForList("SELECT * FROM lottery_v2_cache_task WHERE business_id=? AND activity_id=? AND dirty=1",b,a);
        if(rows.isEmpty()) return;
        Map<String,Object> row=rows.get(0);
        long version=((Number)row.get("config_version")).longValue(), settings=((Number)row.get("settings_id")).longValue();
        redis.execute(PUBLISH, List.of(LotteryKeys.key(b,a,"config-version",0)), Long.toString(version));
        redis.opsForValue().increment(LotteryKeys.key(b,0,"store-list-version",0));
        // 仅清理旧版展示缓存，不触碰库存键。
        for(long id : new long[]{a,settings}) {
            redis.delete(LOTTERY_SETTING+id);
            redis.delete(LOTTERY_PRIZE+id);
            for(Long store : jdbc.queryForList("SELECT DISTINCT store_id FROM lottery_prize WHERE lottery_id=? AND business_id=? AND store_id IS NOT NULL",Long.class,settings,b))
                redis.delete(LOTTERY_PRIZE+id+":store:"+store);
        }
        jdbc.update("UPDATE lottery_v2_cache_task SET dirty=0,attempts=0 WHERE business_id=? AND activity_id=? AND config_version=?",b,a,version);
    }
}
