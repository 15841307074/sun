package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 点检项排序 Request VO")
@Data
public class TemplateChecklistsSortReqVO {

    @Schema(description = "主键ID")
    @NotNull
    private Long templateChecklistId;

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    @NotNull
    private Long templateId;

    @Schema(description = "大类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    @NotNull
    private Long typeId;

    @Schema(description = "排序")
    @NotNull
    private Integer sort;

}
