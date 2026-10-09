package com.htyoudao.youdao.module.promotion.controller.app.activityVote.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class AppVoteResultVO {

    @Schema(description = "投票是否成功")
    private Boolean success;

    @Schema(description = "是否首次投票")
    private Boolean firstVote;

    @Schema(description = "提示信息（无奖励发放时展示）")
    private String message;

    @Schema(description = "奖励信息列表（首次投票发放所有有库存的奖励）")
    private List<AppVoteMyRewardRespVO> rewardList;

    @Schema(description = "剩余投票次数（-1表示不限）")
    private Integer remainVoteCount;
}
