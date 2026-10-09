package com.htyoudao.youdao.module.order.controller.admin.order.vo;


import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductSonDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderPurchaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


@Data
public class BzOrderListRspVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单号
     */
    @Schema(description = "订单号")
    private String orderSn;

    /**
     * 支付单号
     */
    @Schema(description = "支付单号")
    private String paySn;

    /**
     * 商家名称
     */
    @Schema(description = "商家名称")
    private String storeName;

    @Schema(description = "订单来源")
    private Integer orderFrom;

    @Schema(description = "点餐人电话")
    private String takeAwayTel;

    @Schema(description = "跑腿员电话")
    private String deliveryPhone;

    @Schema(description = "跑腿员姓名")
    private String deliveryName;

    @Schema(description = "用户支付跑腿赏金")
    private BigDecimal errandRewardAmount;

    @Schema(description = "门店跑腿补贴金额")
    private BigDecimal errandStoreSubsidyAmount;

    @Schema(description = "收货人")
    private String receiverMobile;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

    /**
     * 订单状态：0-已取消；10-未付款订单；20-已付款；30-待取餐；40-代配送 50-已配送 60-已完成;
     */
    @Schema(description = "订单状态：0-已取消；10-未付款订单；20-已付款；30-待取餐；40-代配送 50-已配送 60-已完成")
    private Integer orderState;

    /**
     * 支付方式名称
     */
    @Schema(description = "支付方式名称")
    private String paymentName;

    /**
     * 订单总金额(用户需要支付的金额)，等于商品总金额＋运费-活动优惠金额总额activity_discount_amount
     */
    @Schema(description = "订单总金额(用户需要支付的金额)，等于商品总金额＋运费-活动优惠金额总额activity_discount_amount")
    private BigDecimal orderAmount;

    /**
     * 三方支付金额
     */
    @Schema(description = "三方支付金额")
    private BigDecimal payAmount;

    /**
     * 退款的金额，订单没有退款则为0
     */
    @Schema(description = "退款的金额，订单没有退款则为0")
    private BigDecimal refundAmount;

    /**
     * 门店电话
     */
    @Schema(description = "门店电话")
    private String storePhone;

    /**
     * 取餐号
     */
    @Schema(description = "取餐号")
    private String pickUpNum;

    /**
     * 商品数量
     */
    @Schema(description = "商品数量")
    private Long goodsNum;

    /**
     * 订单来源
     */
    @Schema(description = "订单来源")
    private String orderFromName;

    /**
     * 预约时间
     */
    @Schema(description = "预约时间")
    private String appointmentTime;

    /**
     * 订单类型
     */
    @Schema(description = "订单类型名称")
    private String orderTypeName;

    /**
     * 订单类型
     */
    @Schema(description = "订单类型")
    private Integer orderType;

    @Schema(description = "openId")
    private String openId;

    /**
     * 套餐信息
     */
    @Schema(description = "套餐信息")
    private List<BzOrderProductSonDO> groupBzOrderProductList;

    /**
     * 套餐外的单品信息
     */
    @Schema(description = "套餐外的单品信息")
    private List<BzOrderProductVO> bzOrderProductList;

    /**
     * 加购商品信息
     */
    @Schema(description = "加购商品信息")
    private List<BzOrderPurchaseDO> bzOrderPurchaseList;
}
