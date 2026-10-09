package com.htyoudao.youdao.module.promotion.service.goodcoupon;

import com.htyoudao.youdao.module.promotion.enums.CouponEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import jakarta.annotation.Resource;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CouponEventServiceImpl implements ICouponEventService{


    @Resource
    private ElasticsearchClient client;

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    // PV/UV 查询超时时间
    private static final String PV_UV_QUERY_TIMEOUT = "60s";

    /**
     * 统计指定条件的 PV（总次数）和 UV（去重用户数）
     * @param eventType 事件类型（对应 eventType 字段，如 activity）
     * @param eventId 事件ID（对应 eventId 字段，如 1961342904355057666）
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @param businessId 业务ID（对应 businessId 字段）
     * @return Map<String, Long> key: "pv"（总次数）、"uv"（去重用户数）
     */
    @Override
    public Map<String, Long> statPvUv(CouponEventType eventType, String eventId,
                                      LocalDateTime startTime, LocalDateTime endTime,
                                      Long businessId) {
        // 结果封装：默认值为 0（避免空指针）
        Map<String, Long> pvUvResult = new HashMap<>();
        pvUvResult.put("pv", 0L);
        pvUvResult.put("uv", 0L);

        try {
            // 1. 构建查询请求（复用原有工具方法，适配目标查询条件）
            SearchRequest searchRequest = buildPvUvSearchRequest(
                    eventType, eventId, startTime, endTime, businessId
            );

            // 2. 执行查询（打印耗时日志，便于排查慢查询）
//            long queryStart = System.currentTimeMillis();
            SearchResponse<Void> response = client.search(searchRequest, Void.class);
//            long queryCost = System.currentTimeMillis() - queryStart;
//            log.info("PV/UV 统计查询完成，耗时：{}ms，时间范围：{} ~ {}",
//                    queryCost, startTime, endTime);

            // 3. 解析 PV（总命中数）
            if (response.hits() != null && response.hits().total() != null) {
                pvUvResult.put("pv", response.hits().total().value());
            }

            // 4. 解析 UV（cardinality 聚合结果）
            if (response.aggregations() != null) {
                Aggregate uvAggregate = response.aggregations().get("uv_stat");
                if (uvAggregate != null && uvAggregate.cardinality() != null) {
                    pvUvResult.put("uv", uvAggregate.cardinality().value());
                }
            }

        } catch (IOException e) {
            log.error("PV/UV 统计查询失败，eventId：{}，时间范围：{} ~ {}",
                    eventId, startTime, endTime, e);
        }

        return pvUvResult;
    }


    /**
     * 构建 PV/UV 统计的查询请求（核心：适配目标 ES 查询语句）
     */
    private SearchRequest buildPvUvSearchRequest(CouponEventType eventType, String eventId,
                                                 LocalDateTime startTime, LocalDateTime endTime,
                                                 Long businessId) {
        // 1. 构建基础查询条件（复用原有 buildBaseQuery 方法，补充 eventId 过滤）
        Query baseQuery = buildPvUvBaseQuery(eventType, eventId, startTime, endTime, businessId);

        // 2. 构建 UV 聚合（cardinality 对 memberId 去重，100w 数据精度阈值设 1w 足够）
        Aggregation uvAggregation = Aggregation.of(a -> a
                .cardinality(c -> c
                        .field("memberId") // 与 ES 中 memberId 字段类型一致（long）
                        .precisionThreshold(40000) // 误差 <1%，平衡性能与精度
                        .missing(0) // 无数据时返回 0，避免空指针
                )
        );

        // 3. 绑定聚合（仅 UV 聚合，PV 由 hits.total 直接获取）
        Map<String, Aggregation> topAggregations = new HashMap<>();
        topAggregations.put("uv_stat", uvAggregation);

        // 4. 解析目标索引（复用原有方法，自动匹配 event_logs_2025-09 这类按月分表的索引）
        List<String> targetIndices = resolveIndexPattern(startTime, endTime);
        // 5. 获取存在的索引
        Map<Boolean, List<String>> indexExistsMap = partitionExistingIndices(targetIndices);
        List<String> existingIndices = indexExistsMap.get(true);

        log.debug("PV/UV 统计查询索引列表：{}", targetIndices);

        // 5. 构建最终查询请求（对应目标 ES 语句）
        return SearchRequest.of(s -> s
                .index(existingIndices) // 目标索引（如 event_logs_2025-09）
                .query(baseQuery) // 基础筛选条件（eventId、eventType、timestamp 等）
                .aggregations(topAggregations) // 绑定 UV 聚合
                .size(0) // 不返回原始文档，提升性能
                .trackTotalHits(t -> t.enabled(true)) // 开启总命中数统计（用于获取 PV）
                .timeout(PV_UV_QUERY_TIMEOUT) // 超时时间（30s）
                // 关键修复：通过 fetch(false) 禁用源数据加载（完全匹配 SourceConfig 类结构）
                .source(builder -> builder.fetch(false))
        );
    }


    /**
     * 构建 PV/UV 统计的基础查询条件（复用原有 buildBaseQuery 逻辑，补充 eventId 过滤）
     * 适配目标条件：eventId=1961342904355057666、eventType=activity、timestamp 时间范围
     */
    private Query buildPvUvBaseQuery(CouponEventType eventType, String eventId,
                                     LocalDateTime startTime, LocalDateTime endTime,
                                     Long businessId) {
        List<Query> mustQueries = new ArrayList<>();

        // 1. 业务ID过滤（复用原有逻辑）
        mustQueries.add(Query.of(q -> q.term(t -> t
                .field("businessId")
                .value(businessId)
        )));

        // 2. 事件类型过滤（复用原有逻辑，与 CouponEventType 枚举的 code 对应）
        mustQueries.add(Query.of(q -> q.term(t -> t
                .field("eventType")
                .value(eventType.getCode())
        )));

        // 3. 事件ID过滤（新增：对应 eventId=1961342904355057666）
        if (eventId != null && !eventId.trim().isEmpty()) {
            mustQueries.add(Query.of(q -> q.term(t -> t
                            .field("eventId")
                            .value(eventId) // 注意：若 ES 中 eventId 是 long 类型，需转 Long 传值
                    // 若 eventId 是 long 类型，替换为：.value(Long.parseLong(eventId))
            )));
        }

        // 4. 时间范围过滤（复用原有逻辑，timestamp 用毫秒时间戳，与 ES 语句一致）
        mustQueries.add(Query.of(q -> q.range(r -> r
                .date(dr -> dr
                        .field("timestamp")
                        .gte(formatDateTime(startTime)) // 开始时间（如 1756627200000）
                        .lte(formatDateTime(endTime)) // 结束时间（如 1759219199000）
                        .format("epoch_millis") // 明确时间格式为毫秒时间戳，避免解析耗时
                )
        )));

        // 组合所有 must 条件（相当于 ES 中的 bool.must）
        return Query.of(q -> q.bool(b -> b.must(mustQueries)));
    }


    // ---------------------- 以下为原有工具方法（保留，无需修改） ----------------------
    private String formatDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            throw new IllegalArgumentException("LocalDateTime不能为null");
        }

        // 转换逻辑正确：东八区时间 → UTC时间 → 时间戳
        ZonedDateTime utcZonedDateTime = localDateTime
                .atZone(ZoneId.of("Asia/Shanghai"))  // 明确输入为东八区时间
                .withZoneSameInstant(UTC_ZONE);      // 转换为UTC时间

        // 返回毫秒级时间戳字符串
        return String.valueOf(utcZonedDateTime.toInstant().toEpochMilli());
    }

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


    /**
     * 仅获取指定条件的 UV（独立用户数）
     * @param eventType 事件类型（对应 eventType 字段，如 activity）
     * @param eventId 事件ID（对应 eventId 字段，如 1961342904355057666）
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @param businessId 业务ID（对应 businessId 字段）
     * @return Long 独立用户数 UV（无数据时返回 0）
     */
    @Override
    public Long statUv(CouponEventType eventType, String eventId,
                       LocalDateTime startTime, LocalDateTime endTime,
                       Long businessId) {
        try {
            // 1. 构建仅获取 UV 的查询请求（复用基础逻辑，精简配置）
            SearchRequest searchRequest = buildUvOnlySearchRequest(
                    eventType, eventId, startTime, endTime, businessId
            );

            // 2. 执行查询（打印耗时日志，便于排查慢查询）
            long queryStart = System.currentTimeMillis();
            SearchResponse<Void> response = client.search(searchRequest, Void.class);
            long queryCost = System.currentTimeMillis() - queryStart;
            log.info("UV 统计查询完成，耗时：{}ms，时间范围：{} ~ {}",
                    queryCost, startTime, endTime);

            // 3. 解析 UV 结果（核心：仅处理 cardinality 聚合）
            return parseUvFromResponse(response);

        } catch (IOException e) {
            log.error("UV 统计查询失败，eventId：{}，时间范围：{} ~ {}",
                    eventId, startTime, endTime, e);
            return 0L; // 异常时返回 0，避免业务中断
        }
    }

    /**
     * 构建仅获取 UV 的查询请求（精简配置：移除 PV 相关项）
     */
    private SearchRequest buildUvOnlySearchRequest(CouponEventType eventType, String eventId,
                                                   LocalDateTime startTime, LocalDateTime endTime,
                                                   Long businessId) {
        // 1. 复用基础查询条件（与 PV/UV 统计共用，确保筛选逻辑一致）
        Query baseQuery = buildPvUvBaseQuery(eventType, eventId, startTime, endTime, businessId);

        // 2. 构建 UV 聚合（与原有逻辑一致，确保去重精度）
        Aggregation uvAggregation = Aggregation.of(a -> a
                .cardinality(c -> c
                        .field("memberId") // 与 ES 中 memberId 字段类型一致（long）
                        .precisionThreshold(40000) // 100w 数据误差 <1%，平衡性能
                        .missing(0) // 无数据时返回 0，避免解析异常
                )
        );

        // 3. 绑定聚合（仅保留 UV 聚合，无其他冗余聚合）
        Map<String, Aggregation> topAggregations = new HashMap<>();
        topAggregations.put("uv_stat", uvAggregation);

        // 4. 解析目标索引（复用原有方法，自动匹配按月分表的索引）
        List<String> targetIndices = resolveIndexPattern(startTime, endTime);
        Map<Boolean, List<String>> indexExistsMap = partitionExistingIndices(targetIndices);
        List<String> existingIndices = indexExistsMap.get(true);
        log.debug("UV 统计查询索引列表：{}", targetIndices);

        // 5. 构建最终请求（精简配置：无 trackTotalHits，仅保留必要项）
        return SearchRequest.of(s -> s
                .index(existingIndices) // 目标索引（如 event_logs_2025-09）
                .query(baseQuery) // 基础筛选条件（eventId、eventType、timestamp 等）
                .aggregations(topAggregations) // 仅绑定 UV 聚合
                .size(0) // 不返回原始文档，提升性能
                .timeout(PV_UV_QUERY_TIMEOUT) // 复用超时配置（60s）
                .source(builder -> builder.fetch(false)) // 禁用源数据加载，减少 IO
        );
    }

    /**
     * 从响应中解析 UV 值（独立方法，便于复用和维护）
     */
    private Long parseUvFromResponse(SearchResponse<Void> response) {
        // 1. 检查聚合结果是否存在
        if (response.aggregations() == null) {
            log.warn("UV 统计响应中无聚合结果，返回 0");
            return 0L;
        }

        // 2. 获取 UV 聚合（与请求中定义的 "uv_stat" 对应）
        Aggregate uvAggregate = response.aggregations().get("uv_stat");
        if (uvAggregate == null) {
            log.warn("UV 聚合结果 key=uv_stat 不存在，返回 0");
            return 0L;
        }

        // 3. 解析 cardinality 聚合值（确保类型匹配）
        if (uvAggregate.cardinality() == null) {
            log.warn("UV 聚合结果非 cardinality 类型，返回 0");
            return 0L;
        }

        // 4. 返回 UV 值（无数据时会返回 0，因聚合中配置了 missing(0)）
        return uvAggregate.cardinality().value();
    }

    /**
     * 仅获取指定条件的 PV（总访问次数）
     * @param eventType 事件类型（对应 eventType 字段，如 activity）
     * @param eventId 事件ID（对应 eventId 字段，如 1961342904355057666）
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     * @param businessId 业务ID（对应 businessId 字段）
     * @return Long 总访问次数 PV（无数据时返回 0）
     */
    @Override
    public Long statPv(CouponEventType eventType, String eventId,
                       LocalDateTime startTime, LocalDateTime endTime,
                       Long businessId) {
        try {
            // 1. 构建仅获取 PV 的查询请求（精简 UV 相关配置）
            SearchRequest searchRequest = buildPvOnlySearchRequest(
                    eventType, eventId, startTime, endTime, businessId
            );

            // 2. 执行查询（打印耗时日志，便于排查慢查询）
            long queryStart = System.currentTimeMillis();
            SearchResponse<Void> response = client.search(searchRequest, Void.class);
            long queryCost = System.currentTimeMillis() - queryStart;
            log.info("PV 统计查询完成，耗时：{}ms，时间范围：{} ~ {}",
                    queryCost, startTime, endTime);

            // 3. 解析 PV 结果（核心：从总命中数获取 PV）
            return parsePvFromResponse(response);

        } catch (IOException e) {
            log.error("PV 统计查询失败，eventId：{}，时间范围：{} ~ {}",
                    eventId, startTime, endTime, e);
            return 0L; // 异常时返回 0，避免业务中断
        }
    }

    /**
     * 构建仅获取 PV 的查询请求（核心：移除 UV 聚合，保留 PV 必要配置）
     */
    private SearchRequest buildPvOnlySearchRequest(CouponEventType eventType, String eventId,
                                                   LocalDateTime startTime, LocalDateTime endTime,
                                                   Long businessId) {
        // 1. 复用基础查询条件（与 UV/PV 统计共用，确保筛选逻辑一致）
        Query baseQuery = buildPvUvBaseQuery(eventType, eventId, startTime, endTime, businessId);

        // 2. 解析目标索引（复用原有方法，自动匹配按月分表的索引，如 event_logs_2025-09）
        List<String> targetIndices = resolveIndexPattern(startTime, endTime);
        log.debug("PV 统计查询索引列表：{}", targetIndices);

        // 3. 构建最终请求（精简配置：无 UV 聚合，仅保留 PV 必要项）
        return SearchRequest.of(s -> s
                .index(targetIndices) // 目标索引
                .query(baseQuery) // 基础筛选条件（eventId、eventType、timestamp 等）
                .size(0) // 不返回原始文档，提升性能
                .trackTotalHits(t -> t.enabled(true)) // 必须开启：通过总命中数获取 PV
                .timeout(PV_UV_QUERY_TIMEOUT) // 复用超时配置（60s）
                .source(builder -> builder.fetch(false)) // 禁用源数据加载，减少 IO 消耗
        );
    }

    /**
     * 从响应中解析 PV 值（独立方法，便于复用和维护）
     */
    private Long parsePvFromResponse(SearchResponse<Void> response) {
        // 1. 检查响应和总命中数元数据是否存在
        if (response == null || response.hits() == null || response.hits().total() == null) {
            log.warn("PV 统计响应中无总命中数数据，返回 0");
            return 0L;
        }

        // 2. 返回总命中数（即 PV 值）：ES 的 hits.total.value 代表符合条件的文档总数
        return response.hits().total().value();
    }

    /**
     * 按天统计 PV/UV 数据（确保时间范围内每日数据完整，含不存在索引的日期）
     */
    @Override
    public Map<String, Map<String, Long>> statDailyPvUv(CouponEventType eventType, String eventId,
                                                        LocalDateTime startTime, LocalDateTime endTime,
                                                        Long businessId) {
        // 1. 生成时间范围内的所有日期（核心：无论索引是否存在，先确保日期全量）
        Map<String, Map<String, Long>> dailyResult = generateFullDateRangeMap(startTime, endTime);

        try {
            // 2. 解析目标索引 + 区分存在/不存在的索引
            List<String> targetIndices = resolveIndexPattern(startTime, endTime);
            Map<Boolean, List<String>> indexExistsMap = partitionExistingIndices(targetIndices);
            List<String> existingIndices = indexExistsMap.get(true);
            List<String> nonExistingIndices = indexExistsMap.get(false);

            // 3. 记录不存在的索引及其对应的日期范围（用于日志和验证）
            if (!nonExistingIndices.isEmpty()) {
                log.warn("以下索引不存在，对应日期将填充 0 值：{}", nonExistingIndices);
                // 无需额外处理，因为 dailyResult 已初始化所有日期为 0
            }

            // 4. 若存在有效索引，执行查询并填充实际数据
            if (!existingIndices.isEmpty()) {
                SearchRequest searchRequest = buildDailyPvUvSearchRequest(
                        eventType, eventId, startTime, endTime, businessId, existingIndices
                );

                long queryStart = System.currentTimeMillis();
                SearchResponse<Void> response = client.search(searchRequest, Void.class);
                long queryCost = System.currentTimeMillis() - queryStart;
                log.info("PV/UV 查询完成，耗时：{}ms，查询索引：{}", queryCost, existingIndices);

                // 用查询结果覆盖对应日期的 0 值
                Map<String, Map<String, Long>> queryResult = parseDailyPvUvFromResponse(response);
                dailyResult.putAll(queryResult);
            }

        } catch (ElasticsearchException e) {
            if (e.getMessage() != null && e.getMessage().contains("index_not_found_exception")) {
                log.error("查询中包含不存在的索引，已自动填充 0 值，异常：{}", e.getMessage());
                // 保留初始的全量日期 0 值，不做修改
            } else {
                log.error("PV/UV 统计异常，返回全量 0 值", e);
                // 异常时仍返回全量日期 0 值，保证结构完整
            }
        } catch (Exception e) {
            log.error("PV/UV 统计失败，返回全量 0 值", e);
            // 任何异常都返回全量日期 0 值
        }

        return dailyResult;
    }

    /**
     * 核心1：生成时间范围内的所有日期，并初始化 PV=0、UV=0
     * 确保即使索引不存在，所有日期都有数据
     */
    private Map<String, Map<String, Long>> generateFullDateRangeMap(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Map<String, Long>> fullDateMap = new LinkedHashMap<>(); // 保持日期顺序
        LocalDate startDate = startTime.atZone(UTC_ZONE).toLocalDate();
        LocalDate endDate = endTime.atZone(UTC_ZONE).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(formatter);
            Map<String, Long> zeroPvUv = new HashMap<>();
            zeroPvUv.put("pv", 0L);
            zeroPvUv.put("uv", 0L);
            fullDateMap.put(dateStr, zeroPvUv);
            currentDate = currentDate.plusDays(1);
        }
        return fullDateMap;
    }

    /**
     * 核心2：区分存在和不存在的索引（返回分区Map）
     */
    private Map<Boolean, List<String>> partitionExistingIndices(List<String> targetIndices) {
        if (targetIndices == null || targetIndices.isEmpty()) {
            return new HashMap<>() {{
                put(true, new ArrayList<>());
                put(false, new ArrayList<>());
            }};
        }

        // 串行检查索引存在性（提高多索引场景的效率）
        return targetIndices.parallelStream().collect(Collectors.partitioningBy(index -> {
            try {
                BooleanResponse exists = client.indices().exists(ExistsRequest.of(req -> req.index(index)));
                return exists.value();
            } catch (Exception e) {
                log.warn("检查索引 {} 存在性失败，视为不存在", index, e);
                return false;
            }
        }));
    }

    /**
     * 构建每日 PV/UV 统计的查询请求（核心：按天分组 + 嵌套UV聚合）
     */
    /**
     * 构建查询请求（使用存在的索引）
     */
    private SearchRequest buildDailyPvUvSearchRequest(CouponEventType eventType, String eventId,
                                                      LocalDateTime startTime, LocalDateTime endTime,
                                                      Long businessId, List<String> existingIndices) {
        // 原有聚合逻辑不变，仅查询存在的索引
        Query baseQuery =  buildPvUvBaseQuery(eventType, eventId, startTime, endTime, businessId);

        CardinalityAggregation uvAggregation = CardinalityAggregation.of(card -> card
                .field("memberId")
                .precisionThreshold(40000)
                .missing(0)
        );
        Aggregation uvSubAggregation = Aggregation.of(aggr -> aggr.cardinality(uvAggregation));

        DateHistogramAggregation dailyHistogram = DateHistogramAggregation.of(dh -> dh
                .field("timestamp")
                .fixedInterval(Time.of(builder -> builder.time("1d")))
                .format("yyyy-MM-dd")
                .timeZone(UTC_ZONE.getId())
                .minDocCount(0)
                .extendedBounds(ebBuilder -> ebBuilder
                        .min(FieldDateMath.of(fdBuilder -> fdBuilder.expr(formatDate(startTime))))
                        .max(FieldDateMath.of(fdBuilder -> fdBuilder.expr(formatDate(endTime))))
                )
        );

        Map<String, Aggregation> subAggregations = new HashMap<>();
        subAggregations.put("daily_uv", uvSubAggregation);

        Aggregation dailyGroupAggregation = Aggregation.of(aggr -> aggr
                .dateHistogram(dailyHistogram)
                .aggregations(subAggregations)
        );

        Map<String, Aggregation> topAggregations = new HashMap<>();
        topAggregations.put("daily_group_by_date", dailyGroupAggregation);

        return SearchRequest.of(req -> req
                .index(existingIndices)
                .query(baseQuery)
                .aggregations(topAggregations)
                .size(0)
                .timeout(PV_UV_QUERY_TIMEOUT)
                .source(builder -> builder.fetch(false))
        );
    }
    // 专门用于格式化日期为 yyyy-MM-dd 的工具方法（替代 substring 方式，避免时间戳截取错误）
    private String formatDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            throw new IllegalArgumentException("LocalDateTime不能为null");
        }

        return localDateTime.atZone(ZoneId.of("Asia/Shanghai"))
                .toLocalDate()  // 直接取东八区的日期
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    /**
     * 解析每日 PV/UV 聚合结果（从响应中提取每日分组的 PV 和 UV）
     */
    private Map<String, Map<String, Long>> parseDailyPvUvFromResponse(SearchResponse<Void> response) {
        Map<String, Map<String, Long>> dailyResult = new HashMap<>();

        // 1. 检查顶级聚合是否存在（daily_group 为按天分组的聚合 key）
        if (response.aggregations() == null || response.aggregations().get("daily_group_by_date") == null) {
            log.warn("每日 PV/UV 统计响应中无按天聚合结果");
            return dailyResult;
        }

        // 2. 获取按天分组的聚合结果（转为 DateHistogramBucket 列表）
        Aggregate dailyGroupAgg = response.aggregations().get("daily_group_by_date");
        List<DateHistogramBucket> dailyBuckets = dailyGroupAgg.dateHistogram().buckets().array();

        // 3. 遍历每日分组，提取 PV 和 UV
        for (DateHistogramBucket bucket : dailyBuckets) {
            // 3.1 提取日期（keyAsString 为 "yyyy-MM-dd" 格式，与聚合配置一致）
            String dateStr = bucket.keyAsString();
            if (dateStr == null || dateStr.trim().isEmpty()) {
                log.warn("每日分组的日期为空，跳过该分组");
                continue;
            }

            // 3.2 提取 PV（每日分组的 doc_count 即当日总次数）
            long dailyPv = bucket.docCount();

            // 3.3 提取 UV（嵌套聚合 daily_uv 的 cardinality 结果）
            long dailyUv = 0L;
            if (bucket.aggregations() != null && bucket.aggregations().get("daily_uv") != null) {
                Aggregate uvAgg = bucket.aggregations().get("daily_uv");
                if (uvAgg.cardinality() != null) {
                    dailyUv = uvAgg.cardinality().value();
                }
            }

            // 3.4 封装当日结果（key=日期，value=PV+UV）
            Map<String, Long> pvUvMap = new HashMap<>();
            pvUvMap.put("pv", dailyPv);
            pvUvMap.put("uv", dailyUv);
            dailyResult.put(dateStr, pvUvMap);
        }

        return dailyResult;
    }

    /**
     * 补充空日期（可选）：确保统计范围内的所有日期都存在，无数据时填充 PV=0、UV=0
     * @param dailyResult 已解析的每日结果
     * @param startTime 统计开始时间
     * @param endTime 统计结束时间
     */
    private void fillEmptyDates(Map<String, Map<String, Long>> dailyResult,
                                LocalDateTime startTime, LocalDateTime endTime) {
        // 1. 转换开始/结束时间为 UTC 时区的 LocalDate（与聚合时区一致）
        LocalDate startDate = startTime.atZone(ZoneId.of("Asia/Shanghai"))
                .toLocalDate();
        LocalDate endDate = endTime.atZone(ZoneId.of("Asia/Shanghai"))
                .toLocalDate();

        // 2. 遍历统计范围内的所有日期，补充空数据
        LocalDate currentDate = startDate;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (!currentDate.isAfter(endDate)) {
            String dateStr = currentDate.format(dateFormatter);
            // 若该日期不存在于结果中，填充默认值
            if (!dailyResult.containsKey(dateStr)) {
                Map<String, Long> emptyPvUv = new HashMap<>();
                emptyPvUv.put("pv", 0L);
                emptyPvUv.put("uv", 0L);
                dailyResult.put(dateStr, emptyPvUv);
            }
            // 日期递增
            currentDate = currentDate.plusDays(1);
        }
    }
}
