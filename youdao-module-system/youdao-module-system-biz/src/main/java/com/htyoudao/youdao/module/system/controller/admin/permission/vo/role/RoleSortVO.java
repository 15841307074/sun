package com.htyoudao.youdao.module.system.controller.admin.permission.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleSortVO {

    @NotNull
    @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long roleId;

    @NotNull
    @Schema(description = "角色排序值", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Integer sort;
}
