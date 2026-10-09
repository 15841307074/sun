package com.htyoudao.youdao.framework.common.enums;

/**
 * dubbo 过滤器顺序
 *
 * @author liuzhaowang
 */
public interface DubboFilterOrderEnum {

    /**
     * Dubbo Filter 顺序常量
     * 原则：
     * 1. 核心 Filter（如 Sentinel）应该最先执行：-100 到 -1
     * 2. 业务 Filter 在中间执行：0 到 100
     * 3. 日志等收尾 Filter 最后执行：Integer.MAX_VALUE
     */

    int DATA_PERMISSION_FILTER = 1;
    int HEADER_FILTER = DATA_PERMISSION_FILTER + 1;
    int EXCEPTION_FILTER = Integer.MAX_VALUE - 1;
    int LOG_FILTER = Integer.MAX_VALUE;
}
