package com.htyoudao.youdao.module.system.controller.app.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "app - 门店分组列表 Response VO")
public class StoreItemVO {

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "门店名称")
    private String storeName;
}