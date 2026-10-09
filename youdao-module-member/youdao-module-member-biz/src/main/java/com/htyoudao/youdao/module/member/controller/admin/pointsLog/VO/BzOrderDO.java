package com.htyoudao.youdao.module.member.controller.admin.pointsLog.VO;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单对象 bz_order
 *
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BzOrderDO {
    private static final long serialVersionUID = 1L;

    /**
     * 订单id
     */
    @TableId
    private Long orderId;

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 支付单号
     */
    private String paySn;

    /**
     * 商家ID
     */
    private Long storeId;

    /**
     * 商家名称
     */
    private String storeName;

    /**
     * 买家name
     */
    private String memberName;

    /**
     * 买家ID
     */
    private Long memberId;

    /**
     * 支付成功时间
     */
    private LocalDateTime payTime;

    /**
     * 订单完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 订单状态：0-已取消；10-未付款订单；20-已付款；30-待取餐；40-代配送 50-已配送 60-已完成;
     */
    private Integer orderState;

    /**
     * 支付方式名称
     */
    private String paymentName;

    /**
     * 支付方式code  0 现金 1在线
     */
    private String paymentCode;

    /**
     * 商品金额，等于订单中所有的商品的单价乘以数量之和
     */
    private BigDecimal goodsAmount;

    /**
     * 物流费用
     */
    private BigDecimal expressFee;

    /**
     * 活动优惠总金额 （= 店铺优惠券 + 平台优惠券 + 活动优惠【店铺活动 + 平台活动】 + 积分抵扣金额）
     */
    private BigDecimal activityDiscountAmount;

    /**
     * 订单总金额(用户需要支付的金额)，等于商品总金额＋运费-活动优惠金额总额activity_discount_amount
     */
    private BigDecimal orderAmount;

    /**
     * 余额账户支付总金额
     */
    private BigDecimal balanceAmount;

    /**
     * 三方支付金额
     */
    private BigDecimal payAmount;

    /**
     * 退款的金额，订单没有退款则为0
     */
    private BigDecimal refundAmount;

    /**
     * 收货人
     */
    private String receiverName;

    /**
     * 省市区组合
     */
    private String receiverAreaInfo;

    /**
     * 收货人详细地址
     */
    private String receiverAddress;

    /**
     * 收货人手机号
     */
    private String receiverMobile;

    /**
     * 延长多少天收货
     */
    private Long delayDays;

    /**
     * 物流公司ID
     */
    private Long expressId;

    /**
     * 物流编号
     */
    private String expressCode;

    /**
     * 物流公司
     */
    private String expressName;

    /**
     * 快递单号
     */
    private String expressNumber;

    /**
     * 是否评价:1.未评价,2.部分评价,3.全部评价
     */
    private Integer evaluateState;

    /**
     * 订单类型：0 堂食 1 打包 2 外卖
     */
    private Integer orderType;

    /**
     * 订单来源 1-微信小程序
     */
    private Integer orderFrom;

    /**
     * 锁定状态：0-是正常, 大于0是锁定状态，用户申请退款或退货时锁定状态加1，处理完毕减1。锁定后不能操作订单
     */
    private Integer lockState;

    /**
     * 取消原因
     */
    private String refuseReason;

    /**
     * 取消备注
     */
    private String refuseRemark;

    /**
     * 是否结算：0-未结算；1-已结算
     */
    private Integer isSettlement;

    /**
     * 是否已生成电子面单 1 - 已生成 2 - 未生成
     */
    private Integer isGenerateFacesheet;

    /**
     * 是否是虚拟商品：1-实物商品；2-虚拟商品
     */
    private Integer isVirtualGoods;

    /**
     * 星级，1-5
     */
    private Long star;

    /**
     * 商户备注
     */
    private String storeRemark;

    /**
     * 外卖姓名
     */
    private String takeAwayName;

    /**
     * 外卖手机号
     */
    private String takeAwayTel;

    /**
     * 取餐号
     */
    private String pickUpNum;

    /**
     * 外卖地址
     */
    private String takeAwayAddress;

    /**
     * 商家电话
     */
    private String storePhone;

    /**
     * 起送费
     */
    private BigDecimal minimumDeliveryFee;
    /**
     * 打包费
     */
    private BigDecimal packingCharge;

    /**
     * 餐具数量
     */
    private Integer tableWareNum;
    /**
     * openId
     */
    private String openId;

    /**
     * 骑手电话
     */
    private String deliveryPhone;
    /**
     * 骑手姓名
     */
    private String deliveryName;
    /**
     * 预约时间
     */
    private String appointmentTime;

    /**
     * 用户优惠券id
     */
    private Long userCouponId;
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String tableFix;
}
