package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.module.analysis.AnalysisServerApplication;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.StoreAnomalyWeeklyDO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

class ComplaintAnomalyDataSourceServiceTest {

    private AutoCloseable mocks;
    @Mock
    private ComplaintMetricClickHouseQueryService clickHouseQueryService;
    @Mock
    private ComplaintAnomalyWeeklyMysqlService weeklyMysqlService;
    @Mock
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void shouldReadSourceFromClickHouseAndWriteResultToMysql() throws Exception {
        ComplaintAnomalyClickHouseService service = new ComplaintAnomalyClickHouseService();
        ReflectionTestUtils.setField(service, "clickHouseQueryService", clickHouseQueryService);
        ReflectionTestUtils.setField(service, "weeklyMysqlService", weeklyMysqlService);
        LocalDate start = LocalDate.of(2026, 10, 5);
        var metrics = List.of(new ComplaintMetricClickHouseQueryService.ComplaintMetric(1L, "测试门店", 2, 10, 1));
        when(clickHouseQueryService.queryWeek(10L, start, start.plusWeeks(1))).thenReturn(metrics);
        when(weeklyMysqlService.mergeWeek(10L, 2026, 41, metrics)).thenReturn(1);

        assertEquals(1, service.calculateAndSaveWeek(10L, start));
        verify(clickHouseQueryService).queryWeek(10L, start, start.plusWeeks(1));
        verify(weeklyMysqlService).mergeWeek(10L, 2026, 41, metrics);
        assertEquals("clickhouse", ComplaintMetricClickHouseQueryService.class.getAnnotation(DS.class).value());
        assertEquals("master", ComplaintAnomalyWeeklyMysqlService.class.getAnnotation(DS.class).value());
    }

    @Test
    void shouldEnableJdbcTransactionManagerAutoConfiguration() {
        SpringBootApplication application = AnalysisServerApplication.class
                .getAnnotation(SpringBootApplication.class);

        assertFalse(Arrays.asList(application.exclude())
                .contains(DataSourceTransactionManagerAutoConfiguration.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldInsertMysqlRowWithoutClickHouseVersionColumn() throws Exception {
        ComplaintAnomalyWeeklyMysqlService service = new ComplaintAnomalyWeeklyMysqlService();
        ReflectionTestUtils.setField(service, "jdbcTemplate", jdbcTemplate);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class),
                eq(10L), eq(2026), eq(41))).thenReturn(List.of());
        var metric = new ComplaintMetricClickHouseQueryService.ComplaintMetric(1L, "测试门店", 2, 10, 1);

        assertEquals(1, service.mergeWeek(10L, 2026, 41, List.of(metric)));
        verify(jdbcTemplate).batchUpdate(
                org.mockito.ArgumentMatchers.startsWith("INSERT INTO store_anomaly_metrics_weekly"), any(List.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUpdateExistingEmptyMetricRowAndInsertOnlyMissingStore() {
        ComplaintAnomalyWeeklyMysqlService service = new ComplaintAnomalyWeeklyMysqlService();
        ReflectionTestUtils.setField(service, "jdbcTemplate", jdbcTemplate);
        StoreAnomalyWeeklyDO existing = new StoreAnomalyWeeklyDO();
        existing.setId(101L);
        existing.setStoreId(1L);
        existing.setDeleted(true);
        StoreAnomalyWeeklyDO otherMetrics = new StoreAnomalyWeeklyDO();
        otherMetrics.setId(103L);
        otherMetrics.setStoreId(3L);
        otherMetrics.setWeeklyTurnover(new BigDecimal("500.00"));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(10L), eq(2026), eq(41)))
                .thenReturn(List.of(existing, otherMetrics));
        var existingMetric = new ComplaintMetricClickHouseQueryService.ComplaintMetric(1L, "已有门店", 2, 10, 1);
        var missingMetric = new ComplaintMetricClickHouseQueryService.ComplaintMetric(2L, "补全门店", 1, 20, 0);

        var otherMetric = new ComplaintMetricClickHouseQueryService.ComplaintMetric(3L, "已有营业额门店", 1, 10, 0);
        assertEquals(3, service.mergeWeek(10L, 2026, 41, List.of(existingMetric, missingMetric, otherMetric)));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<Object[]>> batches = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate, times(2)).batchUpdate(sql.capture(), batches.capture());
        assertTrue(sql.getAllValues().get(0).startsWith("UPDATE store_anomaly_metrics_weekly"));
        assertFalse(sql.getAllValues().get(0).contains("weekly_turnover="));
        Object[] update = batches.getAllValues().get(0).get(0);
        assertEquals(new BigDecimal("20.00"), update[1]);
        assertEquals(2L, update[2]);
        assertEquals(10L, update[3]);
        assertEquals(0, update[7]);
        assertEquals(101L, update[8]);
        assertEquals(10L, update[9]);
        assertEquals(2, batches.getAllValues().get(0).size());
        assertTrue(sql.getAllValues().get(1).startsWith("INSERT INTO store_anomaly_metrics_weekly"));
        assertEquals(1, batches.getAllValues().get(1).size());
        Object[] insert = batches.getAllValues().get(1).get(0);
        assertEquals(10L, insert[0]);
        assertEquals(2L, insert[1]);
        assertEquals(new BigDecimal("5.00"), insert[5]);
        assertEquals(20L, insert[7]);
        assertEquals(new BigDecimal("500.00"), otherMetrics.getWeeklyTurnover());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCountOnlyCompletedOrdersAsValidOrders() {
        ComplaintMetricClickHouseQueryService service = new ComplaintMetricClickHouseQueryService();
        ReflectionTestUtils.setField(service, "jdbcTemplate", jdbcTemplate);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class))).thenReturn(List.of());

        service.queryWeek(10L, LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 12));

        ArgumentCaptor<String> sql = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(sql.capture(), any(RowMapper.class), any(Object[].class));
        assertTrue(sql.getValue().contains("order_state = '60'"));
        assertFalse(sql.getValue().contains("order_state IN ('20'"));
    }
}
