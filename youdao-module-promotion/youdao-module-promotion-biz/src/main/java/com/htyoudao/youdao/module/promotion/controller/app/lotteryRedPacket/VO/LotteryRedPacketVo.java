package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.VO;

import com.baomidou.mybatisplus.annotation.TableField;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket.LotteryTransferSceneReportDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LotteryRedPacketVo {

    @Schema(name = "outBillNo", description = "商户转账单号")
    private String outBillNo;


    @Schema(name = "openId", description = "用户OpenID")
    private String openId;

    @Schema(name = "userName", description = "用户姓名(加密)")
    private String userName;

    @Schema(name = "transferAmount", description = "转账金额(分)")
    private Integer transferAmount;

    @Schema(name = "transferRemark", description = "转账备注")
    private String transferRemark;


    @Schema(name = "userRecvPerception", description = "用户收款感知")
    private String userRecvPerception;

    @Schema(name = "activityId", description = "活动ID")
    private Long activityId;


    private List<LotteryTransferSceneReportDO> sceneReports = new ArrayList<>();
}
