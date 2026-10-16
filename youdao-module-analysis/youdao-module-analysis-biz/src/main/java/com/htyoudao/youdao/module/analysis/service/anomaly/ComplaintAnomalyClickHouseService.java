package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.ComplaintRatioRankingRespVO;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.FoodHygieneComplaintRankingRespVO;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.StoreAnomalyWeeklyDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants.CH_QUERY_ERROR;
import static com.htyoudao.youdao.module.analysis.enums.OrderStatusConstants.VALID_ORDER_STATES;

/** ClickHouse 投诉周指标计算、查询和版本写入。 */
@Service
@DS("clickhouse")
@Slf4j
public class ComplaintAnomalyClickHouseService {

    private static final String WEEKLY_TABLE = "analytics.store_anomaly_metrics_weekly";
    private static final String COMPLAINT_TABLE = "analytics.system_complaint_ch";
    private static final String ORDER_TABLE = "analytics.bz_order_ch";
    private static final String VALID_ORDER_STATES_SQL = VALID_ORDER_STATES.stream()
            .map(value -> "'" + value + "'")
            .collect(Collectors.joining(","));

    private static final String WEEKLY_COLUMNS = "id, business_id, store_id, store_name, stat_year, stat_week, "
            + "weekly_turnover, prev_week_turnover, weekly_avg_order_amount_change_rate, weekly_avg_order_amount, "
            + "prev_week_avg_order_amount, avg_order_amount_change, weekly_repeat_customer_count, "
            + "weekly_order_customer_count, weekly_meal_sales_quantity, prev_week_meal_sales_quantity, "
            + "complaint_order_ratio, complaint_order_count, valid_order_count, "
            + "food_hygiene_complaint_order_count, creator, create_time, updater, update_time, deleted, version";

    @Resource
    private JdbcTemplate clickHouseJdbc;

    /** 计算指定自然周的投诉指标，并写入新的 ReplacingMergeTree 版本。 */
    public int calculateAndSaveWeek(Long businessId, LocalDate periodStart) {
        WeekFields iso = WeekFields.ISO;
        int statYear = periodStart.get(iso.weekBasedYear());
        int statWeek = periodStart.get(iso.weekOfWeekBasedYear());
        LocalDate periodEnd = periodStart.plusWeeks(1);
        try {
            Map<Long, StoreAnomalyWeeklyDO> rows = loadCurrentRows(businessId, statYear, statWeek).stream()
                    .collect(Collectors.toMap(StoreAnomalyWeeklyDO::getStoreId, row -> row,
                            (first, ignored) -> first, LinkedHashMap::new));
            Map<Long, ComplaintMetric> metrics = loadComplaintMetrics(businessId, periodStart, periodEnd).stream()
                    .collect(Collectors.toMap(ComplaintMetric::storeId, metric -> metric,
                            (first, ignored) -> first));

            LocalDateTime now = LocalDateTime.now();
            long version = System.currentTimeMillis();
            for (StoreAnomalyWeeklyDO row : rows.values()) {
                applyComplaintMetric(row, metrics.remove(row.getStoreId()));
                row.setDeleted(isEmptyMetricRow(row));
                row.setUpdater("complaint-anomaly-job");
                row.setUpdateTime(now);
            }
            for (ComplaintMetric metric : metrics.values()) {
                StoreAnomalyWeeklyDO row = new StoreAnomalyWeeklyDO();
                row.setId(IdWorker.getId());
                row.setBusinessId(businessId);
                row.setStoreId(metric.storeId());
                row.setStoreName(metric.storeName());
                row.setStatYear(statYear);
                row.setStatWeek(statWeek);
                row.setCreator("complaint-anomaly-job");
                row.setUpdater("complaint-anomaly-job");
                row.setCreateTime(now);
                row.setUpdateTime(now);
                applyComplaintMetric(row, metric);
                row.setDeleted(false);
                rows.put(row.getStoreId(), row);
            }
            if (rows.isEmpty()) {
                return 0;
            }
            writeVersions(rows.values(), version);
            return rows.size();
        } catch (DataAccessException e) {
            log.error("[calculateAndSaveWeek] ClickHouse 周投诉指标计算失败, businessId={}, periodStart={}",
                    businessId, periodStart, e);
            throw e;
        }
    }

