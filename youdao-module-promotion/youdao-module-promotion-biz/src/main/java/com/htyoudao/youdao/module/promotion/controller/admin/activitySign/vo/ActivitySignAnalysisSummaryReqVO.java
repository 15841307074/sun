package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 签到活动数据分析汇总 Request VO")
@Data
public class ActivitySignAnalysisSummaryReqVO {

    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "签到周期标识，如20260601_20260630；不传默认当前周期", example = "20260601_20260630")
    private String periodKey;

    @Schema(hidden = true, description = "后端调试参数：true 时跳过中间件，直接查询 MySQL")
    private Boolean forceMysql;

}
