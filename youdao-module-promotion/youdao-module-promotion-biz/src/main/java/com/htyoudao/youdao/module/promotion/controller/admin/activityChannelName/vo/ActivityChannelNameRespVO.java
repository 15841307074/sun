package com.htyoudao.youdao.module.promotion.controller.admin.activityChannelName.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 渠道名称 Response VO")
@Data
public class ActivityChannelNameRespVO {

    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "渠道名称", example = "默认渠道(微信H5路径)")
    private String name;

    @Schema(description = "是否启用 1是 0否", example = "1")
    private Integer isEnable;



    @Schema(description = "是否默认 1是 0否", example = "1")
    private Integer isDefault;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}

