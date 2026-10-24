package com.htyoudao.youdao.module.system.service.store;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.mq.rabbitmq.service.RabbitMQService;
import com.htyoudao.youdao.framework.redis.core.utils.RedissonUtils;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreSaveReqVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.SystemStoreExpensesVO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreWecomConfigResDTO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreExpensesDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreDeliveryScopeMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreExpensesMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.service.store.cache.StoreCityListCacheService;
import com.htyoudao.youdao.module.system.service.storebackground.StoreBackgroundCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.redisson.api.RedissonClient;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreBossFeeUpdateTest {
    private SystemStoreInfoServiceImpl service;
    private SystemStoreInfoMapper stores;
    private SystemStoreExpensesMapper expenses;

    @BeforeEach
    void setup() {
        service = new SystemStoreInfoServiceImpl();
        stores = mock(SystemStoreInfoMapper.class);
        expenses = mock(SystemStoreExpensesMapper.class);
        ReflectionTestUtils.setField(service, "systemStoreInfoMapper", stores);
        ReflectionTestUtils.setField(service, "systemStoreExpensesMapper", expenses);
        ReflectionTestUtils.setField(service, "systemStoreDeliveryScopeMapper", mock(SystemStoreDeliveryScopeMapper.class));
        ReflectionTestUtils.setField(service, "rabbitMQService", mock(RabbitMQService.class));
        ReflectionTestUtils.setField(service, "storeCityListCacheService", mock(StoreCityListCacheService.class));
        ReflectionTestUtils.setField(service, "storeBackgroundCacheService", mock(StoreBackgroundCacheService.class));
        SystemStoreInfoDO store = new SystemStoreInfoDO();
        store.setStoreId(1L);
        store.setOpenStatus(1);
        when(stores.selectById(1L)).thenReturn(store);
    }

    @Test
    void nullFeesArePersistedAsZeroForEverySubmittedExpenseType() {
        StoreSaveReqVO request = request();
        List<SystemStoreExpensesVO> submitted = new ArrayList<>();
        submitted.add(null);
        for (int type = 0; type <= 3; type++) {
            SystemStoreExpensesVO expense = new SystemStoreExpensesVO();
            expense.setStoreExpensesType(type);
            submitted.add(expense);
        }
        request.setSystemStoreExpensesList(submitted);

        update(request);

        SystemStoreInfoDO saved = savedStore();
        assertEquals(BigDecimal.ZERO, saved.getPackingCharge());
        assertEquals(BigDecimal.ZERO, saved.getDeliveryFee());
        assertEquals(BigDecimal.ZERO, saved.getCampusDeliverySubsidy());
        ArgumentCaptor<SystemStoreExpensesDO> captured = ArgumentCaptor.forClass(SystemStoreExpensesDO.class);
        verify(expenses, times(4)).insert(captured.capture());
        for (SystemStoreExpensesDO expense : captured.getAllValues()) {
            assertEquals(1L, expense.getStoreId());
            assertEquals(BigDecimal.ZERO, expense.getMinimumDeliveryFee());
            assertEquals(BigDecimal.ZERO, expense.getAdditionaaCosts());
        }
    }

    @Test
    void suppliedMoneyAndCalculationModeArePreserved() {
        StoreSaveReqVO request = request();
        request.setPackingCharge(new BigDecimal("1.20"));
        request.setDeliveryFee(new BigDecimal("3.50"));
        request.setCampusDeliverySubsidy(BigDecimal.ONE);
        SystemStoreExpensesVO expense = new SystemStoreExpensesVO();
        expense.setStoreExpensesType(0);
        expense.setMinimumDeliveryFee(new BigDecimal("12.50"));
        expense.setAdditionaaCosts(new BigDecimal("2.80"));
        expense.setStoreCalculationType(1);
        request.setSystemStoreExpensesList(List.of(expense));

        update(request);

        SystemStoreInfoDO saved = savedStore();
        assertEquals(new BigDecimal("1.20"), saved.getPackingCharge());
        assertEquals(new BigDecimal("3.50"), saved.getDeliveryFee());
        assertEquals(BigDecimal.ONE, saved.getCampusDeliverySubsidy());
        ArgumentCaptor<SystemStoreExpensesDO> captured = ArgumentCaptor.forClass(SystemStoreExpensesDO.class);
        verify(expenses).insert(captured.capture());
        assertEquals(expense.getMinimumDeliveryFee(), captured.getValue().getMinimumDeliveryFee());
        assertEquals(expense.getAdditionaaCosts(), captured.getValue().getAdditionaaCosts());
        assertEquals(1, captured.getValue().getStoreCalculationType());
    }

    @Test
    void omittedExpenseListDoesNotCreateOrOverwriteExpenseRows() {
        StoreSaveReqVO request = request();
        request.setSystemStoreExpensesList(null);

        update(request);

        assertEquals(BigDecimal.ZERO, savedStore().getDeliveryFee());
        verifyNoInteractions(expenses);
    }

    @Test
    void subsidyLimitStillRejectsInvalidAmountBeforeWriting() {
        StoreSaveReqVO request = request();
        request.setCampusDeliverySubsidy(new BigDecimal("20.01"));

        assertThrows(ServiceException.class, () -> service.updateStoreByBoss(request));

        verify(stores, never()).updateById(any(SystemStoreInfoDO.class));
        verifyNoInteractions(expenses);
    }

    @Test
    void detailReturnsZeroWhenExpenseRowsAreMissing() {
        assertDetailMoneyZero(service.getStoreById(1L));
    }

    @Test
    void detailReturnsZeroForHistoricalNullAmountsWithoutUpdatingDatabase() {
        when(expenses.selectList(any(Wrapper.class))).thenReturn(List.of(new SystemStoreExpensesDO()));

        assertDetailMoneyZero(service.getStoreById(1L));

        verify(stores, never()).updateById(any(SystemStoreInfoDO.class));
        verify(expenses, never()).insert(any(SystemStoreExpensesDO.class));
    }

    @Test
    void detailPreservesConfiguredAmountsAndCalculationMode() {
        SystemStoreExpensesDO expense = new SystemStoreExpensesDO();
        expense.setMinimumDeliveryFee(new BigDecimal("12.50"));
        expense.setAdditionaaCosts(new BigDecimal("2.80"));
        expense.setStoreCalculationType(1);
        when(expenses.selectList(any(Wrapper.class))).thenReturn(List.of(expense));

        StoreWecomConfigResDTO detail = service.getStoreById(1L);

        assertEquals(new BigDecimal("12.50"), detail.getMinimumDeliveryFee());
        assertEquals(new BigDecimal("12.50"), detail.getMinimumPackageFee());
        assertEquals(new BigDecimal("12.50"), detail.getMinimumDeliveryPackFee());
        assertEquals(new BigDecimal("2.80"), detail.getAdditionaaCosts());
        assertEquals(new BigDecimal("2.80"), detail.getPackCosts());
        assertEquals(new BigDecimal("2.80"), detail.getPackDeliveryCosts());
        assertEquals(1, detail.getStoreCalculationType());
    }

    private void assertDetailMoneyZero(StoreWecomConfigResDTO detail) {
        assertEquals(BigDecimal.ZERO, detail.getAdditionaaCosts());
        assertEquals(BigDecimal.ZERO, detail.getMinimumDeliveryFee());
        assertEquals(BigDecimal.ZERO, detail.getPackCosts());
        assertEquals(BigDecimal.ZERO, detail.getMinimumPackageFee());
        assertEquals(BigDecimal.ZERO, detail.getPackDeliveryCosts());
        assertEquals(BigDecimal.ZERO, detail.getMinimumDeliveryPackFee());
        assertEquals(BigDecimal.ZERO, detail.getCampusDeliverySubsidy());
        assertEquals(BigDecimal.ZERO, detail.getCampusMinimumDeliveryFee());
        assertEquals(BigDecimal.ZERO, detail.getCampusDeliveryFee());
    }

    private StoreSaveReqVO request() {
        StoreSaveReqVO request = new StoreSaveReqVO();
        request.setStoreId(1L);
        request.setStoreStatus(0);
        request.setOpenStatus(0);
        return request;
    }

    private void update(StoreSaveReqVO request) {
        try (var spring = mockStatic(SpringUtil.class)) {
            spring.when(() -> SpringUtil.getBean(RedissonClient.class)).thenReturn(mock(RedissonClient.class));
            try (var ignored = mockStatic(RedissonUtils.class)) {
                assertDoesNotThrow(() -> service.updateStoreByBoss(request));
            }
        }
    }

    private SystemStoreInfoDO savedStore() {
        ArgumentCaptor<SystemStoreInfoDO> captured = ArgumentCaptor.forClass(SystemStoreInfoDO.class);
        verify(stores).updateById(captured.capture());
        return captured.getValue();
    }
}
