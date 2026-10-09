package com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo;

import com.htyoudao.youdao.module.promotion.controller.admin.couponpackage.vo.CouponPackageRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 营销活动秒杀新增/修改 Request VO")
@Data
public class ActivityJDCouponPackageReqSaveVO {

    private Long goodsId;

    @Schema(description = "奖品ID")
    @NotNull(message = "奖品名称 不能为空")
    private Long id;

    @Schema(description = "奖品类型: 1 优惠券 2 优惠券包")
    private Long goodsType = 2L;

    @Schema(description = "奖品名称")
    @NotNull(message = "奖品名称 不能为空")
    private String packageName;

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

    private CouponPackageRespVO couponPackageRespVO;

}
