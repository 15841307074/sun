package com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.es;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(indexName = "bz_order_points")
@Setting(shards = 3, replicas = 0)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BzOrderPoints implements Serializable {

    @Transient
    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @Id
    @Field(type = FieldType.Keyword)
    private String orderId;  // 改为String类型

    /**
     * 活动id
     */
    @Field(type = FieldType.Keyword)  // 改为Keyword类型
    private String activityId;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date)
    private Date createTime;

    /**
     * 用户id
     */
    @Field(type = FieldType.Keyword)  // 改为Keyword类型
    private String memberId;

    /**
     * 用户openID
     */
    @Field(type = FieldType.Keyword)
    private String openId;

    /**
     * 订单来源
     */
    @Field(type = FieldType.Integer)
    private Integer orderFrom;

    /**
     * 订单编码
     */
    @Field(type = FieldType.Keyword)
    private String orderSn;

    /**
     * 订单类型
     */
    @Field(type = FieldType.Integer)
    private Integer orderType;

    /**
     * 支付金额
     */
    @Field(type = FieldType.Double)
    private Double payAmount;

    /**
     * 支付编码
     */
    @Field(type = FieldType.Keyword)
    private String paymentCode;

    /**
     * 集点数
     */
    @Field(type = FieldType.Keyword)
    private String pickUpNum;

    /**
     * 门店id
     */
    @Field(type = FieldType.Keyword)  // 改为Keyword类型
    private String storeId;

    /**
     * 门店名称
     */
    @Field(type = FieldType.Keyword)
    private String storeName;

    @Field(type = FieldType.Keyword)
    private String takeAwayTel;
}