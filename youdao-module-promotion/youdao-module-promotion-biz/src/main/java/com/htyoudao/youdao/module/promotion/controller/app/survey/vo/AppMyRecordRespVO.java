package com.htyoudao.youdao.module.promotion.controller.app.survey.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "小程序 - 我的答卷记录 Response VO")
@Data
public class AppMyRecordRespVO {

    @Schema(description = "答卷ID")
    private Long answerId;

    @Schema(description = "问卷ID")
    private Long surveyId;

    @Schema(description = "问卷名称")
    private String surveyName;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "奖励类型")
    private Integer rewardType;

    @Schema(description = "奖励类型文本")
    private String rewardTypeText;

    @Schema(description = "奖励状态")
    private Integer rewardStatus;

    @Schema(description = "奖励状态文本")
    private String rewardStatusText;
}
