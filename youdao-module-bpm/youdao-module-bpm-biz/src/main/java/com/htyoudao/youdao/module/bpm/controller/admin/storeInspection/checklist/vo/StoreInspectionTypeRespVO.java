package com.htyoudao.youdao.module.bpm.controller.admin.storeInspection.checklist.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 点检项大类 Response VO")
@Data
public class StoreInspectionTypeRespVO {

    @Schema(description = "大类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "123")
    private Long typeId;

    @Schema(description = "大类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "大类")
    private String typeName;

    @Schema(description = "分数", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer typeScore;

}
