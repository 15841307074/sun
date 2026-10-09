package com.htyoudao.youdao.module.system.controller.admin.permission.vo.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.apache.dubbo.common.logger.FluentLogger.S;
import org.checkerframework.checker.units.qual.K;
import org.checkerframework.checker.units.qual.N;

@Schema(description = "管理后台 - 复制角色 Request VO")
@Data
public class PermissionCopyRoleReqVO {

    @Schema(description = "来源角色 id", example = "1")
    @NotNull(message = "来源角色不能为空")
    private Long sourceRoleId;

    @Schema(description = "角色名称", example = "1")
    @NotBlank(message = "角色名称不能为空")
    private String name;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注", example = "1")
    private String remark;

}
