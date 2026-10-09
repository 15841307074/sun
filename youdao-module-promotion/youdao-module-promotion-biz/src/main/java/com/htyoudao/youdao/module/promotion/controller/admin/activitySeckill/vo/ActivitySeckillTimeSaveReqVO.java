package com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo;

import cn.hutool.core.date.DateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 营销活动秒杀场次新增/修改 Request VO")
@Data
public class ActivitySeckillTimeSaveReqVO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "场次")
    @NotNull(message = "场次 不能为空")
    private Integer times;
    @Schema(description = "开始时间")
    @NotNull(message = "开始时间 不能为空")
    private Integer startTime;
    @Schema(description = "结束时间")
    @NotNull(message = "结束时间 不能为空")
    private Integer endTime;
}
