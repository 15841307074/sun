package com.htyoudao.youdao.module.analysis.controller.admin.vo.res.product;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductSingleResult {
    @Schema(description = "商品ID")
    private Long commodityId;
    @Schema(description = "商品名称")
    private String goodsName;
    @Schema(description = "商品图片URL")
    private String goodsImage;
    @Schema(description = "销量")
    private Integer salesVolume;
}
