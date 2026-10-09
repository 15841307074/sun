package com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "管理后台 - 兑换券下单时必选商品 Request VO")
@Data
public class ExchangeCommodityReqVO {

    @Schema(description = "商品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "23994")
    private Long commodityId;

    @Schema(description = "商品id", requiredMode = Schema.RequiredMode.REQUIRED, example = "23994")
    private String commodityName;
}
