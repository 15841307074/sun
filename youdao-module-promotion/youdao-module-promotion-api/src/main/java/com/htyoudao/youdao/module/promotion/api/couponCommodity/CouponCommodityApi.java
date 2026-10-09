package com.htyoudao.youdao.module.promotion.api.couponCommodity;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.api.usercoupon.VO.UserCouponVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Tag(name = "RPC 服务 - 优惠卷商品")
public interface CouponCommodityApi {

   // String PREFIX = "/promotion/coupon-commodity";

    //@GetMapping(PREFIX + "/selectCouponHaveCommodity")
    @Operation(summary = "是否有优惠卷包含商品")
    public CommonResult<Boolean> selectCouponHaveCommodity(@RequestParam("commodityId") Long commodityId);

    public CommonResult<Boolean> updateCommodityName(@RequestParam("commodityId")Long commodityId,@RequestParam("commodityName")String commodityName );
}
