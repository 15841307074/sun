package com.htyoudao.youdao.module.promotion.controller.app.couponRedeem;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.controller.app.couponRedeem.VO.RedeemDouyinCouponReqVO;
import com.htyoudao.youdao.module.promotion.service.couponRedeem.DouyinCouponRedeemRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-07-02
 */
@Tag(name = "app - 抖音优惠券")
@RestController
@RequestMapping("/promotion/coupon-redeem-record")
@Validated
public class DouyinCouponRedeemRecordController {

    @Resource
    private DouyinCouponRedeemRecordService douyinCouponRedeemRecordService;

    @PostMapping("/redeemDouyinCoupon")
    @Operation(summary = "兑换抖音优惠券")
    public CommonResult<String> redeemDouyinCoupon(@Validated @RequestBody RedeemDouyinCouponReqVO reqVO) {
        douyinCouponRedeemRecordService.redeemDouyinCoupon(reqVO);
        return CommonResult.success("兑换成功");
    }
}
