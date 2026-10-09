package com.htyoudao.youdao.module.system.api.tenant;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.enums.ApiConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "RPC 服务 - 多租户")
public interface TenantApi {

    @Operation(summary = "获得所有租户编号")
    CommonResult<List<Long>> getTenantIdList();

    @Operation(summary = "校验租户是否合法")
    @Parameter(name = "id", description = "租户编号", required = true, example = "1024")
    CommonResult<Boolean> validTenant(@RequestParam("id") Long id);

}
