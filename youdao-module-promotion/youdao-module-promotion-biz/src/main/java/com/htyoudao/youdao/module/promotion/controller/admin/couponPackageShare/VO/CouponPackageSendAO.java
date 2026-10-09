package com.htyoudao.youdao.module.promotion.controller.admin.couponPackageShare.VO;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(name = "优惠券包发送入参", description = "优惠券包发送入参")
public class CouponPackageSendAO {

    /**
     * 会员手机号
     */
    @Schema(description = "会员手机号")
    @NotNull(message = "会员手机号 不能为空")
    private String memberMobile;

    /**
     * 发放数量
     */
    @Schema(description = "发放数量")
    @NotNull(message = "会员手机号 不能为空")
    private Integer couponNum;

    /**
     * 优惠券id
     */
    @Schema(description = "优惠券包id")
    @NotNull(message = "优惠券包id 不能为空")
    private Long packageId;

}
