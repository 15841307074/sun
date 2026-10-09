package com.htyoudao.youdao.module.commodity.framework.thread;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadConfig {

    @Bean("syncExecutor")
    public ThreadPoolExecutor syncExecutor() {
        return new ThreadPoolExecutor(
                1,
                1,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2000), // 有界队列，容量2000
                new BasicThreadFactory.Builder()
                        .namingPattern("store-sync-%d")
                        .build(),
                new ThreadPoolExecutor.AbortPolicy() // 队列满时抛出RejectedExecutionException
        );
    }

    /**
     * 活动标签并行打标专用线程池（N件N折/满减满折/满赠并行查询）
     * 队列满时由调用线程降级串行执行，防止打标任务丢失
     */
    @Bean("activityTagExecutor")
    public ThreadPoolExecutor activityTagExecutor() {
        return new ThreadPoolExecutor(
                8,
                16,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(500),
                new BasicThreadFactory.Builder()
                        .namingPattern("activity-tag-%d")
                        .build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }


}