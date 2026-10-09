package com.htyoudao.youdao.framework.apilog.config;

import com.htyoudao.youdao.framework.apilog.core.filter.ApiAccessLogFilter;
import com.htyoudao.youdao.framework.apilog.core.interceptor.ApiAccessLogInterceptor;
import com.htyoudao.youdao.framework.common.enums.WebFilterOrderEnum;
import com.htyoudao.youdao.framework.web.config.WebProperties;
import com.htyoudao.youdao.framework.web.config.YoudaoWebAutoConfiguration;
import com.htyoudao.youdao.module.infra.api.logger.ApiAccessLogApi;
import jakarta.servlet.Filter;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration(after = YoudaoWebAutoConfiguration.class)
public class YoudaoApiLogAutoConfiguration implements WebMvcConfigurer {

    @DubboReference
    private ApiAccessLogApi apiAccessLogApi;

    /**
     * 创建 ApiAccessLogFilter Bean，记录 API 请求日志
     */
    @Bean
    @ConditionalOnProperty(prefix = "youdao.access-log", value = "enable", matchIfMissing = true) // 允许使用 youdao.access-log.enable=false 禁用访问日志
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public FilterRegistrationBean<ApiAccessLogFilter> apiAccessLogFilter(WebProperties webProperties,
                                                                         @Value("${spring.application.name}") String applicationName) {
        ApiAccessLogFilter filter = new ApiAccessLogFilter(webProperties, applicationName, apiAccessLogApi);
        return createFilterBean(filter, WebFilterOrderEnum.API_ACCESS_LOG_FILTER);
    }

    private static <T extends Filter> FilterRegistrationBean<T> createFilterBean(T filter, Integer order) {
        FilterRegistrationBean<T> bean = new FilterRegistrationBean<>(filter);
        bean.setOrder(order);
        return bean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ApiAccessLogInterceptor());
    }

}
