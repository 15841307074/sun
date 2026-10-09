package com.htyoudao.youdao.module.system.controller.admin.store;

import cn.hutool.core.collection.CollUtil;
import com.htyoudao.youdao.framework.apilog.core.annotation.ApiAccessLog;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mq.rabbitmq.service.RabbitMQService;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.*;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.user.UserUpdateStatusReqVO;
import com.htyoudao.youdao.module.system.controller.app.store.vo.SysDeptCityVO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import com.htyoudao.youdao.module.system.service.store.cache.StoreCityListCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

import static com.htyoudao.youdao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Slf4j
@Tag(name = "管理后台 - 门店")
@RestController
@RequestMapping("/system/store")
@Validated
public class SystemStoreInfoController {
    @Resource
    private SystemStoreInfoService systemStoreInfoService;
    @Resource
    private StoreCityListCacheService storeCityListCacheService;
    @Resource
    private RabbitMQService rabbitMQService;
    @PostMapping("/page")
    @Operation(summary = "获得门店分页列表")
    @PreAuthorize("@ss.hasPermission('system:store:query')")
    public CommonResult<PageResult<StoreResVO>> getStorePage(@Valid @RequestBody StorePageReqVO pageReqVO) {
        // 获得用户分页列表
        PageResult<StoreResVO> pageResult = systemStoreInfoService.getStorePage(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        return success(pageResult);
    }

    @PostMapping("/create")
    @Operation(summary = "新增门店")
    @PreAuthorize("@ss.hasPermission('system:store:add')")
    public CommonResult<Long> createStore(@Valid @RequestBody StoreSaveReqVO reqVO) {
        Long id = systemStoreInfoService.createStore(reqVO);
        return success(id);
    }

    @PutMapping("/update")
    @Operation(summary = "修改门店")
    @PreAuthorize("@ss.hasPermission('system:store:update')")
    public CommonResult<Boolean> updateStore(@Valid @RequestBody StoreSaveReqVO reqVO) {
        systemStoreInfoService.updateStore(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除门店")
    @Parameter(name = "storeId", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:store:delete')")
    public CommonResult<Boolean> deleteStore(@RequestParam("storeId") Long storeId) {
        systemStoreInfoService.deleteStore(storeId);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "修改营业状态")
    public CommonResult<Boolean> updateOpenStatus(@Valid @RequestBody UserUpdateStatusReqVO reqVO) {
        systemStoreInfoService.updateOpenStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    /**
     * 手动同步刷新小程序城市门店列表缓存。
     */
    @PostMapping("/refresh-city-list-cache")
    @Operation(summary = "同步刷新城市门店列表缓存")
    @PreAuthorize("@ss.hasPermission('system:store:update')")
    public CommonResult<Boolean> refreshCityListCache() {
        return success(storeCityListCacheService.refreshAllSynchronously());
    }

    /**
     * 初始化门店标签 Redis 缓存，key 为 activity_tag{businessId}:{storeId}，value 为该门店的标签 id 列表
     */
    @PostMapping("/init-store-tag-cache")
    @Operation(summary = "初始化门店标签Redis缓存")
    public CommonResult<Boolean> initStoreTagCache() {
        return success(systemStoreInfoService.initStoreTagCache());
    }

    @GetMapping("/match-store-tag-cache")
    @Operation(summary = "判断门店标签缓存是否命中")
    @Parameter(name = "businessId", description = "项目编号", required = true, example = "10")
    @Parameter(name = "storeId", description = "门店编号", required = true, example = "1024")
    @Parameter(name = "tagIds", description = "标签编号列表", required = true, example = "1,2,3")
    public CommonResult<Boolean> matchStoreTagCache(@RequestParam("businessId") Long businessId,
                                                    @RequestParam("storeId") Long storeId,
                                                    @RequestParam("tagIds") List<Long> tagIds) {
        return success(systemStoreInfoService.matchStoreTagCache(businessId, storeId, tagIds));
    }

    /**
     * 生成二维码
     */
    @GetMapping("/getStoreQRCode")
    @Operation(summary = "生成二维码")
    public CommonResult<Boolean> getStoreQRCode(@RequestParam("storeId") Long storeId) {
        systemStoreInfoService.getStoreQRCode(storeId);
        return success(true);
    }

    /**
     * 下载门店的二维码
     *
     * @param storeId
     * @return
     */
    @GetMapping("/image/getWxaCodeImag")
    public CommonResult<String> getWxaCodeByStoreId(@RequestParam("storeId") Long storeId) {
        String wxaCodeByStoreId = systemStoreInfoService.getWxaCodeByStoreId(storeId);
        return success(wxaCodeByStoreId);
    }

    @GetMapping("/get")
    @Operation(summary = "获得门店详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('system:store:query')")
    public CommonResult<StoreDetailResVO> getStoreDetail(@RequestParam("id") Long id) {
        StoreDetailResVO storeDetailResVO = systemStoreInfoService.getStoreDetail(id);
        return success(storeDetailResVO);
    }

    @PutMapping("/updateBatchTag")
    @Operation(summary = "批量修改标签")
    @PreAuthorize("@ss.hasPermission('system:store:update')")
    public CommonResult<Boolean> updateBatchTag(@RequestBody StoresUpdateVO storeUpdateVO) {
        systemStoreInfoService.updateBatchTag(storeUpdateVO);
        return success(true);
    }

    @PutMapping("/updateDelTag")
    @Operation(summary = "批量删除标签")
    @PreAuthorize("@ss.hasPermission('system:store:update')")
    public CommonResult<Boolean> updateDelTag(@RequestBody StoresUpdateVO storeUpdateVO) {
        systemStoreInfoService.updateDelTag(storeUpdateVO);
        return success(true);
    }

    @PostMapping("/selectStoreByTag")
    @Operation(summary = "根据标签查询门店")
//    @PreAuthorize("@ss.hasPermission('system:store:query')")
    public CommonResult<List<StoreResVO>> selectStoreByTag(@RequestBody StoreTagReqVO storeTagReqVO) {
        List<StoreResVO> storeInfoDOS = systemStoreInfoService.selectStoreByTag(storeTagReqVO);
        return success(storeInfoDOS);
    }

    @GetMapping("/export")
    @Operation(summary = "导出门店")
    @PreAuthorize("@ss.hasPermission('system:store:export')")
    @ApiAccessLog(operateType = EXPORT)
    public CommonResult<String> exportStoreList(@Validated StorePageReqVO exportReqVO, HttpServletRequest request, HttpServletResponse response) throws IOException {
        systemStoreInfoService.exportStoreList(exportReqVO, request, response);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }


    @GetMapping("/selectByOrgStoreList")
    @Operation(summary = "根据组织ID获取门店列表ID")
    public CommonResult<List<Long>> selectByOrgStoreList(@RequestParam("orgId") Long orgId) {
        List<Long> storeIdList = systemStoreInfoService.selectByOrgStoreList(orgId);
        return success(storeIdList);
    }

    @PostMapping("/choose/page")
    @Operation(summary = "查询门店")
    public CommonResult<PageResult<SystemStoreInfoDO>> chooseStore(@RequestBody StorePageListReqVO pageReqVO) {
        // 获得用户分页列表
        PageResult<SystemStoreInfoDO> pageResult = systemStoreInfoService.chooseStore(pageReqVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        return success(pageResult);
    }

    @PostMapping("/selectDouyinAllStore")
    @Operation(summary = "一键全选抖音全部门店")
    public CommonResult<List<SystemStoreInfoDO>> selectDouyinAllStore(@RequestBody(required = false) StorePageListReqVO reqVO) {
        List<SystemStoreInfoDO> list = systemStoreInfoService.selectDouyinAllStore(reqVO);
        return success(list);
    }


    @PostMapping("/selectAllStore")
    @Operation(summary = "一键全选全部门店")
    public CommonResult<List<SystemStoreInfoDO>> selectAllStore(@RequestBody(required = false) StorePageListReqVO reqVO) {
        List<SystemStoreInfoDO> list = systemStoreInfoService.selectAllStore(reqVO);
        return success(list);
    }

    @GetMapping("/selectOrgStoreList")
    @Operation(summary = "根据组织ID获取门店列表")
    public CommonResult<Set<SystemStoreInfoDO>> selectOrgStoreList(@RequestParam("orgId") Long orgId) {
        Set<SystemStoreInfoDO> storeIdList = systemStoreInfoService.selectOrgStoreList(orgId);
        return success(storeIdList);
    }

    @DataPermission(enable = false)
    @PermitAll
    @GetMapping("/batch/exchange")
    @Operation(summary = "批量创建交换机")
    public CommonResult<String> batchExchange() {
        List<StoreInfoDTO> list = systemStoreInfoService.getAllStoreList();
        list.forEach(i -> {
            String exchangeName = "exchange" + i.getStoreId();
            String topicName = "queue" + i.getStoreId();
            log.info(">>> 初始化" + i.getStoreName() + "交换机");
            rabbitMQService.bindExchange(rabbitMQService.creatExchange(exchangeName), rabbitMQService.createQueue(topicName));
        });

        return CommonResult.success("OK");
    }
    @GetMapping("/listSimple")
    @Operation(summary = "下拉选择门店")
    public CommonResult<List<StoreSimpleResVO>> listSimple() {
        // 获得用户分页列表
        List<StoreSimpleResVO> listSimple = systemStoreInfoService.listSimple();
        return success(listSimple);
    }


    @GetMapping("getStoreListByUser")
    @Operation(summary = "根据登录人获取门店列表")
    public CommonResult<List<StoreSimpleResVO>> getStoreListByUser() {
        return success(systemStoreInfoService.getStoreListByUser());
    }
    @GetMapping("syncLocation")
    @Operation(summary = "同步门店经纬度 省市区")
    @PermitAll
    public CommonResult<String> syncLocation() {
        systemStoreInfoService.syncLocation();
        return success("0K");
    }

    @GetMapping("fixStoreExpenses")
    @PermitAll
    @DataPermission(enable = false)
    public CommonResult<String> fixStoreExpenses() {
        systemStoreInfoService.fixStoreExpenses();
        return success("0K");
    }



    @GetMapping("getMaterialStoreList")
    @Operation(summary = "进销存-获取门店列表")
    public CommonResult<List<StoreSimpleResVO>> getMaterialStoreList(@RequestParam(required = false) String name,@RequestParam(required = false) Long storeId,@RequestParam(required = false) Long warehouseId) {
        return success(systemStoreInfoService.getMaterialStoreList(name,storeId,warehouseId));
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
     * 查询部外部id 是否重复
     */
    @GetMapping("/getCheckOutId")
    @Operation(summary = "查询部外部id")
    @PermitAll
    public CommonResult<StoreSimpleResVO> getCheckOutId(@Param("type") int type,@Param("outId") String outId,@Param("storeId") String storeId){
        return success(systemStoreInfoService.getCheckOutId(type,outId,storeId));
    }

    @GetMapping("/getStoreIdsByTagIds")
    @Operation(summary = "后端测试用")
    public CommonResult<Map<Long, List<StoreInfoDTO>>> getStoreIdsByTagIds(@Param("tagIds") List<Long> tagIds) {
        return success(systemStoreInfoService.getStoreIdsByTagIds(tagIds));
    }

    @GetMapping("getByLetterStoreList")
    @Operation(summary = "根据登录人获取按字母排序列表")
    public CommonResult<List<StoreLettersRespVO>> getByLetterStoreList() {
        return success(systemStoreInfoService.getByLetterStoreList());
    }

    @PostMapping("getByLetterStoreListWithIndex")
    @Operation(summary = "根据登录人获取按字母排序列表（带字母索引）")
    public CommonResult<StoreListWithIndexVO> getByLetterStoreListWithIndex(@RequestBody(required = false) StoreQueryDTO queryDTO) {  // 新增模糊查询参数
        return success(systemStoreInfoService.getByLetterStoreListWithIndex(queryDTO));
    }
}
