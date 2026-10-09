package com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "我的抽签码汇总")
public class MyCodeVO {

    @Schema(description = "总签码数")
    private Integer totalCount;

    @Schema(description = "待开奖数")
    private Integer pendingCount;

    @Schema(description = "中奖数")
    private Integer winningCount;

    @Schema(description = "未中奖数")
    private Integer loseCount;

    @Schema(description = "签到获得数")
    private Integer signCount;

    @Schema(description = "下单获得数")
    private Integer orderCount;

    @Schema(description = "分享获得数")
    private Integer shareCount;
}
