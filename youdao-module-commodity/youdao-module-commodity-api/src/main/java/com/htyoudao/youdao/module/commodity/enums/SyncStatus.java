package com.htyoudao.youdao.module.commodity.enums;

/**
 * 同步状态 0-队列中，1-执行中，2-已完成，3-失败
 */
public enum SyncStatus {
    PENDING(0),
    EXECUTING(1),
    COMPLETED(2),
    FAILED(3);

    private final int code;

    SyncStatus(int code) { this.code = code; }
    public int getCode() { return code; }
}
