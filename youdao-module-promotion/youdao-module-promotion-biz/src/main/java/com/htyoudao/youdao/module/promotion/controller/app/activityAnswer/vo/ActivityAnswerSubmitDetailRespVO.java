package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 有奖问答提交明细响应。
 */
@Data
public class ActivityAnswerSubmitDetailRespVO {

    @Schema(description = "题目ID")
    private Long questionId;

    @Schema(description = "题目名称")
    private String questionTitle;

    @Schema(description = "是否已作答 0未作答 1已作答")
    private Integer isAnswered;

    @Schema(description = "是否正确")
    private Boolean isCorrect;
}
