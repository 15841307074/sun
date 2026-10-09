package com.htyoudao.youdao.module.order.api.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class OrderDetailRspDTO  implements java.io.Serializable{

    @Serial
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
     * 商家id
     */
    @Schema(description = "商家id")
    private Long storeId;

    /**
     * 商家
     */
    @Schema(description = "商家")
    private String storeName;

    /**
     * 活动名称，逗号间隔
     */
    @Schema(description = "活动名称 逗号间隔")
    private String activityName;

    /**
     * 门店纬度
     */
    @Schema(description = "门店纬度")
    private Double storeLatitude;

    /**
     * 门店经度
     */
    @Schema(description = "门店经度")
    private Double storeLongitude;

    /**
     * 订单状态
     **/
    @Schema(description = "订单状态")
    private Integer orderState;

    /**
     * 订单状态
     **/
    @Schema(description = "订单状态")
    private String orderStateName;

    /**
     * 商品数量
     */
    @Schema(description = "商品数量")
    private Long goodsNum;

    /**
     * 支付方式名称
     */
    @Schema(description = "支付方式名称")
    private String paymentName;

    /**
     * 支付方式code 0现金 1 微信 2支付宝
     */
    @Schema(description = "支付方式code 0现金 1 微信 2支付宝")
    private String paymentCode;

    @Schema(description = "物流编号 (临时存储拼单主体mainId)")
    private String expressCode;

    /**
     * 订单来源 1-微信小程序
     */
    @Schema(description = "订单来源 1-微信小程序")
    private Integer orderFrom;

    /**
     * 订单来源名称
     */
    @Schema(description = "订单来源名称")
    private String orderFromName;

    /**
     * 取餐号
     */
    @Schema(description = "取餐号")
    private String pickUpNum;

    /**
     * 用户订单备注
     */
    @Schema(description = "用户订单备注")
    private String orderRemark;

    /**
     * 活动优惠总金额
     */
    @Schema(description = "活动优惠总金额 （= 店铺优惠券 + 平台优惠券 + 活动优惠【店铺活动 + 平台活动】 + 积分抵扣金额）")
    private BigDecimal activityDiscountAmount;

    /**
     * 营销活动优惠金额
     */
    @Schema(description = "营销活动优惠金额")
    private BigDecimal promotionDiscountAmount;

    /**
     * 订单总金额
     */
    @Schema(description = "订单总金额(用户需要支付的金额)，等于商品总金额＋运费-活动优惠金额总额activity_discount_amount")
    private BigDecimal orderAmount;

    /**
     * 商品金额
     */
    @Schema(description = "商品金额，等于订单中所有的商品的单价乘以数量之和")
    private BigDecimal goodsAmount;

    /**
     * 三方支付金额
     */
    @Schema(description = "三方支付金额")
    private BigDecimal payAmount;

    /**
     * 订单类型：0 堂食 1 打包 2 外卖
     */
    @Schema(description = "订单类型：0 堂食 1 打包 2 外卖")
    private Integer orderType;

    /**
     * 订单类型名称
     */
    @Schema(description = "订单类型名称")
    private String orderTypeName;

    /**
     * 配送费
     */
    @Schema(description = "配送费")
    private BigDecimal expressFee;

    /**
     * 打包费
     */
    @Schema(description = "打包费")
    private BigDecimal packingCharge;

    /**
     * 下单时间
     */
    @Schema(description = "下单时间")
    private String createTime;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 收货人
     */
    @Schema(description = "收货人")
    private String receiverName;

    /**
     * 收货人详细地址
     */
    @Schema(description = "收货人详细地址")
    private String receiverAddress;

    /**
     * 收货人手机号
     */
    @Schema(description = "收货人手机号")
    private String receiverMobile;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称")
    private String memberName;

    /**
     * 配送员
     */
    @Schema(description = "配送员")
    private String deliveryName;

    /**
     * 配送员电话
     */
    @Schema(description = "配送员电话")
    private String deliveryPhone;

    /**
     * 外卖电话
     */
    @Schema(description = "外卖电话")
    private String takeAwayTel;

    /**
     * 预约取餐时间
     */
    @Schema(description = "预约取餐时间")
    private String appointmentTime;

    /**
     * 优惠券名称
     */
    @Schema(description = "优惠券名称")
    private String couponName;

    /**
     * 优惠券编码
     */
    @Schema(description = "优惠券编码")
    private String couponCode;
}
