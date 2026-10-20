package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.baomidou.dynamic.datasource.annotation.DS;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/** 只负责从 ClickHouse 读取投诉和订单明细并计算周投诉指标。 */
@Service
@DS("clickhouse")
public class ComplaintMetricClickHouseQueryService {

    private static final String COMPLAINT_TABLE = "analytics.system_complaint_ch";
    private static final String ORDER_TABLE = "analytics.bz_order_ch";
    /** 投诉占比的有效订单口径：仅统计已完成订单。 */
    private static final String COMPLETED_ORDER_STATE = "'60'";

    @Resource
    private JdbcTemplate jdbcTemplate;

    /** 汇总指定项目各门店的投诉及有效订单，无投诉的门店也统计订单数。 */
    public List<ComplaintMetric> queryWeek(Long businessId, LocalDate start, LocalDate end) {
        String sql = "WITH complaints AS ("
                + " SELECT toUInt64OrZero(toString(store_id)) store_id, "
                + " uniqExactIf(order_sn, notEmpty(ifNull(order_sn, ''))) complaint_count, "
                + " uniqExactIf(order_sn, toString(business_type) = '3' AND notEmpty(ifNull(order_sn, ''))) food_count "
                + " FROM " + COMPLAINT_TABLE + " FINAL"
                + " WHERE toString(business_id) = ? AND toString(deleted) = '0'"
                + " AND create_time >= toDateTime(?) AND create_time < toDateTime(?) GROUP BY store_id"
                + "), orders AS ("
                + " SELECT toUInt64OrZero(store_id) store_id, anyLast(ifNull(store_name, '')) store_name, "
                + " uniqExactIf(order_sn, order_state = " + COMPLETED_ORDER_STATE
                + " AND notEmpty(ifNull(order_sn, ''))) valid_count"
                + " FROM " + ORDER_TABLE + " FINAL"
                + " WHERE business_id = ? AND deleted = '0'"
                + " AND create_time >= toDateTime(?) AND create_time < toDateTime(?) GROUP BY store_id"
                + "), store_metrics AS ("
                + " SELECT store_id, '' store_name, complaint_count, toUInt64(0) valid_count, food_count FROM complaints"
                + " UNION ALL SELECT store_id, store_name, toUInt64(0) complaint_count, valid_count, "
                + "toUInt64(0) food_count FROM orders)"
                + " SELECT store_id, max(store_name) store_name, sum(complaint_count) complaint_count, "
                + "sum(valid_count) valid_count, sum(food_count) food_count"
                + " FROM store_metrics GROUP BY store_id";
        Object[] params = {String.valueOf(businessId), start.toString(), end.toString(),
                String.valueOf(businessId), start.toString(), end.toString()};
        return jdbcTemplate.query(sql, (rs, rowNum) -> new ComplaintMetric(
                rs.getLong("store_id"), rs.getString("store_name"),
                rs.getLong("complaint_count"), rs.getLong("valid_count"), rs.getLong("food_count")), params);
    }

    public record ComplaintMetric(long storeId, String storeName, long complaintCount,
                                  long validOrderCount, long foodHygieneCount) {
    }
}
