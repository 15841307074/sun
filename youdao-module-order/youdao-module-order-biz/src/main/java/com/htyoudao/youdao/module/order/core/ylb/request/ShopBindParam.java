package com.htyoudao.youdao.module.order.core.ylb.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-03-26
 */
@Data
public class ShopBindParam {

    /**
     * 店铺唯一标识
     */
    @NotBlank
    private String shopId;

    /**
     * 云喇叭开放平台分配的开发者帐号ID
     */
    @NotNull
    protected Integer source;

    /**
     * 授权校验码，从授权接口传入
     */
    @NotBlank
    private String state;

}
