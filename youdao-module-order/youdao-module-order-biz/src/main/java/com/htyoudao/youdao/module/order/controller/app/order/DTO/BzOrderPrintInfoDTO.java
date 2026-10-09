package com.htyoudao.youdao.module.order.controller.app.order.DTO;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 点餐机统计信息打印
 * </p>
 *
 * @author zhangjihe
 * @since 2024-07-22
 */
@Data
public class BzOrderPrintInfoDTO implements Serializable {

    private static final long serialVersionUID = -2928488774721859953L;

    /**
     * 店铺名称
     */
    private String storeName;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 操作员
     */
    private String operator;

    /**
     * 打印时间
     */
    private String currentTime;

    /**
     * 订单实收
     */
    private BigDecimal turnover = BigDecimal.ZERO;

    /**
     * 订单数
     */
    private Integer orderQuantity = 0;

    /**
     * 笔均价
     */
    private BigDecimal avgTurnover = BigDecimal.ZERO;

    /**
     * 订单金额
     */
    private BigDecimal orderTurnover = BigDecimal.ZERO;

    /**
     * 商家优惠
     */
    private BigDecimal activityDiscountTurnover = BigDecimal.ZERO;

    /**
     * 退款总额
     */
    private BigDecimal returnTurnover = BigDecimal.ZERO;

    /**
     * 支付宝收款
     */
    private BigDecimal aliPayTurnover = BigDecimal.ZERO;

    /**
     * 支付宝订单数
     */
    private Integer aliPayQuantity = 0;

    /**
     * 微信支付收款
     */
    private BigDecimal wechatTurnover = BigDecimal.ZERO;

    /**
     * 微信订单数
     */
    private Integer wechatQuantity = 0;

    /**
     * 现金收款
     */
    private BigDecimal cashTurnover = BigDecimal.ZERO;

    /**
     * 现金订单数
     */
    private Integer cashQuantity = 0;

    /**
     * 美团券收款
     */
    private BigDecimal meituanTurnover = BigDecimal.ZERO;

    /**
     * 美团券订单数
     */
    private Integer meituanQuantity = 0;

    /**
     * 抖音券收款
     */
    private BigDecimal tiktokTurnover = BigDecimal.ZERO;

    /**
     * 抖音券订单数
     */
    private Integer tiktokQuantity = 0;

    /**
     * 堂食
     */
    private Integer tsQuantity = 0;

    /**
     * 打包
     */
    private Integer dbQuantity = 0;

    /**
     * 外卖
     */
    private Integer wmQuantity = 0;
}
