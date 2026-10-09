package com.htyoudao.youdao.module.promotion.service.lottery.v2;

import com.htyoudao.youdao.module.promotion.service.lottery.v2.impl.*;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class LotteryServiceWiringTest {
    @Test
    void allNewServiceContractsResolveAndResourceDependenciesCanBeInjected() {
        Class<?>[] implementations = {
                LotteryCachePublisherImpl.class, LotteryCashGatewayImpl.class,
                LotteryConfigurationCacheImpl.class, LotteryCounterCacheImpl.class,
                LotteryGrantStorageImpl.class, LotteryLedgerImpl.class,
                LotteryReissueServiceImpl.class, LotteryResultCacheImpl.class,
                LotteryRuntimeLifecycleImpl.class, LotteryScopeServiceImpl.class,
                LotteryV2ServiceImpl.class, LotteryWinnerFeedImpl.class, LotteryWinnerQueryImpl.class
        };
        Set<Class<?>> contracts = new HashSet<>();
        for (var implementation : implementations) contracts.addAll(Arrays.asList(implementation.getInterfaces()));
        try (var context = new AnnotationConfigApplicationContext()) {
            Set<Class<?>> dependencies = new HashSet<>();
            for (var implementation : implementations) {
                for (var field : implementation.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Resource.class) && !contracts.contains(field.getType())
                            && dependencies.add(field.getType())) {
                        context.getBeanFactory().registerSingleton("test" + field.getType().getSimpleName(), mock(field.getType()));
                    }
                }
            }
            context.register(implementations);
            context.refresh();
            for (var implementation : implementations) {
                var contract = implementation.getInterfaces()[0];
                var beanName = implementation.getAnnotation(Service.class).value();
                assertSame(context.getBean(contract), context.getBean(beanName));
            }
        }
    }
}
