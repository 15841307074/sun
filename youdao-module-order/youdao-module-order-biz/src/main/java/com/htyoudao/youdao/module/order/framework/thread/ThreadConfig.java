package com.htyoudao.youdao.module.order.framework.thread;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
public class ThreadConfig {

    private final int CPU_COUNT = Runtime.getRuntime().availableProcessors();

//    @Bean("syncExecutor")
//    public ThreadPoolExecutor syncExecutor() {
//        return new ThreadPoolExecutor(
//                12,
//                200,
//                60L, TimeUnit.SECONDS,
//                new ArrayBlockingQueue<>(5000),
//                new BasicThreadFactory.Builder()
//                        .namingPattern("order-sync-%d")
//                        .build(),
//                new ThreadPoolExecutor.AbortPolicy()
//        );
//    }

    /**
     * 强一致性线程池（库存扣减、积分增加）
     * 1. 需要快速执行，失败率低。
     * 2. CPU 消耗中等，依赖数据库/缓存操作。
     * 3. 线程池要保证核心线程常驻，避免排队过多。
     */
    @Bean(name = "strongExecutor")
    public Executor strongExecutor() {
        log.info("========================>  创建strongExecutor线程池: corePoolSize {} | maximumPoolSize {}  <========================", CPU_COUNT * 2, CPU_COUNT * 4);
        return new ThreadPoolExecutor(
                CPU_COUNT * 2,
                CPU_COUNT * 4,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new NamedThreadFactory("strong-task"),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    /**
     * 弱一致性线程池（消息推送、活动判断）
     * 1. 可以容忍一定延迟。
     * 2. 队列可以大一些，避免丢任务。
     */
    @Bean(name = "weakExecutor")
    public Executor weakExecutor() {
        log.info("========================>  创建weakExecutor线程池: corePoolSize {} | maximumPoolSize {}  <========================", CPU_COUNT, CPU_COUNT * 2);
        return new ThreadPoolExecutor(
                CPU_COUNT,
                CPU_COUNT * 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000),
                new NamedThreadFactory("weak-task"),
                new ThreadPoolExecutor.CallerRunsPolicy() // 避免丢任务
        );
    }

    /**
     * IO 线程池（小票打印、云喇叭）
     * 1. 打印机、硬件接口、HTTP 调用，延迟高。
     * 2. 线程池需要更多线程来覆盖 IO 等待。
     */
    @Bean(name = "ioExecutor")
    public Executor ioExecutor() {
        log.info("========================>  创建ioExecutor线程池: corePoolSize {} | maximumPoolSize {}  <========================", 20, 100);
        return new ThreadPoolExecutor(
                20, // 核心线程数
                100, // 最大线程数
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5000), // 较大的队列，保证小票打印不会丢
                new NamedThreadFactory("io-task"),
                new ThreadPoolExecutor.CallerRunsPolicy() // 避免丢任务，必要时调用方线程执行
        );
    }

    @Bean(name = "simulationPrintExecutor")
    public ThreadPoolExecutor simulationPrintExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                4,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new NamedThreadFactory("simulation-print"),
                new ThreadPoolExecutor.AbortPolicy()
        );
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    /**
     * 自定义线程工厂（方便日志排查）
     */
    static class NamedThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
        private int counter = 0;

        NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = defaultFactory.newThread(r);
            thread.setName(namePrefix + "-" + counter++);
            thread.setUncaughtExceptionHandler((t, e) ->
                    System.err.printf("线程池任务执行异常: %s, %s%n", t.getName(), e.getMessage()));
            return thread;
        }
    }


}
