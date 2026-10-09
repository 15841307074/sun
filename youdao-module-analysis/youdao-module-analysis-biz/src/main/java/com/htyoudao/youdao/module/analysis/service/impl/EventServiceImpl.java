package com.htyoudao.youdao.module.analysis.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import com.htyoudao.youdao.module.analysis.dal.es.BaseEvent;
import com.htyoudao.youdao.module.analysis.enums.EventType;
import com.htyoudao.youdao.module.analysis.service.IEventService;
import com.htyoudao.youdao.module.analysis.service.dto.EventQueryDTO;
import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventServiceImpl implements IEventService {

    @Resource
    private BulkIngester<BaseEvent> bulkIngester;

    @Resource
    private ElasticsearchClient client;

    @Override
    public void add(BaseEvent baseEvent) {

        String format = DateFormatUtils.format(baseEvent.getTimestamp(), "yyyy-MM");
        String indexName = "event_logs_" + format;
        bulkIngester.add(op -> op.create(idx -> idx.document(baseEvent).index(indexName)));
    }

    @Override
    public Long queryUV(EventQueryDTO queryDTO) {
        LocalDateTime startTime = queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO.getEndTime();
        // 0. 检查索引是否存在，过滤不存在的索引
        List<String> existingIndices = filterExistingIndices(resolveIndexPattern(startTime, endTime));
        if (existingIndices.isEmpty()) {
            return 0L;
        }

        // 1. 构建基础查询
        Query boolQuery = buildBaseQuery(queryDTO);

        // 2. 构建聚合请求
        SearchRequest searchRequest = SearchRequest.of(s -> s.index(existingIndices) // 自动解析索引模式
            .size(0) // 不需要返回原始文档
            .query(boolQuery)
            .aggregations("uv", a -> a.cardinality(c -> c.field("memberId").precisionThreshold(40000) // 精度阈值（根据业务规模调整）
            )));

        // 3. 执行查询
        SearchResponse<Object> response = null;
        try {
            response = client.search(searchRequest, Object.class);
        } catch (Exception e) {
            log.warn("埋点索引查询失败,start:{},end:{}", startTime, endTime, e);
            return 0L;
        }

        // 4. 解析聚合结果
        return extractUVFromResponse(response);
    }

    @Override
    public Long queryPV(EventQueryDTO queryDTO) {
        LocalDateTime startTime = queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO.getEndTime();

        // 0. 检查索引是否存在，过滤不存在的索引
        List<String> existingIndices = filterExistingIndices(resolveIndexPattern(startTime, endTime));
        if (existingIndices.isEmpty()) {
            return 0L;
        }

        // 1. 构建基础查询
        Query boolQuery = buildBaseQuery(queryDTO);

        // 2. 构建聚合请求
        SearchRequest searchRequest = SearchRequest.of(s -> s.index(existingIndices) // 自动解析索引模式
            .size(0) // 不需要返回原始文档
            .query(boolQuery)
            .trackTotalHits(t -> t.enabled(queryDTO.getTrackTotalHits()))
        );

        // 3. 执行查询
        SearchResponse<Object> response = null;
        try {
            response = client.search(searchRequest, Object.class);
            return response != null
                && response.hits() != null
                && response.hits().total() != null
                ? response.hits().total().value()
                : 0L;
        } catch (Exception e) {
            log.warn("埋点索引查询失败,start:{},end:{}", startTime, endTime, e);
            return 0L;
        }
    }
    @Override
    public Map<Long, Long> queryPVByStore(EventQueryDTO queryDTO) {
        Map<Long, Long> result = new HashMap<>();
        if (queryDTO == null || queryDTO.getStoreIds() == null || queryDTO.getStoreIds().isEmpty()) {
            return result;
        }

        Query boolQuery = buildBaseQuery(queryDTO);
        LocalDateTime startTime = queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO.getEndTime();
        Map<String, FieldValue> afterKey = null;

        do {
            SearchRequest searchRequest = buildBatchStorePvRequest(boolQuery, startTime, endTime, afterKey);
            SearchResponse<Object> response;
            try {
                response = client.search(searchRequest, Object.class);
            } catch (Exception e) {
                log.warn("埋点索引按门店批量查询PV失败,start:{},end:{}", startTime, endTime, e);
                return result;
            }

            CompositeAggregate compositeAggregate = response.aggregations().get("store_pv").composite();
            for (CompositeBucket bucket : compositeAggregate.buckets().array()) {
                Long storeId = getLongCompositeValue(bucket.key().get("storeId"));
                if (storeId != null) {
                    result.put(storeId, bucket.docCount());
                }
            }
            afterKey = compositeAggregate.afterKey();
        } while (afterKey != null && !afterKey.isEmpty());

        return result;
    }

    @Override
    public Map<String, Long> queryUVByDay(EventQueryDTO queryDTO) {
        LocalDateTime startTime = queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO.getEndTime();
        // 0. 检查索引是否存在，过滤不存在的索引
        List<String> existingIndices = filterExistingIndices(resolveIndexPattern(startTime, endTime));
        if (existingIndices.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1. 构建基础查询
        Query boolQuery = buildBaseQuery(queryDTO);

        // 2. 构建聚合请求：先按天分组，再计算每天的UV
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(existingIndices) // 自动解析索引模式
                .size(0) // 不需要返回原始文档
                .query(boolQuery)
                .aggregations("by_day", a -> a
                        .dateHistogram(d -> d
                                .field("timestamp") // 时间字段，与buildBaseQuery中一致
                                .calendarInterval(CalendarInterval.Day)
                                .format("yyyy-MM-dd")// 日期格式化
                                .minDocCount(0) // 无数据的日期返回0
                                .extendedBounds(b -> b
                                        // 构建日期边界（使用UTC时区格式化，与索引解析保持一致）
                                        .min(FieldDateMath.of(fdm -> fdm.expr(formatDateForBound(startTime))))
                                        .max(FieldDateMath.of(fdm -> fdm.expr(formatDateForBound(endTime))))
                                )
                        )
                        // 子聚合：计算每天的独立用户数（UV）
                        .aggregations("uv", sub -> sub
                                .cardinality(c -> c
                                        .field("memberId")
                                        .precisionThreshold(40000) // 保持与原UV查询相同的精度阈值
                                )
                        )
                )
        );

        // 3. 执行查询
        SearchResponse<Object> response;
        try {
            response = client.search(searchRequest, Object.class);
        } catch (Exception e) {
            log.warn("埋点索引按天查询UV失败,start:{},end:{}", startTime, endTime, e);
            return Collections.emptyMap();
        }

        // 4. 解析聚合结果
        Map<String, Long> resultMap = new LinkedHashMap<>(); // 保持日期顺序
        if (response == null || response.aggregations() == null) {
            return resultMap;
        }

        // 获取按天聚合的结果
        Aggregate byDayAgg = response.aggregations().get("by_day");
        if (byDayAgg == null) {
            return resultMap;
        }
        // 遍历每天的桶，提取日期和对应的UV值
        byDayAgg.dateHistogram().buckets().array().forEach(bucket -> {
            String dateStr = bucket.keyAsString();
            CardinalityAggregate uvAgg = bucket.aggregations().get("uv").cardinality();
            long uv = uvAgg != null ? uvAgg.value() : 0L;
            resultMap.put(dateStr, uv);
        });

        return resultMap;
    }
    @Override
    public Map<String, Long> queryPVByDay(EventQueryDTO queryDTO) {
        LocalDateTime startTime = queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO.getEndTime();

        // 0. 检查索引是否存在，过滤不存在的索引
        List<String> existingIndices = filterExistingIndices(resolveIndexPattern(startTime, endTime));
        if (existingIndices.isEmpty()) {
            return Collections.emptyMap();
        }

        // 1. 构建基础查询
        Query boolQuery = buildBaseQuery(queryDTO);

        // 2. 构建聚合请求：按天分组统计
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(existingIndices)
                .size(0)
                .query(boolQuery)
                .aggregations("by_day", a -> a
                        .dateHistogram(d -> d
                                .field("timestamp")
                                .calendarInterval(CalendarInterval.Day)// 与buildBaseQuery中一致的时间字段
                                .format("yyyy-MM-dd") // 日期格式化
                                .minDocCount(0) // 无数据的天返回0
                                .extendedBounds(b -> b
                                        // 正确构建FieldDateMath（使用格式化后的日期字符串）
                                        .min(FieldDateMath.of(fdm -> fdm.expr(formatDate(startTime))))
                                        .max(FieldDateMath.of(fdm -> fdm.expr(formatDate(endTime))))
                                )
                        )
                        .aggregations("pv", sub -> sub.valueCount(v -> v.field("timestamp")))
                )
        );

        // 3. 执行查询
        SearchResponse<Object> response;
        try {
            response = client.search(searchRequest, Object.class);
        } catch (Exception e) {
            log.warn("埋点索引按天查询失败,start:{},end:{}", startTime, endTime, e);
            return Collections.emptyMap();
        }

        // 4. 解析聚合结果
        Map<String, Long> resultMap = new LinkedHashMap<>(); // 保持日期顺序
        if (response == null || response.aggregations() == null) {
            return resultMap;
        }

        // 提取按天聚合结果
        Aggregate byDayAgg = response.aggregations().get("by_day");
        if (byDayAgg == null) {
            return resultMap;
        }

       // 遍历每天的桶 - 使用dateHistogram()而不是histogram()
        byDayAgg.dateHistogram().buckets().array().forEach(bucket -> {
            String dateStr = bucket.keyAsString();
            // 提取当天PV值
            ValueCountAggregate pvAgg = bucket.aggregations().get("pv").valueCount();
            long pv = pvAgg != null ? (long) pvAgg.value() : 0L;
            resultMap.put(dateStr, pv);
        });
        return resultMap;
    }
    // 新增辅助方法：格式化日期为yyyy-MM-dd（用于extendedBounds）
    private String formatDate(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }


    private Query buildBaseQuery(EventQueryDTO queryDTO) {

        List<Query> mustQueries = new ArrayList<>();

        // 事件类型必须匹配
        if (queryDTO.getEventType() != null){
            mustQueries.add(Query.of(q -> q.term(t -> t.field("eventType").value(queryDTO.getEventType().getCode()))));
        }

        if (queryDTO.getEventTypes() != null && !queryDTO.getEventTypes().isEmpty()) {
            mustQueries.add(Query.of(q -> q.terms(
                t -> t.field("eventType").terms(tv -> tv.value(queryDTO.getEventTypes().stream().map(EventType::getCode).map(FieldValue::of).toList())))));
        }

        if (queryDTO.getChannelIds() != null && !queryDTO.getChannelIds().isEmpty()) {
            mustQueries.add(Query.of(q -> q.terms(
                t -> t.field("channel").terms(tv -> tv.value(queryDTO.getChannelIds().stream().map(FieldValue::of).toList())))));
        }

        // 时间范围过滤
        mustQueries.add(Query.of(q -> q.range(
            r -> r.date(dr -> dr.field("timestamp").gte(formatDateTime(queryDTO.getStartTime())).lt(formatDateTime(queryDTO.getEndTime()))))));

        if (queryDTO.getEventId() != null) {
            mustQueries.add(Query.of(q -> q.term(t -> t.field("eventId").value(queryDTO.getEventId()))));
        }

        if (queryDTO.getEventIds() != null && !queryDTO.getEventIds().isEmpty()) {
            mustQueries.add(Query.of(q -> q.terms(
                t -> t.field("eventId").terms(tv -> tv.value(queryDTO.getEventIds().stream().map(FieldValue::of).toList())))));
        }

        // 门店ID过滤（如果参数不为空）
        if (queryDTO.getStoreIds() != null && !queryDTO.getStoreIds().isEmpty()) {
            mustQueries.add(Query.of(q -> q.terms(
                t -> t.field("storeId").terms(tv -> tv.value(queryDTO.getStoreIds().stream().map(FieldValue::of).toList())))));
        }



        if (queryDTO.getIsNew() != null){
            if (queryDTO.getIsNew()){
                mustQueries.add(Query.of(q -> q.term(t -> t.field("crowdStatus").value(2))));
            }else {
                mustQueries.add(Query.of(q -> q.bool(b -> b.mustNot(m -> m.term(t -> t.field("crowdStatus").value(2))))));
            }
        }

        return Query.of(q -> q.bool(b -> b.must(mustQueries)));
    }


    // 辅助方法：格式化 LocalDateTime 为 ISO 格式字符串
    private String formatDateTime(LocalDateTime dateTime) {
        ZonedDateTime zoned = dateTime.atZone(ZoneId.of("Asia/Shanghai"));
        return zoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    // 过滤不存在的索引，只返回ES中实际存在的索引
    private List<String> filterExistingIndices(List<String> targetIndices) {
        if (targetIndices == null || targetIndices.isEmpty()) {
            return Collections.emptyList();
        }
        return targetIndices.parallelStream().filter(index -> {
            try {
                return client.indices().exists(ExistsRequest.of(req -> req.index(index))).value();
            } catch (Exception e) {
                log.warn("检查索引 {} 存在性失败，视为不存在", index, e);
                return false;
            }
        }).collect(Collectors.toList());
    }

    // 解析索引模式（示例：event_logs_2023-10）
    private List<String> resolveIndexPattern(LocalDateTime start, LocalDateTime end) {
        LocalDate startDate = start.atZone(ZoneId.of("UTC")).toLocalDate();
        LocalDate endDate = end.atZone(ZoneId.of("UTC")).toLocalDate();

        // 如果时间范围在同一个月内
        if (startDate.getYear() == endDate.getYear() && startDate.getMonth() == endDate.getMonth()) {
            return List.of("event_logs_" + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        ZoneId utc = ZoneId.of("UTC");
        // 计算涉及的所有月份
        List<YearMonth> months = new ArrayList<>();
        YearMonth current = YearMonth.from(start.atZone(utc));
        YearMonth last = YearMonth.from(end.atZone(utc));

        while (!current.isAfter(last)) {
            months.add(current);
            current = current.plusMonths(1);
        }

        // 生成索引模式：event_logs_2023-10,event_logs_2023-11
        return months.stream().map(ym -> "event_logs_" + ym.format(DateTimeFormatter.ofPattern("yyyy-MM"))).toList();
    }

    // 从响应中提取UV值
    private long extractUVFromResponse(SearchResponse<?> response) {
        CardinalityAggregate uvAgg = response.aggregations().get("uv").cardinality();

        // 处理没有匹配数据的情况
        return uvAgg != null ? uvAgg.value() : 0;
    }

    // 辅助方法：格式化日期边界（与索引解析的UTC时区保持一致）
    private String formatDateForBound(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Override
    public Map<String, Long> batchQueryUV(EventQueryDTO queryDTO) {
        Map<String, Long> result = new HashMap<>();
        
        List<String> eventIds = queryDTO.getEventIds();
        if (eventIds == null || eventIds.isEmpty()) {
            return result;
        }
        
        // 遍历每个eventId，使用现有的queryUV方法查询
        for (String eventId : eventIds) {
            EventQueryDTO singleQuery = new EventQueryDTO();
            singleQuery.setEventType(queryDTO.getEventType());
            singleQuery.setStartTime(queryDTO.getStartTime());
            singleQuery.setEndTime(queryDTO.getEndTime());
            singleQuery.setStoreIds(queryDTO.getStoreIds());
            singleQuery.setEventId(eventId);
            
            Long uv = queryUV(singleQuery);
            result.put(eventId, uv);
        }
        
        return result;
    }

    @Override
    public Map<Long, Map<String, Long>> batchQueryUVByStoreAndEvent(EventQueryDTO queryDTO) {
        Map<Long, Map<String, Long>> result = new HashMap<>();
        if (queryDTO.getEventIds() == null || queryDTO.getEventIds().isEmpty()
            || queryDTO.getStoreIds() == null || queryDTO.getStoreIds().isEmpty()) {
            return result;
        }

        Query boolQuery = buildBaseQuery(queryDTO);
        LocalDateTime startTime = queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO.getEndTime();

        // 检查索引是否存在，过滤不存在的索引
        List<String> existingIndices = filterExistingIndices(resolveIndexPattern(startTime, endTime));
        if (existingIndices.isEmpty()) {
            return result;
        }

        Map<String, FieldValue> afterKey = null;

        do {
            SearchRequest searchRequest = buildBatchStoreEventUvRequest(boolQuery, existingIndices, afterKey);
            SearchResponse<Object> response;
            try {
                response = client.search(searchRequest, Object.class);
            } catch (Exception e) {
                log.warn("埋点索引批量查询门店商品UV失败,start:{},end:{}", startTime, endTime, e);
                return result;
            }

            CompositeAggregate compositeAggregate = response.aggregations().get("store_event_uv").composite();
            for (CompositeBucket bucket : compositeAggregate.buckets().array()) {
                Long storeId = getLongCompositeValue(bucket.key().get("storeId"));
                String eventId = getStringCompositeValue(bucket.key().get("eventId"));
                if (storeId == null || StringUtils.isBlank(eventId)) {
                    continue;
                }
                CardinalityAggregate uvAggregate = bucket.aggregations().get("uv").cardinality();
                long uv = uvAggregate != null ? uvAggregate.value() : 0L;
                result.computeIfAbsent(storeId, ignored -> new HashMap<>()).put(eventId, uv);
            }
            afterKey = compositeAggregate.afterKey();
        } while (afterKey != null && !afterKey.isEmpty());

        return result;
    }

    private SearchRequest buildBatchStoreEventUvRequest(Query boolQuery, List<String> indices,
        Map<String, FieldValue> afterKey) {
        List<Map<String, CompositeAggregationSource>> sources = new ArrayList<>();
        sources.add(Map.of("storeId", CompositeAggregationSource.of(cas -> cas.terms(t -> t
            .field("storeId")
            .missingBucket(false)))));
        sources.add(Map.of("eventId", CompositeAggregationSource.of(cas -> cas.terms(t -> t
            .field("eventId")
            .missingBucket(false)))));

        return SearchRequest.of(s -> s.index(indices)
            .size(0)
            .query(boolQuery)
            .aggregations("store_event_uv", a -> a.composite(c -> {
                    var builder = c.sources(sources).size(1000);
                    if (afterKey != null && !afterKey.isEmpty()) {
                        builder.after(afterKey);
                    }
                    return builder;
                })
                .aggregations("uv", sub -> sub.cardinality(card -> card.field("memberId").precisionThreshold(40000)))));
    }

    private SearchRequest buildBatchStorePvRequest(Query boolQuery, LocalDateTime startTime, LocalDateTime endTime,
        Map<String, FieldValue> afterKey) {
        List<Map<String, CompositeAggregationSource>> sources = new ArrayList<>();
        sources.add(Map.of("storeId", CompositeAggregationSource.of(cas -> cas.terms(t -> t
            .field("storeId")
            .missingBucket(false)))));

        return SearchRequest.of(s -> s.index(resolveIndexPattern(startTime, endTime))
            .size(0)
            .query(boolQuery)
            .aggregations("store_pv", a -> a.composite(c -> {
                var builder = c.sources(sources).size(1000);
                if (afterKey != null && !afterKey.isEmpty()) {
                    builder.after(afterKey);
                }
                return builder;
            })));
    }

    private Long getLongCompositeValue(FieldValue fieldValue) {
        if (fieldValue == null) {
            return null;
        }
        if (fieldValue.isLong()) {
            return fieldValue.longValue();
        }
        if (fieldValue.isString()) {
            try {
                return Long.valueOf(fieldValue.stringValue());
            } catch (NumberFormatException e) {
                log.warn("解析 composite storeId 失败, value={}", fieldValue.stringValue());
            }
        }
        return null;
    }

    private String getStringCompositeValue(FieldValue fieldValue) {
        if (fieldValue == null) {
            return null;
        }
        if (fieldValue.isString()) {
            return fieldValue.stringValue();
        }
        if (fieldValue.isLong()) {
            return String.valueOf(fieldValue.longValue());
        }
        return fieldValue.toString();
    }
}
