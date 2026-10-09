package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.checklist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 点检项大类 Request VO")
@Data
public class StoreInspectionTypeReqVO {

    @Schema(description = "大类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private String typeId;

    @Schema(description = "大类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "大类")
    private String typeName;
}
