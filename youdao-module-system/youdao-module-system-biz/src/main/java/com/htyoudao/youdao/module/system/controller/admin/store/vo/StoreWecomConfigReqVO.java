package com.htyoudao.youdao.module.system.controller.admin.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class StoreWecomConfigReqVO {
    @Schema(description = "门店经度", example = "1024")
    private double longitude;

    @Schema(description = "门店维度", example = "1024")
    private double latitude;

    @Schema(description = "城市名称", example = "1024")
    private String cityName;

    @Schema(description = "门店名", example = "1024")
    private String storeName;

    @Schema(description = "门店id", example = "1024")
    private Long storeId;

}
