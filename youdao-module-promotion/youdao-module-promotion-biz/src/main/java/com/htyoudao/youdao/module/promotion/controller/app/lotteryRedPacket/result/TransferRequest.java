package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.result;

import com.google.gson.annotations.SerializedName;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferRequest {
    @NotBlank
    private String openId;

    @NotNull(message = "金额不能为空")
    @Min(value = 10, message = "金额不能少于0.1元")
    @Max(value = 20000, message = "金额不能超过200元")
    private Long amount;

    @NotBlank
    private String description;

    private String batchName = "抽奖红包";
    private String batchRemark = "小程序抽奖活动红包";
    private String userName; // 真实姓名（选填）

    /**
     * 感知行为例如（现金红包）
     */
    public String userRecvPerception;

    // 批次信息（单笔转账时固定）
    public Long getTotalAmount() {
        return amount;
    }

    public Integer getTotalNum() {
        return 1;
    }
}
