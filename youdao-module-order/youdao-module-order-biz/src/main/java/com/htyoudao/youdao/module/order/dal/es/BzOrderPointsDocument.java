package com.htyoudao.youdao.module.order.dal.es;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * 0090订单 bz_order 集点活动
 *
 * @author wangwei
 * @date 2025-01-02
 */
@Data
@Document(indexName = "bz_order_points")
@Setting(shards = 3, replicas = 0)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BzOrderPointsDocument {

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
     * 买家ID
     */
    @Field(name = "memberId", type = FieldType.Long)
    private Long memberId;

    /**
     * 创建时间
     */
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Field(name = "createTime",type = FieldType.Date, format = DateFormat.epoch_millis)
    private Date createTime;

    /**
     * 支付方式code  0 现金 1在线
     */
    @Field(name = "paymentCode", type = FieldType.Keyword)
    private String paymentCode;

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
     * 手机号
     */
    @Field(name = "takeAwayTel", type = FieldType.Keyword)
    private String takeAwayTel;

    /**
     * 取餐号
     */
    @Field(name = "pickUpNum", type = FieldType.Keyword)
    private String pickUpNum;

    /**
     * 三方支付金额
     */
    @Field(name = "payAmount", type = FieldType.Double)
    private BigDecimal payAmount;

    /**
     * openId
     */
    @Field(name = "openId", type = FieldType.Keyword)
    private String openId;

    @Field(name = "activityId", type = FieldType.Long)
    private Long activityId;
}
