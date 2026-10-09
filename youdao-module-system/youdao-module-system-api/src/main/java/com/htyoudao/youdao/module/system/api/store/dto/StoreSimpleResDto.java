package com.htyoudao.youdao.module.system.api.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "管理后台 - 门店信息 Response VO")
@Data
public class StoreSimpleResDto implements Serializable {
    @Schema(description = "门店id", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long storeId;
    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    private String storeName;
    @Schema(description = "抖音id", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    private Long tiktokId;
    @Schema(description = "美团id", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    private String meituanId;
    @Schema(description = "饿了么id", requiredMode = Schema.RequiredMode.REQUIRED, example = "0090")
    private String hungryId;
}
