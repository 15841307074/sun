package com.htyoudao.youdao.module.promotion.service.pvstatistics;

import com.htyoudao.youdao.module.promotion.controller.admin.market.vo.AddPvReqVO;

/**
 * @author dht
 */
public interface PvStatisticsService {
    /**
     * 检查用户是否访问过
     * @param couponPackageId couponPackageId
     * @param userId userId
     * @return Boolean
     */
    boolean isClaimCouponPackage(Long couponPackageId, Long userId);

    /**
     * 记录用户领取
     * @param couponPackageId couponPackageId
     * @param userId userId
     */
    void claimCouponPackage(Long couponPackageId, Long userId);

    /**
     * 获取总PV
     * @param couponPackageId couponPackageId
     */
    Integer getTotalPv(Long couponPackageId);

    /**
     * 获取总UV
     * @param couponPackageId couponPackageId
     */
    Long getTotalUv(Long couponPackageId);

    /**
     * 记录用户访问（PV和UV）
     * @param reqVO 用户ID（数字类型）
     */
    void recordVisit(AddPvReqVO reqVO);
}
