package com.htyoudao.youdao.module.analysis.service.impl;

import static com.htyoudao.youdao.module.analysis.enums.OrderStatusConstants.INVALID_ORDER_STATES;
import static com.htyoudao.youdao.module.analysis.enums.OrderStatusConstants.VALID_ORDER_STATES;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.order.AnalysisOrderVO;
import com.htyoudao.youdao.module.analysis.service.IChOrderAggregationService;
import com.baomidou.dynamic.datasource.annotation.DS;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 订单指标聚合查询 —— ClickHouse 数据源实现
 * <p>
 * 查询表 analytics.bz_order_ch FINAL，
 * FINAL 处理 ReplacingMergeTree 去重，deleted 过滤软删除。
 * 实测 FINAL 直查比 VIEW 层（75列 anyMerge/argMaxMerge）快得多。
 * <p>
 * 注意: CH 表列均为 String 类型，SQL 中所有比较值需使用字符串。
 * <p>
 * 使用条件聚合在单次 SQL 中完成 9 个指标的计算，避免多次查询。
 */
@Slf4j
@DS("clickhouse")
@Service
public class ChOrderAggServiceImpl implements IChOrderAggregationService {

    @Resource
    private JdbcTemplate clickHouseJdbc;

    private static final DateTimeFormatter CH_DT_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ========== 指标 SQL 片段 ==========

    /** 有效订单状态列表: '20','30','40','50','60','80','200' (String 类型需要引号) */
    private static final String VALID_IN =
        VALID_ORDER_STATES.stream().map(v -> "'" + v + "'").collect(Collectors.joining(","));

    /** 无效订单状态列表: '0','70','90' */
    private static final String INVALID_IN =
        INVALID_ORDER_STATES.stream().map(v -> "'" + v + "'").collect(Collectors.joining(","));

    /**
     * 9 个指标的条件聚合列。
     * 注意: CH 表列均为 String 类型，所有比较值必须加单引号。
     */
    private static final String METRICS_SQL =
        "countIf(order_state IN (" + VALID_IN + ")) AS validOrders, "
        + "countIf(order_type IN ('0', '1') AND order_state IN (" + VALID_IN + ")) AS canteenFoodOrders, "
        + "countIf(order_type = '2' AND order_state IN (" + VALID_IN + ")) AS takeawayOrders, "
        + "countIf(order_type = '1' AND order_state IN (" + VALID_IN + ")) AS packOrders, "
        + "countDistinctIf(member_id, order_from IN ('1', '2') AND order_state IN (" + VALID_IN + ")) AS customerCount, "
        + "sumIf(pay_amount, order_state IN (" + VALID_IN + ") AND payment_code IN ('1', '2')) AS payAmount, "
        + "sumIf(pay_amount, order_state IN (" + VALID_IN + ")) AS valid_pay_sum, "
        + "countIf(order_state IN (" + VALID_IN + ")) AS valid_order_cnt, "
        + "countIf(order_state IN (" + INVALID_IN + ")) AS invalidOrders, "
        + "countIf(order_state IN (" + VALID_IN + ") AND payment_code = '0') AS cashPayOrders";

    @Override
    public AnalysisVO<AnalysisOrderVO> queryFromCh(AggregationRequestVO requestVO) {
        Map<String, Double> currentResult = queryMetrics(
            requestVO.getCurrentTimeStart(), requestVO.getCurrentTimeEnd(),
            requestVO.getOrderFrom(), requestVO.getStoreIds());

        Map<String, Double> beforeResult = queryMetrics(
            requestVO.getBeforeTimeStart(), requestVO.getBeforeTimeEnd(),
            requestVO.getOrderFrom(), requestVO.getStoreIds());

        AnalysisOrderVO currentVO = new AnalysisOrderVO(currentResult);
        AnalysisOrderVO beforeVO = new AnalysisOrderVO(beforeResult);
        return new AnalysisVO<>(currentVO, beforeVO);
    }

