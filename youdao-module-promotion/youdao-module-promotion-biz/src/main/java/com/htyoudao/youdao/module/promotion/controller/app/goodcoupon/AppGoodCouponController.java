package com.htyoudao.youdao.module.promotion.controller.app.goodcoupon;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.service.goodcoupon.GoodCouponService;
import com.htyoudao.youdao.module.system.api.store.dto.StoreWecomConfigReqDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreWecomConfigResDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "小程序 - 优惠券")
@RestController
@RequestMapping("/promotion/app-good-coupon")
@Validated
@Slf4j
public class AppGoodCouponController {

    @Resource
    private GoodCouponService goodCouponService;


    @PostMapping("/selectStoreList")
    @Operation(summary = "查询优惠券关联的附近的门店")
    public CommonResult<List<StoreWecomConfigResDTO>> selectStoreList(@RequestBody StoreWecomConfigReqDTO reqDTO) {
        List<StoreWecomConfigResDTO> list = goodCouponService.selectStoreList(reqDTO);
        return success(list);
    }





}