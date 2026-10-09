package com.htyoudao.youdao.module.order.dal.es;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Data;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

@Data
@Document(indexName = "bz_order_product")
@Setting(shards = 3, replicas = 0)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BzOrderProductDocument implements Serializable {

    @Transient
    @JsonIgnore
    private static final long serialVersionUID = 1L;


    @Field(type = FieldType.Long)
    @JsonProperty("commodityId")
    private Long commodityId;

    @Field(type = FieldType.Keyword)
    @JsonProperty("goodsName")
    private String goodsName;

    @Field(type = FieldType.Integer)
    @JsonProperty("goodsNum")
    private Integer goodsNum;

    /**
     * 是否加购商品 0否 1是
     */
    @Field(name = "isPurchase", type = FieldType.Integer)
    @JsonProperty("isPurchase")
    private Integer isPurchase;

    @Field(type = FieldType.Long)
    @JsonProperty("activityId")
    private Long activityId;

    /**
     * 优惠活动价格
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
     * 活动名称
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


}
