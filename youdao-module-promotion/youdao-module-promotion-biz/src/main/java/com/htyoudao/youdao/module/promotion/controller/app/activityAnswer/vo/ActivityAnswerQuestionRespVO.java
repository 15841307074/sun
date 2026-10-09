package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 有奖问答题目响应。
 */
@Data
public class ActivityAnswerQuestionRespVO {

    @Schema(description = "题目ID")
    private Long questionId;

    @Schema(description = "题目类型 1选择题")
    private Integer questionType;

    @Schema(description = "题干")
    private String questionTitle;

    @Schema(description = "提示文案")
    private String questionTips;

    @Schema(description = "正确答案选项编码")
    private String correctAnswer;

    @Schema(description = "选项列表")
    private List<ActivityAnswerQuestionOptionRespVO> options;

    @Schema(description = "单题答题时间，单位秒")
    private Integer answerTime;

    @Schema(description = "是否已作答 0未作答 1已作答")
    private Integer isAnswered;

    @Schema(description = "用户选择的选项编码")
    private String selectedAnswer;
}
