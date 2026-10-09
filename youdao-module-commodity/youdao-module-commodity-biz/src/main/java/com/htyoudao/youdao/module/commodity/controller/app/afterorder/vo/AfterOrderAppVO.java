package com.htyoudao.youdao.module.commodity.controller.app.afterorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dht
 */
@Schema(description = "app - 订单生成后加购商品集合 Request VO")
@Data
@ToString(callSuper = true)
public class AfterOrderAppVO implements Serializable {

    /**
     * 订单生成后加购商品ID
     */
    @Schema(description = "订单生成后加购商品ID", example = "14445")
    private Long afterId;

    /**
     * 商品ID
     */
    @Schema(description = "商品ID", example = "14445")
    private Long commodityId;

    /**
     * 商品缩略图
     */
    @Schema(description = "商品缩略图", example = "14445")
    private String thumbnailUrl;

    /**
     * 商品名称
     */
    @Schema(description = "商品名称", example = "14445")
    private String commodityName;

    /**
     * 加购价格
     */
    @Schema(description = "加购价格", example = "14445")
    private BigDecimal afterPrice;

    /**
     * 划线价格
     */
    @Schema(description = "划线价格", example = "14445")
    private BigDecimal strikeThroughPrice;

    /**
     * 商品状态 (1: 正常, 2: 售罄, 3: 异常)
     */
    @Schema(description = "商品状态 (1: 正常, 2: 售罄, 3: 异常)", example = "1")
    private Integer commodityStatus;

    /**
     * skuCode
     */
    @Schema(description = "skuCode", example = "14445")
    private List<String> skuCode = new ArrayList<>();
}
