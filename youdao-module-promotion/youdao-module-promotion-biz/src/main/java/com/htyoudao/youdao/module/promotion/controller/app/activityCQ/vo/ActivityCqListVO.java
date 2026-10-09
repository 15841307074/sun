package com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "抽签活动列表")
public class ActivityCqListVO {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "活动标题")
    private String activityTitle;

    @Schema(description = "活动封面图")
    private String activityCoverImage;

    @Schema(description = "活动类型")
    private Long activityType;
}
