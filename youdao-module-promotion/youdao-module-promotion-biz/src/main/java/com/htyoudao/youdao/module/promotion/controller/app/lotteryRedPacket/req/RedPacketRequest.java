package com.htyoudao.youdao.module.promotion.controller.app.lotteryRedPacket.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RedPacketRequest {
//    /**
//     * 商户订单号
//     */
//    @NotBlank(message = "商户订单号不能为空")
//    private String partnerTradeNo;

    /**
     * 用户openid
     */
    @NotBlank(message = "用户openid不能为空")
    private String openId;

    /**
     * 金额（分）
     */
    @NotNull(message = "金额不能为空")
    @Min(value = 10, message = "金额不能少于0.1元")
    @Max(value = 20000, message = "金额不能超过200元")
    private Integer amount;

    /**
     * 描述
     */
    @NotBlank(message = "描述不能为空")
    private String description;

    /**
     * 用户IP
     */
    private String clientIp;



    /**
     * 祝福语
     */
    private String wishing;


    /**
     * 活动名称
     */
    private String actName;

    /**
     * 备注
     */
    private String remark;
}


