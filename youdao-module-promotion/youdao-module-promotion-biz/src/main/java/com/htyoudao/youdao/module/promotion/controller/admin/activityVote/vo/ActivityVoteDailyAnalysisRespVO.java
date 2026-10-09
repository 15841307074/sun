package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ActivityVoteDailyAnalysisRespVO {

    @Schema(description = "每日投票数 key=日期 value=票数")
    private Map<String, Long> dailyVotes;

    @Schema(description = "每日参与人数 key=日期 value=人数")
    private Map<String, Long> dailyParticipants;

    @Schema(description = "各选项投票统计")
    private List<ActivityVoteOptionAnalysisVO> optionStats;

    @Schema(description = "总投票数")
    private Long totalVotes;
}
