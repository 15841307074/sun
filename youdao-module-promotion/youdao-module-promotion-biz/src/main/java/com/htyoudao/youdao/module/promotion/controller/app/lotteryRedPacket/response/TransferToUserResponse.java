package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.response;

import com.google.gson.annotations.SerializedName;
import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.enums.TransferBillStatus;
import lombok.Data;

@Data
public class TransferToUserResponse {
    @SerializedName("out_bill_no")
    public String outBillNo;

    @SerializedName("transfer_bill_no")
    public String transferBillNo;

    @SerializedName("create_time")
    public String createTime;

    @SerializedName("state")
    public TransferBillStatus state;

    @SerializedName("package_info")
    public String packageInfo;
}
