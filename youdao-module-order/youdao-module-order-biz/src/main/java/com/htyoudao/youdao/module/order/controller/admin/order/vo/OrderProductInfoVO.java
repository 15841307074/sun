package com.htyoudao.youdao.module.order.controller.admin.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 订单商品信息
 * </p>
 *
 * @author zhangjihe
 * @since 2024-04-23
 */
@Data
public class OrderProductInfoVO {

    private Long orderId;

    private Integer orderQuantity;

    private BigDecimal payAmount;

    private BigDecimal moneyAmount;

    private Long memberId;

    private Long storeId;

    private BigDecimal goodsShowPrice;

    private String goodsName;

    private Integer goodsNum;

    private String goodsImage;

    private Long goodsId;

    private Long categoryId;

    private String categoryName;

    private Integer orderFrom;
}
