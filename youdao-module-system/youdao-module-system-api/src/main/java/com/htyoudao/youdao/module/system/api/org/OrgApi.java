package com.htyoudao.youdao.module.system.api.org;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.org.dto.OrgRespDTO;
import com.htyoudao.youdao.module.system.api.org.dto.StoreOrgDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "RPC 服务 - 组织")
public interface OrgApi   {

    @Operation(summary = "根据当前登入人以及项目 获取本级组织以及下级组织id")
     CommonResult<Set<Long>> getStoreIdsByUser(@RequestParam("businessId") Long businessId) ;

    @Operation(summary = "根据当前登入人以及项目 获取本级组织以及下级组织id")
    CommonResult<Set<Long>> getStoreIdListByOrgID(@RequestParam("orgId")  Long orgId,@RequestParam("businessId") Long businessId) ;

    @Operation(summary = "根据当前组织ID 获取本级组织以及下级组织id")
    public CommonResult<Set<OrgRespDTO>> getChildOrgList(Long orgId);

    /**
     * 根据门店id获取组织信息
     * @param storeIds storeIds
     * @return CommonResult
     */
    CommonResult<Set<StoreOrgDTO>> getOrgListByStoreId(List<Long> storeIds);

    /**
     * 根据门店id获取组织信息
     * @param storeIds storeIds
     * @return CommonResult
     */
    CommonResult<Set<StoreOrgDTO>> getOrgListByStoreIdV2(List<Long> storeIds);

    /**
     * 根据门店id获取组织信息 全部
     * @return CommonResult
     */
    CommonResult<Set<StoreOrgDTO>> getAllOrgListByStoreIdV2();

    /**
     * 根据组织id获取所有门店id
     * @param orgId 组织id
     * @return 门店id集合
     */
    CommonResult<Set<Long>> getAllStoreIdListByOrgID(Long orgId);
}
