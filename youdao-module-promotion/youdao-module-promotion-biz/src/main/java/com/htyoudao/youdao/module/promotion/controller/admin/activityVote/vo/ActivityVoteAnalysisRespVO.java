package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActivityVoteAnalysisRespVO {

    @Schema(description = "总浏览量PV")
    private Long totalPv;

    @Schema(description = "总访客量UV")
    private Long totalUv;

    @Schema(description = "参与人数")
    private Long participants;

    @Schema(description = "分享人数")
    private Long shareCount;

    @Schema(description = "投票次数")
    private Long totalVotes;

    @Schema(description = "今日投票")
    private Long todayVotes;

    @Schema(description = "发放积分")
    private BigDecimal totalPoints;

    @Schema(description = "发放优惠券(张)")
    private Long couponCount;

    @Schema(description = "发放红包金额")
    private BigDecimal totalRedPacket;

    @Schema(description = "奖励发放总数")
    private Long rewardCount;
}
