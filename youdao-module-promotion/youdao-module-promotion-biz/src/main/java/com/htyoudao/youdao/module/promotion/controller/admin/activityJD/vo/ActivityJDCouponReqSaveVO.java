package com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo;

import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcoupon.GoodCouponDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 营销集点活动 兑换优惠券 Request VO")
@Data
public class ActivityJDCouponReqSaveVO {

    private Long goodsId;

    @Schema(description = "奖品ID")
    @NotNull(message = "奖品名称 不能为空")
    private Long id;

    @Schema(description = "奖品类型: 1 优惠券 2 优惠券包")
    private Long goodsType = 1L;

    @Schema(description = "奖品名称")
    @NotNull(message = "奖品名称 不能为空")
    private String couponName;

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

    @Schema(description = "优惠券完整信息")
    private GoodCouponRespVO goodCouponRespVO;
}
