package com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO;

import com.htyoudao.youdao.framework.common.validation.InEnum;
import com.htyoudao.youdao.module.promotion.enums.advertising.carousel.UserRestrictionsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - 轮播优惠券包已选择 Request VO")
@Data
public class CouponPackageChoosedReqVO {

    @Schema(description = "广告轮播图ID",requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String advertisingImageId;

}
