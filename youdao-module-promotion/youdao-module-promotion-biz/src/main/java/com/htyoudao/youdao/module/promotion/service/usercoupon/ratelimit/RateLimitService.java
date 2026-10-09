package com.htyoudao.youdao.module.promotion.service.usercoupon.ratelimit;

/**
 * @author dht
 */
public interface RateLimitService {

    /**
     * 是否允许请求（领取券包）
     * @param userId userId
     * @return boolean
     */
    boolean allowRequest(Long userId);


    /**
     * 是否允许请求（领取券）
     * @param userId userId
     * @return boolean
     */
    boolean allowClaimCouponRequest(Long userId);

    /**
     * 是否允许请求（集点领取券）
     * @param userId userId
     * @return boolean
     */
    boolean allowClaimCouponJDRequest(Long userId,Long couponId);

    /**
     * 是否允许请求（领取周周惠券包）
     * @param userId userId
     * @return boolean
     */
    boolean zzPackageAllowRequest(Long userId);

    /**
     * 是否允许请求（领取积分商品优惠券）
     * @param userId userId
     * @return boolean
     */
    boolean claimCouponWithProductAllowRequest(Long userId);
}
