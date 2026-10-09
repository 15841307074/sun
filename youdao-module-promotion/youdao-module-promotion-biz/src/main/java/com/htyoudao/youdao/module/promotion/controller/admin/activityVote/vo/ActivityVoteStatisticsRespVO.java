package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ActivityVoteStatisticsRespVO {

    @Schema(description = "参与人数")
    private Long participantCount;

    @Schema(description = "总投票次数")
    private Long totalVoteCount;
}
