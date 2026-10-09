package com.htyoudao.youdao.module.promotion.controller.admin.advertising;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.advertising.VO.*;
import com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.AdvertisingDO;
import com.htyoudao.youdao.module.promotion.service.advertising.AdvertisingService;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.api.storeinfo.dto.StorePageResVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Tag(name = "后台pc - 广告配置页面")
@RestController
@RequestMapping("/promotion/advertising")
public class AdvertisingController {

    @Autowired
    private AdvertisingService advertisingService;



    @Operation(summary = "pc端获取广告列表页面")
    @PostMapping("/getList")
    public CommonResult<PageResult<AdvertisingPageRespVO>> getList(@RequestBody(required = false) AdvertisingPageReqVO advertising){
        PageResult<AdvertisingPageRespVO> advertisingPage = advertisingService.getPage(advertising);
        return CommonResult.success(advertisingPage);
    }


    @Operation(summary = "pc端获取详情")
    @GetMapping("/getInfo")
    public CommonResult<AdvertisingRespVO> getInfo(@RequestParam("id") Long id){
        AdvertisingRespVO advertising1 = advertisingService.getInfo(id);
        return CommonResult.success(advertising1);
    }

    @Operation(summary = "新建广告")
    @PreAuthorize("@ss.hasPermission('promotion:advertising:add')")
    @PostMapping("/createAdvertising")
    public CommonResult<Boolean> createAdvertising(@Valid @RequestBody AdvertisingSaveReqVO saveReqVO){
        advertisingService.createAdvertising(saveReqVO);
        return CommonResult.success(true);
    }


    @Operation(summary = "修改广告")
    @PreAuthorize("@ss.hasPermission('promotion:advertising:update')")
    @PostMapping("/updateAdvertising")
    public CommonResult<Boolean> updateAdvertising(@Valid @RequestBody AdvertisingSaveReqVO updateVo){
        advertisingService.updateAdvertising(updateVo);
        return CommonResult.success(true);
    }
    @Operation(summary = "刪除广告")
    @PreAuthorize("@ss.hasPermission('promotion:advertising:delete')")
    @GetMapping("/deleteAdvertising")
    public CommonResult<Boolean> deleteAdvertising(@RequestParam("id") @NotNull(message = "id不能为空") Long id){
        advertisingService.deleteAdvertising(id);
        return CommonResult.success(true);
   }

    @Operation(summary = "修改广告状态")
    @PreAuthorize("@ss.hasPermission('promotion:advertisingStatus:update')")
    @PostMapping("/updateAdvertisingStatus")
    public CommonResult<Boolean> updateAdvertisingStatus(@RequestBody AdvertisingStatusReqVO advertisingStatusReqVO){
        advertisingService.updateAdvertisingStatus(advertisingStatusReqVO);
        return CommonResult.success(true);
   }


    @Operation(summary = "获取的门店列表")
    @PostMapping("/selectByStoreList")
    public CommonResult<PageResult<StorePageResVO>> selectByStoreList(@RequestBody AdvertisingStorePageReqVO storePageReqVO){

        PageResult<StorePageResVO> pageResult = advertisingService.selectByStoreList(storePageReqVO);
        return CommonResult.success(pageResult);
    }

    @Operation(summary = "获取已勾选的门店")
    @PostMapping("/selectCheckedStoreList")
    public CommonResult<List<StoreInfoDTO>> selectCheckedStoreList(@RequestBody AdvertisingStorePageReqVO storePageReqVO){
        List<StoreInfoDTO> list= advertisingService.selectCheckedStoreList(storePageReqVO);
        return CommonResult.success(list);
    }


    @Operation(summary = "广告定时任务调用")
    @GetMapping("/sync")
    public CommonResult<Boolean> sync(){
        advertisingService.execAdvertising();
        return CommonResult.success(true);
    }

    @Operation(summary = "删除关联的门店")
    @GetMapping("/deleteByStoreId")
    public CommonResult<Boolean> deleteByStoreId(@RequestParam("advertisingId") Long advertisingId,@RequestParam("storeId") Long storeId){

        advertisingService.deleteByStoreId(advertisingId,storeId);
        return CommonResult.success(true);
    }

    @Operation(summary = "刪除redis")
    @GetMapping("/deleteRedis")
    public CommonResult<Boolean> deleteRedis(){
        advertisingService.deleteRedis();
        return CommonResult.success(true);
    }


    @Operation(summary = "广告数据转换")
    @PermitAll
    @GetMapping("/dataConversion")
    public CommonResult<Boolean> advertisingDataConversion(){
        Boolean flag = advertisingService.advertisingDataConversion();
        return CommonResult.success(flag);
    }



}
