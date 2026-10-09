package com.htyoudao.youdao.module.order.dal.es;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * 0090订单 bz_order
 *
 * @author wangwei
 * @date 2025-01-02
 */

/**
 * bz_order
 *
 * @author zhangjihe
 * @date 2025-05-11
 */
@Data
@Document(indexName = "bz_order")
@Setting(shards = 3, replicas = 0)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BzOrderDocument {

    /**
     * 订单id
     */
    @Id
    @Field(name = "orderId", type = FieldType.Long)
    private Long orderId;

    /**
     * 订单号
     */
    @Field(name = "orderSn", type = FieldType.Keyword)
    private String orderSn;

    /**
     * 支付单号
     */
    @Field(name = "paySn", type = FieldType.Keyword)
    private String paySn;

    /**
     * 商家ID
     */
    @Field(name = "storeId", type = FieldType.Long)
    private Long storeId;

    /**
     * 商家名称
     */
    @Field(name = "storeName", type = FieldType.Keyword)
    private String storeName;

    /**
     * 买家name
     */
    @Field(name = "memberName", type = FieldType.Keyword)
    private String memberName;

    /**
     * 用户头像
     */
    @Field(name = "memberAvatar", type = FieldType.Keyword)
    private String memberAvatar;

    /**
     * 买家ID
     */
    @Field(name = "memberId", type = FieldType.Long)
    private Long memberId;

    /**
     * 支付成功时间
     */
    @Field(name = "payTime",type = FieldType.Date, format = DateFormat.epoch_millis)
    private Date payTime;

    /**
     * 创建时间
     */
    @Field(name = "createTime",type = FieldType.Date, format = DateFormat.epoch_millis)
    private Date createTime;

    /**
     * 订单完成时间
     */
    @Field(name = "finishTime",type = FieldType.Date, format = DateFormat.epoch_millis)
    private Date finishTime;

    /**
     * 订单状态：0-已取消；10-未付款订单；20-已付款；30-已取餐；40-待配送；50-配送中；60-已完成；80-制作中；110-待接单；120-已接单；200-待取餐
     */
    @Field(name = "orderState", type = FieldType.Integer)
    private Integer orderState;

    /**
     * 支付方式名称
     */
    @Field(name = "paymentName", type = FieldType.Keyword)
    private String paymentName;

    /**
     * 支付方式code  0 现金 1在线
     */
    @Field(name = "paymentCode", type = FieldType.Keyword)
    private String paymentCode;

    /**
     * 商品金额，等于订单中所有的商品的单价乘以数量之和
     */
    @Field(name = "goodsAmount", type = FieldType.Double)
    private BigDecimal goodsAmount;

    /**
     * 物流费用
     */
    @Field(name = "expressFee", type = FieldType.Double)
    private BigDecimal expressFee;

    /**
     * 活动优惠总金额 （= 店铺优惠券 + 平台优惠券 + 活动优惠【店铺活动 + 平台活动】 + 积分抵扣金额）
     */
    @Field(name = "activityDiscountAmount", type = FieldType.Double)
    private BigDecimal activityDiscountAmount;

    /**
     * 营销活动优惠金额
     */
    @Field(name = "promotionDiscountAmount", type = FieldType.Double)
    private BigDecimal promotionDiscountAmount;

    /**
     * 订单总金额(用户需要支付的金额)，等于商品总金额＋运费-活动优惠金额总额activity_discount_amount
     */
    @Field(name = "orderAmount", type = FieldType.Double)
    private BigDecimal orderAmount;

    /**
     * 余额账户支付总金额
     */
    @Field(name = "balanceAmount", type = FieldType.Double)
    private BigDecimal balanceAmount;

    /**
     * 三方支付金额
     */
    @Field(name = "payAmount", type = FieldType.Double)
    private BigDecimal payAmount;

    /**
     * 退款的金额，订单没有退款则为0
     */
    @Field(name = "refundAmount", type = FieldType.Double)
    private BigDecimal refundAmount;

    /**
     * 收货人
     */
    @Field(name = "receiverName", type = FieldType.Keyword)
    private String receiverName;

    /**
     * 省市区组合
     */
    @Field(name = "receiverAreaInfo", type = FieldType.Keyword)
    private String receiverAreaInfo;

    /**
     * 收货人详细地址
     */
    @Field(name = "receiverAddress", type = FieldType.Keyword)
    private String receiverAddress;

    /**
     * 收货人手机号
     */
    @Field(name = "receiverMobile", type = FieldType.Keyword)
    private String receiverMobile;

    /**
     * 延长多少天收货
     */
    @Field(name = "delayDays", type = FieldType.Long)
    private Long delayDays;

    /**
     * 物流公司ID
     */
    @Field(name = "expressId", type = FieldType.Long)
    private Long expressId;

    /**
     * 物流编号
     */
    @Field(name = "expressCode", type = FieldType.Keyword)
    private String expressCode;

    /**
     * 物流公司
     */
    @Field(name = "expressName", type = FieldType.Keyword)
    private String expressName;

    /**
     * 快递单号
     */
    @Field(name = "expressNumber", type = FieldType.Keyword)
    private String expressNumber;

    /**
     * 是否评价:1.未评价,2.部分评价,3.全部评价
     */
    @Field(name = "evaluateState", type = FieldType.Integer)
    private Integer evaluateState;

    /**
     * 订单类型：0 堂食 1 打包 2 外卖
     */
    @Field(name = "orderType", type = FieldType.Integer)
    private Integer orderType;

    /**
     * 订单来源 1-微信小程序
     */
    @Field(name = "orderFrom", type = FieldType.Integer)
    private Integer orderFrom;

    /**
     * 锁定状态：0-是正常, 大于0是锁定状态，用户申请退款或退货时锁定状态加1，处理完毕减1。锁定后不能操作订单
     */
    @Field(name = "lockState", type = FieldType.Integer)
    private Integer lockState;

    /**
     * 取消原因
     */
    @Field(name = "refuseReason", type = FieldType.Keyword)
    private String refuseReason;

    /**
     * 取消备注
     */
    @Field(name = "refuseRemark", type = FieldType.Keyword)
    private String refuseRemark;

    /**
     * 优惠券面额
     */
    @Field(name = "voucherPrice", type = FieldType.Double)
    private BigDecimal voucherPrice;

    /**
     * 优惠券编码
     */
    @Field(name = "voucherCode", type = FieldType.Keyword)
    private String voucherCode;

    /**
     * 订单备注
     */
    @Field(name = "orderRemark", type = FieldType.Keyword)
    private String orderRemark;

    /**
     * 是否结算：0-未结算；1-已结算
     */
    @Field(name = "isSettlement", type = FieldType.Integer)
    private Integer isSettlement;

    /**
     * 是否已生成电子面单 1 - 已生成 2 - 未生成
     */
    @Field(name = "isGenerateFacesheet", type = FieldType.Integer)
    private Integer isGenerateFacesheet;

    /**
     * 是否是虚拟商品：1-实物商品；2-虚拟商品
     */
    @Field(name = "isVirtualGoods", type = FieldType.Integer)
    private Integer isVirtualGoods;

    /**
     * 星级，1-5
     */
    @Field(name = "star", type = FieldType.Long)
    private Long star;

    /**
     * 商户备注
     */
    @Field(name = "storeRemark", type = FieldType.Keyword)
    private String storeRemark;

    /**
     * 外卖姓名
     */
    @Field(name = "takeAwayName", type = FieldType.Keyword)
    private String takeAwayName;

    /**
     * 外卖手机号
     */
    @Field(name = "takeAwayTel", type = FieldType.Keyword)
    private String takeAwayTel;

    /**
     * 取餐号
     */
    @Field(name = "pickUpNum", type = FieldType.Keyword)
    private String pickUpNum;

    /**
     * 外卖地址
     */
    @Field(name = "takeAwayAddress", type = FieldType.Keyword)
    private String takeAwayAddress;

    /**
     * 商家电话
     */
    @Field(name = "storePhone", type = FieldType.Keyword)
    private String storePhone;

    /**
     * 起送费
     */
    @Field(name = "minimumDeliveryFee", type = FieldType.Double)
    private BigDecimal minimumDeliveryFee;

    /**
     * 打包费
     */
    @Field(name = "packingCharge", type = FieldType.Double)
    private BigDecimal packingCharge;

    /**
     * 餐具数量
     */
    @Field(name = "tableWareNum", type = FieldType.Integer)
    private Integer tableWareNum;

    /**
     * openId
     */
    @Field(name = "openId", type = FieldType.Keyword)
    private String openId;

    /**
     * 骑手电话
     */
    @Field(name = "deliveryPhone", type = FieldType.Keyword)
    private String deliveryPhone;

    /**
     * 骑手姓名
     */
    @Field(name = "deliveryName", type = FieldType.Keyword)
    private String deliveryName;

    /**
     * 跑腿员会员ID，对应 wx_member.id
     */
    @Field(name = "deliveryId", type = FieldType.Long)
    private Long deliveryId;

    /**
     * 跑腿状态，备用字段，当前业务状态统一使用 orderState
     */
    @Field(name = "errandStatus", type = FieldType.Integer)
    private Integer errandStatus;

    /**
     * 用户支付跑腿赏金
     */
    @Field(name = "errandRewardAmount", type = FieldType.Double)
    private BigDecimal errandRewardAmount;

    /**
     * 门店跑腿补贴金额
     */
    @Field(name = "errandStoreSubsidyAmount", type = FieldType.Double)
    private BigDecimal errandStoreSubsidyAmount;

    /**
     * 跑腿员性别限制：0不限 1男 2女
     */
    @Field(name = "errandGenderLimit", type = FieldType.Integer)
    private Integer errandGenderLimit;

    /**
     * 预约时间
     */
    @Field(name = "appointmentTime", type = FieldType.Keyword)
    private String appointmentTime;

    /**
     * 用户优惠券id
     */
    @Field(name = "userCouponId", type = FieldType.Long)
    private Long userCouponId;

    /**
     * 优惠券id
     */
    @Field(name = "couponId", type = FieldType.Long)
    private Long couponId;

    /**
     * 项目归属id
     */
    @Field(name = "projectOwnerShip", type = FieldType.Long)
    private Long projectOwnerShip;

    /**
     * 省份
     */
    @Field(name = "province", type = FieldType.Keyword)
    private String province;

    /**
     * 市
     */
    @Field(name = "city", type = FieldType.Text)
    private String city;

    /**
     * 区
     */
    @Field(name = "area", type = FieldType.Keyword)
    private String area;

    /**
     * businessId
     */
    @Field(name = "businessId", type = FieldType.Long)
    private Long businessId;

    @Field(name = "channel", type = FieldType.Integer)
    private Integer channel;

    @Field(name = "channelType", type = FieldType.Keyword)
    private String channelType;

    /**
     * 商品信息集合
     */
    @Field(name = "product", type = FieldType.Nested)
    @JsonProperty("product") // 关键：让反序列化时能映射 ES 中的 "product"
    private List<ProductInfo> products;

    /**
     * 商品信息
     */
    @Data
    public static class ProductInfo {

        @Field(name = "goodsId", type = FieldType.Long)
        private Long goodsId;

        @Field(name = "goodsName", type = FieldType.Text)
        private String goodsName;

        @Field(name = "goodsAmount", type = FieldType.Double)
        private Double goodsAmount;

        @Field(name = "goodsNum", type = FieldType.Integer)
        private Integer goodsNum;

        @Field(name = "goodsImage", type = FieldType.Keyword)
        private String goodsImage;

        @Field(name = "commodityId", type = FieldType.Long)
        private Long commodityId;

        @Field(name = "isSingle", type = FieldType.Integer)
        private Integer isSingle;

        @Field(name = "categoryId", type = FieldType.Long)
        private Long categoryId;

        @Field(name = "categoryName", type = FieldType.Keyword)
        private String categoryName;

        @Field(name = "activityId", type = FieldType.Long)
        private Long activityId;

        @Field(name = "activityType", type = FieldType.Long)
        private Long activityType;

        @Field(name = "activityName", type = FieldType.Keyword)
        private String activityName;

        /**
         * 优惠类型 1（1第二件半件，2买一送一，3自定义优惠）
         */
        @Field(name = "discountType", type = FieldType.Integer)
        private Integer discountType;

        /**
         * 优惠第几件
         */
        @Field(name = "discountItemNum", type = FieldType.Integer)
        private Integer discountItemNum;

        /**
         * 优惠打几折
         */
        @Field(name = "discountRate", type = FieldType.Double)
        private Double discountRate;

        /**
         * 满减、满折
         */
        @Field(name = "discountOffer", type = FieldType.Integer)
        private Integer discountOffer;

        /**
         * 优惠标签
         */
        @Field(name = "activityTag", type = FieldType.Text)
        private String activityTag;

        @Field(name = "couponId", type = FieldType.Long)
        private Long couponId;

        @Field(name = "userCouponId", type = FieldType.Long)
        private Long userCouponId;

        @Field(name = "couponName", type = FieldType.Keyword)
        private String couponName;

        @Field(name = "promotionDiscountAmount", type = FieldType.Double)
        private BigDecimal promotionDiscountAmount;

        @Field(name = "activityDiscountAmount", type = FieldType.Double)
        private BigDecimal activityDiscountAmount;
    }
}
