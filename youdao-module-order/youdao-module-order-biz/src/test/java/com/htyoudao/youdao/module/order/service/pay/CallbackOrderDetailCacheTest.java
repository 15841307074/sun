package com.htyoudao.youdao.module.order.service.pay;

import com.alibaba.fastjson2.JSON;
import com.htyoudao.youdao.module.order.dal.DTO.OrderDetailDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductDO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductSonDO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CallbackOrderDetailCacheTest {
    private static final String ORDER = "ORD202609101200000001";
    private static final String KEY = "order:callback:detail:v1:10:" + ORDER;
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final ValueOperations<String, String> values = mock(ValueOperations.class);
    private final CallbackOrderDetailCache cache = new CallbackOrderDetailCache(redis, 180);

    @AfterEach
    void cleanup() {
        TransactionSynchronizationManager.clear();
    }

    @Test
    void publishOnlyAfterCommitAndPreserveLongParentId() {
        begin();
        when(redis.opsForValue()).thenReturn(values);
        cache.putAfterCommit(10L, ORDER, detail());
        verifyNoInteractions(redis);
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        ArgumentCaptor<String> snapshot = ArgumentCaptor.forClass(String.class);
        verify(values).set(eq(KEY), snapshot.capture(), eq(Duration.ofSeconds(180)));
        when(values.get(KEY)).thenReturn(snapshot.getValue());
        OrderDetailDTO restored = cache.get(10L, ORDER);
        assertNotNull(restored);
        assertEquals(947284703181373468L, restored.getProductDOList().get(0).getOrderProductId());
        assertEquals(restored.getProductDOList().get(0).getOrderProductId(),
                restored.getProductSonDOList().get(0).getParentGoodsId());
        assertNull(restored.getBzOrderDO());
    }

    @Test
    void rollbackAndMissingTransactionNeverPublish() {
        cache.putAfterCommit(10L, ORDER, detail());
        verifyNoInteractions(redis);
        begin();
        cache.putAfterCommit(10L, ORDER, detail());
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(sync -> sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
        verifyNoInteractions(redis);
    }

    @Test
    void cacheMissCorruptionAndRedisFailureReturnNull() {
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(KEY)).thenReturn(null, "not-json").thenThrow(new IllegalStateException("Redis unavailable"));
        assertNull(cache.get(10L, ORDER));
        assertNull(cache.get(10L, ORDER));
        assertNull(cache.get(10L, ORDER));
    }

    @Test
    void invalidParentLinkIsNotUsedOrPublished() {
        OrderDetailDTO broken = detail();
        broken.getProductSonDOList().get(0).setParentGoodsId(123L);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(KEY)).thenReturn(JSON.toJSONString(broken));
        assertNull(cache.get(10L, ORDER));
        begin();
        cache.putAfterCommit(10L, ORDER, broken);
        assertTrue(TransactionSynchronizationManager.getSynchronizations().isEmpty());
    }

    @Test
    void writeFailureDoesNotFailCommittedOrder() {
        begin();
        when(redis.opsForValue()).thenReturn(values);
        doThrow(new IllegalStateException("Redis unavailable")).when(values)
                .set(anyString(), anyString(), any(Duration.class));
        cache.putAfterCommit(10L, ORDER, detail());
        assertDoesNotThrow(() -> TransactionSynchronizationManager.getSynchronizations()
                .forEach(TransactionSynchronization::afterCommit));
    }

    @Test
    void zeroTtlDisablesCacheAndKeysAreBusinessScoped() {
        CallbackOrderDetailCache disabled = new CallbackOrderDetailCache(redis, 0);
        begin();
        disabled.putAfterCommit(10L, ORDER, detail());
        assertNull(disabled.get(10L, ORDER));
        verifyNoInteractions(redis);
        when(redis.opsForValue()).thenReturn(values);
        cache.get(11L, ORDER);
        verify(values).get("order:callback:detail:v1:11:" + ORDER);
    }

    private static void begin() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
    }

    private static OrderDetailDTO detail() {
        BzOrderProductDO product = new BzOrderProductDO();
        product.setOrderSn(ORDER);
        product.setOrderProductId(947284703181373468L);
        BzOrderProductSonDO son = new BzOrderProductSonDO();
        son.setOrderSn(ORDER);
        son.setOrderProductId(947284703181373469L);
        son.setParentGoodsId(product.getOrderProductId());
        OrderDetailDTO detail = new OrderDetailDTO();
        detail.setProductDOList(List.of(product));
        detail.setProductSonDOList(List.of(son));
        detail.setPurchaseDOList(List.of());
        return detail;
    }
}
