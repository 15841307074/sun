package com.htyoudao.youdao.module.promotion.controller.app.activityJD.vo;

import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDCouponRespSaveVO;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityJDFullRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author dht
 */
@Data
@Schema(description = "APP - 秒杀活动详情 返回 VO")
public class ActPointRespVO {

    @Schema(description = "活动信息")
    private ActivityJDFullRespVO activity;

    @Schema(description = "剩余时间")
    private Long surplusTime;

    @Schema(description = "兑换物")
    List<ActivityJDCouponRespSaveVO> couponList;

    @Schema(description = "已领取优惠券的id")
    private Set<Long> memberClaimedCoupons;

    @Schema(description = "用户手中点数")
    private Integer memberPoints;
}
