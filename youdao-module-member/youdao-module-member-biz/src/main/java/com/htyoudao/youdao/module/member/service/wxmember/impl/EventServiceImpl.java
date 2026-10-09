package com.htyoudao.youdao.module.member.service.wxmember.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.htyoudao.youdao.module.member.enums.EventType;
import com.htyoudao.youdao.module.member.service.wxmember.IEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.calcite.avatica.util.DateTimeUtils.UTC_ZONE;

@Slf4j
@Service
public class EventServiceImpl implements IEventService {
    @Resource
    private ElasticsearchClient client;
    // Composite聚合分页大小（保留原有配置）
    private static final int COMPOSITE_PAGE_SIZE = 2000;
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    //新增循环上限（防止异常场景死循环，123133/2000≈62页，200页足够）单日
    private static final int MAX_LOOP_COUNT = 20000;

    /**
     * 分页获取memberId，支持超大数据量
     */
    @Override
    public List<Long> queryMemberIDs(EventType eventType, LocalDateTime startTime, LocalDateTime endTime, Long businessId) {
        Set<Long> memberIdSet = new HashSet<>();
        Map<String, FieldValue> compositeAfter = null; // 分页游标
        // 新增循环计数器
        int loopCount = 0;

        try {

            while (true) {
                loopCount++;
                // 死循环保护（超过上限强制终止，避免OOM）
                if (loopCount > MAX_LOOP_COUNT) {
                    log.error("查询循环超过上限{}次，强制终止！当前累计memberId数：{}（可能存在数据丢失）",
                            MAX_LOOP_COUNT, memberIdSet.size());
                    break;
                }

                // 构建查询请求（传入loopCount用于日志）
                SearchRequest searchRequest = buildCompositeSearchRequest(
                        eventType, startTime, endTime, compositeAfter, businessId, loopCount
                );

                // 执行查询（新增耗时日志，排查慢查询）
                long queryStart = System.currentTimeMillis();
                SearchResponse<Object> response = client.search(searchRequest, Object.class);
                long queryCost = System.currentTimeMillis() - queryStart;

                Map<String, Aggregate> aggregations = response.aggregations();
                if (aggregations == null || !aggregations.containsKey("distinct_member_ids")) {
                    break;
                }

                // 解析聚合结果
                CompositeAggregate compositeAgg = aggregations.get("distinct_member_ids").composite();
                List<CompositeBucket> buckets = compositeAgg.buckets().array();

                if (buckets.isEmpty()) {
                    break;
                }

                // 提取memberId
                extractMemberIdsFromComposite(buckets, memberIdSet, loopCount);

                Map<String, FieldValue> nextAfterKey = compositeAgg.afterKey();

                if (nextAfterKey == null) {
                    break;
                }

                compositeAfter = nextAfterKey;
            }

        } catch (Exception e) {
            log.error("查询memberId失败，时间范围：{} ~ {}", startTime, endTime, e);
            return new ArrayList<>(); // 按原有逻辑返回空列表
        }

        return new ArrayList<>(memberIdSet);
    }

    /**
     * 构建包含Composite聚合的查询请求
     */
    private SearchRequest buildCompositeSearchRequest(
            EventType eventType, LocalDateTime startTime, LocalDateTime endTime,
            Map<String, FieldValue> compositeAfter, Long businessId, int loopCount
    ) {
        // 1. 构建基础查询条件（复用原有逻辑）
        Query baseQuery = buildBaseQuery(null, eventType, startTime, endTime, null, businessId);

        // 显式设置聚合valueType（解决字段类型不兼容问题）
        Map<String, CompositeAggregationSource> compositeSources = new HashMap<>();
        compositeSources.put("memberId", CompositeAggregationSource.of(s -> s
                .terms(t -> t
                                .field("memberId")
                                .missingBucket(false) // 排除null值
                                // 关键：根据ES中memberId实际类型选择（二选一！）
                                // 场景1：memberId是long类型（推荐，与原代码isLong()匹配）
                                .valueType(ValueType.Long)
                        // 场景2：memberId是keyword类型（替换上方，需同步修改extract方法）
                        // .valueType(ValueType.KEYWORD)
                )
        ));

        // 构建Composite聚合（游标非空判断，避免无效传递）
        CompositeAggregation compositeAgg = CompositeAggregation.of(c -> {
            c.size(COMPOSITE_PAGE_SIZE);
            c.sources(compositeSources);
            if (compositeAfter != null && !compositeAfter.isEmpty()) {
                c.after(compositeAfter);
//                log.debug("第{}页：传递游标={}", loopCount, convertAfterKeyToString(compositeAfter));
            }
            return c;
        });

        // 配置顶级聚合（保留原有逻辑）
        Map<String, Aggregation> topAggregations = new HashMap<>();
        topAggregations.put("distinct_member_ids", Aggregation.of(a -> a
                .composite(compositeAgg)
        ));

        List<String> targetIndices = resolveIndexPattern(startTime, endTime);
//        log.debug("第{}页：查询索引列表={}", loopCount, targetIndices);

        return SearchRequest.of(s -> s
                .index(targetIndices)
                .query(baseQuery)
                .aggregations(topAggregations)
                .size(0) // 不返回原始文档，提升性能
                .trackTotalHits(t -> t.enabled(false))
                .timeout("120s") // 与ES客户端超时一致
        );
    }

