package com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq;

import lombok.Data;

@Data
public class ActivityCqPendingMemberDO {

    /**
     * 待开奖用户id
     */
    private Long memberId;

    /**
     * 用户待开奖签码数
     */
    private Integer signCount;
}
