package com.htyoudao.youdao.module.promotion.controller.app.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "小程序 - 签到结果 Response VO")
@Data
public class AppActivitySignResultRespVO {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "是否签到成功")
    private Boolean signSuccess;

    @Schema(description = "今日是否已签到")
    private Boolean todaySigned;

    @Schema(description = "本次签到后连续天数")
    private Integer continuousDays;

    @Schema(description = "本次签到后累计天数")
    private Integer totalDays;

    @Schema(description = "本次是否触发奖励")
    private Boolean rewardTriggered;

    @Schema(description = "本次获得的奖励列表")
    private List<AppActivitySignPrizeRespVO> prizeList;
}
