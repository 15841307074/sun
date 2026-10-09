package com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActivityCqStatisticsRespVO {

    /**
     * 参与人数
     */

    @Schema(description = "参与人数")
    private Long participation;

    /**
     * 参与次数
     */

    @Schema(description = "参与次数")
    private Long count;

    /**
     * 中奖人数
     */
    @Schema(description = "中奖人数")
    private Long winningParticipation;

    /**
     * 发放积分
     */
    @Schema(description = "发放积分")
    private BigDecimal pointsCount;

    /**
     * 发放优惠券
     */
    @Schema(description = "发放优惠券")
    private Long couponCount;

    /**
     * 发放红包
     */
    @Schema(description = "发放红包")
    private BigDecimal totalCash;
}
