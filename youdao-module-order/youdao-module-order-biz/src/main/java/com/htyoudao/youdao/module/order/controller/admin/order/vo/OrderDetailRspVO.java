package com.htyoudao.youdao.module.order.controller.admin.order.vo;

import com.htyoudao.youdao.module.order.core.calc.DTO.ActivityDiscountDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderPurchaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class OrderDetailRspVO {

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
     * 订单类型：0 堂食 1 打包 2 外卖 3代取
     */
    @Schema(description = "订单类型：0 堂食 1 打包 2 外卖 3代取")
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
     * 起送费，外卖订单为外卖起送费，代取订单为代取起送费
     */
    @Schema(description = "起送费，外卖订单为外卖起送费，代取订单为代取起送费")
    private BigDecimal minimumDeliveryFee;

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
     * 支付时间
     */
    @Schema(description = "支付时间")
    private String payTime;

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
     * 取餐地址，代取订单存门店地址
     */
    @Schema(description = "取餐地址，代取订单存门店地址")
    private String receiverAreaInfo;

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
     * 跑腿员会员ID，对应 wx_member.id
     */
    @Schema(description = "跑腿员会员ID，对应 wx_member.id")
    private Long deliveryId;

    /**
     * 代取退款状态：0/null未退款 1赏金已退 2餐费已退 3餐费和赏金均已退
     */
    @Schema(description = "代取退款状态：0/null未退款 1赏金已退 2餐费已退 3餐费和赏金均已退")
    private Integer errandStatus;

    /**
     * 用户支付跑腿赏金
     */
    @Schema(description = "用户支付跑腿赏金")
    private BigDecimal errandRewardAmount;

    /**
     * 门店跑腿补贴金额
     */
    @Schema(description = "门店跑腿补贴金额")
    private BigDecimal errandStoreSubsidyAmount;

    /**
     * 跑腿员性别限制：0不限 1男 2女
     */
    @Schema(description = "跑腿员性别限制：0不限 1男 2女")
    private Integer errandGenderLimit;

    /**
     * 送达照片，最多3张
     */
    @Schema(description = "送达照片，最多3张")
    private List<String> errandDeliveryImages;

    /**
     * 跑腿员接单时间
     */
    @Schema(description = "跑腿员接单时间")
    private String makingTime;

    /**
     * 跑腿员开始配送时间
     */
    @Schema(description = "跑腿员开始配送时间")
    private String waitingTime;

    /**
     * 跑腿员确认送达时间
     */
    @Schema(description = "跑腿员确认送达时间")
    private String finishTime;

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

    /**
     * 参考submit入参
     */
    @Schema(description = "参考submit入参")
    private Integer orderSource;

    /**
     * 订单时间流
     */
    @Schema(description = "订单时间流")
    Map<String, String> bzOrderStateDateTimeMap;

    /**
     * 套餐外的单品信息
     */
    @Schema(description = "套餐外的单品信息")
    private List<BzOrderProductVO> bzOrderProductList;

    /**
     * 加购商品信息
     */
    @Schema(description = "加购商品信息")
    private List<BzOrderPurchaseDO> bzOrderPurchaseDOList;

    /**
     * 营销活动信息
     */
    List<ActivityDiscountDTO> activityDiscounts;
}
