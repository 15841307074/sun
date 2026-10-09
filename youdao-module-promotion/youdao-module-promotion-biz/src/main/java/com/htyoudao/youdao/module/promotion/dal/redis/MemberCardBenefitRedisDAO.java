package com.htyoudao.youdao.module.promotion.dal.redis;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Set;

/**
 * 会员卡权益 Redis 数据访问。
 *
 * <p>Redis 只保存一级会员实际写库成功过的 couponId，不保存用户券明细和发放时间。
 * 归档时间由 user_coupon 表中的 coupon_create_time 统一计算。</p>
 */
@Repository
public class MemberCardBenefitRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 将一级会员实际发放成功的优惠券 ID 写入 Redis Set。
     *
     * <p>Set 天然去重：同一业务线、同一 couponId 即使每天任务重复命中，也只保存一份。</p>
     *
     * @param businessId 业务线 ID，用于隔离不同业务线的券 ID 集合
     * @param couponId 已成功写入 user_coupon 分表的优惠券 ID
     * @return 新增到集合的元素数量；已存在时返回 0
     */
    public Long addLevelOneCouponId(Long businessId, Long couponId) {
        return stringRedisTemplate.opsForSet().add(buildLevelOneCouponIdsKey(businessId), String.valueOf(couponId));
    }

    /**
     * 获取指定业务线内一级会员实际发放过的全部优惠券 ID。
     *
     * <p>Redis 返回 null 时转换为空集合，让上层任务可以直接安全遍历。</p>
     *
     * @param businessId 业务线 ID
     * @return Redis 中保存的十进制字符串券 ID 集合
     */
    public Set<String> getLevelOneCouponIds(Long businessId) {
        Set<String> couponIds = stringRedisTemplate.opsForSet().members(buildLevelOneCouponIdsKey(businessId));
        return couponIds == null ? Collections.emptySet() : couponIds;
    }

    /**
     * 构造业务线级 Redis Key。
     */
    private String buildLevelOneCouponIdsKey(Long businessId) {
        return RedisKeyConstants.MEMBER_CARD_LEVEL_ONE_COUPON_IDS + businessId;
    }
}
