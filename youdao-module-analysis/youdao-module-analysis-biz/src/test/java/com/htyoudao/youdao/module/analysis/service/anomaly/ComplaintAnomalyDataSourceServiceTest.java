package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.module.analysis.AnalysisServerApplication;
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