    /** 给周度报告任务读取 ClickHouse 当前有效版本。 */
    public List<StoreAnomalyWeeklyDO> listActiveByWeek(Long businessId, int statYear, int statWeek) {
        try {
            return loadCurrentRows(businessId, statYear, statWeek).stream()
                    .filter(row -> !Boolean.TRUE.equals(row.getDeleted()))
                    .filter(row -> !isEmptyMetricRow(row))
                    .toList();
        } catch (DataAccessException e) {
            log.error("[listActiveByWeek] ClickHouse 周异常查询失败, businessId={}, year={}, week={}",
                    businessId, statYear, statWeek, e);
            throw exception(CH_QUERY_ERROR);
        }
    }

    public List<ComplaintRatioRankingRespVO> queryComplaintRatioRanking(
            Long businessId, Map<Long, String> storeNames, Collection<WeekKey> weeks,
            ComplaintRankingService.RankType rankType) {
        String direction = rankType == ComplaintRankingService.RankType.TOP ? "DESC" : "ASC";
        SqlAndParams scope = rankingScope(businessId, storeNames.keySet(), weeks);
        String sql = "SELECT store_id, sum(complaint_order_count) complaint_count, "
                + "round(sum(complaint_order_count) / sum(valid_order_count) * 100, 2) complaint_ratio "
                + "FROM " + WEEKLY_TABLE + " FINAL WHERE " + scope.sql()
                + " AND complaint_order_count IS NOT NULL "
                + "GROUP BY store_id HAVING sum(complaint_order_count) > 0 AND sum(valid_order_count) > 0 "
                + "ORDER BY complaint_ratio " + direction + ", complaint_count " + direction
                + ", store_id ASC LIMIT 10";
        try {
            return clickHouseJdbc.query(sql, (rs, rowNum) -> {
                long storeId = rs.getLong("store_id");
                ComplaintRatioRankingRespVO vo = new ComplaintRatioRankingRespVO();
                vo.setStoreId(Long.toString(storeId));
                vo.setStoreName(storeNames.get(storeId));
                vo.setComplaintOrderRatio(rs.getBigDecimal("complaint_ratio"));
                vo.setComplaintOrderCount(rs.getLong("complaint_count"));
                return vo;
            }, scope.params().toArray());
        } catch (DataAccessException e) {
            log.error("[queryComplaintRatioRanking] ClickHouse 查询失败, businessId={}", businessId, e);
            throw exception(CH_QUERY_ERROR);
        }
    }

    public List<FoodHygieneComplaintRankingRespVO> queryFoodHygieneComplaintRanking(
            Long businessId, Map<Long, String> storeNames, Collection<WeekKey> weeks,
            ComplaintRankingService.RankType rankType) {
        String direction = rankType == ComplaintRankingService.RankType.TOP ? "DESC" : "ASC";
        SqlAndParams scope = rankingScope(businessId, storeNames.keySet(), weeks);
        String sql = "SELECT store_id, sum(food_hygiene_complaint_order_count) food_count, "
                + "sum(complaint_order_count) complaint_count FROM " + WEEKLY_TABLE
                + " FINAL WHERE " + scope.sql()
                + " AND food_hygiene_complaint_order_count IS NOT NULL "
                + "GROUP BY store_id HAVING sum(food_hygiene_complaint_order_count) > 0 "
                + "ORDER BY food_count " + direction + ", complaint_count " + direction
                + ", store_id ASC LIMIT 10";
        try {
            return clickHouseJdbc.query(sql, (rs, rowNum) -> {
                long storeId = rs.getLong("store_id");
                FoodHygieneComplaintRankingRespVO vo = new FoodHygieneComplaintRankingRespVO();
                vo.setStoreId(Long.toString(storeId));
                vo.setStoreName(storeNames.get(storeId));
                vo.setFoodHygieneComplaintOrderCount(rs.getLong("food_count"));
                vo.setComplaintOrderCount(rs.getLong("complaint_count"));
                return vo;
            }, scope.params().toArray());
        } catch (DataAccessException e) {
            log.error("[queryFoodHygieneComplaintRanking] ClickHouse 查询失败, businessId={}", businessId, e);
            throw exception(CH_QUERY_ERROR);
        }
    }

    private List<StoreAnomalyWeeklyDO> loadCurrentRows(Long businessId, int statYear, int statWeek) {
        String sql = "SELECT " + WEEKLY_COLUMNS + " FROM " + WEEKLY_TABLE
                + " FINAL WHERE business_id = ? AND stat_year = ? AND stat_week = ?";
        return clickHouseJdbc.query(sql, this::mapWeeklyRow, businessId, statYear, statWeek);
    }

