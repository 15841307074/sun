package com.htyoudao.youdao.module.promotion.api.couponCommodity;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.service.couponcommodity.CouponCommodityService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
@Validated
public class CouponCommodityApiImpl implements CouponCommodityApi{

    @Resource
    private CouponCommodityService couponCommodityService;

    @Override
    public CommonResult<Boolean> selectCouponHaveCommodity(Long commodityId) {
        return success(couponCommodityService.selectCouponHaveCommodity(commodityId));
    }

    @Override
    public CommonResult<Boolean> updateCommodityName(Long commodityId, String commodityName) {
        return success(couponCommodityService.updateCommodityName(commodityId,commodityName));
    }
}
