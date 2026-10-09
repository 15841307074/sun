package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 签到活动推广配置 Response VO")
@Data
public class ActivitySignSpreadRespVO {

    @Schema(description = "活动ID", example = "10086")
    private Long id;

    @Schema(description = "分享标题", example = "签到领福利")
    private String shareTitle;

    @Schema(description = "分享内容/分享描述", example = "每日签到，福利享不停")
    private String shareNote;

    @Schema(description = "分享图片", example = "https://xxx/share.png")
    private String shareImgUrl;
}
