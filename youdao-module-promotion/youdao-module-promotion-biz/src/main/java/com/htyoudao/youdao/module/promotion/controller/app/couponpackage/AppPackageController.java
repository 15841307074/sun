package com.htyoudao.youdao.module.promotion.controller.app.couponpackage;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.CouponPackageRespVO;
import com.htyoudao.youdao.module.promotion.controller.app.couponpackage.vo.ClaimCouponPackageReqVO;
import com.htyoudao.youdao.module.promotion.service.couponpackage.CouponPackageService;
import com.htyoudao.youdao.module.promotion.service.usercoupon.ratelimit.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.CLAIM_COUPON_LIMITER;

/**
 * @author dht
 */
@Tag(name = "app - 优惠券包")
@RestController
@RequestMapping("/promotion/app-coupon-package")
@Validated
public class AppPackageController {

    @Resource
    private CouponPackageService couponPackageService;

    @Resource
    private RateLimitService rateLimitService;

    @GetMapping("/getPackageById")
    @Operation(summary = "查询优惠券包详情  需要开放鉴权")
    @DataPermission(enable = false)
    //@PreAuthorize("@ss.hasPermission('coupon-package:query')")
    public CommonResult<CouponPackageRespVO> getById(@RequestParam(value = "id", required = false) Long id) {
        CouponPackageRespVO couponPackageVO = couponPackageService.selectById(id);
        return success(couponPackageVO);
    }

    @PostMapping("/claimCouponPackage")
    @Operation(summary = "领取优惠券包")
    public CommonResult<Boolean> claimCouponPackage(@RequestBody ClaimCouponPackageReqVO claimCouponPackageReqVO){
        boolean b = rateLimitService.allowRequest(claimCouponPackageReqVO.getMemberId());
        if(!b){
            throw exception(CLAIM_COUPON_LIMITER);
        }
        return success(couponPackageService.claimCouponPackage(claimCouponPackageReqVO));
    }


    @PostMapping("/claimZZCouponPackage")
    @Operation(summary = "领取周周优惠券包")
    public CommonResult<Boolean> claimZZCouponPackage(@RequestBody ClaimCouponPackageReqVO claimCouponPackageReqVO){
        boolean b = rateLimitService.zzPackageAllowRequest(claimCouponPackageReqVO.getMemberId());
        if(!b){
            throw exception(CLAIM_COUPON_LIMITER);
        }
        return success(couponPackageService.claimZZCouponPackage(claimCouponPackageReqVO));
    }

    @PostMapping("/claimSmsCouponPackage")
    @Operation(summary = "短信营销-领取优惠券包")
    public CommonResult<Boolean> claimSmsCouponPackage(@RequestBody ClaimCouponPackageReqVO claimCouponPackageReqVO){
        return success(couponPackageService.claimSmsCouponPackage(claimCouponPackageReqVO));
    }

}
