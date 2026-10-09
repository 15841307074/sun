package com.htyoudao.youdao.module.bpm.framework.flowable.config;

import cn.hutool.core.collection.ListUtil;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.behavior.BpmActivityBehaviorFactory;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateInvoker;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.event.BpmProcessInstanceEventPublisher;
import com.htyoudao.youdao.module.system.api.user.AdminUserApi;
import org.flowable.common.engine.api.delegate.FlowableFunctionDelegate;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * BPM 模块的 Flowable 配置类
 *
 * @author jason
 */
@Configuration(proxyBeanMethods = false)
@EnableAsync
public class BpmFlowableConfiguration {

    /**
     * 参考 {@link org.flowable.spring.boot.FlowableJobConfiguration} 类，创建对应的 AsyncListenableTaskExecutor Bean
     *
     * 如果不创建，会导致项目启动时，Flowable 报错的问题
     */
    @Bean(name = "applicationTaskExecutor")
    @ConditionalOnMissingBean(name = "applicationTaskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("flowable-task-Executor-");
        executor.setAwaitTerminationSeconds(120);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setTaskDecorator(runnable -> () -> {
            try {
                // 执行任务（createProcessInstance0 中会手动设置 Authentication）
                runnable.run();
            } finally {
                // 清理上下文，避免线程复用导致的用户ID污染
                Authentication.setAuthenticatedUserId(null);
            }
        });

        executor.initialize();
        return executor;
    }

    /**
     * BPM 模块的 ProcessEngineConfigurationConfigurer 实现类：
     *
     * 1. 设置各种监听器
     * 2. 设置自定义的 ActivityBehaviorFactory 实现
     */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> bpmProcessEngineConfigurationConfigurer(
            ObjectProvider<FlowableEventListener> listeners,
            ObjectProvider<FlowableFunctionDelegate> customFlowableFunctionDelegates,
            BpmActivityBehaviorFactory bpmActivityBehaviorFactory) {
        return configuration -> {
            // 注册监听器，例如说 BpmActivityEventListener
            configuration.setEventListeners(ListUtil.toList(listeners.iterator()));
            // 设置 ActivityBehaviorFactory 实现类，用于流程任务的审核人的自定义
            configuration.setActivityBehaviorFactory(bpmActivityBehaviorFactory);
            // 设置自定义的函数
            configuration.setCustomFlowableFunctionDelegates(ListUtil.toList(customFlowableFunctionDelegates.stream().iterator()));
        };
    }

    // =========== 审批人相关的 Bean ==========

    @Bean
    public BpmActivityBehaviorFactory bpmActivityBehaviorFactory(BpmTaskCandidateInvoker bpmTaskCandidateInvoker) {
        BpmActivityBehaviorFactory bpmActivityBehaviorFactory = new BpmActivityBehaviorFactory();
        bpmActivityBehaviorFactory.setTaskCandidateInvoker(bpmTaskCandidateInvoker);
        return bpmActivityBehaviorFactory;
    }

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") // adminUserApi 可以注入成功
    public BpmTaskCandidateInvoker bpmTaskCandidateInvoker(List<BpmTaskCandidateStrategy> strategyList,
                                                           AdminUserApi adminUserApi) {
        return new BpmTaskCandidateInvoker(strategyList, adminUserApi);
    }

    // =========== 自己拓展的 Bean ==========

    @Bean
    public BpmProcessInstanceEventPublisher processInstanceEventPublisher(ApplicationEventPublisher publisher) {
        return new BpmProcessInstanceEventPublisher(publisher);
    }

//    @Bean("threadPoolTaskBpmn")
//    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        // 核心线程数
//        executor.setCorePoolSize(5);
//        // 最大线程数
//        executor.setMaxPoolSize(10);
//        // 队列容量
//        executor.setQueueCapacity(100);
//        // 线程名前缀
//        executor.setThreadNamePrefix("Async-Thread-");
//        // 拒绝策略
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//        // 线程空闲时间
//        executor.setKeepAliveSeconds(60);
//        // 等待所有任务结束后再关闭线程池
//        executor.setWaitForTasksToCompleteOnShutdown(true);
//        // 等待时间
//        executor.setAwaitTerminationSeconds(60);
//        // 初始化
//        executor.initialize();
//        return executor;
//    }

}
