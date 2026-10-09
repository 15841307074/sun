package com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

/**
 * @author DHT
 */
@Data
@ToString(callSuper = true)
@Schema(description = "app - 用户的优惠券分页 Request VO")
public class AppCouponListReqVO extends PageParam {

    @Schema(description = "是否使用过，0：未使用，1：已使用 2：已过期", example = "0")
    @NotNull(message = "是否使用过不能为空")
    private Integer isUsed;

    @Schema(description = "会员id", example = "0")
    @NotNull(message = "会员id不能为空")
    private Long userId;

    @Schema(name = "用餐方式 0 堂食 1 打包 2 外卖", description = "用餐方式 0 堂食 1 打包 2 外卖")
    private Integer habit;

    @Schema(description = "领取方式(0-自动发放,1-手动领取)")
    private Long distributionMethod;

    @Schema(description = "优惠券编码")
    private String couponCode;

    @Schema(description = "指定门店id")
    @NotNull(message = "storeId不能为空")
    private String storeId;
}
