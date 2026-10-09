package com.htyoudao.youdao.module.order.controller.app.pay.VO;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
public class XingYiPayReqVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 5532786919123790717L;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 支付号
     */
    private String paySn;

    /**
     * 微信支付方式  0 微信跳转小程序支付 1 微信小程序本地支付
     */
    public int wxPayType;

    /**
     * 支付方式 1 微信 2支付宝
     */
    public String payWay;

    /**
     * 微信appid
     */
    public String appId;

    /**
     * secret
     */
    public String secret;

    /**
     * 换取openid的code
     */
    public String code;

    /**
     * 跳转用的传参
     */
    public String query;

    /**
     * 回调地址
     */
    public String notifyUrl;

    /**
     * 终端号
     */
    private String terminalSn;

    /**
     * 支付码标识
     */
    private String dynamicId;

    /**
     * 金额
     */
    private String payAmount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
