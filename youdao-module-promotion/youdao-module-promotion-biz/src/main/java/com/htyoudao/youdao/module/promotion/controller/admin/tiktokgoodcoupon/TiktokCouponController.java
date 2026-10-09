package com.htyoudao.youdao.module.promotion.controller.admin.tiktokgoodcoupon;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO.AdvertisingStorePageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.tiktokgoodcoupon.vo.TiktokCouponSaveReqVO;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.GoodCouponService;
import com.htyoudao.youdao.module.system.api.storeinfo.dto.StorePageResVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * @author dht
 */
@Tag(name = "管理后台 - 抖音优惠券")
@RestController("tiktokCouponController")
@RequestMapping("/promotion/tiktok-coupon")
@Validated
@Slf4j
public class TiktokCouponController {

    @Resource
    private GoodCouponService goodCouponService;


    @Operation(summary = "抖音券新增")
    @PostMapping("/save")
    public CommonResult<Integer> save(@Valid @RequestBody TiktokCouponSaveReqVO reqVO){
        return CommonResult.success(goodCouponService.saveTiktokGoodCoupon(reqVO));
    }


    @Operation(summary = "抖音券修改")
    @PostMapping("/update")
    public CommonResult<Integer> update(@Valid @RequestBody TiktokCouponSaveReqVO reqVO){
        return CommonResult.success(goodCouponService.updateTiktokGoodCoupon(reqVO));
    }


    @PostMapping(value = "/copy")
    @Operation(summary = "复制")
    public CommonResult<Integer> copy(@RequestParam("id") Long id) {
        return success(goodCouponService.tiktokCopy(id));
    }


    @Operation(summary = "获取抖音门店")
    @PostMapping("/selectByStoreList")
    public CommonResult<PageResult<StorePageResVO>> selectByStoreList(@RequestBody AdvertisingStorePageReqVO storePageReqVO){
        return CommonResult.success(goodCouponService.selectByStoreList(storePageReqVO));
    }

}