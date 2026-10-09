package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.template.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 模板点检项删除 Request VO")
@Data
public class TemplateChecklistDelReqVO {

    @Schema(description = "主键ID")
    private Long templateId;

    @Schema(description = "模板点检项", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private List<Long> checklistIds;



}
