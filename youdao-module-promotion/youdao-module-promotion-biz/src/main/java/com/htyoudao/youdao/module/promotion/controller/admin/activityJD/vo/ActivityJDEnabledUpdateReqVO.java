package com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "集点活动开启关闭 VO")
@Data
public class ActivityJDEnabledUpdateReqVO {
    @Schema(description = "活动 id")
    @NotNull(message = "活动 id 不能为空")
    private Long id;

    @Schema(description = "开启状态 1是 0否")
    @NotNull(message = "开启状态 1是 0否 不能为空")
    private Integer isEnabled;
}
