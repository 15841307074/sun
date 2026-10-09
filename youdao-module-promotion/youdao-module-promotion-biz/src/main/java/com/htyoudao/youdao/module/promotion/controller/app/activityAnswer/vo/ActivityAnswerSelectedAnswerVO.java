package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 有奖问答用户答案。
 */
@Data
public class ActivityAnswerSelectedAnswerVO {

    @Schema(description = "题目ID")
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    @Schema(description = "用户选择的选项编码")
    @NotNull(message = "用户选择不能为空")
    private String selectedAnswer;
}
