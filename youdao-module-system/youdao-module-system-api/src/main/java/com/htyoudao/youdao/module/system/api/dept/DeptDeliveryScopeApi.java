package com.htyoudao.youdao.module.system.api.dept;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptDeliveryScopeDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "RPC 服务 - 门店配送范围")
public interface DeptDeliveryScopeApi {

    @Operation(summary = "获得部门配送范围")
    @Parameter(name = "id", description = "部门编号", example = "1024", required = true)
    CommonResult<List<DeptDeliveryScopeDTO>> getDeptDeliveryScope(DeptDeliveryScopeDTO deptDeliveryScopeDTO);

}
