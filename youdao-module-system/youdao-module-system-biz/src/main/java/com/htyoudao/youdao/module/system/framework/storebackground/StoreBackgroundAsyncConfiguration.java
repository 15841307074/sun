package com.htyoudao.youdao.module.system.framework.storebackground;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 门店背景缓存异步任务配置。
 */
@Configuration
public class StoreBackgroundAsyncConfiguration {

    /**
     * 创建门店背景缓存专用线程池，与其他业务异步任务相互隔离。
     *
     * @return 门店背景缓存执行器
     */
    @Bean("storeBackgroundCacheExecutor")
    public Executor storeBackgroundCacheExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("store-background-cache-");
        executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.initialize();
        return executor;
    }
}
