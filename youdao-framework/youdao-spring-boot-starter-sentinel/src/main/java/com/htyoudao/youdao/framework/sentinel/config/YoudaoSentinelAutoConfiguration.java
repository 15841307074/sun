package com.htyoudao.youdao.framework.sentinel.config;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.htyoudao.youdao.framework.sentinel.core.handler.SentinelExceptionHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * Sentinel 基础配置类
 *
 * 功能：
 * 1. 配置 Sentinel 控制台地址
 * 2. 配置 Dubbo Sentinel 全局 fallback 处理
 * 3. 初始化 Sentinel
 * 4. 根据 Web 应用类型导入对应配置
 *
 * @author 0090
 */
@Slf4j
@AutoConfiguration(before = SentinelAutoConfiguration.class)
@EnableConfigurationProperties({SentinelProperties.class, SentinelCustomProperties.class})
@Import({
    YoudaoSentinelDubboConfiguration.class // 导入 Dubbo Sentinel 配置，具体启用由 @Conditional 控制
})
public class YoudaoSentinelAutoConfiguration {

    @Resource
    private SentinelProperties properties;
    @Resource
    private SentinelCustomProperties customProperties;
    @Resource
    private DiscoveryClient discoveryClient;

    @PostConstruct
    public void init() {
        // 1. 设置 Sentinel 控制台地址
        setupSentinelDashboard();

        // 2. 初始化 Sentinel
        InitExecutor.doInit();

        log.info("[Sentinel] 基础配置初始化完成");
    }

    private void setupSentinelDashboard() {
        if (StringUtils.isNotBlank(customProperties.getServerName())) {
            List<ServiceInstance> instances = discoveryClient.getInstances(customProperties.getServerName());
            String serverList = instances.stream()
                    .map(instance -> String.format("http://%s:%s", instance.getHost(), instance.getPort()))
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");
            if (StringUtils.isNotEmpty(serverList)) {
                System.setProperty(TransportConfig.CONSOLE_SERVER, serverList);
                log.info("[Sentinel] 使用服务发现方式设置控制台地址：{}", serverList);
            }
        } else if (StringUtils.isEmpty(System.getProperty(TransportConfig.CONSOLE_SERVER))
                && StringUtils.isNotBlank(properties.getTransport().getDashboard())) {
            System.setProperty(TransportConfig.CONSOLE_SERVER, properties.getTransport().getDashboard());
            log.info("[Sentinel] 使用配置方式设置控制台地址：{}", properties.getTransport().getDashboard());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public SentinelExceptionHandler sentinelExceptionHandler() {
        return new SentinelExceptionHandler();
    }
}
