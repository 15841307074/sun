package com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "App - 食品卫生投诉排行响应")
@Data
public class FoodHygieneComplaintRankingRespVO {

    @Schema(description = "门店ID", example = "2109515101383192577")
    private String storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "食品卫生投诉订单数", example = "20")
    private Long foodHygieneComplaintOrderCount;

    @Schema(description = "全部投诉订单数", example = "200")
    private Long complaintOrderCount;
}
