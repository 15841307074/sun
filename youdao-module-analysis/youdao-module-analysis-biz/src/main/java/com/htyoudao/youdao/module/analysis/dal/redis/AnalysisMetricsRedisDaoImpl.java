package com.htyoudao.youdao.module.analysis.dal.redis;


import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import jakarta.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

@Repository
class AnalysisMetricsRedisDaoImpl implements AnalysisMetricsRedisDao {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<String> getMetricsByKey(String key) {
        String metricsKey = getMetricsKey(key);
        String metricsValueStr = stringRedisTemplate.opsForValue().get(metricsKey);

        if (StringUtils.isEmpty(metricsValueStr)){
            return List.of(
                MetricsConfig.ORDER_AMOUNT.getCode(),
                MetricsConfig.PAY_AMOUNT.getCode(),
                MetricsConfig.VALID_ORDERS.getCode()
            );
        }

        return List.of(metricsValueStr.split(","));
    }

    private static String getMetricsKey(String key) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        String metricsKey = String.format(RedisKeyConstants.ANALYSIS_METRICS_LIST, loginUserId , key);
        return metricsKey;
    }

    @Override
    public void saveOrUpdateMetrics(String key, List<String> metrics) {
        if (CollectionUtils.isEmpty(metrics)){
            return;
        }
        String metricsKey = getMetricsKey(key);

        // 当天 23:59:59 自动过期
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
        Duration duration = Duration.between(now, endOfDay);
        stringRedisTemplate.opsForValue().set(metricsKey, String.join(",", metrics), duration);
    }
}