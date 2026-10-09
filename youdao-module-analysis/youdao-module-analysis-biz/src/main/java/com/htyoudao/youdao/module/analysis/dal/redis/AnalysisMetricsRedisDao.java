package com.htyoudao.youdao.module.analysis.dal.redis;

import java.util.List;

/*
 * 关注指标
 */
public interface AnalysisMetricsRedisDao {

    /**
     * 获取用户设置的指标
     */
    List<String> getMetricsByKey(String key);

    /**
     * 新增或更新指标
     */
    void saveOrUpdateMetrics(String key, List<String> metrics);


}