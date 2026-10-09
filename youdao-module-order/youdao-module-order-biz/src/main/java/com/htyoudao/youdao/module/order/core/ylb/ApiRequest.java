package com.htyoudao.youdao.module.order.core.ylb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-03-26
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ApiRequest <T>{

    /**
     * 签名
     */
    protected String sign;

    /**
     * 接口名
     */
    protected String cmd;

    /**
     * 云喇叭开放平台分配的开发者帐号ID
     */
    protected Integer source;

    /**
     * 接口版本号，默认v1.0
     */
    protected String version;

    /**
     * 时间戳
     */
    protected Integer timestamp;

    /**
     * 业务参数
     */
    protected T body;
}
