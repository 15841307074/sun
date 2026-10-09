package com.htyoudao.youdao.module.promotion.service.usercoupon.ratelimit;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * @author dht
 */
@Service
public class RateLimitServiceImpl implements RateLimitService{

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean allowRequest(Long userId) {
        String key = "{rate_limit}:" + userId;
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(10));
        return Boolean.TRUE.equals(result);
    }

    @Override
    public boolean allowClaimCouponRequest(Long userId) {
        String key = "{coupon_rate_limit}:" + userId;
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(10));
        return Boolean.TRUE.equals(result);
    }

    @Override
    public boolean allowClaimCouponJDRequest(Long userId,Long couponId) {
        String key = "{coupon_rate_limit}:" + couponId + ":" + userId;
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(10));
        return Boolean.TRUE.equals(result);
    }

    @Override
    public boolean zzPackageAllowRequest(Long userId) {
        String zzRateLimit = "zz_rate_limit";
        String key = zzRateLimit + userId;
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(10));
        return Boolean.TRUE.equals(result);
    }


    @Override
    public boolean claimCouponWithProductAllowRequest(Long userId) {
        String rateLimit = "product";
        String key = rateLimit + userId;
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", Duration.ofSeconds(5));
        return Boolean.TRUE.equals(result);
    }

}