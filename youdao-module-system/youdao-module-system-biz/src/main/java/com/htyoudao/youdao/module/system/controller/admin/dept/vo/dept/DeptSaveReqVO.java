package com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 部门创建/修改 Request VO")
@Data
public class DeptSaveReqVO {

    @Schema(description = "部门编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "youdao")
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 30, message = "部门名称长度不能超过 30 个字符")
    private String name;

    @Schema(description = "父部门 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "父部门 ID不能为空")
    private Long parentId;

    @Schema(description = "负责人id,应该没这字段不用传",requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
}
