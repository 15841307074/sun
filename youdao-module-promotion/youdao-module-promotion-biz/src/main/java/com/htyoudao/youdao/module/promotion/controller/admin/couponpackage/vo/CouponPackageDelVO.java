package com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 优惠券包删除 Request VO")
@Data

public class CouponPackageDelVO {
    @Schema(description = "Id")
    @NotNull(message = "id 不能为空")
    private  Long id;
}
