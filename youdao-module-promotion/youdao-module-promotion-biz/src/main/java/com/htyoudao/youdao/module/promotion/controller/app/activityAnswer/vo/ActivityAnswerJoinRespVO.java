package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 有奖问答开始或继续响应。
 */
@Data
public class ActivityAnswerJoinRespVO {

    @Schema(description = "答题记录ID")
    private Long recordId;

    @Schema(description = "答题编号")
    private String answerNo;

    @Schema(description = "参与类型 1新开始 2继续上次")
    private Integer joinType;

    @Schema(description = "剩余答题次数")
    private Integer chanceCount;

    @Schema(description = "题目总数")
    private Integer questionCount;

    @Schema(description = "已作答题目ID")
    private List<Long> answeredQuestionIds;

    @Schema(description = "已作答答案")
    private List<ActivityAnswerSelectedAnswerVO> answers;

    @Schema(description = "题目列表，包含是否已作答和用户选择")
    private List<ActivityAnswerQuestionRespVO> questions;
}
