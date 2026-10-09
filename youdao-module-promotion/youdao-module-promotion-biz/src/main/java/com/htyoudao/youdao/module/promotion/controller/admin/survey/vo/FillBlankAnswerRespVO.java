package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 填空题答案 Response VO")
@Data
public class FillBlankAnswerRespVO {

    @Schema(description = "答卷ID")
    private Long answerId;

    @Schema(description = "手机号")
    private Long phone;

    @Schema(description = "提交时间")
    private LocalDateTime submitTime;

    @Schema(description = "答案文本")
    private String answerText;
}