    /**
     * 单次查询 9 个订单指标。
     * 去重策略: 今天数据加 FINAL，历史数据不加 FINAL（已 merge），UNION ALL 合并后外层聚合。
     */
    private Map<String, Double> queryMetrics(LocalDateTime timeStart, LocalDateTime timeEnd,
                                             Integer orderFrom, List<Long> storeIds) {

        // 构建 WHERE 条件（不含时间范围）
        StringBuilder whereSql = new StringBuilder(256);
        List<Object> whereParams = new ArrayList<>();
        whereSql.append("deleted = '0' AND business_id = ?");
        whereParams.add(String.valueOf(BusinessContextHolder.getBusinessId()));

        if (storeIds != null && !storeIds.isEmpty()) {
            String placeholders = storeIds.stream().map(s -> "?").collect(Collectors.joining(","));
            whereSql.append(" AND store_id IN (").append(placeholders).append(")");
            storeIds.forEach(id -> whereParams.add(String.valueOf(id)));
        }
        if (orderFrom != null && orderFrom >= 0) {
            whereSql.append(" AND order_from = ?");
            whereParams.add(String.valueOf(orderFrom));
        }

        // 今天0点作为分界线
        String todayStart = LocalDateTime.now().toLocalDate().atStartOfDay().format(CH_DT_FMT);
        String tsStart = timeStart != null ? timeStart.format(CH_DT_FMT) : null;
        String tsEnd = timeEnd != null ? timeEnd.format(CH_DT_FMT) : null;

        // 需要的列（用于 UNION ALL 子查询）
        String cols = "order_state, order_type, order_from, member_id, pay_amount, payment_code";

        StringBuilder sql = new StringBuilder(1024);
        sql.append("SELECT ").append(METRICS_SQL);
        sql.append(" FROM (");

        List<Object> params = new ArrayList<>();

        // 今天数据: 加 FINAL 去重
        sql.append("SELECT ").append(cols).append(" FROM analytics.bz_order_ch FINAL WHERE ").append(whereSql);
        params.addAll(whereParams);
        if (tsStart != null) {
            sql.append(" AND create_time >= ?");
            params.add(tsStart);
        }
        sql.append(" AND create_time >= ?");
        params.add(todayStart);
        if (tsEnd != null) {
            sql.append(" AND create_time <= ?");
            params.add(tsEnd);
        }

        sql.append(" UNION ALL ");

        // 历史数据: 不加 FINAL（已 merge，无重复）
        sql.append("SELECT ").append(cols).append(" FROM analytics.bz_order_ch WHERE ").append(whereSql);
        params.addAll(whereParams);
        if (tsStart != null) {
            sql.append(" AND create_time >= ?");
            params.add(tsStart);
        }
        sql.append(" AND create_time < ?");
        params.add(todayStart);
        if (tsEnd != null) {
            sql.append(" AND create_time <= ?");
            params.add(tsEnd);
        }

        sql.append(")");
        sql.append(" SETTINGS optimize_aggregation_in_order = 1");

        log.info("CH orderView SQL: {}", sql);

        return clickHouseJdbc.queryForObject(sql.toString(), (rs, rowNum) -> {
            double validOrders    = rs.getDouble("validOrders");
            double validPaySum    = rs.getDouble("valid_pay_sum");
            double validOrderCnt  = rs.getDouble("valid_order_cnt");
            double averagePayment = validOrderCnt > 0 ? validPaySum / validOrderCnt : 0.0;

            return Map.of(
                "validOrders",       validOrders,
                "canteenFoodOrders", rs.getDouble("canteenFoodOrders"),
                "takeawayOrders",    rs.getDouble("takeawayOrders"),
                "packOrders",        rs.getDouble("packOrders"),
                "customerCount",     rs.getDouble("customerCount"),
                "averagePayment",    averagePayment,
                "payAmount",         rs.getDouble("payAmount"),
                "invalidOrders",     rs.getDouble("invalidOrders"),
                "cashPayOrders",     rs.getDouble("cashPayOrders")
            );
        }, params.toArray());
    }

    /**
     * 构建 FROM 子句，始终使用 FINAL 确保 ReplacingMergeTree 去重准确。
     */
    private void appendFinalAwareFrom(StringBuilder sql, List<Object> params,
            String table, String whereSql, List<Object> whereParams,
            String timeStart, String timeEnd) {

        sql.append(" FROM ").append(table).append(" FINAL");

        if (whereSql != null && !whereSql.isEmpty()) {
            sql.append(" WHERE ").append(whereSql);
            params.addAll(whereParams);
        }

        if (timeStart != null && timeEnd != null) {
            sql.append(" AND create_time >= ? AND create_time <= ?");
            params.add(timeStart);
            params.add(timeEnd);
        }
    }
}
