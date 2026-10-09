package com.htyoudao.youdao.module.promotion.controller.app.activityJK.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 任务信息VO
 *
 * @date 2026-03-14
 */
@Data
@Schema(description = "任务信息")
public class TaskVO {


    @Schema(description = "任务类型 1签到 2下单 3分享助力 4浏览首页")
    private Integer taskType;
    @Schema(description = "下单金额门槛")
    private BigDecimal amount;
    @Schema(description = "完成上限")
    private Integer taskLimit;
    @Schema(description = "完成数量")
    private Integer finishCount;
}
