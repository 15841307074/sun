package com.htyoudao.youdao.module.system.controller.admin.permission.vo.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Schema(description = "管理后台 - 赋予用户角色 Request VO")
@Data
public class PermissionRemoveUserRoleReqVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "用户编号不能为空")
    private List<Long> userIds;


    @Schema(description = "移除的角色 id", example = "1,3,5")
    @NotNull(message = "移除的角色不能为空")
    private Long roleId;
}
