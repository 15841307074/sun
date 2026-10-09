package com.htyoudao.youdao.module.commodity.api.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品活动 DO
 *
 * @author dht
 */
@Data
public class CommodityActivityDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2535358732418838790L;
    /**
     * 活动的唯一标识符
     */
    private Long activityId;
    /**
     * 商品表-商品唯一标识符
     */
    private Long commodityId;
    /**
     * 商品表-商品分类ID
     */
    private Long categoryId;
    /**
     * 商品表-商品名称
     */
    private String commodityName;
    /**
     * 商品缩略图
     */
    private String thumbnailUrl;
    /**
     * 是否是单品（1是 2 否）
     */
    private Integer isSingle;
    /**
     * 商品关联 skuId
     */
    private String skuIds;
    /**
     * 活动类型（如1-抽奖,2-折扣,3-促销）
     */
    private Integer activityType;
    /**
     * 活动开始时间
     */
    private LocalDateTime activityStartDate;
    /**
     * 活动结束时间
     */
    private LocalDateTime activityEndDate;
    /**
     * 活动描述
     */
    private String activityDescription;
    /**
     * 是否启用（1表示启用，0表示停用）
     */
    private Integer isActive;

}