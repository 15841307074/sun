package com.htyoudao.youdao.module.promotion.api.usercoupon;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.redis.core.utils.RedissonUtils;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.MemberCouponDTO;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.*;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.GoodCouponService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.UserCouponService;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.*;

@Slf4j
@DubboService
public class UserCouponApiImpl implements UserCouponApi {
    @Resource
    private UserCouponService userCouponService;
    @Resource
    private GoodCouponService goodCouponService;

    @Resource
    private CouponPackageService couponPackageService;

    @Override
    public List<UserCouponVO> selectCouponData(Long memberId) {
        return userCouponService.selectCouponData(memberId);
    }

    @Override
    public GoodCouponVO selectCouponByCode(String couponCode) {
        return goodCouponService.selectCouponByCode(couponCode);
    }

    /**
     * 查询积分商品详情专用的完整优惠券信息。
     */
    @Override
    public PointsProductCouponDetailVO selectPointsProductCouponByCode(String couponCode) {
        return goodCouponService.selectPointsProductCouponByCode(couponCode);
    }

    @Override
    public void insertCouponByPoints(UserCouponVO userCouponVO) {
        userCouponService.insertCouponByPoints(userCouponVO);

    }

    @SentinelResource(value = "getReduceAmount", fallback = "getReduceAmountFallback", blockHandler = "getReduceAmountExceptionHandler")
    @Override
    public CouponCalculateRespVO getReduceAmount(GetReduceAmountReqVO reduceAmountReqVO) {
        return userCouponService.getReduceAmount(reduceAmountReqVO);
    }

    public CouponCalculateRespVO getReduceAmountFallback(GetReduceAmountReqVO reduceAmountReqVO, Throwable ex) {
        log.error("userCouponApi.getReduceAmountFallback", ex);
        throw exception(PROMOTION_FALLBACK_ERROR);
    }

    public CouponCalculateRespVO getReduceAmountExceptionHandler(GetReduceAmountReqVO reduceAmountReqVO, BlockException ex) {
        log.error("userCouponApi.getReduceAmountBlock", ex);
        throw exception(PROMOTION_BLOCK_ERROR);
    }

    @Override
    public void usedCoupon(UsedCouponReqVO usedCouponReqVO) {
        userCouponService.usedCoupon(usedCouponReqVO);
    }

    @Override
    public void updateUserCoupon(UpdateUserCouponReqVO updateUserCouponReqVO) {
        userCouponService.updateUserCoupon(updateUserCouponReqVO);
    }

    @Override
    public Long claimTiktokCoupon(ClaimTiktokCouponReqVO claimCoupon) {
        return userCouponService.claimTiktokCoupon(claimCoupon);
    }

    @Override
    public Boolean sendCoupon(Set<MemberCouponDTO> result, Long couponId, Integer sendNum) {
        return userCouponService.sendCoupon(result, couponId, sendNum);
    }

    @Override
    public Boolean sendPackage(Set<MemberCouponDTO> result, Long couponId, Integer sendNum) {
        return userCouponService.sendPackage(result, couponId, sendNum);
    }

    @Override
    public void insertCouponWithRegister(MemberCouponDTO memberCouponDTO) {
        userCouponService.insertCouponWithRegister(memberCouponDTO);
    }
}
