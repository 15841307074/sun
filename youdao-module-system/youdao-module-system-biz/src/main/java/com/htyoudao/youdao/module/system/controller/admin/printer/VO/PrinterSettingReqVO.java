package com.htyoudao.youdao.module.system.controller.admin.printer.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PrinterSettingReqVO {
    @Schema(description = "小票名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "小票类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer documentType;
}
