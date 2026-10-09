package com.htyoudao.youdao.module.analysis.service.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Elasticsearch 聚合查询数据传输对象
 * 用于封装聚合查询所需的筛选条件，包括时间范围、门店、订单来源等
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EsAggDTO {
    
    /**
     * 时间范围 [开始时间, 结束时间]
     */
    @Schema(description = "时间范围 [开始时间, 结束时间]")
    public LocalDateTime[] times;
    
    /**
     * 门店ID列表
     */
    @Schema(description = "门店ID列表")
    public List<Long> storeIds;
    
    /**
     * 订单来源列表：0点餐 1微信 2支付宝
     */
    @Schema(description = "订单来源列表：0点餐 1微信 2支付宝")
    public List<Integer> orderFroms;

    /**
     * 订单状态列表
     */
    @Schema(description = "订单状态列表")
    public List<Integer> orderS;

    /**
     * 是否会员
     * 0: 否 1: 是
     * 用于筛选会员订单
     */
    @Schema(description = "是否会员 0否 1是")
    private Integer isSettlement;

    /**
     * 是否新客
     * 0: 否 1: 是
     * 用于筛选新客户订单
     */
    @Schema(description = "是否新客 0否 1是")
    private Integer expressId;

    /**
     * 城市名称
     * 用于按城市筛选数据
     */
    @Schema(description = "城市名称")
    private String cityName;

    /**
     * 门店名称
     * 用于按门店名称模糊查询
     */
    @Schema(description = "门店名称")
    private String storeName;

    /**
     * 活动ID
     * 用于筛选特定活动的订单数据
     */
    @Schema(description = "活动ID")
    private Long activityId;

    /**
     * 是否排除不展示的数据
     * true: 排除 false: 不排除
     */
    @Schema(description = "是否排除不展示的数据")
    private Boolean selectNoShow = false;
    
    /**
     * 门店ID（单个）
     * 用于精确查询单个门店的数据
     */
    @Schema(description = "门店ID（单个）")
    public Long storeId;

    /**
     * 会员标识文本
     * 用于会员相关的数据筛选
     */
    @Schema(description = "会员标识文本")
    public String memberText;

    /**
     * 商品ID
     * 用于筛选特定商品的数据
     */
    @Schema(description = "商品ID")
    private Long commodityId;

    /**
     * 渠道ID
     * 用于按渠道筛选数据
     */
    @Schema(description = "渠道ID")
    private Integer channel;


    /**
     * 渠道类型
     * 用于按渠道类型筛选数据
     */
    @Schema(description = "渠道类型")
    private String channelType;


}