package com.htyoudao.youdao.module.promotion.service.activityMz;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift.ActivityMzGiftDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMzGift.ActivityMzGiftMapper;
import com.htyoudao.youdao.module.promotion.dal.redis.ActivityMzRedisDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActivityMzLazyInventoryTest {
    private final ActivityMzRedisDAO redis = mock(ActivityMzRedisDAO.class);
    private final ActivityMzGiftMapper mapper = mock(ActivityMzGiftMapper.class);
    private final ActivityMzCacheService service = new ActivityMzCacheService();
    private final ActivityMzCacheService.ActivityMzMeta meta = new ActivityMzCacheService.ActivityMzMeta();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "activityMzRedisDAO", redis);
        ReflectionTestUtils.setField(service, "activityMzGiftMapper", mapper);
        meta.setGiftInventoryType(2);
        when(redis.buildStoreKey(1L, 2L)).thenReturn("store-key");
    }

    @Test
    void cachedInventoryNeverQueriesMysql() {
        when(redis.changeInventory("store-key", 3L, -2)).thenReturn(2L);
        assertEquals(2, change(-2).getRemainingInventory());
        verifyNoInteractions(mapper);
        verify(redis, never()).changeInventory(any(), any(), anyInt(), any());
    }

    @Test
    void missingInventoryUsesOriginalConfigurationOnce() {
        ActivityMzGiftDO config = new ActivityMzGiftDO();
        config.setStoreId(99L);
        config.setActivityInventory(30);
        config.setRemainingInventory(7);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(config);
        when(redis.changeInventory("store-key", 3L, -2)).thenReturn(-2L, 2L);
        when(redis.changeInventory("store-key", 3L, -2, 30)).thenReturn(2L);

        assertEquals(2, change(-2).getRemainingInventory());
        assertEquals(2, change(-2).getRemainingInventory());

        verify(mapper, times(1)).selectOne(any(Wrapper.class));
        verify(redis, times(1)).changeInventory("store-key", 3L, -2, 30);
        verifyNoMoreInteractions(mapper);
    }

    @Test
    void unlimitedConfigurationUsesMinusOne() {
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(new ActivityMzGiftDO());
        when(redis.changeInventory("store-key", 3L, -2)).thenReturn(-2L);
        when(redis.changeInventory("store-key", 3L, -2, -1)).thenReturn(-1L);
        assertEquals(2, change(-2).getRemainingInventory());
    }

    @Test
    void missingConfigurationDoesNotInventStock() {
        when(redis.changeInventory("store-key", 3L, -2)).thenReturn(-2L);
        assertFalse(change(-2).isSuccess());
        verify(redis, never()).changeInventory(any(), any(), anyInt(), any());
    }

    @Test
    void zeroStockIsNotReinitialized() {
        when(redis.changeInventory("store-key", 3L, -2)).thenReturn(0L);
        assertEquals(0, change(-2).getRemainingInventory());
        verifyNoInteractions(mapper);
    }

    @Test
    void restorationDoesNotInitializeFullStockAndAddQuantity() {
        when(redis.changeInventory("store-key", 3L, 2)).thenReturn(-2L);
        assertFalse(change(2).isSuccess());
        verifyNoInteractions(mapper);
    }

    private ActivityMzCacheService.InventoryChangeResult change(int quantity) {
        return service.changeGiftInventory(1L, 2L, 3L, quantity, meta);
    }
}
