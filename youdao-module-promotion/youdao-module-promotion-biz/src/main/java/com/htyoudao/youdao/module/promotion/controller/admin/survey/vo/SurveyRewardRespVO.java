package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 问卷奖励配置 Response VO")
@Data
public class SurveyRewardRespVO {

    @Schema(description = "问卷ID")
    private Long id;

    @Schema(description = "奖励类型")
    private Integer rewardType;

    @Schema(description = "奖励类型文本")
    private String rewardTypeText;

    @Schema(description = "积分数")
    private Integer rewardPoints;

    @Schema(description = "优惠券ID")
    private Long couponId;

    @Schema(description = "优惠券名称")
    private String couponName;

    @Schema(description = "发放方式")
    private Integer grantMode;

    @Schema(description = "发放方式文本")
    private String grantModeText;

    @Schema(description = "发放条件")
    private String grantCondition;
}
