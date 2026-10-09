package com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 部门设置/撤销负责人 Request VO")
@Data
public class DeptUserSetChargeReqVO {

    @Schema(description = "部门id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "部门id不能为空")
    private Long deptId;

    @Schema(description = "用户部门关系表id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "用户部门关系表id不能为空")
    private Long userDeptId;
}
