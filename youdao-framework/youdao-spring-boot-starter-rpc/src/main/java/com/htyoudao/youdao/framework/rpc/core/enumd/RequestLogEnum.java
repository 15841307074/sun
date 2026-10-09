package com.htyoudao.youdao.framework.rpc.core.enumd;

import lombok.AllArgsConstructor;

/**
 * 请求日志泛型
 *
 * @author Liu
 */
@AllArgsConstructor
public enum RequestLogEnum {

    /**
     * info 基础信息
     */
    INFO,

    /**
     * param 参数信息
     */
    PARAM,

    /**
     * full 全部
     */
    FULL;

}
