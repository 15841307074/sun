package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO;

import com.alibaba.nacos.shaded.com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class TransferNotifyData{
    @SerializedName("out_bill_no")
    private String outBillNo;

    @SerializedName("transfer_bill_no")
    private String transferBillNo;

    @SerializedName("state")
    private String state;

    @SerializedName("fail_reason")
    private String failReason;

    private String eventType;
}
