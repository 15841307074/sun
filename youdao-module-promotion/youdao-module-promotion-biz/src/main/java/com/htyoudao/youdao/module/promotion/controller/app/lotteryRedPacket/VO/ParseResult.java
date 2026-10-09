package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO;

import lombok.Data;

@Data
public class ParseResult {

    private boolean success;
    private Long activityId;

    public ParseResult(boolean success, Long activityId) {
        this.success = success;
        this.activityId = activityId;
    }

    public boolean isSuccess() {
        return success;
    }

    public Long getActivityId() {
        return activityId;
    }
}
