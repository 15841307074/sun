package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 有奖问答奖励发放统计。
 */
@Data
@NoArgsConstructor
public class ActivityAnswerRewardStatisticsRespVO {

    /**
     * 发放次数。
     */
    private Long grantCount;

    /**
     * 发放人数，按手机号去重。
     */
    private Long grantUserCount;

    /**
     * 兼容旧字段：发放次数。
     */
    private Long rewardCount;

    /**
     * 兼容旧字段：发放人数，按手机号去重。
     */
    private Long memberCount;

    public ActivityAnswerRewardStatisticsRespVO(Long rewardCount, Long memberCount) {
        this.rewardCount = rewardCount;
        this.memberCount = memberCount;
        this.grantCount = rewardCount;
        this.grantUserCount = memberCount;
    }
}
