package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.req;

import com.google.gson.annotations.SerializedName;
import com.htyoudao.youdao.module.promotion.controller.admin.wechatDemo.TransferToUser;
import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO.LotteryTransferSceneReportVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferSceneReportDO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TransferToUserRequest {
    @SerializedName("appid")
    public String appid;

    @SerializedName("out_bill_no")
    public String outBillNo;

    @SerializedName("transfer_scene_id")
    public String transferSceneId;

    @SerializedName("openid")
    public String openid;

    @SerializedName("user_name")
    public String userName;

    @SerializedName("transfer_amount")
    public Integer transferAmount;

    @SerializedName("transfer_remark")
    public String transferRemark;

    @SerializedName("notify_url")
    public String notifyUrl;

    @SerializedName("user_recv_perception")
    public String userRecvPerception;

    @SerializedName("transfer_scene_report_infos")
    public List<LotteryTransferSceneReportVO> transferSceneReportInfos = new ArrayList<LotteryTransferSceneReportVO>();
}
