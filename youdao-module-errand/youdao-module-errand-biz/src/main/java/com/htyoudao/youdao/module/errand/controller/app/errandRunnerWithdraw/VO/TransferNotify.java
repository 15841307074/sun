package com.htyoudao.youdao.module.errand.controller.app.errandRunnerWithdraw.VO;

import com.alibaba.nacos.shaded.com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class TransferNotify {
    @SerializedName("out_bill_no")
    private String out_bill_no;

    @SerializedName("transfer_bill_no")
    private String transfer_bill_no;

    @SerializedName("state")
    private String state;

    @SerializedName("fail_reason")
    private String fail_reason;

    private String event_type;
}
