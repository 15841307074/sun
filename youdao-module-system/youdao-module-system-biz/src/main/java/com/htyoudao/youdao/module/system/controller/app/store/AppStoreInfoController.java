package com.htyoudao.youdao.module.system.controller.app.store;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.util.DataPermissionUtils;
import com.htyoudao.youdao.module.system.api.store.dto.StoreWecomConfigReqDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreWecomConfigResDTO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreSaveReqVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreSimpleResVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreUpdateStateReqVO;
import com.htyoudao.youdao.module.system.controller.app.printer.vo.*;
import com.htyoudao.youdao.module.system.controller.app.store.vo.*;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 门店")
@RestController
@RequestMapping("/system/store")
@Validated
public class AppStoreInfoController {
    @Resource
    private SystemStoreInfoService systemStoreInfoService;
    /**
     * 查询最近的门店信息-小程序
     */
    @PostMapping("/storeListByCity")
    @Operation(summary = "查询最近的门店信息-小程序")
    @PermitAll
    public CommonResult<List<StoreWecomConfigResVO>>  storeListByCity(@RequestBody StoreWecomConfigReqVO storeWecomConfigReqVO){
        return success(systemStoreInfoService.storeListByCity(storeWecomConfigReqVO));
    }
    /**
     * 查询最近的门店信息-点餐机
     */
    @GetMapping("/storeListByUser")
    @Operation(summary = "查询最近的门店信息-点餐机")
    public CommonResult<List<StoreSimpleResVO>>  storeListByUser(){
        Long businessId = BusinessContextHolder.getBusinessId();
        if(businessId.equals(11L)){
           return success(DataPermissionUtils.executeIgnore(() -> systemStoreInfoService.storeGylListByUser()));
        }else{
            return success(systemStoreInfoService.storeListByUser());
        }
    }




    /**
     * 查询部门所在市区
     */
    @GetMapping("/getCityName")
    @Operation(summary = "城市列表")
    @PermitAll
    public CommonResult<List<SysDeptCityVO>> getCityName(@Param("deptName") String deptName){
        return success(systemStoreInfoService.getCityName(deptName));
    }

    /**
     * 查询同一城市下关联的门店信息-优惠卷小程序
     */
    @PostMapping("/selectByCouponStoreList")
    @Operation(summary = "查询同一城市下关联的门店信息-小程序")
    @PermitAll
    public CommonResult<List<StoreWecomConfigResDTO>> selectByCouponStoreList(@RequestBody StoreWecomConfigReqDTO storeWecomConfigReqDTO){
        List<StoreWecomConfigResDTO> storeWecomConfigResDTOS =systemStoreInfoService.selectByCouponStoreList(storeWecomConfigReqDTO);
        return success(storeWecomConfigResDTOS);
    }
    /**
     * 门店详情-小程序
     */
    @GetMapping("/getStoreById")
    @Operation(summary = "查询最近的门店信息-小程序")
    @PermitAll
    public CommonResult<StoreWecomConfigResDTO>  getStoreById(@Valid @NotNull(message = "请选择门店") @Param("storeId") Long storeId){
        return success(systemStoreInfoService.getStoreById(storeId));
    }

    @GetMapping("/getStoreBusinessStatus")
    @Operation(summary = "查询门店当前营业及外卖状态")
    @PermitAll
    public CommonResult<StoreBusinessStatusRespVO> getStoreBusinessStatus(@Valid @NotNull(message = "请选择门店") @RequestParam("storeId") Long storeId) {
        return success(systemStoreInfoService.getStoreBusinessStatus(storeId));
    }

    @PutMapping("/updateStoreInfoByBoss")
    @Operation(summary = "老板助手修改门店")
    public CommonResult<Boolean> updateStoreInfoByBoss(@Valid @RequestBody StoreSaveReqVO reqVO) {

        systemStoreInfoService.updateStoreByBoss(reqVO);
        return success(true);
    }
    @PutMapping("/updateStoreStateByBoss")
    @Operation(summary = "老板助手修改门店状态")
    public CommonResult<Boolean> updateStoreStateByBoss(@RequestBody StoreUpdateStateReqVO reqVO) {

        systemStoreInfoService.updateStoreStateByBoss(reqVO);
        return success(true);
    }
    /**
     * 查询最近的门店信息-点餐机
     */
    @GetMapping("/storeListByBoss")
    @Operation(summary = "老板助手查询门店")
    public CommonResult<List<StoreSimpleResVO>>  storeListByBoss(){
    return success(systemStoreInfoService.storeListByBoss());
    }

    @GetMapping("/getNearbyStores")
    @Operation(summary = "当前登录人 根据组织架构 获取能查看的门店的接口", description = "根据用户当前的经纬度，计算距离1km之内，并返回由近及远的门店列表")
    @Parameters({
            @Parameter(name = "longitude", description = "当前经度", required = true, example = "121.4737"),
            @Parameter(name = "latitude", description = "当前纬度", required = true, example = "31.2304")
    })
    public CommonResult<List<StoreItemVO>> getNearbyStores(@NotBlank(message = "定位不能为空") @RequestParam("longitude") String longitude,
                                                           @NotBlank(message = "定位不能为空") @RequestParam("latitude") String latitude) {
        List<StoreItemVO> respVOList = systemStoreInfoService.getNearbyStores(longitude, latitude);
        return CommonResult.success(respVOList);
    }

    @GetMapping("/getStoreBasicInfoById")
    @Parameters({
            @Parameter(name = "storeId", description = "storeId", required = true, example = "storeId")
    })
    @Operation(summary = "老板助手门店信息 店长 组织 组织负责人")
    public CommonResult<StoreBasicInfoRespVO>  getStoreBasicInfoById(@RequestParam("storeId") Long storeId){
        return success(systemStoreInfoService.getStoreBasicInfoById(storeId));
    }
}
