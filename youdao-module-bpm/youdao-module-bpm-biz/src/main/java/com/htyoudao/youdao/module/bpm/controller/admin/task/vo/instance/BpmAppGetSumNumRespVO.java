package com.htyoudao.youdao.module.bpm.controller.admin.task.vo.instance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "APP - 任务数量统计响应 VO")
@Data
public class BpmAppGetSumNumRespVO {

    @Schema(description = "任务总数", example = "100")
    private Long totalTasks = 0L;

    @Schema(description = "待审核任务数", example = "20")
    private Long pendingTasks = 0L;

    @Schema(description = "进行中的任务数", example = "30")
    private Long inProgressTasks = 0L;

    @Schema(description = "已拒绝任务数", example = "7")
    private Long rejectedTasks = 0L;

    @Schema(description = "已完成任务数", example = "45")
    private Long completedTasks = 0L;

    @Schema(description = "已逾期任务数（只统计待审核和进行中的逾期数）", example = "5")
    private Long overdueTasks = 0L;

    @Schema(description = "完成率", example = "45.00")
    private Double completionRate = 0.0;

}
