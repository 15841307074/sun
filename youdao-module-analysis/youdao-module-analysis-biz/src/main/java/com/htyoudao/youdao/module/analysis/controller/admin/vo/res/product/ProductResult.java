package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.product;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductResult {
    @Schema(description = "商品ID")
    private Long commodityId;
    @Schema(description = "商品名称")
    private String goodsName;
    @Schema(description = "分类名称")
    private String categoryName;
    @Schema(description = "商品图片URL")
    private String goodsImage;
    @Schema(description = "销售额")
    private Double salesAmount;
    @Schema(description = "商品类型 1-单品 2-套餐")
    private Integer isSingle;
    @Schema(description = "是否加购商品 1-是 0-否")
    private Integer isPurchase;
    @Schema(description = "单独售卖销量")
    private Integer salesVolume;
    @Schema(description = "套餐内单品销量")
    private Integer singleSalesVolume;
    @Schema(description = "总销量")
    private Integer allSalesVolume;
    @Schema(description = "带来订单数")
    private Integer orderCount;
    @Schema(description = "下单人数")
    private Long customerCount;
    @Schema(description = "复购人数")
    private Long repurchaseUserCount;
    @Schema(description = "复购率")
    private BigDecimal repurchaseRate;
    @Schema(description = "点击人数")
    private Long clickUserCount;
    @Schema(description = "加购人数")
    private Long addCartUserCount;
    @Schema(description = "新客占比")
    private BigDecimal newCustomerRate = BigDecimal.ZERO;
    @Schema(description = "新客人数")
    private Long newCustomerCount;
    @Schema(description = "当前时间段的在售门店数")
    private Long storeCount;
}
