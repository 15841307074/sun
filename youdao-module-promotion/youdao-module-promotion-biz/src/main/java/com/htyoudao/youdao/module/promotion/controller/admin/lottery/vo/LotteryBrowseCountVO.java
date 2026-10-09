package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LotteryBrowseCountVO {

    @Schema(description = "浏览任务完成次数")
    private int finishedCount = 0;

    @Schema(description = "浏览额外可用抽奖次数")
    private int availableCount = 0;

    @Schema(description = "浏览任务上限")
    private int maxCount = 0;
}
