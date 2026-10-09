package com.htyoudao.youdao.module.analysis.dal.es;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.*;

@Data
@Document(indexName = "bz_order")
@Setting(shards = 3, replicas = 0)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BzOrder implements Serializable {

    @Transient
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    /**
     * 优惠总金额 营销活动 + 优惠券 [ES]
     */
    @Field(type = FieldType.Double)
    @JsonProperty("allDiscountAmount")
    @Schema(description = "优惠总金额 营销活动 + 优惠券")
    private BigDecimal allDiscountAmount;


    @Id
    @Field(type = FieldType.Keyword)
    private Long orderId;

    @Field(type = FieldType.Keyword, name = "_class")
    private String className;

    @Field(type = FieldType.Double)
    private Double activityDiscountAmount;

    @Field(type = FieldType.Keyword)
    private String appointmentTime;

    @Field(type = FieldType.Keyword)
    private String city;

    @Field(type = FieldType.Double)
    private Double balanceAmount;

    @Field(type = FieldType.Date)
    private Date createTime;

    /**
     * 上一次下单间隔
     */
    @Field(type = FieldType.Long)
    private Long delayDays;

    @Field(type = FieldType.Keyword)
    private String deliveryName;

    @Field(type = FieldType.Keyword)
    private String deliveryPhone;

    @Field(type = FieldType.Integer)
    private Integer evaluateState;

    @Field(type = FieldType.Keyword)
    private String expressCode;

    @Field(type = FieldType.Double)
    private Double expressFee;

    /**
     * 老客0 新客1
     */
    @Field(type = FieldType.Long)
    private Long expressId;

    @Field(type = FieldType.Keyword)
    private String expressName;

    @Field(type = FieldType.Keyword)
    private String expressNumber;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Date finishTime;

    @Field(type = FieldType.Double)
    private Double goodsAmount;

    @Field(type = FieldType.Integer)
    private Integer isGenerateFacesheet;

    /**
     * 临时存储是否是会员下单 0否 1是
     */
    @Field(type = FieldType.Integer)
    private Integer isSettlement;

    @Field(type = FieldType.Integer)
    private Integer isVirtualGoods;

    @Field(type = FieldType.Integer)
    private Integer lockState;

    @Field(type = FieldType.Long)
    private Long memberId;

    @Field(type = FieldType.Keyword)
    private String memberName;

    @Field(type = FieldType.Double)
    private Double minimumDeliveryFee;

    @Field(type = FieldType.Keyword)
    private String openId;

    @Field(type = FieldType.Double)
    private Double orderAmount;

    /**
     * 订单来源 0点餐机 1微信小程序 2支付宝
     */
    @Field(type = FieldType.Integer)
    private Integer orderFrom;

    @Field(type = FieldType.Keyword)
    private String orderSn;

    /**
     * 订单状态
     */
    @Field(type = FieldType.Integer)
    private Integer orderState;

    /**
     * 跑腿状态，兼容 bz_order 索引字段映射
     */
    @Field(type = FieldType.Integer)
    private Integer errandStatus;

    /**
     * 订单类型：0 堂食 1 打包 2 外卖 3 预订单
     */
    @Field(type = FieldType.Integer)
    private Integer orderType;

    @Field(type = FieldType.Double)
    private Double packingCharge;

    /**
     * 实付金额
     */
    @Field(type = FieldType.Double)
    private Double payAmount;

    /**
     * 门店跑腿补贴金额
     */
    @Field(type = FieldType.Double)
    @JsonProperty("errandStoreSubsidyAmount")
    private Double errandStoreSubsidyAmount;

    @Field(type = FieldType.Keyword)
    private String paySn;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time)
    private Date payTime;

    /**
     * 支付方式 0 现金
     */
    @Field(type = FieldType.Keyword)
    private String paymentCode;

    @Field(type = FieldType.Keyword)
    private String paymentName;

    @Field(type = FieldType.Keyword)
    private String pickUpNum;

    @Field(type = FieldType.Long)
    private Long projectOwnerShip;

    @Field(type = FieldType.Keyword)
    private String receiverAddress;

    @Field(type = FieldType.Keyword)
    private String receiverAreaInfo;

    @Field(type = FieldType.Keyword)
    private String receiverMobile;

    @Field(type = FieldType.Keyword)
    private String receiverName;

    @Field(type = FieldType.Double)
    private Double refundAmount;

    @Field(type = FieldType.Keyword)
    private String refuseReason;

    @Field(type = FieldType.Keyword)
    private String refuseRemark;

    @Field(type = FieldType.Long)
    private Long star;

    @Field(type = FieldType.Long)
    private Long storeId;

    @Field(type = FieldType.Keyword)
    private String storeName;

    @Field(type = FieldType.Keyword)
    private String storePhone;

    @Field(type = FieldType.Keyword)
    private String storeRemark;

    @Field(type = FieldType.Integer)
    private Integer tableWareNum;

    @Field(type = FieldType.Keyword)
    private String takeAwayAddress;

    @Field(type = FieldType.Keyword)
    private String takeAwayName;

    @Field(type = FieldType.Keyword)
    private String takeAwayTel;

    @Field(type = FieldType.Long)
    private Long userCouponId;

    /**
     * 商品数
     */
    @Transient
    @JsonIgnore
    private Integer productNum;

    /**
     * 活动名称
     */
    @Transient
    private String activityName;

    @Field(name = "channelType", type = FieldType.Keyword)
    @JsonProperty("channelType")
    private String channelType;

    /**
     * 商品信息
     */
    @Transient
    @JsonIgnore
    private String productName;


    @Field(type = FieldType.Nested, name = "product")
    @JsonProperty("product")
    private List<Product> product;

    @Field(type = FieldType.Long)
    @JsonProperty("channel")
    private Long channel;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Product {

        @Field(type = FieldType.Long)
        @JsonProperty("commodityId")
        private Long commodityId;

        @Field(type = FieldType.Keyword)
        @JsonProperty("goodsName")
        private String goodsName;

        @Field(type = FieldType.Integer)
        @JsonProperty("goodsNum")
        private Integer goodsNum;

        @Field(type = FieldType.Long)
        @JsonProperty("activityId")
        private Long activityId;

        /**
         * 营销活动 优惠金额
         */
        @Field(type = FieldType.Double)
        @JsonProperty("promotionDiscountAmount")
        private Double promotionDiscountAmount;

        /**
         * 商品单价
         */
        @Field(type = FieldType.Double)
        @JsonProperty("goodsAmount")
        private Double goodsAmount;

        /**
         * 优惠优惠卷金额
         */
        @Field(type = FieldType.Double)
        @JsonProperty("activityDiscountAmount")
        private Double activityDiscountAmount;

        /**
         * 营销活动名称
         */
        @Field(type = FieldType.Keyword)
        @JsonProperty("activityName")
        private String activityName;

        /**
         * 优惠卷名称
         */
        @Field(type = FieldType.Keyword)
        @JsonProperty("couponName")
        private String couponName;

        /**
         * 优惠标签
         */
        private String activityTag;

        /**
         * 优惠类型 1（1第二件半件，2买一送一，3自定义优惠）
         */
        private Integer discountType;

//        @Field(name = "allDiscountAmount", type = FieldType.Double)
//        @JsonProperty("allDiscountAmount")
//        private BigDecimal allDiscountAmount;
//

//
//        @Field(name = "channelType", type = FieldType.Keyword)
//        @JsonProperty("channelType")
//        private String channelType;

        private Integer isSon;



        public Integer getGoodsNum() {
            return this.goodsNum != null ? this.goodsNum : 0;
        }

        public String getGoodsName() {
            return this.goodsName;
        }

        public Long getActivityId() {
            return activityId;
        }

        public void setActivityId(Long activityId) {
            this.activityId = activityId;
        }


        public Long getCommodityId() {
            return commodityId;
        }

        public void setCommodityId(Long commodityId) {
            this.commodityId = commodityId;
        }

        public void setGoodsName(String goodsName) {
            this.goodsName = goodsName;
        }

        public void setGoodsNum(Integer goodsNum) {
            this.goodsNum = goodsNum;
        }

        public Double getPromotionDiscountAmount() {
            return promotionDiscountAmount;
        }

        public void setPromotionDiscountAmount(Double promotionDiscountAmount) {
            this.promotionDiscountAmount = promotionDiscountAmount;
        }

        public Double getGoodsAmount() {
            return goodsAmount;
        }

        public void setGoodsAmount(Double goodsAmount) {
            this.goodsAmount = goodsAmount;
        }

        public Double getActivityDiscountAmount() {
            return activityDiscountAmount;
        }

        public void setActivityDiscountAmount(Double activityDiscountAmount) {
            this.activityDiscountAmount = activityDiscountAmount;
        }

        public String getActivityName() {
            return activityName;
        }

        public void setActivityName(String activityName) {
            this.activityName = activityName;
        }

        public String getCouponName() {
            return couponName;
        }

        public void setCouponName(String couponName) {
            this.couponName = couponName;
        }
    }
}
