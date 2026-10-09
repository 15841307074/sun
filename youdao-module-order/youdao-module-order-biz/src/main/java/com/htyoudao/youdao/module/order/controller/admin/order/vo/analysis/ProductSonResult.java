package com.htyoudao.youdao.module.order.controller.admin.order.vo.analysis;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProductSonResult {
    @Schema(description = "商品ID")
    private Long commodityId;
    @Schema(description = "商品名称")
    private String goodsName;
    @Schema(description = "商品图片URL")
    private String goodsImage;
    @Schema(description = "销量")
    private Double salesVolume;
}
