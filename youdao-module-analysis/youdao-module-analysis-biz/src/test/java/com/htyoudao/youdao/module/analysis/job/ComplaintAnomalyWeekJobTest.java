package com.htyoudao.youdao.module.analysis.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.service.anomaly.ComplaintAnomalyClickHouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class ComplaintAnomalyWeekJobTest {

    private ComplaintAnomalyWeekJob job;
    @Mock
    private ComplaintAnomalyClickHouseService clickHouseService;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        job = new ComplaintAnomalyWeekJob();
        ReflectionTestUtils.setField(job, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(job, "clickHouseService", clickHouseService);
    }

    @AfterEach
    void tearDown() throws Exception {
        BusinessContextHolder.clear();
        mocks.close();
    }

    @Test
    void shouldUsePreviousCompleteWeekByDefault() {
        ComplaintAnomalyWeekJob.JobParam param = job.parseParam(
                "{\"businessId\":10}", LocalDate.of(2026, 10, 16));

        assertEquals(10L, param.businessId());
        assertEquals(LocalDate.of(2026, 10, 5), param.periodStart());
    }

    @Test
    void shouldAcceptMondayForHistoricalRerun() {
        ComplaintAnomalyWeekJob.JobParam param = job.parseParam(
                "{\"businessId\":99,\"periodStart\":\"2026-10-05\"}", LocalDate.now());

        assertEquals(99L, param.businessId());
        assertEquals(LocalDate.of(2026, 10, 5), param.periodStart());
    }

    @Test
    void shouldRejectNonMondayPeriodStart() {
        assertThrows(IllegalArgumentException.class, () -> job.parseParam(
                "{\"businessId\":10,\"periodStart\":\"2026-10-06\"}", LocalDate.now()));
    }

    @Test
    void shouldClearBusinessContextWhenCalculationFails() {
        doThrow(new IllegalStateException("clickhouse error")).when(clickHouseService)
                .calculateAndSaveWeek(10L, LocalDate.of(2026, 10, 5));

        assertThrows(IllegalStateException.class, () -> job.execute(
                "{\"businessId\":10,\"periodStart\":\"2026-10-05\"}", LocalDate.now()));
        assertNull(BusinessContextHolder.getBusinessId());
    }

    @Test
    void shouldReturnWrittenRowsForLocalTestEndpoint() {
        LocalDate periodStart = LocalDate.of(2026, 10, 5);
        when(clickHouseService.calculateAndSaveWeek(10L, periodStart)).thenReturn(12);

        assertEquals(12, job.execute(10L, periodStart));
        assertNull(BusinessContextHolder.getBusinessId());
    }

    @Test
    void shouldCalculatePreviousCompleteWeekForNormalLocalRun() {
        LocalDate today = LocalDate.of(2026, 10, 16);
        LocalDate previousMonday = LocalDate.of(2026, 10, 5);
        when(clickHouseService.calculateAndSaveWeek(10L, previousMonday)).thenReturn(7);

        assertEquals(7, job.executePreviousCompleteWeek(10L, today));
        assertNull(BusinessContextHolder.getBusinessId());
    }
}
