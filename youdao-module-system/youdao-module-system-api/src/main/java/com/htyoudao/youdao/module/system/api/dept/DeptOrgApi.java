package com.htyoudao.youdao.module.system.api.dept;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "RPC 服务 - 部门")
public interface DeptOrgApi {

    @Operation(summary = "查询当前用户部门下的所有用户")
    CommonResult<List<Long>> getUserIdsByDept();

}
