package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ActivityVoteRewardStatisticsRespVO {

    @Schema(description = "发放人数(去重)")
    private Long grantPersonCount;

    @Schema(description = "发放次数")
    private Long grantTotalCount;
}
