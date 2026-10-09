package com.htyoudao.youdao.module.system.api.store;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.store.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author dht
 */
@Tag(name = "RPC 服务 - 门店")
public interface StoreApi {


    @Operation(summary = "根据门店id 查询门店")
    CommonResult<List<StoreSimpleResDto>> getStoreSimpleResDtoList(@RequestParam("storeIds") List<Long> storeIds);
    @Operation(summary = "根据名称查询门店列表")
    CommonResult<List<StoreInfoDTO>> getStoreListByName(String storeName);

    @Operation(summary = "通过ids查询门店列表")
    CommonResult<List<StoreInfoDTO>> getStoresByStoreIds(List<Long> storeIds);

    @Operation(summary = "通过id查询门店")
    CommonResult<StoreDTO> getStoreByStoreId(@RequestParam("storeId") Long storeId);

    @Operation(summary = "通过抖音门店id查询门店")
    CommonResult<StoreDTO> getStoreByDouyinStoreId(@RequestParam("douyinStoreId") Long douyinStoreId);

    @Operation(summary = "通过ids查询门店列表")
    CommonResult<List<Long>> getStoreIdsByDeptId(Long orgId);


    @Operation(summary = "通过ids查询门店列表")
    CommonResult<List<StoreInfoDTO>> getStoreIdsByOrgId(Long orgId);


    @Operation(summary = "查询全部门店列表id和name  微信用户导出用")
    CommonResult<List<StoreInfoDTO>> getAllStoreList();

    @Operation(summary = "查询全部门店列表id和name  外部调用传进来businessId")
    CommonResult<List<StoreInfoDTO>> getStoreListByBusinessId(Long businessId);

    @Operation(summary = "查询门店可配送范围")
    CommonResult<StoreDeliveryDTO> getStoreDelivery(Long storeId);

    @Operation(summary = "查询同一城市下关联的门店信息-小程序")
    public CommonResult<List<StoreWecomConfigResDTO>> selectByCouponStoreList(@RequestBody StoreWecomConfigReqDTO storeWecomConfigReqDTO);

    @Operation(summary = "根据当前用户ID获取额外绑定的所有门店ID")
    public CommonResult<List<Long>> selectOtherStoreIdsByUserId(Long userId);

    @Operation(summary = "根据当前用户ID获取组织绑定的所有门店ID")
    public CommonResult<List<Long>> selectUserAllOrgStoreIds(Long userId);

    /**
     * 获取当前org下所有部门下的门店ID
     * @param orgId orgId
     * @return CommonResult<List<Long>>
     */
    CommonResult<List<Long>> getStoreIdsByAllOrgId(Long orgId);

    @Operation(summary = "根据当前用户ID获取可见门店ID")
    public CommonResult<Set<Long>> getAllStoreIdByUser(Long userId);

    @Operation(summary = "通过仓库ID查询门店列表")
    CommonResult<List<Long>> getStoreIdsByWarehouseId(Long warehouseId);
    @Operation(summary = "通过渠道ID查询门店列表")
    CommonResult<List<StoreSimpleResDto>> getStoreListByChannelId(@RequestParam("channelType") String channelType,@RequestParam("channelIds") List<String> channelIds);

    @Operation(summary = "门店是否有这个标签")
    Boolean isTag(@RequestParam("storeId") Long storeId, @RequestParam("tagId") Long tagId);

    @Operation(summary = "广告对应标签关联的门店")
    List<StoreInfoDTO> selectAdvertisingStoreList(@RequestParam("tagList") List<Long> tagList);

    /**
     * 查询门店基础信息（用于：warehouseId分组、仓库名、门店名、门店创建时间等）
     * 要求：只返回 storeIds 范围内、且 deleted=false 的门店
     */
    List<StoreInfoDTO> listStoresByIds(List<Long> storeIds);

    /**
     * 查询闭店日志（用于：闭店门店数统计，不去重）
     * 口径：store_status=1 && deleted=0 && create_time in [start, end)
     * 要求：仅返回 storeIds 范围内的数据
     */
    List<StoreStatusLogDTO> listClosedLogs(List<Long> storeIds, LocalDateTime startInclusive, LocalDateTime endExclusive);

    /**
     * 十天未进货详情接口：查询某仓库下的门店（同时受入参 storeIds 限制）
     * 要求：deleted=false && warehouseId=xx && storeId in storeIds
     */
    List<StoreInfoDTO> listStoresByWarehouse(Long warehouseId, List<Long> storeIds);


    /**
     * 根据标签值 ids 批量获storeIds
     * @param ids 标签值 id 列表
     * @return id -> name 映射
     */
    Map<Long, List<StoreInfoDTO>> getStoreIdsByTagIds(List<Long> ids);

    /**
     * 判断距离
     * @param storeId storeId
     * @param distance distance
     * @param longitude longitude
     * @param latitude latitude
     * @return Boolean
     */
    Boolean judgeDistance(String longitude, String latitude, Long storeId, double distance);

    /**
     * 获取当前登录人能查看的门店id
     * @return List<Long>
     */
    List<Long> getStoreIdsByUser();

    /**
     * 判断门店标签缓存是否命中：activity_tag{businessId}:{storeId} 的标签集合中是否包含入参标签中的任意一个
     *
     * @param businessId 项目编号
     * @param storeId    门店编号
     * @param tagIds     待判断的标签编号列表
     * @return 命中任意一个标签返回 true
     */
    Boolean matchStoreTagCache(Long businessId, Long storeId, List<Long> tagIds);
}
