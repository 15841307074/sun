package com.htyoudao.youdao.module.promotion.controller.admin.couponPackageShare.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "优惠券包请求体", description = "优惠券包请求实体")
public class CouponPackageDetailReqVO {

    @Schema(description = "packageId")
    @NotNull(message = "packageId 不能为空")
    private Long packageId;

    @Schema(description = "图片路径")
    @NotNull(message = "图片路径 不能为空")
    private String pageUrl;
}
