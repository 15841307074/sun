package com.htyoudao.youdao.module.promotion.controller.app.activitySeckill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
@Schema(description = "APP - 秒杀活动场次 返回 VO")
@Data
public class ActivitySeckillAppTimeResqVO {

    @Schema(description = "id")
    private Long id;

    @Schema(description = "activityId")
    private Long activityId;

    @Schema(description = "场次")
    private Integer times;

    @Schema(description = "开始时间")
    private Integer startTime;

    @Schema(description = "结束时间")
    private Integer endTime;

    @Schema(description = "活动场次状态 1 未开始 2 进行中 3 已结束")
    private Integer status;

    @Schema(description = "秒数"
        + "活动状态未开始展示距离开始时间差多少秒，"
        + "进行中时 展示还剩多少时间结束，"
        + "已结束为0")
    private Long secondsCount;
}
