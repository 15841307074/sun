package com.htyoudao.youdao.module.system.api.orgstore;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.orgstore.dto.StorePageReqVO;
import com.htyoudao.youdao.module.system.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@FeignClient(name = ApiConstants.NAME)
@Tag(name = "RPC 服务 - 组织")
public interface OrgStoreApi {

    String PREFIX = ApiConstants.PREFIX + "/org";


    @GetMapping(PREFIX+"getStoreIdsByUser")
    @Operation(summary = "根据当前登入人以及项目 获取本级组织以及下级组织id")
    CommonResult<Set<Long>> getStoreIdsByUser(@RequestParam("businessId") Long businessId) ;

    @GetMapping(PREFIX+"/selectByOrgStoreList")
    @Operation(summary = "根据组织ID获取门店列表")
    public CommonResult<List<Long>> selectByOrgStoreList(@RequestParam("orgId") Long orgId) ;

}
