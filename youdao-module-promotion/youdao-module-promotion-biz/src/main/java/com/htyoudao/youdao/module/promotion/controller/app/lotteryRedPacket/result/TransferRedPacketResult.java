package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.result;

import com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.response.TransferToUserResponse;
import com.htyoudao.youdao.module.promotion.enums.TransferBillStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRedPacketResult {
    /**
     * 请求是否成功
     */
    private boolean success;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 商户转账单号
     */
    private String outBillNo;

    /**
     * 微信转账单号
     */
    private String transferBillNo;

    /**
     * 转账状态
     */
    private String state;

    /**
     * 状态描述
     */
    private String stateDesc;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 调起支付的参数
     */
    private String packageInfo;

    /**
     * 业务数据
     */
    private Object data;

    // 成功返回
    public static TransferRedPacketResult success(TransferToUserResponse response, String outBillNo) {
        return TransferRedPacketResult.builder()
                .success(true)
                .errorCode("SUCCESS")
                .errorMsg("转账请求成功")
                .outBillNo(outBillNo)
                .transferBillNo(response.getTransferBillNo())
                .state(response.getState().name())
                .stateDesc(getStateDescription(response.getState()))
                .createTime(response.getCreateTime())
                .packageInfo(response.getPackageInfo())
                .build();
    }

    // 成功返回（带业务数据）
    public static TransferRedPacketResult success(TransferToUserResponse response, String outBillNo, Object data) {
        return TransferRedPacketResult.builder()
                .success(true)
                .errorCode("SUCCESS")
                .errorMsg("转账请求成功")
                .outBillNo(outBillNo)
                .transferBillNo(response.getTransferBillNo())
                .state(response.getState().name())
                .stateDesc(getStateDescription(response.getState()))
                .createTime(response.getCreateTime())
                .packageInfo(response.getPackageInfo())
                .data(data)
                .build();
    }

    // 失败返回
    public static TransferRedPacketResult fail(String errorMsg) {
        return TransferRedPacketResult.builder()
                .success(false)
                .errorCode("TRANSFER_FAILED")
                .errorMsg(errorMsg)
                .build();
    }

    // 失败返回（带错误码）
    public static TransferRedPacketResult fail(String errorCode, String errorMsg) {
        return TransferRedPacketResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMsg(errorMsg)
                .build();
    }

    // 获取状态描述
    private static String getStateDescription(TransferBillStatus state) {
        if (state == null) return "未知状态";

        switch (state) {
            case ACCEPTED:
                return "已受理";
            case PROCESSING:
                return "处理中";
            case WAIT_USER_CONFIRM:
                return "等待用户确认";
            case TRANSFERING:
                return "转账中";
            case SUCCESS:
                return "成功";
            case FAIL:
                return "失败";
            case CANCELING:
                return "取消中";
            case CANCELLED:
                return "已取消";
            default:
                return "未知状态";
        }
    }
}
