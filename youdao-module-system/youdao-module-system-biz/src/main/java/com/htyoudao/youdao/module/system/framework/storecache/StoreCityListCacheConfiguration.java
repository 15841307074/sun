package com.htyoudao.youdao.module.system.framework.storecache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionException;

/**
 * 城市门店列表缓存线程池配置。
 *
 * <p>缓存重建使用独立且有界的线程池，避免占用正常业务线程。</p>
 */
@Slf4j
@Configuration
public class StoreCityListCacheConfiguration {

    /**
     * 创建城市门店缓存专用线程池。
     *
     * @return 城市门店缓存线程池
     */
    @Bean(name = "storeCityListCacheExecutor")
    public ThreadPoolTaskExecutor storeCityListCacheExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("store-city-cache-");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.setRejectedExecutionHandler((task, threadPoolExecutor) -> {
            log.warn("城市门店缓存线程池队列已满，本次任务留在待刷新集合中等待后续处理");
            throw new RejectedExecutionException("城市门店缓存线程池队列已满");
        });
        executor.initialize();
        return executor;
    }
}
