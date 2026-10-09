package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 签到活动数据分析汇总 Response VO")
@Data
public class ActivitySignAnalysisSummaryRespVO {

    @Schema(description = "总浏览量PV")
    private Long pv;

    @Schema(description = "总访客量UV，按手机号去重")
    private Long uv;

    @Schema(description = "参与人数，按手机号去重")
    private Long joinCount;

    @Schema(description = "签到次数")
    private Long signCount;

    @Schema(description = "分享人数，按分享事件会员ID去重")
    private Long shareCount;

    @Schema(description = "发放人数，按手机号去重")
    private Long rewardUserCount;

    @Schema(description = "发放次数")
    private Long rewardIssueCount;

    @Schema(description = "发放积分数量")
    private BigDecimal pointAmount;

    @Schema(description = "发放优惠券张数")
    private Long couponCount;

    @Schema(description = "发放优惠券包数量")
    private Long couponPackageCount;

    @Schema(description = "发放红包金额")
    private BigDecimal redPacketAmount;

    @Schema(description = "发放实物数量")
    private Long physicalCount;
}
