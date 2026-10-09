package com.htyoudao.youdao.module.promotion.controller.app.activityAnswer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 有奖问答次数响应。
 */
@Data
public class ActivityAnswerChanceCountRespVO {

    @Schema(description = "当前可用答题次数")
    private Integer chanceCount;

    @Schema(description = "当前周期已使用次数")
    private Integer usedCount;

    @Schema(description = "活动总次数限制，0不限")
    private Integer totalLimit;

    @Schema(description = "当前统计周期")
    private String periodKey;
}
