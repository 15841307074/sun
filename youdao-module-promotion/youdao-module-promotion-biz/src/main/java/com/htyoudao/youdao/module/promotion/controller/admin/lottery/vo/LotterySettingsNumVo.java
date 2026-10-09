package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 抽奖次数返回")
@Data
public class LotterySettingsNumVo {

    @Schema(name = "剩余次数", description = "当前总可用抽奖次数")
    private int num = 0;

    @Schema(name = "剩余总次数", description = "活动总剩余次数")
    private int sum = 0;

    @Schema(description = "免费剩余次数")
    private int freeAvailableCount = 0;

    @Schema(description = "积分剩余次数，-1 代表不限制")
    private int pointsAvailableCount = 0;

    @Schema(description = "下单任务可用次数")
    private int orderAvailableCount = 0;

    @Schema(description = "分享任务可用次数")
    private int shareAvailableCount = 0;

    @Schema(description = "浏览任务可用次数")
    private int browseAvailableCount = 0;

    @Schema(description = "下单任务完成次数")
    private int orderFinishCount = 0;

    @Schema(description = "分享任务完成次数")
    private int shareFinishCount = 0;

    @Schema(description = "浏览任务完成次数")
    private int browseFinishCount = 0;
}
