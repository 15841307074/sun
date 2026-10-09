package com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Schema(description = "管理后台 - 秒杀活动 商品新增/修改 Request VO")
@Data
public class ActivitySeckillCouponSaveReqVO {
    @Schema(description = "ID")
    private Long id;

    // 优惠券ID
    @Schema(description = "优惠券ID")
    @NotNull(message = "优惠券ID 不能为空")
    private Long couponId;

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

    @Schema(description = "显示价格")
    private BigDecimal showPrice;

    @Schema(description = "显示标题")
    private String showTitle;


}
