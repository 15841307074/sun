package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ActivityVoteOptionAnalysisVO {

    @Schema(description = "选项id")
    private Long optionId;

    @Schema(description = "选项名称")
    private String optionName;

    @Schema(description = "投票数")
    private Long voteCount;

    @Schema(description = "占比(百分比)")
    private BigDecimal percentage;
}
