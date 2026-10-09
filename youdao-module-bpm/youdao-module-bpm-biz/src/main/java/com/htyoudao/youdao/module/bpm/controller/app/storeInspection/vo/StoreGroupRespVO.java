package com.htyoudao.youdao.module.bpm.controller.app.storeInspection.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "app - 门店分组列表 Response VO")
@Data
public class StoreGroupRespVO {

    @Schema(description = "首字母", example = "A")
    private String initial;

    @Schema(description = "该字母下的门店列表")
    private List<StoreItemVO> storeList;

    @Data
    public static class StoreItemVO {
        @Schema(description = "门店ID")
        private Long storeId;

        @Schema(description = "门店名称")
        private String storeName;
    }
}