    private List<ComplaintMetric> loadComplaintMetrics(Long businessId, LocalDate start, LocalDate end) {
        String sql = "WITH complaints AS ("
                + " SELECT toUInt64OrZero(toString(store_id)) store_id, "
                + " uniqExactIf(order_sn, notEmpty(ifNull(order_sn, ''))) complaint_count, "
                + " uniqExactIf(order_sn, toString(business_type) = '3' AND notEmpty(ifNull(order_sn, ''))) food_count "
                + " FROM " + COMPLAINT_TABLE + " FINAL"
                + " WHERE toString(business_id) = ? AND toString(deleted) = '0'"
                + " AND create_time >= toDateTime(?) AND create_time < toDateTime(?) GROUP BY store_id"
                + "), orders AS ("
                + " SELECT toUInt64OrZero(store_id) store_id, anyLast(ifNull(store_name, '')) store_name, "
                + " uniqExactIf(order_sn, order_state IN (" + VALID_ORDER_STATES_SQL + ")) valid_count"
                + " FROM " + ORDER_TABLE + " FINAL"
                + " WHERE business_id = ? AND deleted = '0'"
                + " AND create_time >= toDateTime(?) AND create_time < toDateTime(?) GROUP BY store_id"
                + ") SELECT c.store_id, ifNull(o.store_name, '') store_name, c.complaint_count, "
                + "ifNull(o.valid_count, 0) valid_count, c.food_count FROM complaints c"
                + " LEFT JOIN orders o ON o.store_id = c.store_id WHERE c.complaint_count > 0";
        Object[] params = {String.valueOf(businessId), start.toString(), end.toString(),
                String.valueOf(businessId), start.toString(), end.toString()};
        return clickHouseJdbc.query(sql, (rs, rowNum) -> new ComplaintMetric(
                rs.getLong("store_id"), rs.getString("store_name"),
                rs.getLong("complaint_count"), rs.getLong("valid_count"), rs.getLong("food_count")), params);
    }

    private void writeVersions(Collection<StoreAnomalyWeeklyDO> rows, long version) {
        String placeholders = "?,".repeat(25) + "?";
        String sql = "INSERT INTO " + WEEKLY_TABLE + " (" + WEEKLY_COLUMNS + ") VALUES ("
                + placeholders + ")";
        List<Object[]> batch = rows.stream().map(row -> toParameters(row, version)).toList();
        clickHouseJdbc.batchUpdate(sql, batch);
    }

    private Object[] toParameters(StoreAnomalyWeeklyDO row, long version) {
        return new Object[]{row.getId(), row.getBusinessId(), row.getStoreId(), blankToEmpty(row.getStoreName()),
                row.getStatYear(), row.getStatWeek(), row.getWeeklyTurnover(), row.getPrevWeekTurnover(),
                row.getWeeklyAvgOrderAmountChangeRate(), row.getWeeklyAvgOrderAmount(), row.getPrevWeekAvgOrderAmount(),
                row.getAvgOrderAmountChange(), row.getWeeklyRepeatCustomerCount(), row.getWeeklyOrderCustomerCount(),
                row.getWeeklyMealSalesQuantity(), row.getPrevWeekMealSalesQuantity(), row.getComplaintOrderRatio(),
                row.getComplaintOrderCount(), row.getValidOrderCount(), row.getFoodHygieneComplaintOrderCount(),
                blankToEmpty(row.getCreator()), row.getCreateTime(), blankToEmpty(row.getUpdater()), row.getUpdateTime(),
                Boolean.TRUE.equals(row.getDeleted()) ? 1 : 0, version};
    }

