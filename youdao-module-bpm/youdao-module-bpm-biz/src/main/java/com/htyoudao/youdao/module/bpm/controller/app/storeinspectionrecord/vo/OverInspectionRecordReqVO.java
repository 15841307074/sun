package com.htyoudao.youdao.module.bpm.controller.app.storeinspectionrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "app - 完成巡检 Request VO")
public class OverInspectionRecordReqVO {

    @Schema(description = "id", requiredMode = Schema.RequiredMode.REQUIRED, example = "28721")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "当前经度", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "longitude不能为空")
    private String longitude;

    @Schema(description = "latitude", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "latitude不能为空")
    private String latitude;
}
