package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 签到活动启停 Request VO")
@Data
public class ActivitySignStatusUpdateReqVO {

    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
    @NotNull(message = "活动ID不能为空")
    private Long id;

    @Schema(description = "是否启用：0停用 1启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "是否启用不能为空")
    private Integer enabled;
}
