package com.htyoudao.youdao.module.promotion.controller.app.survey.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "小程序 - 提交答卷 Response VO")
@Data
public class AppSurveySubmitRespVO {

    @Schema(description = "答卷ID")
    private Long answerId;

    @Schema(description = "是否有奖励")
    private Boolean hasReward;

    @Schema(description = "奖励类型")
    private Integer rewardType;

    @Schema(description = "奖励类型文本")
    private String rewardTypeText;

    @Schema(description = "奖励值")
    private String rewardValue;

    @Schema(description = "奖励状态")
    private Integer rewardStatus;

    @Schema(description = "奖励状态文本")
    private String rewardStatusText;

    @Schema(description = "奖励消息")
    private String rewardMessage;

    @Schema(description = "奖励积分数量（rewardType=1 时有值）", example = "50")
    private Integer rewardPoints;

    @Schema(description = "优惠券名称（rewardType=2 时有值）", example = "满100减20优惠券")
    private String couponName;

    @Schema(description = "优惠券包名称（rewardType=3 时有值）", example = "新人优惠券包")
    private String couponPackageName;

    @Schema(description = "是否显示感谢信息")
    private Boolean showThanks;

    @Schema(description = "感谢文案")
    private String thanksText;

    @Schema(description = "跳转URL")
    private String redirectUrl;
}
