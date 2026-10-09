package com.htyoudao.youdao.module.promotion.controller.app.activitySeckill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "APP - 秒杀活动分享详情 返回 VO")
@Data
public class ActivitySeckillAppShareVO {
    //id
    @Schema(description = "id")
    private Long id;
    //activityId
    @Schema(description = "activityId")
    private Long activityId;
    // 分享图片
    @Schema(description = "分享图片")
    private String shareImageUrl;
    // 分享标题
    @Schema(description = "分享标题")
    private String shareTitle;
    // 分享描述
    @Schema(description = "分享描述")
    private String shareDescription;
}
