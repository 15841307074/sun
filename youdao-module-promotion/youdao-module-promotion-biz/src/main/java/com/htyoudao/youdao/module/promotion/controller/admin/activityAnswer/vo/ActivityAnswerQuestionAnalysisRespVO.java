package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 有奖问答题目分析返回。
 */
@Data
public class ActivityAnswerQuestionAnalysisRespVO {

    @Schema(description = "题目ID")
    private Long questionId;

    @Schema(description = "题目名称")
    private String questionTitle;

    @Schema(description = "作答总人次")
    private Long answerCount;

    @Schema(description = "答对人次")
    private Long correctCount;

    @Schema(description = "答错人次")
    private Long wrongCount;

    @Schema(description = "正确率")
    private BigDecimal accuracy;
}
