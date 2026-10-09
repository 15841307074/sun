package com.htyoudao.youdao.module.order.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单对象 bz_order
 */
@TableName(value = "bz_order", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BzOrderDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 4882177555084321539L;

    /**
     * 订单id [ES]
     */
    @TableId
    @Schema(description = "订单id")
    private Long orderId;

    /**
     * 订单号 [ES]
     */
    @Schema(description = "订单号")
    private String orderSn;

    /**
     * 支付单号 [ES]
     */
    @Schema(description = "支付单号")
    private String paySn;

    /**
     * 商家ID [ES]
     */
    @Schema(description = "商家ID")
    private Long storeId;

    /**
     * 商家名称 [ES]
     */
    @Schema(description = "商家名称")
    private String storeName;

    /**
     * 买家name [ES]
     */
    @Schema(description = "买家name")
    private String memberName;

    /**
     * 用户头像 [ES]
     */
    @Schema(description = "用户头像")
    private String memberAvatar;

    /**
     * 买家ID [ES]
     */
    @Schema(description = "买家ID")
    private Long memberId;

    /**
     * 支付成功时间 [ES]
     */
    @Schema(description = "支付成功时间")
    private LocalDateTime payTime;

    /**
     * 订单完成时间  也代表代取单 已送达[ES]
     */
    @Schema(description = "订单完成时间")
    private LocalDateTime finishTime;

    /**
     * 订单状态：0-已取消；10-未付款订单；20-已付款；30-已取餐；40-待配送；50-配送中；60-已完成；80-制作中；110-待接单；120-已接单；200-待取餐 [ES]
     */
    @Schema(description = "订单状态：0-已取消；10-未付款订单；20-已付款；30-已取餐；40-待配送；50-配送中；60-已完成；80-制作中；110-待接单；120-已接单；200-待取餐")
    private Integer orderState;

    /**
     * 支付方式名称 [ES]
     */
    @Schema(description = "支付方式名称")
    private String paymentName;

    /**
     * 支付方式code  0 现金 1在线 [ES]
     */
    @Schema(description = "支付方式code  0 现金 1在线")
    private String paymentCode;

    /**
     * 商品金额，等于订单中所有的商品的单价乘以数量之和 [ES]
     */
    @Schema(description = "商品金额，等于订单中所有的商品的单价乘以数量之和")
    private BigDecimal goodsAmount;

    /**
     * 物流费用 [ES]
     */
    @Schema(description = "物流费用")
    private BigDecimal expressFee;

    /**
     * 优惠券优惠金额 [ES]
     */
    @Schema(description = "优惠券优惠金额")
    private BigDecimal activityDiscountAmount;

    /**
     * 营销活动优惠金额 [ES]
     */
    @Schema(description = "营销活动优惠金额")
    private BigDecimal promotionDiscountAmount;

    /**
     * 优惠总金额 营销活动 + 优惠券 [ES]
     */
    @Schema(description = "优惠总金额 营销活动 + 优惠券")
    private BigDecimal allDiscountAmount;

    /**
     * 订单金额,不包含优惠 [ES]
     */
    @Schema(description = "订单金额,不包含优惠")
    private BigDecimal orderAmount;

    /**
     * 余额账户支付总金额 [ES] TODO 临时存储实际到账金额
     */
    @Schema(description = "余额账户支付总金额 (临时存储实际到账金额)")
    private BigDecimal balanceAmount;

    /**
     * 三方支付金额 [ES]
     */
    @Schema(description = "三方支付金额")
    private BigDecimal payAmount;

    /**
     * 退款的金额，订单没有退款则为0 [ES]
     */
    @Schema(description = "退款的金额，订单没有退款则为0 ")
    private BigDecimal refundAmount;

    /**
     * 收货人 [ES]
     */
    @Schema(description = "收货人")
    private String receiverName;

    /**
     * 省市区组合 TODO 临时存储门店地址，也是取货地址 [ES]
     */
    @Schema(description = "省市区组合")
    private String receiverAreaInfo;

    /**
     * 收货人详细地址 [ES]
     */
    @Schema(description = "收货人详细地址")
    private String receiverAddress;

    /**
     * 收货人手机号 [ES]
     */
    @Schema(description = "收货人手机号")
    private String receiverMobile;

    /**
     * 延长多少天收货 TODO 临时存储下单间隔天数 [ES]
     */
    @Schema(description = "延长多少天收货  (临时存储下单间隔天数)")
    private Long delayDays;

    /**
     * 是否评价:1.未评价,2.部分评价,3.全部评价 [ES] TODO 临时存储订单渠道 [ES]
     */
    @Schema(description = "是否评价:1.未评价,2.部分评价,3.全部评价 （临时存储订单渠道）")
    private Integer evaluateState;

    /**
     * 订单类型：0 堂食 1 打包 2 外卖 3代取 [ES]
     */
    @Schema(description = "订单类型：0 堂食 1 打包 2 外卖 3代取")
    private Integer orderType;

    /**
     * 订单来源 1-微信小程序 [ES]
     */
    @Schema(description = "订单来源 1-微信小程序")
    private Integer orderFrom;

    /**
     * 锁定状态：0-是正常, 大于0是锁定状态，用户申请退款或退货时锁定状态加1，处理完毕减1。锁定后不能操作订单 [ES] TODO 临时存储是否是秒杀单 0否 1是 [ES]
     */
    @Schema(description = "锁定状态：0-是正常, 大于0是锁定状态，用户申请退款或退货时锁定状态加1，处理完毕减1。锁定后不能操作订单 （临时存储是否是秒杀单 0否 1是）")
    private Integer lockState;

    /**
     * 物流公司ID TODO 临时存储是否是新客下单 0否 1是 [ES]
     */
    @Schema(description = "物流公司ID (临时存储是否是新客下单 0否 1是)")
    private Long expressId;

    /**
     * 物流编号 TODO 临时存储拼单主体mainId [ES]
     */
    @Schema(description = "物流编号 (临时存储拼单主体mainId)")
    private String expressCode;

    /**
     * 物流公司 TODO 临时存储门店经度 [ES]
     */
    @Schema(description = "物流公司 (临时存储门店经度)")
    private String expressName;

    /**
     * 快递单号 TODO 临时存储第三方支付号 [ES]
     */
    @Schema(description = "快递单号 (临时存储第三方支付号)")
    private String expressNumber;

    /**
     * 取消原因 TODO 临时储存顾客经度 [ES]
     */
    @Schema(description = "取消原因 (临时储存经度)")
    private String refuseReason;

    /**
     * 取消备注 TODO 临时储存顾客纬度 [ES]
     */
    @Schema(description = "取消备注 (临时储存纬度)")
    private String refuseRemark;

    /**
     * 是否结算：0-未结算；1-已结算 TODO 临时存储是否是会员下单 0否 1是 [ES]
     */
    @Schema(description = "是否结算：0-未结算；1-已结算 (临时存储是否是会员下单 0否 1是)")
    private Integer isSettlement;

    /**
     * 是否已生成电子面单 1 - 已生成 2 - 未生成 [ES]
     */
    @Schema(description = "是否已生成电子面单 1 - 已生成 2 - 未生成")
    private Integer isGenerateFacesheet;

    /**
     * 是否是虚拟商品：1-实物商品；2-虚拟商品 [ES]
     */
    @Schema(description = "是否是虚拟商品：1-实物商品；2-虚拟商品")
    private Integer isVirtualGoods;

    /**
     * 星级，1-5 TODO 临时存储下单小时 [ES]
     */
    @Schema(description = "星级，1-5 (临时存储下单小时)")
    private Long star;

    /**
     * 商户备注 TODO 临时存储门店纬度 [ES]
     */
    @Schema(description = "商户备注 (临时存储门店纬度)")
    private String storeRemark;

    /**
     * 外卖姓名  [ES]
     */
    @Schema(description = "外卖姓名")
    private String takeAwayName;

    /**
     * 外卖手机号 [ES]
     */
    @Schema(description = "外卖手机号")
    private String takeAwayTel;

    /**
     * 取餐号 [ES]
     */
    @Schema(description = "取餐号")
    private String pickUpNum;

    /**
     * 外卖地址 TODO 临时存储优惠券名称 [ES]
     */
    @Schema(description = "外卖地址 (临时存储优惠券名称)")
    private String takeAwayAddress;

    /**
     * 商家电话 [ES]
     */
    @Schema(description = "商家电话")
    private String storePhone;

    /**
     * 起送费 [ES]
     */
    @Schema(description = "起送费")
    private BigDecimal minimumDeliveryFee;
    /**
     * 打包费 [ES]
     */
    @Schema(description = "打包费")
    private BigDecimal packingCharge;

    /**
     * 餐具数量 [ES]
     */
    @Schema(description = "餐具数量")
    private Integer tableWareNum;

    /**
     * openId [ES]
     */
    @Schema(description = "openId")
    private String openId;

    /**
     * 骑手电话 [ES]
     */
    @Schema(description = "骑手电话")
    private String deliveryPhone;

    /**
     * 骑手姓名 [ES]
     */
    @Schema(description = "骑手姓名")
    private String deliveryName;

    /**
     * 跑腿员会员ID，对应 wx_member.id
     */
    @Schema(description = "跑腿员会员ID，对应 wx_member.id")
    private Long deliveryId;

    /**
     * 跑腿状态：备用字段，当前业务状态统一使用 orderState
     */
    @Schema(description = "跑腿状态：备用字段，当前业务状态统一使用 orderState")
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
     * 送达照片，最多3张逗号分隔
     */
    @Schema(description = "送达照片，最多3张逗号分隔")
    private String errandDeliveryImages;

    /**
     * 预约时间 [ES]
     */
    @Schema(description = "预约时间")
    private String appointmentTime;

    /**
     * 优惠券面额 [ES]
     */
    @Schema(description = "优惠券面额")
    private BigDecimal voucherPrice;

    /**
     * 优惠券编码 [ES]
     */
    @Schema(description = "优惠券编码")
    private String voucherCode;

    /**
     * 订单备注 [ES]
     */
    @Schema(description = "订单备注")
    private String orderRemark;

    /**
     * 制作中时间 现在存储的是代取订单已接单时间
     */
    @Schema(description = "制作中时间")
    private LocalDateTime makingTime;

    /**
     * 配送中/待取餐时间 现在存储的是代取订单已取货时间
     */
    @Schema(description = "配送中/待取餐时间")
    private LocalDateTime waitingTime;

    /**
     * 用户优惠券id [ES]
     */
    @Schema(description = "用户优惠券id")
    private Long userCouponId;

    /**
     * 优惠券id [ES]
     */
    @Schema(description = "优惠券id")
    private Long couponId;

    /**
     * 项目归属 弃用 [ES]
     */
    @Deprecated
    @Schema(description = "项目归属 弃用")
    private Long projectOwnerShip;

    /**
     * 省份 [ES]
     */
    @Schema(description = "省份")
    private String province;

    /**
     * 市 [ES]
     */
    @Schema(description = "市")
    private String city;

    /**
     * 区域  [ES]
     */
    @Schema(description = "区域")
    private String area;
}
