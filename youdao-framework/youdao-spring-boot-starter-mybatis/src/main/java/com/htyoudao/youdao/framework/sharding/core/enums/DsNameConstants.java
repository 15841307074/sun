package com.htyoudao.youdao.framework.sharding.core.enums;

/**
 * 动态数据源名称常量
 *
 * @author liuzhaowang
 */
public interface DsNameConstants {
    /**
     * 主数据源
     */
    String MASTER = "master";
    /**
     * 从数据源
     */
    String SLAVE = "slave";
    /**
     * 分片数据源
     */
    String SHARDING = "sharding";
}
