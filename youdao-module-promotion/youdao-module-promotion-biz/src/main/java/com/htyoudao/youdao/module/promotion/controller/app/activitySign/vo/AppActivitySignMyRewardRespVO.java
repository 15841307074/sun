package com.htyoudao.youdao.module.promotion.controller.app.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "小程序 - 我的签到奖励 Response VO")
@Data
public class AppActivitySignMyRewardRespVO {

    @Schema(description = "活动ID")
    private Long activityId;

    @Schema(description = "奖励列表")
    private List<AppActivitySignMyRewardItemRespVO> rewardList;
}
