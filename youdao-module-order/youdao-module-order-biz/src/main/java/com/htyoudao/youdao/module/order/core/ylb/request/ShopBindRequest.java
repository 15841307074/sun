package com.htyoudao.youdao.module.order.core.ylb.request;

import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * 门店映射报文
 * </p>
 *
 * @author zhangjihe
 * @since 2025-03-26
 */
@Data
@Builder
public class ShopBindRequest {

    /**
     * 店铺唯一标识
     */
    private String shopId;

    /**
     * 地址
     */
    private String address;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 店铺名
     */
    private String name;

    /**
     * 商家联系电话
     */
    private String phone;

    /**
     * 授权校验码，从授权接口传入
     */
    private String state;
}