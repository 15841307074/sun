package com.htyoudao.youdao.module.promotion.enums;

public enum LotteryTransferStatus {

    INIT("初始"),
    ACCEPTED("已受理"),
    PROCESSING("处理中"),
    WAIT_USER_CONFIRM("等待用户确认"),
    SUCCESS("成功"),
    FAIL("失败");

    private final String description;

    LotteryTransferStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
