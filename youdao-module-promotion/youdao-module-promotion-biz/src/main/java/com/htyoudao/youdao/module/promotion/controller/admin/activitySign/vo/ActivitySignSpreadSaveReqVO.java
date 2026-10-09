package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 签到活动推广配置修改 Request VO")
@Data
public class ActivitySignSpreadSaveReqVO {

    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
    @NotNull(message = "活动ID不能为空")
    private Long id;

    @Schema(description = "分享标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "签到领福利")
    @NotBlank(message = "分享标题不能为空")
    private String shareTitle;

    @Schema(description = "分享内容/分享描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "每日签到，福利享不停")
    @NotBlank(message = "分享内容不能为空")
    private String shareNote;

    @Schema(description = "分享图片", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://xxx/share.png")
    @NotBlank(message = "分享图片不能为空")
    private String shareImgUrl;
}
