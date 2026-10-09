package com.htyoudao.youdao.module.bpm.controller.app.storeInspection.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "app - 巡检模板下拉 Response VO")
@Data
public class InspectionTemplateDropDownRespVO {

    @Schema(description = "模板ID", example = "100")
    private Long templateId;

    @Schema(description = "模板名称", example = "巡店模板")
    private String templateName;
}
