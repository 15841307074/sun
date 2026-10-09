package com.htyoudao.youdao.module.promotion.controller.app.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class AppVoteRankingVO {

    @Schema(description = "总投票数")
    private Long totalVoteCount;

    @Schema(description = "总参与人数")
    private Long totalParticipantCount;

    @Schema(description = "排行列表")
    private List<RankItem> rankList;

    @Data
    public static class RankItem {
        @Schema(description = "排名")
        private Integer rank;
        @Schema(description = "选项id")
        private Long optionId;
        @Schema(description = "选项名称")
        private String optionName;
        @Schema(description = "选项主图")
        private String optionUrl;
        @Schema(description = "票数")
        private Long voteNum;
        @Schema(description = "票数占比")
        private String votePercent;
    }
}
