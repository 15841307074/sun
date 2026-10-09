package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 问卷列表 Response VO")
@Data
public class SurveyRespVO {

    @Schema(description = "问卷ID")
    private Long id;

    @Schema(description = "问卷名称")
    private String surveyName;

    @Schema(description = "状态：0-未发布 1-进行中 2-已结束")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "时间控制：0-创建成功后即可填写（无结束时间） 1-按开始/结束时间控制")
    private Integer timeControl;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;

    @Schema(description = "开始时间开关")
    private Integer startTimeEnabled;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;

    @Schema(description = "结束时间开关")
    private Integer endTimeEnabled;

    @Schema(description = "访问UV")
    private Integer uvCount;

    @Schema(description = "提交人数")
    private Integer submitCount;

    @Schema(description = "奖励类型")
    private Integer rewardType;

    @Schema(description = "奖励类型文本")
    private String rewardTypeText;
}
