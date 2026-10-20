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
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
        LocalDate start = LocalDate.of(2025, 12, 1);
        var metrics = List.of(new ComplaintMetricClickHouseQueryService.ComplaintMetric(1L, "测试门店", 2, 10, 1));
        when(clickHouseQueryService.queryWeek(10L, start, start.plusWeeks(1))).thenReturn(metrics);
        when(weeklyMysqlService.mergeWeek(10L, 2025, 49, metrics)).thenReturn(1);

        assertEquals(1, service.calculateAndSaveWeek(10L, start));
        verify(clickHouseQueryService).queryWeek(10L, start, start.plusWeeks(1));
        verify(weeklyMysqlService).mergeWeek(10L, 2025, 49, metrics);
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
        assertTrue((Long) insert[0] > 0);
        assertEquals(10L, insert[1]);
        assertEquals(2L, insert[2]);
        assertEquals(new BigDecimal("5.00"), insert[6]);
        assertEquals(20L, insert[8]);
        assertEquals(new BigDecimal("500.00"), otherMetrics.getWeeklyTurnover());
    }

    @Test
    void shouldUpdateExistingStoreAndInsertMissingStoreWithoutAutoIncrementId() {
        JdbcTemplate database = newWeeklyDatabase();
        database.update("INSERT INTO store_anomaly_metrics_weekly "
                + "(id,business_id,store_id,store_name,stat_year,stat_week,weekly_turnover,deleted) "
                + "VALUES (101,10,1,'已有门店',2025,49,500,0)");
        ComplaintAnomalyWeeklyMysqlService service = new ComplaintAnomalyWeeklyMysqlService();
        ReflectionTestUtils.setField(service, "jdbcTemplate", database);
        var metrics = List.of(
                new ComplaintMetricClickHouseQueryService.ComplaintMetric(1L, "已有门店", 2, 10, 1),
                new ComplaintMetricClickHouseQueryService.ComplaintMetric(2L, "补全门店", 1, 20, 0));

        assertEquals(2, service.mergeWeek(10L, 2025, 49, metrics));
        assertEquals(2, service.mergeWeek(10L, 2025, 49, metrics));
        assertEquals(2, database.queryForObject("SELECT COUNT(*) FROM store_anomaly_metrics_weekly", Integer.class));
        assertEquals(101L, database.queryForObject(
                "SELECT id FROM store_anomaly_metrics_weekly WHERE store_id=1", Long.class));
        assertEquals(new BigDecimal("500.00"), database.queryForObject(
                "SELECT weekly_turnover FROM store_anomaly_metrics_weekly WHERE store_id=1", BigDecimal.class));
        assertEquals(2L, database.queryForObject(
                "SELECT complaint_order_count FROM store_anomaly_metrics_weekly WHERE store_id=1", Long.class));
        assertTrue(database.queryForObject(
                "SELECT id FROM store_anomaly_metrics_weekly WHERE store_id=2", Long.class) > 0);
        assertEquals(20L, database.queryForObject(
                "SELECT valid_order_count FROM store_anomaly_metrics_weekly WHERE store_id=2", Long.class));
    }

    private JdbcTemplate newWeeklyDatabase() {
        JdbcTemplate database = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
        database.execute("CREATE TABLE store_anomaly_metrics_weekly ("
                + "id BIGINT PRIMARY KEY, business_id BIGINT, store_id BIGINT, store_name VARCHAR(100), "
                + "stat_year INT, stat_week INT, weekly_turnover DECIMAL(12,2), prev_week_turnover DECIMAL(12,2), "
                + "weekly_avg_order_amount_change_rate DECIMAL(12,4), weekly_avg_order_amount DECIMAL(12,2), "
                + "prev_week_avg_order_amount DECIMAL(12,2), avg_order_amount_change DECIMAL(12,2), "
                + "weekly_repeat_customer_count BIGINT, weekly_order_customer_count BIGINT, "
                + "weekly_meal_sales_quantity BIGINT, prev_week_meal_sales_quantity BIGINT, "
                + "complaint_order_ratio DECIMAL(12,4), complaint_order_count BIGINT, valid_order_count BIGINT, "
                + "food_hygiene_complaint_order_count BIGINT, creator VARCHAR(64), create_time TIMESTAMP, "
                + "updater VARCHAR(64), update_time TIMESTAMP, deleted INT, "
                + "UNIQUE(business_id, stat_year, stat_week, store_id))");
        return database;
    }

    @Test
    void shouldCountHealthyOrdersDefaultMissingMetricsToZeroAndAvoidFalseAnomalies() {
        JdbcTemplate database = newWeeklyDatabase();
        database.update("INSERT INTO store_anomaly_metrics_weekly "
                + "(id,business_id,store_id,stat_year,stat_week,weekly_turnover,"
                + "complaint_order_count,valid_order_count,food_hygiene_complaint_order_count,deleted) VALUES "
                + "(101,10,1,2025,49,500,3,30,1,0),(102,10,2,2025,49,800,NULL,NULL,NULL,0),"
                + "(103,10,3,2025,49,NULL,5,50,1,0),(104,11,4,2025,49,NULL,9,90,1,0)");
        ComplaintAnomalyWeeklyMysqlService service = new ComplaintAnomalyWeeklyMysqlService();
        ReflectionTestUtils.setField(service, "jdbcTemplate", database);
        var metrics = List.of(
                new ComplaintMetricClickHouseQueryService.ComplaintMetric(1L, "无投诉门店", 0, 100, 0),
                new ComplaintMetricClickHouseQueryService.ComplaintMetric(5L, "仅有正常订单", 0, 200, 0),
                new ComplaintMetricClickHouseQueryService.ComplaintMetric(6L, "无有效订单的投诉门店", 2, 0, 1));

        assertEquals(5, service.mergeWeek(10L, 2025, 49, metrics));
        assertEquals(5, service.mergeWeek(10L, 2025, 49, metrics));
        assertEquals(5, database.queryForObject(
                "SELECT COUNT(*) FROM store_anomaly_metrics_weekly WHERE business_id=10", Integer.class));
        assertEquals(101L, database.queryForObject(
                "SELECT id FROM store_anomaly_metrics_weekly WHERE store_id=1", Long.class));
        assertEquals(new BigDecimal("500.00"), database.queryForObject(
                "SELECT weekly_turnover FROM store_anomaly_metrics_weekly WHERE store_id=1", BigDecimal.class));
        assertEquals(100L, database.queryForObject(
                "SELECT valid_order_count FROM store_anomaly_metrics_weekly WHERE store_id=1", Long.class));
        assertEquals(200L, database.queryForObject(
                "SELECT valid_order_count FROM store_anomaly_metrics_weekly WHERE store_id=5", Long.class));
        for (long store : List.of(1L, 2L, 3L, 5L)) {
            assertEquals(0L, database.queryForObject(
                    "SELECT complaint_order_count FROM store_anomaly_metrics_weekly WHERE store_id=?", Long.class, store));
            assertEquals(0L, database.queryForObject(
                    "SELECT food_hygiene_complaint_order_count FROM store_anomaly_metrics_weekly WHERE store_id=?", Long.class, store));
            assertEquals(0, database.queryForObject(
                    "SELECT complaint_order_ratio FROM store_anomaly_metrics_weekly WHERE store_id=?", BigDecimal.class, store)
                    .compareTo(BigDecimal.ZERO));
        }
        assertEquals(0L, database.queryForObject(
                "SELECT valid_order_count FROM store_anomaly_metrics_weekly WHERE store_id=2", Long.class));
        assertEquals(9L, database.queryForObject(
                "SELECT complaint_order_count FROM store_anomaly_metrics_weekly WHERE business_id=11", Long.class));
        var active = service.listActiveByWeek(10L, 2025, 49);
        assertEquals(Set.of(1L, 2L, 6L), active.stream().map(StoreAnomalyWeeklyDO::getStoreId)
                .collect(java.util.stream.Collectors.toSet()));
        var normal = new StoreAnomalyWeeklyDO();
        normal.setComplaintOrderCount(0L); normal.setComplaintOrderRatio(BigDecimal.ZERO);
        normal.setFoodHygieneComplaintOrderCount(0L); normal.setValidOrderCount(200L);
        Integer metricCount = ReflectionTestUtils.invokeMethod(new AnomalyReportService(), "countMetricUnion", List.of(normal));
        assertEquals(0, metricCount);
        assertTrue(service.queryComplaintRatioRanking(10L, Map.of(5L, "正常门店", 6L, "投诉门店"),
                List.of(new ComplaintAnomalyClickHouseService.WeekKey(2025, 49)), ComplaintRankingService.RankType.TOP).isEmpty());
        var food = service.queryFoodHygieneComplaintRanking(10L, Map.of(5L, "正常门店", 6L, "投诉门店"),
                List.of(new ComplaintAnomalyClickHouseService.WeekKey(2025, 49)), ComplaintRankingService.RankType.TOP);
        assertEquals(1, food.size());
        assertEquals("6", food.get(0).getStoreId());
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
        assertTrue(sql.getValue().contains("UNION ALL"));
        assertFalse(sql.getValue().contains("WHERE c.complaint_count > 0"));
    }
}
