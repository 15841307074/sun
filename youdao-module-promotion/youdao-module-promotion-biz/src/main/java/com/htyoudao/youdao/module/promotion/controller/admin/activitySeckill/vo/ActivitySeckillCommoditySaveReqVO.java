package com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
@Schema(description = "管理后台 - 秒杀活动 商品新增/修改 Request VO")
@Data
public class ActivitySeckillCommoditySaveReqVO {
    @Schema(description = "ID")
    private Long id;

    // 商品ID
    @Schema(description = "商品ID")
    @NotNull(message = "商品ID 不能为空")
    private Long commodityId;

    // 秒杀价格（小数点后两位）
    @Schema(description = "秒杀价格（小数点后两位）")
    @NotNull(message = "秒杀价格（小数点后两位） 不能为空")
    private BigDecimal seckillPrice;

    // 活动库存
    @Schema(description = "活动库存")
    @NotNull(message = "活动库存 不能为空")
    private Integer activityStock;

    // 划线价格（小数点后两位）
    @Schema(description = "划线价格（小数点后两位）")
    private BigDecimal linePrice;

    // 单品限购数
    @Schema(description = "单品限购数")

    private Integer limitPerItem;

    @Schema(description = "规格 ID")
    @NotNull(message = "规格 ID 不能为空")
    private Long skuId;

    @Schema(description = "商品价格")
    @NotNull(message = "商品价格 不能为空")
    private BigDecimal commodityPrice;

    @Schema(description = "商品名称")
    @NotNull(message = "商品名称 不能为空")
    private String commodityName;


}
