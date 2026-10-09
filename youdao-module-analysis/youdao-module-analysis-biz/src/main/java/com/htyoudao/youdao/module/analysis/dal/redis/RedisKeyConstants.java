package com.htyoudao.youdao.module.analysis.dal.redis;


/**
 * System Redis Key 枚举类
 *
 * @author 0090
 */
public interface RedisKeyConstants {

    /**
     *  分析指标列表
     *  key analysis:userId:metrics:metricsName
     *  analysis:1:metrics:general -> ["orderAmount", "orderCount", "orderPayAmount"]
     */
    String ANALYSIS_METRICS_LIST = "analysis:%s:metrics:%s";

    String ANALYSIS_METRICS_GENERAL = "general";
    String ANALYSIS_METRICS_HISTORY = "history";
    String ANALYSIS_METRICS_DC = "dc";
}
