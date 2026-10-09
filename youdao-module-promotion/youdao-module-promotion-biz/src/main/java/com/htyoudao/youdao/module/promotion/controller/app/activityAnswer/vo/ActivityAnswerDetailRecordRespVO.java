package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 有奖问答详情页作答明细响应。
 */
@Data
public class ActivityAnswerDetailRecordRespVO {

    @Schema(description = "题目ID")
    private Long questionId;

    @Schema(description = "题目名称")
    private String questionTitle;

    @Schema(description = "是否已作答 0未作答 1已作答")
    private Integer isAnswered;

    @Schema(description = "用户选择答案编码")
    private String selectedAnswer;

    @Schema(description = "答题结果 0错误 1正确")
    private Integer answerResult;

    @Schema(description = "是否正确")
    private Boolean isCorrect;
}
