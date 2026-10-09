package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 问卷分享配置更新 Request VO")
@Data
public class SurveyShareUpdateReqVO {

    @Schema(description = "问卷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "问卷ID不能为空")
    private Long id;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享图片URL")
    private String shareImgUrl;

    @Schema(description = "小程序分享卡片路径")
    private String miniCardPath;
}
