package com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * @author dht
 */
@Data
@Schema(description = "管理后台 - 部门添加人员 Request VO")
public class DeptSaveUserReqVO {

    @Schema(description = "部门id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "部门id不能为空")
    private Long deptId;

    @Schema(description = "用户id数组", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "用户id不能为空")
    private List<Long> userIds;
}
