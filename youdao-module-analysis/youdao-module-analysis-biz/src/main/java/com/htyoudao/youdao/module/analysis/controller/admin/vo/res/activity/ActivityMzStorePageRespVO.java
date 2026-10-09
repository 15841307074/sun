package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 满赠活动门店分页响应 VO
 */
@Data
@Schema(description = "满赠活动门店分页响应 VO")
public class ActivityMzStorePageRespVO {

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "付款用户数")
    private Long customerCount;

    @Schema(description = "订单数")
    private Long orderNumber;

    @Schema(description = "支付总金额")
    private Double payAmount;

    @Schema(description = "购买商品件数")
    private Long commodityCount;

    @Schema(description = "赠送商品数量")
    private Long giftCommodityCount;

    @Schema(description = "活动优惠")
    private Double offerAmount;
}
