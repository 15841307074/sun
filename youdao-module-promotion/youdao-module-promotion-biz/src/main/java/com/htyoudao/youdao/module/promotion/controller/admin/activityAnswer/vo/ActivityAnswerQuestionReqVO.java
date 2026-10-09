package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivityAnswerQuestionReqVO {

    @Schema(description = "题目ID，新增可不传")
    private Long id;

    @Schema(description = "题目类型 1选择题")
    private Integer questionType = 1;

    @Schema(description = "题干")
    @NotBlank(message = "题干不能为空")
    private String questionTitle;

    @Schema(description = "题目提示文案")
    private String questionTips;

    @Schema(description = "选项JSON")
    @NotBlank(message = "题目选项不能为空")
    private String optionsJson;

    @Schema(description = "正确答案选项编码")
    @NotBlank(message = "正确答案不能为空")
    private String correctAnswer;

    @Schema(description = "每题答题时间，单位秒，0不限制")
    private Integer answerTime;
}
