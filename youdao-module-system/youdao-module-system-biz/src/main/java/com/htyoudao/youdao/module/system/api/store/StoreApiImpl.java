package com.htyoudao.youdao.module.system.api.store;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.api.store.dto.*;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreChannelReqVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreSimpleResVO;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import com.htyoudao.youdao.module.system.service.storeuser.StoreUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.SYSTEM_BLOCK_ERROR;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.SYSTEM_FALLBACK_ERROR;

@DubboService
@Validated
@Slf4j
public class StoreApiImpl implements StoreApi{

    @Resource
    private SystemStoreInfoService systemStoreInfoService;

    @Resource
    private StoreUserService storeUserService;


    @Override
    public CommonResult<List<StoreSimpleResDto>> getStoreSimpleResDtoList(List<Long> storeIds) {
        return success(BeanUtils.toBean(systemStoreInfoService.getStoreSimpleResDtoList(storeIds), StoreSimpleResDto.class));
    }

    @Override
    public CommonResult<List<StoreInfoDTO>> getStoreListByName(String storeName) {
        return success(systemStoreInfoService.getStoreListByName(storeName));
    }

    @Override
    public CommonResult<List<StoreInfoDTO>> getStoresByStoreIds(List<Long> storeIds) {
        return success(systemStoreInfoService.getStoresByStoreIds(storeIds));
    }

    @SentinelResource(value = "getStoreByStoreId", fallback = "getStoreByStoreIdFallback", blockHandler = "getStoreByStoreIdExceptionHandler")
    @Override
    public CommonResult<StoreDTO> getStoreByStoreId(Long storeId) {
        return success(systemStoreInfoService.getStoreByStoreId(storeId));
    }

    @Override
    public CommonResult<StoreDTO> getStoreByDouyinStoreId(Long douyinStoreId) {
        return success(systemStoreInfoService.getStoreByDouyinStoreId(douyinStoreId));
    }

    public CommonResult<StoreDTO> getStoreByStoreIdFallback(Long storeId, Throwable ex) {
        log.error("StoreApi.getStoreByStoreIdFallback",ex);
        throw exception(SYSTEM_FALLBACK_ERROR);
    }

    public CommonResult<StoreDTO> getStoreByStoreIdExceptionHandler(Long storeId, BlockException ex) {
        log.error("StoreApi.getStoreByStoreIdExceptionHandler",ex);
        throw exception(SYSTEM_BLOCK_ERROR);
    }

    public CommonResult<List<Long>> getStoreIdsByDeptId(Long orgId) {
        return success(systemStoreInfoService.getStoreIdsByDeptId(orgId));
    }

    @Override
    public CommonResult<List<StoreInfoDTO>> getAllStoreList() {
        return success(systemStoreInfoService.getAllStoreList());
    }

    @Override
    public CommonResult<List<StoreInfoDTO>> getStoreListByBusinessId(Long businessId) {
        return success(systemStoreInfoService.getStoreListByBusinessId(businessId));
    }

    @Override
    public CommonResult<StoreDeliveryDTO> getStoreDelivery(Long storeId) {
        return success(systemStoreInfoService.getStoreDelivery(storeId));
    }

    @Override
    public CommonResult<List<StoreWecomConfigResDTO>> selectByCouponStoreList(StoreWecomConfigReqDTO storeWecomConfigReqDTO) {
        return success(systemStoreInfoService.selectByCouponStoreList(storeWecomConfigReqDTO));
    }

    /**
     * 用户额外绑定门店绑定关系
     * @return
     */
    @Override
    public CommonResult<List<Long>> selectOtherStoreIdsByUserId(Long userId) {
        return success(storeUserService.selectUserStoreIdsTwo(userId));
    }

    /**
     * 用户组织orgIds 本层级与下级所有门店
     * @return
     */
    @Override
    public CommonResult<List<Long>> selectUserAllOrgStoreIds(Long userId) {
        return success(systemStoreInfoService.selectUserAllOrgStoreIds(userId));
    }

    @Override
    public CommonResult<List<Long>> getStoreIdsByAllOrgId(Long orgId) {
        return success(systemStoreInfoService.getStoreIdsByAllOrgId(orgId));
    }

    @Override
    public CommonResult<Set<Long>> getAllStoreIdByUser(Long userId) {
        Set<Long> allStoreIds = new HashSet<>();
        List<Long> otherBindingIds = selectOtherStoreIdsByUserId(userId).getCheckedData();
        List<Long> allOrgStoreIds = selectUserAllOrgStoreIds(userId).getCheckedData();
        if(CollectionUtil.isNotEmpty(otherBindingIds)){
            allStoreIds.addAll(otherBindingIds);
        }
        if(CollectionUtil.isNotEmpty(allOrgStoreIds)){
            allStoreIds.addAll(allOrgStoreIds);
        }
        return success(allStoreIds);
    }

    @Override
    public CommonResult<List<Long>> getStoreIdsByWarehouseId(Long warehouseId) {
        return success(systemStoreInfoService.getStoreIdsByWarehouseId(warehouseId));
    }

    @Override
    public CommonResult<List<StoreInfoDTO>> getStoreIdsByOrgId(Long orgId) {
        return success(systemStoreInfoService.getStoreIdsByOrgId(orgId));
    }
    @Override
    public CommonResult<List<StoreSimpleResDto>> getStoreListByChannelId(String channelType, List<String> channelIds) {
        return success(systemStoreInfoService.getStoreListByChannelId(channelType, channelIds));
    }

    @Override
    public Boolean isTag(Long storeId, Long tagId) {
        return systemStoreInfoService.isTag(storeId,tagId);
    }

    @Override
    public List<StoreInfoDTO> selectAdvertisingStoreList(List<Long> tagList) {
        return systemStoreInfoService.selectAdvertisingStoreList(tagList);
    }

    @Override
    public List<StoreInfoDTO> listStoresByIds(List<Long> storeIds) {
        return systemStoreInfoService.listStoresByIds(storeIds);
    }

    @Override
    public List<StoreStatusLogDTO> listClosedLogs(List<Long> storeIds, LocalDateTime startInclusive, LocalDateTime endExclusive) {
        return systemStoreInfoService.listClosedLogs(storeIds, startInclusive, endExclusive);
    }

    @Override
    public List<StoreInfoDTO> listStoresByWarehouse(Long warehouseId, List<Long> storeIds) {
        return systemStoreInfoService.listStoresByWarehouse(warehouseId, storeIds);
    }

    @Override
    public Map<Long, List<StoreInfoDTO>> getStoreIdsByTagIds(List<Long> ids) {
        return systemStoreInfoService.getStoreIdsByTagIds(ids);
    }

    @Override
    public Boolean judgeDistance(String longitude, String latitude, Long storeId, double distance) {
        return systemStoreInfoService.judgeDistance(longitude, latitude, storeId, distance);
    }

    @Override
    public List<Long> getStoreIdsByUser() {
        return systemStoreInfoService.getStoreIdsByUser();
    }

    @Override
    public Boolean matchStoreTagCache(Long businessId, Long storeId, List<Long> tagIds) {
        return systemStoreInfoService.matchStoreTagCache(businessId, storeId, tagIds);
    }
}
