package com.htyoudao.youdao.framework.sharding.core.alg;


import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Range;
import com.htyoudao.youdao.framework.redis.core.utils.RedissonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Getter
public class SmartTimeShardingAlgorithm implements ComplexKeysShardingAlgorithm<Comparable<?>> {

    // private final ConcurrentMap<String, Set<String>> tableCache = new ConcurrentHashMap<>();
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
    public static final String ORDER_SN = "order_sn";
    public static final String CREATE_TIME = "create_time";

    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, ComplexKeysShardingValue<Comparable<?>> shardingValue) {

        // 获取逻辑表名
        String logicTable = shardingValue.getLogicTableName();

        // Set<String> realTables = tableCache.getOrDefault(logicTable, Collections.emptySet());
        Set<String> realTables = RedissonUtils.getCacheSet("sharding:tables:" + logicTable);
        if (CollUtil.isNotEmpty(realTables)) {
            availableTargetNames = realTables;
        }

        // 1. 优先使用order_sn进行分片（等值查询）
        String yearMonthFromOrderNo = extractYearMonthFromOrderSn(shardingValue);
        if (yearMonthFromOrderNo != null) {
            String tableName = logicTable + "_" + yearMonthFromOrderNo;
            if (availableTargetNames.contains(tableName)) {
                return Collections.singleton(tableName);
            } else {
                return Collections.emptyList();
            }
        }

        // 2. 使用create_time进行分片（等值或范围查询）
        return handleCreateTimeSharding(availableTargetNames, logicTable, shardingValue);
    }

    private String extractYearMonthFromOrderSn(ComplexKeysShardingValue<Comparable<?>> shardingValue) {

        Map<String, Collection<Comparable<?>>> columnValues = shardingValue.getColumnNameAndShardingValuesMap();

        if (columnValues.containsKey(ORDER_SN)) {
            Collection<Comparable<?>> values = columnValues.get(ORDER_SN);
            if (!values.isEmpty()) {
                Object value = values.iterator().next();
                if (value instanceof String orderSn) {
                    // 从订单号中提取年月: ORD202506121303389365003 -> 202506
                    if (orderSn.length() >= 8) {
                        return orderSn.substring(3, 9);
                    }
                }
            }
        }
        return null;
    }

    private Collection<String> handleCreateTimeSharding(Collection<String> availableTargetNames, String logicTable,
                                                        ComplexKeysShardingValue<Comparable<?>> shardingValue) {

        Set<String> targetTables = new LinkedHashSet<>();

        // 处理等值查询
        handleEqualityQueries(availableTargetNames, targetTables, logicTable, shardingValue);

        // 处理范围查询
        handleRangeQueries(availableTargetNames, targetTables, shardingValue);

        // 如果没有匹配，返回所有表（应避免）
        if (targetTables.isEmpty()) {
            return availableTargetNames;
        }

        return targetTables;
    }

    private void handleEqualityQueries(Collection<String> availableTargetNames, Set<String> targetTables, String logicTable,
                                       ComplexKeysShardingValue<Comparable<?>> shardingValue) {

        Map<String, Collection<Comparable<?>>> columnValues = shardingValue.getColumnNameAndShardingValuesMap();

        if (columnValues.containsKey(CREATE_TIME)) {
            for (Comparable<?> value : columnValues.get(CREATE_TIME)) {
                LocalDateTime time = (LocalDateTime) value;
                String table = logicTable + "_" + time.format(MONTH_FORMAT);
                if (availableTargetNames.contains(table)) {
                    targetTables.add(table);
                }
            }
        }
    }

    private void handleRangeQueries(Collection<String> availableTargetNames, Set<String> targetTables, ComplexKeysShardingValue<Comparable<?>> shardingValue) {

        Map<String, Range<Comparable<?>>> rangeValues = shardingValue.getColumnNameAndRangeValuesMap();

        if (rangeValues.containsKey(CREATE_TIME)) {
            Range<Comparable<?>> range = rangeValues.get(CREATE_TIME);

            LocalDateTime start = range.hasLowerBound() ?
                    (LocalDateTime) range.lowerEndpoint() :
                    LocalDateTime.of(2024, 1, 1, 0, 0);

            LocalDateTime end = range.hasUpperBound() ?
                    (LocalDateTime) range.upperEndpoint() :
                    LocalDateTime.now();

            // 遍历范围内的所有月份
            while (!start.isAfter(end)) {
                String tableSuffix = start.format(MONTH_FORMAT);
                availableTargetNames.stream()
                        .filter(t -> t.endsWith(tableSuffix))
                        .findFirst()
                        .ifPresent(targetTables::add);
                start = start.plusMonths(1).withDayOfMonth(1);
            }
        }
    }
}
