package com.htyoudao.youdao.module.promotion.controller.app.survey.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "小程序 - 问卷分享详情 Response VO")
@Data
public class AppSurveyShareVO {

    @Schema(description = "问卷ID")
    private Long id;

    @Schema(description = "问卷名称")
    private String surveyName;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享内容")
    private String shareNote;

    @Schema(description = "分享图片")
    private String shareImgUrl;
}
