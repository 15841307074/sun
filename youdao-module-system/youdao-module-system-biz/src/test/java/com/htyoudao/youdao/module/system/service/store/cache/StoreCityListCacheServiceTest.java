package com.htyoudao.youdao.module.system.service.store.cache;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.RejectedExecutionException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 城市门店缓存事务通知测试。
 */
@ExtendWith(MockitoExtension.class)
class StoreCityListCacheServiceTest {

    @InjectMocks
    private StoreCityListCacheService service;
    @Mock
    private ThreadPoolTaskExecutor cacheExecutor;

    /**
     * 启动预热应进入有界全量补偿流程，并投递到专用线程池。
     */
    @Test
    void shouldSubmitStartupWarmupThroughRecoveryWorker() {
        service.initializeCache();

        verify(cacheExecutor).execute(any(Runnable.class));
    }

    /**
     * 清理测试线程上的事务同步状态，避免测试之间相互影响。
     */
    @AfterEach
    void clearTransactionSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    /**
     * 非事务调用应直接投递到专用线程池，而不是在当前线程操作 Redis。
     */
    @Test
    void shouldSubmitCacheChangeAsynchronouslyWithoutTransaction() {
        service.refreshStoreAfterCommit(1001L, "沈阳市", "沈阳市", false);

        verify(cacheExecutor).execute(any(Runnable.class));
    }

    /**
     * 事务回滚后不得投递缓存失效任务。
     */
    @Test
    void shouldNotSubmitCacheChangeAfterRollback() {
        TransactionSynchronizationManager.initSynchronization();
        service.refreshStoreAfterCommit(1001L, "沈阳市", "大连市", false);

        verify(cacheExecutor, never()).execute(any(Runnable.class));
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCompletion(
                        TransactionSynchronization.STATUS_ROLLED_BACK));

        verify(cacheExecutor, never()).execute(any(Runnable.class));
    }

    /**
     * 事务提交完成后才投递缓存失效任务。
     */
    @Test
    void shouldSubmitCacheChangeAfterCommit() {
        TransactionSynchronizationManager.initSynchronization();
        service.refreshStoreAfterCommit(1001L, "沈阳市", "大连市", false);

        verify(cacheExecutor, never()).execute(any(Runnable.class));
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(synchronization -> synchronization.afterCompletion(
                        TransactionSynchronization.STATUS_COMMITTED));

        verify(cacheExecutor).execute(any(Runnable.class));
    }

    /**
     * 线程池拒绝任务时必须把门店保留在 Redis 待刷新集合，不能只依赖全量重建。
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldPersistPendingStoreWhenExecutorRejectsTask() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        SetOperations<String, String> setOperations = mock(SetOperations.class);
        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        ReflectionTestUtils.setField(service, "stringRedisTemplate", redisTemplate);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(valueOperations.increment("store:list:v1:revision")).thenReturn(1L);
        when(valueOperations.get("store:list:v1:active")).thenReturn("generation-1");
        doThrow(new RejectedExecutionException("测试线程池已满"))
                .when(cacheExecutor).execute(any(Runnable.class));

        service.refreshStoreAfterCommit(1001L, "沈阳市", "沈阳市", false);

        verify(setOperations).add(eq("store:list:v1:refresh:pending"), aryEq(new String[]{"1001"}));
        verify(hashOperations).delete(eq("store:list:v1:{generation-1}:data"), aryEq(new Object[]{"1001"}));
    }

    /**
     * 局部城市索引没有完成标记时，移除门店不能把它误标记为完整城市。
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldNotCreateReadyMarkerForPartiallyBuiltCity() {
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        ReflectionTestUtils.setField(service, "stringRedisTemplate", redisTemplate);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.hasKey("store:list:v1:{generation-1}:city-ready", "沈阳市"))
                .thenReturn(false);

        ReflectionTestUtils.invokeMethod(service, "updateReadyCountIfPresent", "generation-1", "沈阳市");

        verify(hashOperations, never()).put(anyString(), any(), any());
    }
}
