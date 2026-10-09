package com.htyoudao.youdao.framework.tracer.core.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.store.DataStore;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import org.apache.dubbo.rpc.model.ApplicationModel;
import org.springframework.context.ApplicationListener;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * details
 *
 * @author liuzhaowang
 */
@Slf4j
public class DubboThreadPoolMetrics implements ApplicationListener<ServiceBeanExportedEvent> {

    private final AtomicBoolean done = new AtomicBoolean(false);

    @Resource
    private MeterRegistry meterRegistry;
    @Resource
    ApplicationModel applicationModel;

    public void init() {
        // 多个dubbo service在export后，都会进来，但其实只需要进来一次就可以了
        if (!done.compareAndSet(false, true)) {
            return;
        }
        try {
            DataStore dataStore = applicationModel.getExtensionLoader(DataStore.class).getDefaultExtension();
            Map<String, Object> executors = dataStore.get(CommonConstants.EXECUTOR_SERVICE_COMPONENT_KEY);

            executors.forEach((port, executor) -> {

                Tags tags = Tags.of("thread.pool.name", "dubboThreadPool", "port", port);

                if (executor instanceof ThreadPoolExecutor tp) {
                    // 也可以通过microMeter自带的JVM线程池绑定器ExecutorServiceMetrics去绑定dubbo的线程池
                    // prometheus会将指标转为自己的命名风格：dubbo_thread_pool_core_size
                    Gauge.builder("dubbo.thread.pool.core.size", tp, ThreadPoolExecutor::getCorePoolSize)
                            .description("核心线程数")
                            .baseUnit("threads")
                            .tags(tags)
                            .register(meterRegistry);
                    Gauge.builder("dubbo.thread.pool.largest.size", tp, ThreadPoolExecutor::getLargestPoolSize)
                            .description("历史最高线程数")
                            .baseUnit("threads")
                            .tags(tags)
                            .register(meterRegistry);
                    Gauge.builder("dubbo.thread.pool.max.size", tp, ThreadPoolExecutor::getMaximumPoolSize)
                            .description("最大线程数")
                            .baseUnit("threads")
                            .tags(tags)
                            .register(meterRegistry);
                    Gauge.builder("dubbo.thread.pool.active.size", tp, ThreadPoolExecutor::getActiveCount)
                            .description("活跃线程数")
                            .baseUnit("threads")
                            .tags(tags)
                            .register(meterRegistry);
                    Gauge.builder("dubbo.thread.pool.thread.count", tp, ThreadPoolExecutor::getPoolSize)
                            .description("当前线程数")
                            .baseUnit("threads")
                            .tags(tags)
                            .register(meterRegistry);
                    Gauge.builder("dubbo.thread.pool.queue.size", tp, e -> e.getQueue().size())
                            .description("队列大小")
                            .baseUnit("threads")
                            .tags(tags)
                            .register(meterRegistry);
                    Gauge.builder("dubbo.thread.pool.taskCount", tp, ThreadPoolExecutor::getTaskCount)
                            .description("任务总量")
                            .baseUnit("threads")
                            .tags(tags)
                            .register(meterRegistry);
                    Gauge.builder("dubbo.thread.pool.completedTaskCount", tp, ThreadPoolExecutor::getCompletedTaskCount)
                            .description("已完成的任务量")
                            .baseUnit("threads")
                            .tags(tags)
                            .register(meterRegistry);
                }
            });
        } catch (Exception e) {
            log.error("e:", e);
        }
    }

    @Override
    public void onApplicationEvent(ServiceBeanExportedEvent event) {
        // 等dubbo某一个service export操作完毕后，会通知到这里，此时dubbo的线程池肯定也就初始化好了
        init();
    }
}
