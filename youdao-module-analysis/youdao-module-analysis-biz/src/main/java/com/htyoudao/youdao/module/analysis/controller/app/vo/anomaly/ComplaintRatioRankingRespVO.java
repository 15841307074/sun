package com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "App - 投诉占比排行响应")
@Data
public class ComplaintRatioRankingRespVO {

    @Schema(description = "门店ID", example = "2109515101383192577")
    private String storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "投诉订单占比（%）", example = "70.34")
    private BigDecimal complaintOrderRatio;

    @Schema(description = "投诉订单数", example = "200")
    private Long complaintOrderCount;
}
