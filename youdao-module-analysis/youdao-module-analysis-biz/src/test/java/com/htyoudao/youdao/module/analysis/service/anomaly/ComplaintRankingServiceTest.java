package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.UserStoreRelationDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ComplaintRankingServiceTest {

    private ComplaintRankingService service;
    @Mock
    private StoreApi storeApi;
    @Mock
    private ComplaintAnomalyClickHouseService clickHouseService;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        service = new ComplaintRankingService();
        ReflectionTestUtils.setField(service, "storeApi", storeApi);
        ReflectionTestUtils.setField(service, "clickHouseService", clickHouseService);
        BusinessContextHolder.setBusinessId(10L);
    }

    @AfterEach
    void tearDown() throws Exception {
        BusinessContextHolder.clear();
        mocks.close();
    }

    @Test
    void shouldExpandArbitraryRangeToAllTouchedIsoWeeks() {
        UserStoreRelationDTO relation = new UserStoreRelationDTO();
        relation.setUserId(1L);
        relation.setStoreId(101L);
        relation.setStoreName("测试门店");
        when(storeApi.listUserStoreRelations(List.of(1L), null)).thenReturn(List.of(relation));
        when(clickHouseService.queryComplaintRatioRanking(eq(10L), any(), any(),
                eq(ComplaintRankingService.RankType.TOP))).thenReturn(List.of());

        service.getComplaintRatioRanking(1L,
                LocalDate.of(2026, 12, 30), LocalDate.of(2027, 1, 8), "top");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<ComplaintAnomalyClickHouseService.WeekKey>> weeks =
                ArgumentCaptor.forClass(Collection.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<Long, String>> stores = ArgumentCaptor.forClass(Map.class);
        verify(clickHouseService).queryComplaintRatioRanking(eq(10L), stores.capture(), weeks.capture(),
                eq(ComplaintRankingService.RankType.TOP));
        assertEquals(Map.of(101L, "测试门店"), stores.getValue());
        assertEquals(List.of(new ComplaintAnomalyClickHouseService.WeekKey(2026, 53),
                new ComplaintAnomalyClickHouseService.WeekKey(2027, 1)), List.copyOf(weeks.getValue()));
    }

    @Test
    void shouldReturnEmptyWithoutManageableStores() {
        when(storeApi.listUserStoreRelations(List.of(1L), null)).thenReturn(List.of());

        assertEquals(List.of(), service.getFoodHygieneComplaintRanking(1L,
                LocalDate.of(2026, 10, 7), LocalDate.of(2026, 10, 9), "BOTTOM"));
        verifyNoInteractions(clickHouseService);
    }

    @Test
    void shouldRejectMoreThanNinetyInclusiveDays() {
        assertThrows(ServiceException.class, () -> service.getComplaintRatioRanking(1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 1), "TOP"));
        verifyNoInteractions(storeApi, clickHouseService);
    }

    @Test
    void shouldRejectUnsupportedRankType() {
        assertThrows(ServiceException.class, () -> service.getComplaintRatioRanking(1L,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2), "MIDDLE"));
        verifyNoInteractions(storeApi, clickHouseService);
    }
}
