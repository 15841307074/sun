package com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 优惠券包修改总量 VO")
@Data
public class CouponPackageUpdateNumVO {

    @Schema(description = "Id")
    @NotNull(message = "Id 不能为空")
    private Long id;
    /**
     * 优惠券剩余数目
     */
    @Schema(description = "优惠券剩余数目")
    @NotNull(message = "优惠券剩余数目 不能为空")
    private Integer packageNum;

}
