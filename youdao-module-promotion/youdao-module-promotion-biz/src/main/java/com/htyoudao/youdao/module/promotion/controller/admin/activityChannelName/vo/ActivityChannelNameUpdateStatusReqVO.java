package com.htyoudao.youdao.module.promotion.controller.admin.activityChannelName.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 渠道名称修改状态 Request VO")
@Data
public class ActivityChannelNameUpdateStatusReqVO {

    @Schema(description = "id", example = "1")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "是否启用 1是 0否", example = "1")
    @NotNull(message = "是否启用不能为空")
    private Integer isEnable;
}

