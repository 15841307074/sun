package com.htyoudao.youdao.module.promotion.controller.admin.advertising.carousel.VO;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.validation.InEnum;
import com.htyoudao.youdao.module.promotion.enums.advertising.carousel.PackageTypeEnum;
import com.htyoudao.youdao.module.promotion.enums.advertising.carousel.UserRestrictionsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 轮播优惠券选择 Request VO")
@Data
public class CouponPackageChooseReqVO extends PageParam {

    @Schema(description = "优惠券名称/备注")
    private String couponPackageNameOrRemark;

    @Schema(description = "领取限制 0 不限制 1 新注册用户 2 老用户 3 回归用户")
    @InEnum(UserRestrictionsEnum.class)
    private Integer userRestrictions;

    @Schema(description = "0 普通券包 1 周周惠券包")
    @InEnum(PackageTypeEnum.class)
    private Integer packageType;

}
