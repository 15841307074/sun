package com.htyoudao.youdao.module.promotion.api.activity.VO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 营销集点活动 兑换优惠券 Request VO")
@Data
public class ActivityJDCouponRespSaveVO {

    private Long goodsId;

    @Schema(description = "奖品ID")
    @NotNull(message = "奖品名称 不能为空")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "奖品类型: 1 优惠券 2 优惠券包")
    private Long goodsType = 1L;

    @Schema(description = "奖品名称")
    @NotNull(message = "奖品名称 不能为空")
    private String couponName;

    @Schema(description = "奖品名称")
    @NotNull(message = "奖品名称 不能为空")
    private String packageName;

    @Schema(description = "奖品名称")
    @NotNull(message = "奖品名称 不能为空")
    private String name;

    // 图片地址
    @Schema(description = "奖品图片地址")
    @NotNull(message = "奖品图片地址 不能为空")
    private String imageUrl;

    @Schema(description = "活动库存")
    @NotNull(message = "活动库存")
    private Integer inventory;

    @Schema(description = "兑换点数")
    @NotNull(message = "兑换点数")
    private Integer redeemPoints;

    @Schema(description = "是否领取过")
    private Boolean claimed = Boolean.FALSE;

}
