package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 有奖问答单题提交请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityAnswerSubmitReqVO extends ActivityAnswerBaseReqVO {

    @Schema(description = "答题记录ID")
    @NotNull(message = "答题记录ID不能为空")
    private Long recordId;

    @Schema(description = "当前提交的题目ID")
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    @Schema(description = "用户选择的选项编码")
    @NotBlank(message = "用户答案不能为空")
    private String selectedAnswer;
}
