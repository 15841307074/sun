package com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "活动推广渠道 新增/修改 VO")
@Data
public class ActivityChannelSaveReqVO {

    @Schema(description = "id")
    private Integer id;
    // 渠道名称
    @Schema(description = "渠道名称")
    @NotNull(message = "渠道名称 不能为空")
    private String channelName;

    @Schema(description = "活动 Id'")
    private Long activityId;

    // 链接地址
    @Schema(description = "渠道链接")
    private String linkUrl;

    @Schema(description = "渠道Id")
    @NotNull(message = "渠道Id 不能为空")
    private Long channelId;
}
