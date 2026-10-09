package com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
@Schema(description = "管理后台 - 秒杀活动商品  返回 VO")
@Data
public class ActivitySeckillCommodityRespVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "activityId")
    private Long activityId;


    @Schema(description = "商品价格")
    private BigDecimal commodityPrice;

    // 商品ID
    @Schema(description = "商品ID")
    private Long commodityId;

    // 秒杀价格（小数点后两位）
    @Schema(description = "秒杀价格（小数点后两位）")
    private BigDecimal seckillPrice;

    // 活动库存
    @Schema(description = "活动库存")
    private Integer activityStock;

    // 划线价格（小数点后两位）
    @Schema(description = "划线价格（小数点后两位）")
    private BigDecimal linePrice;

    // 单品限购数
    @Schema(description = "单品限购数")
    private Integer limitPerItem;

    @Schema(description = "规格 ID")
    private Long skuId;

    @Schema(description = "商品名称")
    private String commodityName;

}
