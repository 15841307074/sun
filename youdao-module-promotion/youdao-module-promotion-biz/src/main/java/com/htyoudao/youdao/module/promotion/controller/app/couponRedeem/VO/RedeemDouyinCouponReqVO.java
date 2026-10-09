package com.htyoudao.youdao.module.promotion.controller.app.couponRedeem.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 兑换
 * </p>
 *
 * @author zhangjihe
 * @since 2025-07-02
 */
@Data
public class RedeemDouyinCouponReqVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 248054797420232420L;

    @Schema(description = "抖音门店ID")
    @NotNull(message = "抖音门店ID不能为空")
    private Long douyinStoreId;

    @Schema(description = "抖音券码")
    private String douyinCouponCode;

    @Schema(description = "扫码连接")
    private String shortLink;
}
