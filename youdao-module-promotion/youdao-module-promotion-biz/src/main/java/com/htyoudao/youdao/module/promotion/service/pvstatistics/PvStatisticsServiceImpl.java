package com.htyoudao.youdao.module.promotion.service.pvstatistics;

import com.htyoudao.youdao.module.promotion.controller.admin.market.vo.AddPvReqVO;
import com.htyoudao.youdao.module.promotion.util.redis.RedisCache;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author dht
 */
@Service
public class PvStatisticsServiceImpl implements PvStatisticsService{

    @Resource
    private RedisCache redisCache;

    /**
     * 券包领取（使用Bitmap）
     */
    private static final String COUPON_REV_KEY = "coupon:rev:";

    /**
     * PV总数key（使用String类型计数器）
     */
    private static final String TOTAL_PV_KEY = "stats:total:pv:";

    /**
     * UV总数key（使用Bitmap）
     */
    private static final String TOTAL_UV_KEY = "stats:total:uv:";


    @Override
    public boolean isClaimCouponPackage(Long couponPackageId, Long userId) {
        String key = COUPON_REV_KEY + couponPackageId;
        int hash1 = userId.hashCode();
        int hash2 = String.valueOf(userId).concat("SALT").hashCode();
        int offset = (Math.abs(hash1) ^ (Math.abs(hash2)) % 1000000);
        return Boolean.TRUE.equals(redisCache.getBit(key, offset));
    }

    @Override
    public void claimCouponPackage(Long couponPackageId, Long userId) {
        String key = COUPON_REV_KEY + couponPackageId;
        int hash1 = userId.hashCode();
        int hash2 = String.valueOf(userId).concat("SALT").hashCode();
        int offset = (Math.abs(hash1) ^ (Math.abs(hash2)) % 1000000);
        if(Boolean.FALSE.equals(redisCache.hasKey(key))){
            redisCache.setBit(key, offset);
            redisCache.expire(key, 30, TimeUnit.DAYS);
        }else {
            redisCache.setBit(key, offset);
        }
    }

    /**
     * 获取总PV
     */
    @Override
    public Integer getTotalPv(Long couponPackageId) {
        String pv = TOTAL_PV_KEY + couponPackageId;
        Integer count = (Integer) redisCache.getOpsForValue(pv);
        return count == null ? 0 : count;
    }

    /**
     * 获取总UV
     */
    @Override
    public Long getTotalUv(Long couponPackageId) {
        String uv = TOTAL_UV_KEY + couponPackageId;
        return redisCache.bitCount(uv);
    }


    /**
     * 记录用户访问（PV和UV）
     * @param addPvAO 用户ID（数字类型）
     */
    @Override
    public void recordVisit(AddPvReqVO addPvAO) {

        Long memberId = addPvAO.getMemberId();

        // PV总数+1（使用INCR命令）
        String pv = TOTAL_PV_KEY + addPvAO.getMarketId();
        Boolean b = redisCache.hasKey(pv);
        if(Boolean.FALSE.equals(b)){
            redisCache.increment(pv);
            redisCache.expire(pv, 30, TimeUnit.DAYS);
        }else {
            redisCache.increment(pv);
        }

        int hash1 = memberId.hashCode();
        int hash2 = String.valueOf(memberId).concat("SALT").hashCode();
        int offset = (Math.abs(hash1) ^ (Math.abs(hash2)) % 1000000);

        // UV统计（使用Bitmap记录用户是否访问过）
        String uv = TOTAL_UV_KEY + addPvAO.getMarketId();
        if(Boolean.FALSE.equals(redisCache.hasKey(uv))){
            redisCache.setBit(uv, offset);
            redisCache.expire(pv, 30, TimeUnit.DAYS);
        }else {
            redisCache.setBit(uv, offset);
        }
    }

}
