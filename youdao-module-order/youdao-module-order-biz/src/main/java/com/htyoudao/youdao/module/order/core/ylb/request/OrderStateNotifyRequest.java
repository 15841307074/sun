package com.htyoudao.youdao.module.order.core.ylb.request;

import com.alibaba.fastjson2.JSON;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 * 订单状态回调
 * </p>
 *
 * @author zhangjihe
 * @since 2025-03-27
 */
@Data
public class OrderStateNotifyRequest {

    /**
     * 签名
     */
    @NotBlank
    private String sign;

    /**
     * 接口名
     */
    @NotBlank
    private String cmd;

    /**
     * 云喇叭开放平台分配的开发者帐号ID
     */
    @NotNull
    private Integer source;

    /**
     * 接口版本号，默认v1.0
     */
    @NotBlank
    private String version;

    /**
     * 时间戳
     */
    @NotNull
    private Integer timestamp;

    /**
     * 业务参数
     */
    private OrderInfo  body;

    @Data
    public static class OrderInfo {
        /**
         * 订单号
         */
        private String orderId;

        /**
         * 枚举： 1：骑手收餐入箱 2：骑手配送中 3：骑手已送达 4：商家出餐 5：骑手到店取餐
         */
        private Integer logisticsStatus;

        private String courierName;

        private String courierPhone;

        private Double latitude;

        private Double longitude;
    }


    public void setBody(@NotNull String body) {
        this.body = JSON.parseObject(body, OrderInfo.class);
    }


}
