package com.htyoudao.youdao.module.promotion.api.usercoupon;


import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.MemberCouponDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.UpdateUserCouponReqVO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;


@Tag(name = "RPC 服务 - 优惠卷")
public interface UserCouponApi {
    String PREFIX = "/promotion/user-coupon";

    @GetMapping(PREFIX + "/selectCouponData")
    @Operation(summary = "获取优惠卷详情")
    List<UserCouponVO> selectCouponData(@RequestParam("memberId") Long memberId);

    /**
     * 获取优惠卷
     */
    @Operation(summary = "获取优惠卷")
    GoodCouponVO selectCouponByCode(@RequestParam("couponCode") String couponCode);

    /**
     * 根据优惠券编码查询积分商品详情专用的完整优惠券信息。
     *
     * @param couponCode 优惠券编码
     * @return 优惠券完整信息，不存在时返回 null
     */
    @Operation(summary = "获取积分商品详情的完整优惠券信息")
    PointsProductCouponDetailVO selectPointsProductCouponByCode(
            @RequestParam("couponCode") String couponCode);

    /**
     * 添加优惠卷
     */
    @Operation(summary = "获取优惠卷")
    void insertCouponByPoints(@RequestBody UserCouponVO userCouponVO);

    /**
     * 获取优惠金额，单张优惠券
     */
    @Operation(summary = "获取优惠金额，单张优惠券")
    CouponCalculateRespVO getReduceAmount(GetReduceAmountReqVO reduceAmountReqVO);

    /**
     * 使用/回退优惠券
     */
    @Operation(summary = "使用/回退优惠券")
    void usedCoupon(UsedCouponReqVO usedCouponReqVO);

    /**
     * 更新优惠券
     */
    @Operation(summary = "更新优惠券")
    void updateUserCoupon(@RequestBody UpdateUserCouponReqVO body);

    /**
     * 领取抖音优惠券
     * @param  claimCoupon claimCoupon
     * @return Long
     */
    @Operation(summary = "领取抖音优惠券")
    Long claimTiktokCoupon(@RequestBody ClaimTiktokCouponReqVO claimCoupon);

    /**
     * 批量发放优惠券（会员管理页面）
     * @param result memberIds
     * @param couponId couponId
     * @param sendNum sendNum
     * @return Boolean
     */
    Boolean sendCoupon(Set<MemberCouponDTO> result, Long couponId, Integer sendNum);

    /**
     * 批量发放优惠券包（会员管理页面）
     * @param result memberIds
     * @param couponId couponId
     * @param sendNum sendNum
     * @return Boolean
     */
    Boolean sendPackage(Set<MemberCouponDTO> result, Long couponId, Integer sendNum);

    /**
     * 注册时送优惠券
     * @param memberCouponDTO memberCouponDTO
     */
    void insertCouponWithRegister(MemberCouponDTO memberCouponDTO);
}

