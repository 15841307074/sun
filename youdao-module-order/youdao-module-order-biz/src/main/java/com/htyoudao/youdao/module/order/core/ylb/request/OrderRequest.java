package com.htyoudao.youdao.module.order.core.ylb.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 菜品报文
 * </p>
 *
 * @author zhangjihe
 * @since 2025-03-26
 */
@Builder
@Data
public class OrderRequest {

    /**
     * 订单来源
     */
    private String source;

    /**
     * 门店唯一标识ID
     */
    private String shop_id;

    /**
     * 门店地址
     */
//    private String shop_address;

    /**
     * 门店名
     */
    private String shop_name;

    /**
     * 门店联系号码
     */
    private String shop_phone;

    /**
     * 收餐人
     */
    private String recipient_name;

    /**
     * 收餐人号码
     */
    private String recipient_phone;

    /**
     * 送餐地址
     */
    private String recipient_address;

    /**
     * 送餐纬度
     */
    private String recipient_latitude;

    /**
     * 送餐经度
     */
    private String recipient_longitude;

    /**
     * 订单ID
     */
    private String order_id;

    /**
     * 订单创建时间（时间戳字符串）
     */
    private Long created_time;

    /**
     * 订单更新时间（时间戳字符串）
     */
    private Long updated_time;

    /**
     * 订单流水号
     */
    private int day_seq;

    /**
     * 订单原价（单位：元）
     */
    private String original_price;

    /**
     * 菜品总份数
     */
    private int quantity;

    /**
     * 实际支付价格（单位：元）
     */
    private String paid_price;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 预计送达时间（非预订单传0）
     */
//    private Long delivery_time;

    /**
     * 订单状态回调地址
     */
    private String notify_url;

    /**
     * 菜品列表
     */
    private List<Food> foods;

    /**
     * 菜品信息实体类
     */
    @Builder
    @Data
    public static class Food {

        /**
         * 餐盒数量
         */
        private int box_num;

        /**
         * 餐盒费（单位：元）
         */
        private int box_price;

        /**
         * 菜品名
         */
        private String name;

        /**
         * 菜品单价（单位：元）
         */
        private String price;

        /**
         * 菜品份数
         */
        private int quantity;
    }
}

