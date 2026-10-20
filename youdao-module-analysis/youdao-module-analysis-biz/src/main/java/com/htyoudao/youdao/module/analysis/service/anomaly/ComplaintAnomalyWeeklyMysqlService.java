package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.ComplaintRatioRankingRespVO;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.FoodHygieneComplaintRankingRespVO;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.StoreAnomalyWeeklyDO;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 负责 MySQL 周异常结果表的合并写入和排行查询。 */
@Service
@DS("master")
public class ComplaintAnomalyWeeklyMysqlService {

    private static final String WEEKLY_TABLE = "store_anomaly_metrics_weekly";
    private static final String WEEKLY_COLUMNS = "id, business_id, store_id, store_name, stat_year, stat_week, "
            + "weekly_turnover, prev_week_turnover, weekly_avg_order_amount_change_rate, weekly_avg_order_amount, "
            + "prev_week_avg_order_amount, avg_order_amount_change, weekly_repeat_customer_count, "
            + "weekly_order_customer_count, weekly_meal_sales_quantity, prev_week_meal_sales_quantity, "
            + "complaint_order_ratio, complaint_order_count, valid_order_count, "
            + "food_hygiene_complaint_order_count, creator, create_time, updater, update_time, deleted";

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 合并指定项目、指定周的投诉指标。
     *
     * <p>按项目、年、周和门店匹配，已有行即使投诉指标为空也做更新，缺少门店行才插入补全。
     * 只更新投诉相关字段，保留其他经营异常指标；同周重跑不会新增重复记录。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public int mergeWeek(Long businessId, int statYear, int statWeek,
                         Collection<ComplaintMetricClickHouseQueryService.ComplaintMetric> sourceMetrics) {
        Map<Long, StoreAnomalyWeeklyDO> rows = loadCurrentRows(businessId, statYear, statWeek, true).stream()
                .collect(Collectors.toMap(StoreAnomalyWeeklyDO::getStoreId, row -> row,
                        (first, ignored) -> first, LinkedHashMap::new));
        Map<Long, ComplaintMetricClickHouseQueryService.ComplaintMetric> metrics = sourceMetrics.stream()
                .collect(Collectors.toMap(ComplaintMetricClickHouseQueryService.ComplaintMetric::storeId,
                        metric -> metric, (first, ignored) -> first, LinkedHashMap::new));

        LocalDateTime now = LocalDateTime.now();
        for (StoreAnomalyWeeklyDO row : rows.values()) {
            applyComplaintMetric(row, metrics.remove(row.getStoreId()));
            row.setDeleted(isEmptyMetricRow(row));
            row.setUpdater("complaint-anomaly-job");
            row.setUpdateTime(now);
        }
        for (ComplaintMetricClickHouseQueryService.ComplaintMetric metric : metrics.values()) {
            StoreAnomalyWeeklyDO row = new StoreAnomalyWeeklyDO();
            row.setBusinessId(businessId);
            row.setStoreId(metric.storeId());
            row.setStoreName(blankToEmpty(metric.storeName()));
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
        writeRows(businessId, rows.values());
        return rows.size();
    }

    public List<StoreAnomalyWeeklyDO> listActiveByWeek(Long businessId, int statYear, int statWeek) {
        return loadCurrentRows(businessId, statYear, statWeek, false).stream()
                .filter(row -> !Boolean.TRUE.equals(row.getDeleted()))
                .filter(row -> !isEmptyMetricRow(row))
                .filter(row -> !row.hasOnlyNormalComplaintStatistics())
                .toList();
    }

    public List<ComplaintRatioRankingRespVO> queryComplaintRatioRanking(
            Long businessId, Map<Long, String> storeNames,
            Collection<ComplaintAnomalyClickHouseService.WeekKey> weeks,
            ComplaintRankingService.RankType rankType) {
        if (storeNames.isEmpty() || weeks.isEmpty()) {
            return List.of();
        }
        String direction = rankType == ComplaintRankingService.RankType.TOP ? "DESC" : "ASC";
        SqlAndParams scope = rankingScope(businessId, storeNames.keySet(), weeks);
        String sql = "SELECT store_id, SUM(complaint_order_count) complaint_count, "
                + "ROUND(SUM(complaint_order_count) / NULLIF(SUM(valid_order_count), 0) * 100, 2) complaint_ratio "
                + "FROM " + WEEKLY_TABLE + " WHERE " + scope.sql()
                + " AND complaint_order_count IS NOT NULL "
                + "GROUP BY store_id HAVING SUM(complaint_order_count) > 0 AND SUM(valid_order_count) > 0 "
                + "ORDER BY complaint_ratio " + direction + ", complaint_count " + direction
                + ", store_id ASC LIMIT 10";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            long storeId = rs.getLong("store_id");
            ComplaintRatioRankingRespVO vo = new ComplaintRatioRankingRespVO();
            vo.setStoreId(Long.toString(storeId));
            vo.setStoreName(storeNames.get(storeId));
            vo.setComplaintOrderRatio(rs.getBigDecimal("complaint_ratio"));
            vo.setComplaintOrderCount(rs.getLong("complaint_count"));
            return vo;
        }, scope.params().toArray());
    }

    public List<FoodHygieneComplaintRankingRespVO> queryFoodHygieneComplaintRanking(
            Long businessId, Map<Long, String> storeNames,
            Collection<ComplaintAnomalyClickHouseService.WeekKey> weeks,
            ComplaintRankingService.RankType rankType) {
        if (storeNames.isEmpty() || weeks.isEmpty()) {
            return List.of();
        }
        String direction = rankType == ComplaintRankingService.RankType.TOP ? "DESC" : "ASC";
        SqlAndParams scope = rankingScope(businessId, storeNames.keySet(), weeks);
        String sql = "SELECT store_id, SUM(food_hygiene_complaint_order_count) food_count, "
                + "SUM(complaint_order_count) complaint_count FROM " + WEEKLY_TABLE
                + " WHERE " + scope.sql()
                + " AND food_hygiene_complaint_order_count IS NOT NULL "
                + "GROUP BY store_id HAVING SUM(food_hygiene_complaint_order_count) > 0 "
                + "ORDER BY food_count " + direction + ", complaint_count " + direction
                + ", store_id ASC LIMIT 10";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            long storeId = rs.getLong("store_id");
            FoodHygieneComplaintRankingRespVO vo = new FoodHygieneComplaintRankingRespVO();
            vo.setStoreId(Long.toString(storeId));
            vo.setStoreName(storeNames.get(storeId));
            vo.setFoodHygieneComplaintOrderCount(rs.getLong("food_count"));
            vo.setComplaintOrderCount(rs.getLong("complaint_count"));
            return vo;
        }, scope.params().toArray());
    }

    private List<StoreAnomalyWeeklyDO> loadCurrentRows(Long businessId, int statYear, int statWeek,
                                                        boolean forUpdate) {
        String sql = "SELECT " + WEEKLY_COLUMNS + " FROM " + WEEKLY_TABLE
                + " WHERE business_id = ? AND stat_year = ? AND stat_week = ?"
                + (forUpdate ? " FOR UPDATE" : "");
        return jdbcTemplate.query(sql, this::mapWeeklyRow, businessId, statYear, statWeek);
    }

    private void writeRows(Long businessId, Collection<StoreAnomalyWeeklyDO> rows) {
        List<Object[]> updates = rows.stream().filter(row -> row.getId() != null)
                .map(row -> updateParameters(row, businessId)).toList();
        if (!updates.isEmpty()) {
            jdbcTemplate.batchUpdate("UPDATE " + WEEKLY_TABLE
                    + " SET store_name=?, complaint_order_ratio=?, complaint_order_count=?, valid_order_count=?, "
                    + "food_hygiene_complaint_order_count=?, updater=?, update_time=?, deleted=? "
                    + "WHERE id=? AND business_id=?", updates);
        }
        List<Object[]> inserts = rows.stream().filter(row -> row.getId() == null)
                .map(this::insertParameters).toList();
        if (!inserts.isEmpty()) {
            jdbcTemplate.batchUpdate("INSERT INTO " + WEEKLY_TABLE
                    + " (id, business_id, store_id, store_name, stat_year, stat_week, complaint_order_ratio, "
                    + "complaint_order_count, valid_order_count, food_hygiene_complaint_order_count, "
                    + "creator, create_time, updater, update_time, deleted) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    inserts);
        }
    }

    private Object[] updateParameters(StoreAnomalyWeeklyDO row, Long businessId) {
        return new Object[]{blankToEmpty(row.getStoreName()), row.getComplaintOrderRatio(),
                row.getComplaintOrderCount(), row.getValidOrderCount(), row.getFoodHygieneComplaintOrderCount(),
                blankToEmpty(row.getUpdater()), row.getUpdateTime(), Boolean.TRUE.equals(row.getDeleted()) ? 1 : 0,
                row.getId(), businessId};
    }

    private Object[] insertParameters(StoreAnomalyWeeklyDO row) {
        // 与其他周表任务一致，由代码生成主键，不依赖数据库自增。
        return new Object[]{IdWorker.getId(), row.getBusinessId(), row.getStoreId(), blankToEmpty(row.getStoreName()),
                row.getStatYear(), row.getStatWeek(), row.getComplaintOrderRatio(), row.getComplaintOrderCount(),
                row.getValidOrderCount(), row.getFoodHygieneComplaintOrderCount(), blankToEmpty(row.getCreator()),
                row.getCreateTime(), blankToEmpty(row.getUpdater()), row.getUpdateTime(),
                Boolean.TRUE.equals(row.getDeleted()) ? 1 : 0};
    }

    private void applyComplaintMetric(StoreAnomalyWeeklyDO row,
                                      ComplaintMetricClickHouseQueryService.ComplaintMetric metric) {
        if (metric != null && (row.getStoreName() == null || row.getStoreName().isBlank()) && metric.storeName() != null) {
            row.setStoreName(metric.storeName());
        }
        long complaintCount = metric == null ? 0L : metric.complaintCount();
        long validCount = metric == null ? 0L : metric.validOrderCount();
        row.setComplaintOrderCount(complaintCount);
        row.setValidOrderCount(validCount);
        row.setComplaintOrderRatio(validCount == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(complaintCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(validCount), 2, RoundingMode.HALF_UP));
        row.setFoodHygieneComplaintOrderCount(metric == null ? 0L : metric.foodHygieneCount());
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

    private SqlAndParams rankingScope(Long businessId, Collection<Long> storeIds,
                                      Collection<ComplaintAnomalyClickHouseService.WeekKey> weeks) {
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

    private record SqlAndParams(String sql, List<Object> params) {
    }
}
