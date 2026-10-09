package com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo;

import com.htyoudao.youdao.module.promotion.controller.admin.goodcoupon.vo.GoodCouponRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 营销集点活动 兑换优惠券更新对比对象 Request VO")
@Data
@EqualsAndHashCode(callSuper = false)
public class ActivityJDCouponCompareVO {


    @Schema(description = "奖品ID")
    @NotNull(message = "奖品名称 不能为空")
    private Long id;

    @Schema(description = "活动库存")
    @NotNull(message = "活动库存")
    private Integer inventory;

}
