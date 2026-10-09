package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.product;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 实时的返回结果
 */
@Data
public class ProductRealTimeResult {
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
    @Schema(description = "是否套餐")
    private Integer isSingle;
    @Schema(description = "销量")
    private Integer salesVolume;
    @Schema(description = "当前时间段的在售门店数")
    private Long storeCount;
}
