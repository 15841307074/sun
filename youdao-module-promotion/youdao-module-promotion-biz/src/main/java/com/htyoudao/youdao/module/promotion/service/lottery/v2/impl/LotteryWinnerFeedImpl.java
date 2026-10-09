package com.htyoudao.youdao.module.promotion.service.lottery.v2.impl;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.promotion.controller.app.lottery.vo.LotteryRecentWinnerRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.Duration;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/** 仅用于展示，不参与库存和发奖事务。 */
@Service("lotteryWinnerFeed")
@Slf4j
public class LotteryWinnerFeedImpl implements LotteryWinnerFeed {
    static final int LIMIT = 20;
    // 分数相同时按时间和编号的字典序排序，避免雪花 ID 转浮点数丢精度。
    static final DefaultRedisScript<Long> MERGE = new DefaultRedisScript<>("""
            for i=2,#ARGV do redis.call('ZADD',KEYS[1],0,ARGV[i]) end
            local n=redis.call('ZCARD',KEYS[1])
            if n>20 then redis.call('ZREMRANGEBYRANK',KEYS[1],0,n-21) end
            if n>0 then redis.call('EXPIRE',KEYS[1],86400) end
            if ARGV[1]=='1' then redis.call('SET',KEYS[2],'1','EX',60) end
            return 1
            """, Long.class);
    private static final DefaultRedisScript<Long> UNLOCK = new DefaultRedisScript<>(
            "if redis.call('GET',KEYS[1])==ARGV[1] then return redis.call('DEL',KEYS[1]) end return 0", Long.class);
    @Resource
    private StringRedisTemplate redis;
    @Resource
    private ObjectMapper json;
    @Resource
    private LotteryConfigurationCache configuration;
    @Resource
    private LotteryWinnerQuery query;
    private final AtomicInteger refills = new AtomicInteger();

    @Override
    public List<LotteryRecentWinnerRespVO> latest(Long lotteryId) {
        if (lotteryId == null || lotteryId <= 0) throw new IllegalArgumentException("lotteryId 不能为空");
        var cfg = configuration.settings(lotteryId);
        long b = BusinessContextHolder.getRequiredBusinessId();
        String key = key(b, cfg.getActivityId());
        if (!Boolean.TRUE.equals(redis.hasKey(key + ":ready"))) refill(b, cfg.getActivityId(), cfg.getId(), key);
        Set<String> values = redis.opsForZSet().reverseRange(key, 0, LIMIT - 1);
        if (values == null || values.isEmpty()) return List.of();
        try {
            List<LotteryRecentWinnerRespVO> result = new ArrayList<>(values.size());
            for (String value : values) result.add(json.readValue(value.substring(value.indexOf('|') + 1), LotteryRecentWinnerRespVO.class));
            return result;
        } catch (Exception e) {
            redis.delete(key + ":ready");
            throw new IllegalStateException("中奖记录缓存恢复中，请稍后重试", e);
        }
    }

    private void refill(long b, long a, long settingsId, String key) {
        if (refills.incrementAndGet() > 4) { refills.decrementAndGet(); throw new IllegalStateException("中奖记录缓存恢复中，请稍后重试"); }
        String token = UUID.randomUUID().toString();
        boolean locked = false;
        try {
            locked = Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key + ":refill", token, Duration.ofSeconds(10)));
            if (!locked) throw new IllegalStateException("中奖记录缓存恢复中，请稍后重试");
            if (!Boolean.TRUE.equals(redis.hasKey(key + ":ready"))) merge(key, query.latest(b, a, settingsId), true);
        } finally {
            refills.decrementAndGet();
            if (locked) redis.execute(UNLOCK, List.of(key + ":refill"), token);
        }
    }

    /** 只发布已提交记录；缓存失败不回滚已发放奖品。 */
    @Override
    public void afterLogSaved(long activityId, LotteryLogDO row) {
        if (!eligible(row)) return;
        String key = key(Objects.requireNonNull(row.getBusinessId()), activityId);
        Runnable publish = () -> {
            try { merge(key, List.of(row), false); }
            catch (Exception e) {
                try { redis.delete(key + ":ready"); } catch (Exception ignored) { /* 标记将在 60 秒内过期。 */ }
                log.warn("Lottery winner display cache deferred: activity={}, log={}", activityId, row.getId());
            }
        };
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() { publish.run(); }
            });
        } else publish.run();
    }

    private void merge(String key, List<LotteryLogDO> rows, boolean ready) {
        List<String> args = new ArrayList<>(); args.add(ready ? "1" : "0");
        for (LotteryLogDO row : rows) if (eligible(row)) args.add(encode(row));
        redis.execute(MERGE, List.of(key, key + ":ready"), args.toArray());
    }

    static boolean eligible(LotteryLogDO row) {
        return row != null && Objects.equals(row.getIsGuarantees(), 0) && row.getPrizeType() != null
                && !Objects.equals(row.getPrizeType(), 4) && row.getId() != null && row.getCreateTime() != null;
    }

    String encode(LotteryLogDO row) {
        try {
            long time = row.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return String.format(Locale.ROOT, "%013d%019d|", time, row.getId())
                    + json.writeValueAsString(new LotteryRecentWinnerRespVO(maskMobile(row.getMemberMobile()), row.getPrizeName()));
        } catch (Exception e) { throw new IllegalStateException("中奖展示数据序列化失败", e); }
    }

    static String maskMobile(String value) {
        if (value == null || value.isBlank()) return "";
        String mobile = value.trim();
        if (mobile.matches("1[0-9]{10}")) return mobile.substring(0, 3) + "****" + mobile.substring(7);
        return "****"; // 非预期手机号格式不直接展示。
    }

    static String key(long businessId, long activityId) { return LotteryKeys.key(businessId, activityId, "winners", "recent"); }
}