    private void applyComplaintMetric(StoreAnomalyWeeklyDO row, ComplaintMetric metric) {
        if (metric == null) {
            row.setComplaintOrderRatio(null);
            row.setComplaintOrderCount(null);
            row.setValidOrderCount(null);
            row.setFoodHygieneComplaintOrderCount(null);
            return;
        }
        if ((row.getStoreName() == null || row.getStoreName().isBlank()) && metric.storeName() != null) {
            row.setStoreName(metric.storeName());
        }
        row.setComplaintOrderCount(metric.complaintCount());
        row.setValidOrderCount(metric.validOrderCount());
        row.setComplaintOrderRatio(metric.validOrderCount() == 0 ? null
                : BigDecimal.valueOf(metric.complaintCount())
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(metric.validOrderCount()), 2, RoundingMode.HALF_UP));
        row.setFoodHygieneComplaintOrderCount(metric.foodHygieneCount() == 0 ? null : metric.foodHygieneCount());
    }

    private boolean isEmptyMetricRow(StoreAnomalyWeeklyDO row) {
        return row.getWeeklyTurnover() == null && row.getPrevWeekTurnover() == null
                && row.getWeeklyAvgOrderAmountChangeRate() == null && row.getWeeklyAvgOrderAmount() == null
                && row.getPrevWeekAvgOrderAmount() == null && row.getAvgOrderAmountChange() == null
                && row.getWeeklyRepeatCustomerCount() == null && row.getWeeklyOrderCustomerCount() == null
                && row.getWeeklyMealSalesQuantity() == null && row.getPrevWeekMealSalesQuantity() == null
                && row.getComplaintOrderRatio() == null && row.getComplaintOrderCount() == null
                && row.getValidOrderCount() == null && row.getFoodHygieneComplaintOrderCount() == null;
    }

    private SqlAndParams rankingScope(Long businessId, Collection<Long> storeIds, Collection<WeekKey> weeks) {
        String storePlaceholders = storeIds.stream().map(ignored -> "?").collect(Collectors.joining(","));
        String weekPlaceholders = weeks.stream().map(ignored -> "(?, ?)").collect(Collectors.joining(","));
        String sql = "business_id = ? AND deleted = 0 AND store_id IN (" + storePlaceholders
                + ") AND (stat_year, stat_week) IN (" + weekPlaceholders + ")";
        List<Object> params = new ArrayList<>();
        params.add(businessId);
        params.addAll(storeIds);
        weeks.forEach(week -> {
            params.add(week.statYear());
            params.add(week.statWeek());
        });
        return new SqlAndParams(sql, params);
    }

    private StoreAnomalyWeeklyDO mapWeeklyRow(ResultSet rs, int rowNum) throws SQLException {
        StoreAnomalyWeeklyDO row = new StoreAnomalyWeeklyDO();
        row.setId(rs.getLong("id"));
        row.setBusinessId(rs.getLong("business_id"));
        row.setStoreId(rs.getLong("store_id"));
        row.setStoreName(rs.getString("store_name"));
        row.setStatYear(rs.getInt("stat_year"));
        row.setStatWeek(rs.getInt("stat_week"));
        row.setWeeklyTurnover(rs.getBigDecimal("weekly_turnover"));
        row.setPrevWeekTurnover(rs.getBigDecimal("prev_week_turnover"));
        row.setWeeklyAvgOrderAmountChangeRate(rs.getBigDecimal("weekly_avg_order_amount_change_rate"));
        row.setWeeklyAvgOrderAmount(rs.getBigDecimal("weekly_avg_order_amount"));
        row.setPrevWeekAvgOrderAmount(rs.getBigDecimal("prev_week_avg_order_amount"));
        row.setAvgOrderAmountChange(rs.getBigDecimal("avg_order_amount_change"));
        row.setWeeklyRepeatCustomerCount(nullableLong(rs, "weekly_repeat_customer_count"));
        row.setWeeklyOrderCustomerCount(nullableLong(rs, "weekly_order_customer_count"));
        row.setWeeklyMealSalesQuantity(nullableLong(rs, "weekly_meal_sales_quantity"));
        row.setPrevWeekMealSalesQuantity(nullableLong(rs, "prev_week_meal_sales_quantity"));
        row.setComplaintOrderRatio(rs.getBigDecimal("complaint_order_ratio"));
        row.setComplaintOrderCount(nullableLong(rs, "complaint_order_count"));
        row.setValidOrderCount(nullableLong(rs, "valid_order_count"));
        row.setFoodHygieneComplaintOrderCount(nullableLong(rs, "food_hygiene_complaint_order_count"));
        row.setCreator(rs.getString("creator"));
        row.setUpdater(rs.getString("updater"));
        if (rs.getTimestamp("create_time") != null) {
            row.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
        }
        if (rs.getTimestamp("update_time") != null) {
            row.setUpdateTime(rs.getTimestamp("update_time").toLocalDateTime());
        }
        row.setDeleted(rs.getInt("deleted") != 0);
        return row;
    }

    private Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }

    public record WeekKey(int statYear, int statWeek) {
    }

    private record ComplaintMetric(long storeId, String storeName, long complaintCount,
                                   long validOrderCount, long foodHygieneCount) {
    }

    private record SqlAndParams(String sql, List<Object> params) {
    }
}
