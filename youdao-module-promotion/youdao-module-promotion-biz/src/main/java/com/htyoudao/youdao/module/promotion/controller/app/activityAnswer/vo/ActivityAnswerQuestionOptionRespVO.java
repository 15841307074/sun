package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 有奖问答题目选项响应。
 */
@Data
public class ActivityAnswerQuestionOptionRespVO {

    @Schema(description = "选项编码")
    private String code;

    @Schema(description = "选项文案")
    private String text;
}
