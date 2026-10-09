package com.htyoudao.youdao.framework.tracer.config;

import com.htyoudao.youdao.framework.tracer.core.metrics.DataSourcePoolMetricsBinder;
import com.htyoudao.youdao.framework.tracer.core.metrics.DubboThreadPoolMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * Metrics 配置类
 *
 * @author 0090
 */
@AutoConfiguration
@ConditionalOnClass({MeterRegistryCustomizer.class})
@ConditionalOnProperty(prefix = "youdao.metrics", value = "enable", matchIfMissing = true) // 允许使用 youdao.metrics.enable=false 禁用 Metrics
public class YoudaoMetricsAutoConfiguration {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(
            @Value("${spring.application.name}") String applicationName) {
        return registry -> registry.config().commonTags("application", applicationName);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public ApplicationListener<ServiceBeanExportedEvent> dubboThreadPoolMetrics() {
        return new DubboThreadPoolMetrics();
    }

    @Bean
    @ConditionalOnClass(name = "com.baomidou.dynamic.datasource.DynamicRoutingDataSource")
    @ConditionalOnBean(DataSource.class)
    public MeterBinder dataSourcePoolMetricsBinder() {
        return new DataSourcePoolMetricsBinder();
    }
}
