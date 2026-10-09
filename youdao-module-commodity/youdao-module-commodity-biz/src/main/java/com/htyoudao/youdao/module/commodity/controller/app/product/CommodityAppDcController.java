package com.htyoudao.youdao.module.commodity.controller.app.product;


import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.*;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.SingleUp.StoreSingleUpConfirmReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.SingleUp.StoreSingleUpReqVO;
import com.htyoudao.youdao.module.commodity.controller.admin.storeSpu.VO.SingleUp.StoreSingleUpRespVO;
import com.htyoudao.youdao.module.commodity.controller.app.product.vo.DcUpdateSpu.DCSpuUpdateReqVO;
import com.htyoudao.youdao.module.commodity.dal.dto.StoreCategoryDTO;
import com.htyoudao.youdao.module.commodity.enums.ClientType;
import com.htyoudao.youdao.module.commodity.service.storeSpu.ICommodityStoreSpuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.List;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "app - 点餐机门店商品接口")
@RestController
@RequestMapping("/commodity/app/dc")
@Validated
@Slf4j
public class CommodityAppDcController {

    @Resource
    private ICommodityStoreSpuService commodityStoreSpuService;


    @GetMapping("/appletGetAllSpu")
    @Operation(summary = "点餐获取门店分类商品接口")
    public CommonResult<List<StoreCategoryDTO>> dcAppletGetAllSpu(@Param("storeId") Long storeId) {
        return success(commodityStoreSpuService.appletGetAllSpu(storeId, ClientType.DC));
    }


    @GetMapping("/appletGetAllSpu/filter")
    @Operation(summary = "点餐获取门店分类商品接口")
    public CommonResult<List<StoreCategoryDTO>> dcAppletGetAllSpuFilter(@Param("storeId") Long storeId) {
        return success(commodityStoreSpuService.appletGetAllSpu(storeId, ClientType.DC_FILTER));
    }

    @PostMapping("/DcUpdateSpu")
    @Operation(summary = "点餐机修改商品")
    public CommonResult<Boolean> appletUpdateSpu(@Valid @RequestBody DCSpuUpdateReqVO reqVO) {
        commodityStoreSpuService.appletUpdateSpu(reqVO);
        return success(true);
    }

    @Operation(summary = "门店单品下架前查询逻辑")
    @PostMapping("/V3/storeSingleDownSelect")
    public CommonResult<List<CommodityStoreSpuRespVo>> storeSingleDownSelectV3(@Valid @RequestBody StoreItemDelistReqVo storeItemDelistReqVo) {
        List<CommodityStoreSpuRespVo> storeSpu =  commodityStoreSpuService.storeSingleDownSelectV3(storeItemDelistReqVo);
        return success(storeSpu);
    }


    @Operation(summary = "门店单品下架")
    @PostMapping("/V3/singleDown")
    public CommonResult<Boolean> singleDownV3(@Valid @RequestBody StoreItemDelistReqVo delistReqVo) {
        delistReqVo.setIsApp(Boolean.TRUE);
        commodityStoreSpuService.singleDownV3(delistReqVo);
        return success(true);
    }


    @Operation(summary = "门店单品上架")
    @PostMapping("/V3/singleUp")
    public CommonResult<Boolean> singleUpV3(@Valid @RequestBody StoreItemListingReqVo itemListingReqVo) {
        itemListingReqVo.setIsApp(Boolean.TRUE);
        commodityStoreSpuService.singleUpV3(itemListingReqVo);
        return success(true);
    }


    @Operation(summary = "门店套餐上架查询")
    @PostMapping("/V3/packageSelect")
    public CommonResult<List<CommodityStoreSingleRespVO>> packageSelectV3(@Valid @RequestBody StoreItemListingReqVo listingReqVo) {

        List<CommodityStoreSingleRespVO> singleList = commodityStoreSpuService.packageSelectV3(listingReqVo);
        return success(singleList);
    }


    @Operation(summary = "门店套餐上下架")
    @PostMapping("/V3/packageUpAndDown")
    public CommonResult<Boolean> packageUpAndDownV3(@Valid @RequestBody StoreItemUnmountReqVo itemUnmountReqVo) {
        itemUnmountReqVo.setIsApp(Boolean.TRUE);
        commodityStoreSpuService.packageUpAndDownV3(itemUnmountReqVo);

        return success(true);
    }

    @Operation(summary = "门店单品联动套餐上架查询")
    @PostMapping("/singleUpSelect")
    public CommonResult<StoreSingleUpRespVO> singleUpSelect(@Valid @RequestBody StoreSingleUpReqVO singleUpReqVo) {
        singleUpReqVo.setIsApp(Boolean.TRUE);
        StoreSingleUpRespVO storeSingleUpRespVO =  commodityStoreSpuService.singleUpSelect(singleUpReqVo);
        return success(storeSingleUpRespVO);
    }


    @Operation(summary = "门店单品联动套餐上架确认")
    @PostMapping("/singleUpConfirm")
    public CommonResult<Boolean> singleUpConfirm(@Valid @RequestBody StoreSingleUpConfirmReqVO singleUpConfirmReqVO) {
        singleUpConfirmReqVO.setIsApp(Boolean.TRUE);
        commodityStoreSpuService.singleUpConfirm(singleUpConfirmReqVO);
        return success(true);
    }


}
