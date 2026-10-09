package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.result;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TransferResult {
    private boolean success;
    private String batchId;
    private String batchStatus;
    private String errorMsg;

    public static TransferResult success(String batchId, String batchStatus) {
        return new TransferResult(true, batchId, batchStatus, null);
    }

    public static TransferResult fail(String errorMsg) {
        return new TransferResult(false, null, null, errorMsg);
    }
}
