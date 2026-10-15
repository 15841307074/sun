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
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

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
            if n>0 then redis.call('PEXPIREAT',KEYS[1],ARGV[1]) end
            return 1
            """, Long.class);
    @Resource
    private StringRedisTemplate redis;
    @Resource
    private ObjectMapper json;

    @Override
    public List<LotteryRecentWinnerRespVO> latest(Long activityId) {
        if (activityId == null || activityId <= 0) throw new IllegalArgumentException("activityId 必须为正数活动ID");
        long b = BusinessContextHolder.getRequiredBusinessId();
        String key = key(b, activityId);
        Set<String> values = redis.opsForZSet().reverseRange(key, 0, LIMIT - 1);
        if (values == null || values.isEmpty()) return List.of();
        try {
            List<LotteryRecentWinnerRespVO> result = new ArrayList<>(values.size());
            for (String value : values) result.add(json.readValue(value.substring(value.indexOf('|') + 1), LotteryRecentWinnerRespVO.class));
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("中奖展示数据解析失败", e);
        }
    }

    /** 只发布已提交记录；缓存失败不回滚已发放奖品。 */
    @Override
    public void afterLogSaved(long activityId, LotteryLogDO row) {
        if (!eligible(row)) return;
        String key = key(Objects.requireNonNull(row.getBusinessId()), activityId);
        Runnable publish = () -> {
            try { publish(key, row); }
            catch (Exception e) {
                log.warn("Lottery winner display cache write failed: activity={}, log={}", activityId, row.getId());
            }
        };
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() { publish.run(); }
            });
        } else publish.run();
    }

    private void publish(String key, LotteryLogDO row) {
        // 直接使用中奖记录中的活动结束时间；展示缓存缺失时不查库补齐。
        if (row.getLotteryEndTime() == null) {
            log.warn("Lottery winner display skipped: missing activity end time, log={}", row.getId());
            return;
        }
        long expiresAt = Instant.ofEpochMilli(row.getLotteryEndTime().getTime()).plus(Duration.ofDays(7)).toEpochMilli();
        if (expiresAt <= Instant.now().toEpochMilli()) return;
        // 合并、裁剪和设置固定过期时刻在一个脚本内完成，适用于多实例并发写入。
        redis.execute(MERGE, List.of(key), Long.toString(expiresAt), encode(row));
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
