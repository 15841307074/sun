package com.htyoudao.youdao.module.promotion.controller.admin.survey.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 问卷分享配置 Response VO")
@Data
public class SurveyShareRespVO {

    @Schema(description = "问卷ID")
    private Long id;

    @Schema(description = "分享链接")
    private String shareLink;

    @Schema(description = "二维码图片URL")
    private String shareQrcodeUrl;

    @Schema(description = "分享标题")
    private String shareTitle;

    @Schema(description = "分享图片URL")
    private String shareImgUrl;

    @Schema(description = "小程序分享卡片路径")
    private String miniCardPath;
}
