package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 有奖问答数据分析请求。
 */
@Data
public class ActivityAnswerAnalysisReqVO {

    @Schema(description = "活动主表 ID")
    @NotNull(message = "活动 ID 不能为空")
    private Long activityId;

    @Schema(description = "开始时间，支持 yyyy-MM-dd / yyyy-MM-dd HH:mm / yyyy-MM-dd HH:mm:ss")
    private String startTime;

    @Schema(description = "结束时间，支持 yyyy-MM-dd / yyyy-MM-dd HH:mm / yyyy-MM-dd HH:mm:ss")
    private String endTime;

    public LocalDateTime getStartTime() {
        return ActivityAnswerTimeParser.parseStart(startTime);
    }

    public LocalDateTime getEndTime() {
        return ActivityAnswerTimeParser.parseEnd(endTime);
    }
}
