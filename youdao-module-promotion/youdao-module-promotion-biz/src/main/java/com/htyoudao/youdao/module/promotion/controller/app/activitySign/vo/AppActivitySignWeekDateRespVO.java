package com.htyoudao.youdao.module.promotion.controller.app.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Schema(description = "小程序 - 签到活动详情已签到日期 Response VO")
@Data
public class AppActivitySignWeekDateRespVO {

    @Schema(description = "日期")
    private LocalDate date;

    @Schema(description = "页面展示文案：当天展示今天，其余展示M.d")
    private String displayText;

    @Schema(description = "是否当天")
    private Boolean today;

    @Schema(description = "该日期是否已签到；该列表只返回已签到日期，所以恒为true")
    private Boolean signed;
}
