package com.htyoudao.youdao.module.promotion.service.activityMz;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMz.ActivityMzParticipationDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift.ActivityMzGiftDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift.ActivityMzGiftStockRecordDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMz.ActivityMzParticipationMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMzGift.ActivityMzGiftMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMzGift.ActivityMzGiftStockRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActivityMzDynamicStoreOrderTest {
    @BeforeAll
    static void initTableMetadata() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        assistant.setCurrentNamespace("activityMzTest");
        TableInfoHelper.initTableInfo(assistant, ActivityMzGiftStockRecordDO.class);
        TableInfoHelper.initTableInfo(assistant, ActivityMzParticipationDO.class);
    }

    private final ActivityMzCacheService cache = mock(ActivityMzCacheService.class);
    private final ActivityMzGiftMapper gifts = mock(ActivityMzGiftMapper.class);
    private final ActivityMzGiftStockRecordMapper records = mock(ActivityMzGiftStockRecordMapper.class);
    private final ActivityMzOrderService service = new ActivityMzOrderService();
    private final ActivityMzCacheService.ActivityMzMeta meta = new ActivityMzCacheService.ActivityMzMeta();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "cacheService", cache);
        ReflectionTestUtils.setField(service, "giftMapper", gifts);
        ReflectionTestUtils.setField(service, "stockRecordMapper", records);
        ReflectionTestUtils.setField(service, "participationMapper", mock(ActivityMzParticipationMapper.class));
        meta.setGiftInventoryType(2);
        when(cache.getActivityMetaPublic(1L)).thenReturn(meta);
        when(cache.changeGiftInventory(1L, 2L, 3L, -2, meta))
                .thenReturn(new ActivityMzCacheService.InventoryChangeResult(2, "ok", true));
    }

    @Test
    void dynamicStoreLocksWithoutCreatingGiftRow() {
        assertEquals(2, lock());
        ArgumentCaptor<ActivityMzGiftStockRecordDO> captor = ArgumentCaptor.forClass(ActivityMzGiftStockRecordDO.class);
        verify(records).insert(captor.capture());
        assertNull(captor.getValue().getActivityMzGiftId());
        assertEquals(2L, captor.getValue().getStoreId());
        assertEquals(2, captor.getValue().getQuantity());
        verify(gifts).selectOne(any(Wrapper.class));
        verifyNoMoreInteractions(gifts);
    }

    @Test
    void existingGiftRowStillUpdatesMysqlSummary() {
        ActivityMzGiftDO gift = new ActivityMzGiftDO();
        gift.setId(9L);
        gift.setActivityInventory(30);
        when(gifts.selectOne(any(Wrapper.class))).thenReturn(gift);
        when(gifts.update(isNull(), any(Wrapper.class))).thenReturn(1);
        assertEquals(2, lock());
        verify(gifts).update(isNull(), any(Wrapper.class));
    }

    @Test
    void sharedInventoryStillRequiresItsConfiguration() {
        meta.setGiftInventoryType(1);
        assertEquals(0, lock());
        verify(cache).changeGiftInventory(1L, 2L, 3L, 2, meta);
        verify(records, never()).insert(any(ActivityMzGiftStockRecordDO.class));
    }

    @Test
    void duplicateDynamicLockReturnsPreviouslyLockedQuantityAndCompensatesRedis() {
        ActivityMzGiftStockRecordDO existing = record();
        when(records.selectOne(any(Wrapper.class))).thenReturn(null, existing);
        when(records.insert(any(ActivityMzGiftStockRecordDO.class))).thenThrow(new DuplicateKeyException("duplicate"));
        assertEquals(2, lock());
        verify(cache).changeGiftInventory(1L, 2L, 3L, 2, meta);
        verify(gifts, never()).update(isNull(), any(Wrapper.class));
    }

    @Test
    void paymentConfirmationOnlyUpdatesLedgerForDynamicStore() {
        when(records.selectList(any(Wrapper.class))).thenReturn(List.of(record()));
        when(records.update(isNull(), any(Wrapper.class))).thenReturn(1);
        assertTrue(service.confirm("order"));
        verifyNoInteractions(gifts, cache);
    }

    @Test
    void releaseAndRefundRestoreRedisWithoutUpdatingGiftTable() {
        when(records.selectList(any(Wrapper.class))).thenReturn(List.of(record()));
        when(records.update(isNull(), any(Wrapper.class))).thenReturn(1);
        service.release("order");
        service.refund("order");
        verify(cache, times(2)).changeGiftInventory(1L, 2L, 3L, 2);
        verifyNoInteractions(gifts);
    }

    @Test
    void duplicateReleaseDoesNotRestoreTwice() {
        when(records.selectList(any(Wrapper.class))).thenReturn(List.of(record()));
        when(records.update(isNull(), any(Wrapper.class))).thenReturn(0);
        service.release("order");
        verifyNoInteractions(gifts, cache);
    }

    private int lock() {
        return service.lock(1L, 2L, null, "order", 3L, 4L, 2, false);
    }

    private ActivityMzGiftStockRecordDO record() {
        ActivityMzGiftStockRecordDO record = new ActivityMzGiftStockRecordDO();
        record.setId(8L);
        record.setActivityId(1L);
        record.setStoreId(2L);
        record.setGiftCommodityId(3L);
        record.setQuantity(2);
        return record;
    }
}
