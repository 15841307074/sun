package com.htyoudao.youdao.module.analysis.dal.es;

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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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
    private Long orderId;


    /**
     * 活动id
     */
    @Field(type = FieldType.Long)
    private Long activityId;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date)
    private Date createTime;


    /**
     * 用户id
     */
    @Field(type = FieldType.Long)
    private Long memberId;


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
    @Field(type = FieldType.Long)
    private Long storeId;



    /**
     * 门店名称
     */
    @Field(type = FieldType.Keyword)
    private String storeName;


    /**
     *
     */
    @Field(type = FieldType.Keyword)
    private String takeAwayTel;


}