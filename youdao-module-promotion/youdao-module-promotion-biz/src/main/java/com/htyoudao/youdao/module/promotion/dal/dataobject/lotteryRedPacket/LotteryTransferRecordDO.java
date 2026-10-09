package com.htyoudao.youdao.module.promotion.dal.dataobject.lotteryRedPacket;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.promotion.enums.LotteryTransferStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@TableName(value = "lottery_transfer_record", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryTransferRecordDO extends BusinessBaseDO {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(name = "outBillNo", description = "商户转账单号")
    private String outBillNo;

    @Schema(name = "transferBillNo", description = "微信转账单号")
    private String transferBillNo;

    @Schema(name = "appId", description = "小程序AppID")
    private String appId;

    @Schema(name = "openId", description = "用户OpenID")
    private String openId;

    @Schema(name = "userName", description = "用户姓名(加密)")
    private String userName;

    @Schema(name = "transferAmount", description = "转账金额(分)")
    private Integer transferAmount;

    @Schema(name = "transferRemark", description = "转账备注")
    private String transferRemark;

    @Schema(name = "transferSceneId", description = "转账场景ID")
    private String transferSceneId;

    @Schema(name = "userRecvPerception", description = "用户收款感知")
    private String userRecvPerception;

    @Schema(name = "notifyUrl", description = "回调地址")
    private String notifyUrl;

    @Schema(name = "status", description = "状态: INIT-初始, ACCEPTED-已受理, PROCESSING-处理中, WAIT_USER_CONFIRM-等待用户确认, SUCCESS-成功, FAIL-失败")
    private LotteryTransferStatus status = LotteryTransferStatus.INIT;

    @Schema(name = "packageInfo", description = "调起支付的参数")
    private String packageInfo;

    @Schema(name = "failReason", description = "失败原因")
    private String failReason;


    @TableField(exist = false)
    private List<LotteryTransferSceneReportDO> sceneReports = new ArrayList<>();
}
