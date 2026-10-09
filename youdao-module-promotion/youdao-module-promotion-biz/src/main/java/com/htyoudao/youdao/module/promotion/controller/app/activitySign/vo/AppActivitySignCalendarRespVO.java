package com.htyoudao.youdao.module.promotion.controller.app.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "小程序 - 签到日历 Response VO")
@Data
public class AppActivitySignCalendarRespVO {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "当前用户在该活动中所有已签到日期列表")
    private List<String> signDateList;
}
