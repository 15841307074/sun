package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.baomidou.dynamic.datasource.annotation.DS;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.module.analysis.enums.OrderStatusConstants.VALID_ORDER_STATES;

/** 只负责从 ClickHouse 读取投诉和订单明细并计算周投诉指标。 */
@Service
@DS("clickhouse")
public class ComplaintMetricClickHouseQueryService {

    private static final String COMPLAINT_TABLE = "analytics.system_complaint_ch";
    private static final String ORDER_TABLE = "analytics.bz_order_ch";
    private static final String VALID_ORDER_STATES_SQL = VALID_ORDER_STATES.stream()
            .map(value -> "'" + value + "'")
            .collect(Collectors.joining(","));

    @Resource
    private JdbcTemplate jdbcTemplate;

    /** 查询指定项目和时间范围内有投诉的门店周指标。 */
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
                + " uniqExactIf(order_sn, order_state IN (" + VALID_ORDER_STATES_SQL + ")) valid_count"
                + " FROM " + ORDER_TABLE + " FINAL"
                + " WHERE business_id = ? AND deleted = '0'"
                + " AND create_time >= toDateTime(?) AND create_time < toDateTime(?) GROUP BY store_id"
                + ") SELECT c.store_id, ifNull(o.store_name, '') store_name, c.complaint_count, "
                + "ifNull(o.valid_count, 0) valid_count, c.food_count FROM complaints c"
                + " LEFT JOIN orders o ON o.store_id = c.store_id WHERE c.complaint_count > 0";
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
