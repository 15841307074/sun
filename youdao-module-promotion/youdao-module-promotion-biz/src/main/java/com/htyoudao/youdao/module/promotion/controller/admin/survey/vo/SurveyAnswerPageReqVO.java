package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 答卷分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SurveyAnswerPageReqVO extends PageParam {

    @Schema(description = "问卷ID")
    private Long surveyId;

    @Schema(description = "奖励状态：0-未发放 1-已发放 2-发放失败")
    private Integer rewardStatus;

    @Schema(description = "提交时间起始")
    private LocalDateTime submitTimeBegin;

    @Schema(description = "提交时间截止")
    private LocalDateTime submitTimeEnd;
}
