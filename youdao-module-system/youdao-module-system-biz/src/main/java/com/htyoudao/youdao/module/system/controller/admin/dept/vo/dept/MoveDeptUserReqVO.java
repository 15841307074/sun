package com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author dht
 */
@Data
@Schema(description = "管理后台 - 部门移动人员 Request VO")
public class MoveDeptUserReqVO {

    @Schema(description = "旧的部门id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "旧的部门id不能为空")
    private Long oldDeptId;

    @Schema(description = "新的部门id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "新的部门id不能为空")
    private Long newDeptId;

    @Schema(description = "用户部门关系表id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "用户部门关系表id不能为空")
    private List<Long> userDeptIds;
}