    /**
     * 从Composite聚合结果中提取memberId
     */
    private void extractMemberIdsFromComposite(List<CompositeBucket> buckets, Set<Long> memberIdSet, int loopCount) {
        int successCount = 0; // 成功提取数量
        int skipCount = 0;    // 跳过数量
        int errorCount = 0;   // 解析错误数量

        for (CompositeBucket bucket : buckets) {
            FieldValue memberIdField = bucket.key().get("memberId");
            if (memberIdField == null) {
                skipCount++;
                continue;
            }

            try {
                Long memberId = null;
                if (memberIdField.isLong()) {
                    memberId = memberIdField.longValue();
                } else {
                    skipCount++;
                    log.warn("第{}页：跳过不支持的memberId类型，字段值={}",
                            loopCount, memberIdField);
                    continue;
                }

                // 过滤无效值（如负数，避免脏数据）
                if (memberId < 0) {
                    errorCount++;
                    log.warn("第{}页：跳过无效memberId（非正数），值={}", loopCount, memberId);
                    continue;
                }

                memberIdSet.add(memberId);
                successCount++;

            } catch (Exception e) {
                errorCount++;
                log.error("第{}页：解析memberId失败，字段值={}",
                        loopCount, memberIdField, e);
            }
        }

        // 打印提取统计（定位数据丢失原因）
        /*log.info("第{}页提取统计：总桶数={}，成功={}，跳过={}，解析错误={}",
                loopCount, buckets.size(), successCount, skipCount, errorCount);*/
    }

    private Query buildBaseQuery(List<Long> storeIds, EventType eventType, LocalDateTime startTime,
                                 LocalDateTime endTime, Long productId, Long businessId) {

        List<Query> mustQueries = new ArrayList<>();

        mustQueries.add(Query.of(q -> q.term(t -> t
                .field("businessId")
                .value(businessId)
        )));
        mustQueries.add(Query.of(q -> q.term(t -> t.field("eventType").value(eventType.getCode()))));

        // 时间范围过滤
        mustQueries.add(Query.of(q -> q.range(
                r -> r.date(dr -> dr.field("timestamp").gte(formatDateTime(startTime)).lt(formatDateTime(endTime))))));

        // 商品ID过滤
        if (productId != null) {
            mustQueries.add(Query.of(q -> q.term(t -> t.field("eventId").value(productId))));
        }

        // 门店ID过滤
        if (!CollectionUtils.isEmpty(storeIds)) {
            mustQueries.add(Query.of(q -> q.terms(
                    t -> t.field("storeId").terms(tv -> tv.value(storeIds.stream().map(FieldValue::of).toList())))));
        }

        return Query.of(q -> q.bool(b -> b.must(mustQueries)));
    }

    // 使用定义的UTC_ZONE
    private String formatDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            throw new IllegalArgumentException("LocalDateTime不能为null");
        }
        ZonedDateTime utcZonedDateTime = localDateTime.atZone(ZoneId.of("Asia/Shanghai"))
                .withZoneSameInstant(UTC_ZONE);
        return String.valueOf(utcZonedDateTime.toInstant().toEpochMilli());
    }

    // 使用定义的UTC_ZONE
    private List<String> resolveIndexPattern(LocalDateTime start, LocalDateTime end) {
        LocalDate startDate = start.atZone(UTC_ZONE).toLocalDate();
        LocalDate endDate = end.atZone(UTC_ZONE).toLocalDate();

        if (startDate.getYear() == endDate.getYear() && startDate.getMonth() == endDate.getMonth()) {
            return List.of("event_logs_" + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }

        List<YearMonth> months = new ArrayList<>();
        YearMonth current = YearMonth.from(start.atZone(UTC_ZONE));
        YearMonth last = YearMonth.from(end.atZone(UTC_ZONE));

        while (!current.isAfter(last)) {
            months.add(current);
            current = current.plusMonths(1);
        }

        return months.stream()
                .map(ym -> "event_logs_" + ym.format(DateTimeFormatter.ofPattern("yyyy-MM")))
                .collect(Collectors.toList());
    }

    // 新增辅助方法：将游标转为字符串（便于日志打印）
    private String convertAfterKeyToString(Map<String, FieldValue> afterKey) {
        if (afterKey == null || afterKey.isEmpty()) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, FieldValue> entry : afterKey.entrySet()) {
            FieldValue value = entry.getValue();
            // 根据字段类型拼接字符串
            String valueStr = value.isLong() ? String.valueOf(value.longValue()) :
                                    "unknown_type";
            sb.append(entry.getKey()).append("=").append(valueStr).append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }
}
