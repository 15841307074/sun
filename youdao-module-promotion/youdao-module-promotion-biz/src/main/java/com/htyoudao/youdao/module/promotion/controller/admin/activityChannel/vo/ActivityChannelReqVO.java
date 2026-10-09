package com.htyoudao.youdao.module.promotion.controller.admin.activityChannel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 活动推广渠道 刷新请求 VO")
@Data
public class ActivityChannelReqVO {

    @Schema(description = "活动ID", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "渠道名称ID 列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "渠道ID不能为空")
    private List<Long> channelIds;

    @Schema(description = "活动类型：1 n件n折 2 秒杀 3 转盘 4 福袋 5 集点", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "活动类型不能为空")
    private Integer type;
}
