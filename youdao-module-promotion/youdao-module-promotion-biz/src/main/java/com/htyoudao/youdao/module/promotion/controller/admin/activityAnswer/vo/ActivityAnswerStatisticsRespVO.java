package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActivityAnswerStatisticsRespVO {

    /**
     * 答题人数，按手机号去重。
     */
    private Long answerUserCount;

    /**
     * 答题次数。
     */
    private Long answerCount;

    /**
     * 完成提交次数。
     */
    private Long submitCount;

    /**
     * 兼容旧字段：答题人数，按手机号去重。
     */
    private Long memberCount;

    public ActivityAnswerStatisticsRespVO(Long answerCount, Long submitCount, Long memberCount) {
        this.answerCount = answerCount;
        this.submitCount = submitCount;
        this.memberCount = memberCount;
        this.answerUserCount = memberCount;
    }
}
