package com.htyoudao.youdao.module.analysis.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad.AdOverviewVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad.AdPositionStatVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad.AdStatVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad.AdTrendPointVO;
import com.htyoudao.youdao.module.analysis.dal.es.AdEvent;
import com.htyoudao.youdao.module.analysis.enums.AdEventType;
import com.htyoudao.youdao.module.analysis.service.IAdEventService;
import com.htyoudao.youdao.module.analysis.service.dto.AdEventQueryDTO;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AdEventServiceImpl implements IAdEventService {

    @Resource
    private ElasticsearchClient client;

    /** 异步客户端：与同步客户端共享同一 transport，懒初始化 */
    private volatile ElasticsearchAsyncClient asyncClient;

    private ElasticsearchAsyncClient asyncClient() {
        ElasticsearchAsyncClient ac = asyncClient;
        if (ac == null) {
            synchronized (this) {
                ac = asyncClient;
                if (ac == null) {
                    ac = new ElasticsearchAsyncClient(client._transport());
                    asyncClient = ac;
                }
            }
        }
        return ac;
    }

    /**
     * 本地缓存：记录已确认存在的索引名称，避免每次写入都调用 ES 检查/创建。
     * 月度索引一旦创建不会删除，因此缓存无需过期机制。
     */
    private final Set<String> confirmedIndices = ConcurrentHashMap.newKeySet();

    /** 累计异步写入失败数 */
    private final AtomicLong totalWriteFailed = new AtomicLong(0);

    /** 失败日志限流：上次输出时间戳，避免 ES 抖动时异步回调大量打日志拖慢 IO 线程 */
    private final AtomicLong lastFailLogTime = new AtomicLong(0);

    @Override
    public void add(AdEvent adEvent) {

        String format = DateFormatUtils.format(adEvent.getTimestamp(), "yyyy-MM");
        String indexName = "ad_event_logs_" + format;
        ensureIndexExists(indexName);
        // 异步写入：HTTP 线程发出请求后立即返回，由 ES 客户端异步完成写入，无队列无消费线程
        asyncClient().index(idx -> idx.index(indexName).document(adEvent))
                .whenComplete((response, ex) -> logWriteFailure(indexName, ex));
    }

    @Override
    public void addAll(List<AdEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        // 批量事件共享同一 timestamp，日期格式化和索引检查只执行一次
        String format = DateFormatUtils.format(events.get(0).getTimestamp(), "yyyy-MM");
        String indexName = "ad_event_logs_" + format;
        ensureIndexExists(indexName);
        // 异步批量写入：一次 bulk 请求，HTTP 线程不等待 ES 返回
        BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
        for (AdEvent event : events) {
            bulkBuilder.operations(op -> op.create(idx -> idx.document(event).index(indexName)));
        }
        asyncClient().bulk(bulkBuilder.build())
                .whenComplete((response, ex) -> {
                    if (ex != null) {
                        logWriteFailure(indexName, ex);
                        return;
                    }
                    if (response.errors()) {
                        logWriteFailure(indexName, null);
                    }
                });
    }

    /**
     * 异步写入失败日志（限流：每秒最多 1 条）
     */
    private void logWriteFailure(String indexName, Throwable ex) {
        totalWriteFailed.incrementAndGet();
        long now = System.currentTimeMillis();
        long last = lastFailLogTime.get();
        if (now - last >= 1000 && lastFailLogTime.compareAndSet(last, now)) {
            log.error("异步写入ES失败，索引={}，累计失败={}", indexName, totalWriteFailed.get(), ex);
        }
    }

    /**
     * 检查索引是否存在，不存在则自动创建（settings/mappings 与 index template 一致）。
     * 使用本地缓存避免每次写入都调用 ES：已确认存在的索引直接跳过。
     * 并发创建时可能抛出 "resource_already_exists_exception"，捕获后忽略。
     */
    private void ensureIndexExists(String indexName) {
        if (confirmedIndices.contains(indexName)) {
            return;
        }
        try {
            boolean exists = client.indices().exists(ExistsRequest.of(req -> req.index(indexName))).value();
            if (exists) {
                confirmedIndices.add(indexName);
                return;
            }
            client.indices().create(CreateIndexRequest.of(c -> c
                .index(indexName)
                .settings(s -> s
                    .numberOfShards("3")
                    .numberOfReplicas("1")
                )
                .mappings(m -> m
                    .properties("from", p -> p.keyword(k -> k))
                    .properties("eventType", p -> p.keyword(k -> k))
                    .properties("timestamp", p -> p.date(d -> d
                        .format("yyyy-MM-dd HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss||strict_date_optional_time||epoch_millis")))
                    .properties("storeId", p -> p.keyword(k -> k))
                    .properties("memberId", p -> p.keyword(k -> k))
                    .properties("businessId", p -> p.keyword(k -> k))
                    .properties("adId", p -> p.keyword(k -> k))
                    .properties("adName", p -> p.keyword(k -> k))
                    .properties("adInfoPosition", p -> p.integer(i -> i))
                    .properties("stayMs", p -> p.long_(l -> l))
                )
            ));
            confirmedIndices.add(indexName);
            log.info("自动创建广告事件索引: {}", indexName);
        } catch (Exception e) {
            // 并发场景下多个线程/节点同时创建索引，忽略 already_exists 异常
            if (e.getMessage() != null && e.getMessage().contains("resource_already_exists_exception")) {
                confirmedIndices.add(indexName);
                log.debug("索引 {} 已存在，跳过创建", indexName);
            } else {
                log.error("自动创建索引 {} 失败", indexName, e);
            }
        }
    }

    @Override
    public AdOverviewVO queryOverview(AdEventQueryDTO queryDTO) {
        LocalDateTime startTime = queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO.getEndTime();
        AdOverviewVO overviewVO = new AdOverviewVO();
        overviewVO.setTotalPv(0L);
        overviewVO.setTotalUv(0L);
        overviewVO.setExposureCount(0L);
        overviewVO.setClickCount(0L);
        overviewVO.setCtr(0.0);
        overviewVO.setAvgStayMs(0.0);

        // 0. 检查索引是否存在，过滤不存在的索引
        List<String> existingIndices = filterExistingIndices(resolveIndexPattern(startTime, endTime));
        if (existingIndices.isEmpty()) {
            return overviewVO;
        }

        // 1. 构建基础查询
        Query boolQuery = buildBaseQuery(queryDTO);

        // 2. 构建聚合请求：一次请求聚合曝光PV/UV、点击PV、平均停留时长
        SearchRequest searchRequest = SearchRequest.of(s -> s.index(existingIndices) // 自动解析索引模式
            .size(0) // 不需要返回原始文档
            .query(boolQuery)
            .aggregations("exposure", a -> a
                // 曝光事件过滤聚合：doc_count即曝光PV
                .filter(f -> f.term(t -> t.field("eventType").value(AdEventType.AD_EXPOSURE.getCode())))
                // 子聚合：基于曝光事件的独立用户数（UV）；memberId为空的访客不计入UV
                .aggregations("uv", sub -> sub
                    .filter(f -> f.bool(b -> b
                        .must(m -> m.exists(e -> e.field("memberId")))
                        .mustNot(mn -> mn.term(t -> t.field("memberId").value("")))))
                    .aggregations("uv_count", uc -> uc.cardinality(c -> c.field("memberId").precisionThreshold(40000)))))
            .aggregations("click", a -> a
                // 点击事件过滤聚合：doc_count即点击PV
                .filter(f -> f.term(t -> t.field("eventType").value(AdEventType.AD_CLICK.getCode()))))
            .aggregations("stay", a -> a
                // 离开事件过滤聚合
                .filter(f -> f.term(t -> t.field("eventType").value(AdEventType.AD_LEAVE.getCode())))
                // 子聚合：平均停留时长
                .aggregations("avg_stay", sub -> sub.avg(av -> av.field("stayMs")))));

        // 3. 执行查询
        SearchResponse<Object> response;
        try {
            response = client.search(searchRequest, Object.class);
        } catch (Exception e) {
            log.warn("广告埋点索引查询概览失败,start:{},end:{}", startTime, endTime, e);
            return overviewVO;
        }

        // 4. 解析聚合结果
        if (response == null || response.aggregations() == null) {
            return overviewVO;
        }

        FilterAggregate exposureAgg = response.aggregations().get("exposure").filter();
        long exposureCount = exposureAgg.docCount();
        // totalPv 只统计曝光事件，与趋势接口的 pv 口径一致
        overviewVO.setTotalPv(exposureCount);
        // uv 为 filter 聚合，memberId 为空的文档被排除，只统计有 memberId 的访客
        CardinalityAggregate uvAgg = exposureAgg.aggregations().get("uv").filter()
                .aggregations().get("uv_count").cardinality();
        overviewVO.setExposureCount(exposureCount);
        overviewVO.setTotalUv(uvAgg != null ? uvAgg.value() : 0L);

        long clickCount = response.aggregations().get("click").filter().docCount();
        overviewVO.setClickCount(clickCount);

        AvgAggregate avgStayAgg = response.aggregations().get("stay").filter().aggregations().get("avg_stay").avg();
        if (avgStayAgg != null && !Double.isNaN(avgStayAgg.value()) && !Double.isInfinite(avgStayAgg.value())) {
            // 保留两位小数
            overviewVO.setAvgStayMs(BigDecimal.valueOf(avgStayAgg.value()).setScale(2, RoundingMode.HALF_UP).doubleValue());
        }

        // 5. 计算点击率（点击量/曝光量，曝光为0返回0）
        overviewVO.setCtr(calcCtr(exposureCount, clickCount));
        return overviewVO;
    }

    @Override
    public List<AdTrendPointVO> queryTrendByDay(AdEventQueryDTO queryDTO) {
        LocalDateTime startTime = queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO.getEndTime();
        // 0. 检查索引是否存在，过滤不存在的索引
        List<String> existingIndices = filterExistingIndices(resolveIndexPattern(startTime, endTime));
        if (existingIndices.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 构建基础查询
        Query boolQuery = buildBaseQuery(queryDTO);

        // 2. 构建聚合请求：按天分组，再嵌套曝光PV/UV、点击PV
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(existingIndices) // 自动解析索引模式
                .size(0) // 不需要返回原始文档
                .query(boolQuery)
                .aggregations("by_day", a -> a
                        .dateHistogram(d -> d
                                .field("timestamp") // 时间字段，与buildBaseQuery中一致
                                .calendarInterval(CalendarInterval.Day)
                                // 按上海时区分桶，与buildBaseQuery的时间范围口径一致；
                                // 不设时区时ES默认用UTC分桶，上海时间0点~8点的数据会落到前一天的桶里
                                .timeZone("Asia/Shanghai")
                                .format("yyyy-MM-dd")// 日期格式化
                                .minDocCount(0) // 无数据的日期返回0
                                .extendedBounds(b -> b
                                        // 构建日期边界（与分桶时区保持一致，用上海时区格式化）
                                        .min(FieldDateMath.of(fdm -> fdm.expr(formatDateForBound(startTime))))
                                        .max(FieldDateMath.of(fdm -> fdm.expr(formatDateForBound(endTime))))
                                )
                        )
                        // 子聚合：当天曝光事件，doc_count即曝光PV，嵌套曝光UV
                        .aggregations("exposure", sub -> sub
                                .filter(f -> f.term(t -> t.field("eventType").value(AdEventType.AD_EXPOSURE.getCode())))
                                // memberId为空的访客不计入UV：仅统计memberId存在且非空字符串的文档
                                .aggregations("uv", u -> u
                                        .filter(f -> f.bool(b -> b
                                                .must(m -> m.exists(e -> e.field("memberId")))
                                                .mustNot(mn -> mn.term(t -> t.field("memberId").value("")))))
                                        .aggregations("uv_count", uc -> uc.cardinality(c -> c.field("memberId").precisionThreshold(40000))))
                        )
                        // 子聚合：当天点击事件，doc_count即点击PV
                        .aggregations("click", sub -> sub
                                .filter(f -> f.term(t -> t.field("eventType").value(AdEventType.AD_CLICK.getCode())))
                        )
        ));

        // 3. 执行查询
        SearchResponse<Object> response;
        try {
            response = client.search(searchRequest, Object.class);
        } catch (Exception e) {
            log.warn("广告埋点索引按天查询趋势失败,start:{},end:{}", startTime, endTime, e);
            return Collections.emptyList();
        }

        // 4. 解析聚合结果
        List<AdTrendPointVO> resultList = new ArrayList<>();
        if (response == null || response.aggregations() == null) {
            return resultList;
        }
        Aggregate byDayAgg = response.aggregations().get("by_day");
        if (byDayAgg == null) {
            return resultList;
        }
        // 遍历每天的桶，提取曝光PV/UV、点击PV，点击率在Java侧计算
        for (DateHistogramBucket bucket : byDayAgg.dateHistogram().buckets().array()) {
            // 空桶可能缺少子聚合，各指标分别判空独立取值：为空的记0，有值的正常取值
            Aggregate exposure = bucket.aggregations().get("exposure");
            Aggregate click = bucket.aggregations().get("click");
            long pv = 0L;
            long uv = 0L;
            if (exposure != null) {
                FilterAggregate exposureAgg = exposure.filter();
                pv = exposureAgg.docCount();
                // uv 为 filter 聚合，memberId 为空的文档被排除，只统计有 memberId 的访客；uv 子聚合理论上也可能缺失，一并判空
                Aggregate uvFilter = exposureAgg.aggregations().get("uv");
                if (uvFilter != null) {
                    CardinalityAggregate uvAgg = uvFilter.filter()
                            .aggregations().get("uv_count").cardinality();
                    uv = uvAgg != null ? uvAgg.value() : 0L;
                }
            }
            long clicks = click != null ? click.filter().docCount() : 0L;

            AdTrendPointVO pointVO = new AdTrendPointVO();
            pointVO.setDate(bucket.keyAsString());
            pointVO.setPv(pv);
            pointVO.setUv(uv);
            pointVO.setClicks(clicks);
            pointVO.setCtr(calcCtr(pv, clicks));
            resultList.add(pointVO);
        }
        return resultList;
    }

    @Override
    public List<AdPositionStatVO> queryByPosition(AdEventQueryDTO queryDTO) {
        LocalDateTime startTime = queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO.getEndTime();
        // 0. 检查索引是否存在，过滤不存在的索引
        List<String> existingIndices = filterExistingIndices(resolveIndexPattern(startTime, endTime));
        if (existingIndices.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 构建基础查询
        Query boolQuery = buildBaseQuery(queryDTO);

        // 2. 构建聚合请求：按广告位分组，嵌套曝光PV/UV、点击PV
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(existingIndices)
                .size(0)
                .query(boolQuery)
                .aggregations("by_position", a -> a
                        .terms(t -> t.field("adInfoPosition").size(100)) // 广告位数量有限，size给足
                        // 子聚合：该广告位曝光事件，doc_count即曝光PV，嵌套曝光UV
                        .aggregations("exposure", sub -> sub
                                .filter(f -> f.term(t -> t.field("eventType").value(AdEventType.AD_EXPOSURE.getCode())))
                                // memberId为空的访客不计入UV：仅统计memberId存在且非空字符串的文档
                                .aggregations("uv", u -> u
                                        .filter(f -> f.bool(b -> b
                                                .must(m -> m.exists(e -> e.field("memberId")))
                                                .mustNot(mn -> mn.term(t -> t.field("memberId").value("")))))
                                        .aggregations("uv_count", uc -> uc.cardinality(c -> c.field("memberId").precisionThreshold(40000)))))
                        )
                        // 子聚合：该广告位点击事件，doc_count即点击PV
                        .aggregations("click", sub -> sub
                                .filter(f -> f.term(t -> t.field("eventType").value(AdEventType.AD_CLICK.getCode())))
                        )
        );

        // 3. 执行查询
        SearchResponse<Object> response;
        try {
            response = client.search(searchRequest, Object.class);
        } catch (Exception e) {
            log.warn("广告埋点索引按广告位查询失败,start:{},end:{}", startTime, endTime, e);
            return Collections.emptyList();
        }

        // 4. 解析聚合结果
        List<AdPositionStatVO> resultList = new ArrayList<>();
        if (response == null || response.aggregations() == null) {
            return resultList;
        }
        Aggregate byPositionAgg = response.aggregations().get("by_position");
        if (byPositionAgg == null) {
            return resultList;
        }
        try {
            // 数值字段terms聚合返回lterms，字符串字段返回sterms，双分支解析
            if (byPositionAgg.isLterms()) {
                for (LongTermsBucket bucket : byPositionAgg.lterms().buckets().array()) {
                    addPositionStat(resultList, (int) bucket.key(), bucket.aggregations());
                }
            } else if (byPositionAgg.isSterms()) {
                for (StringTermsBucket bucket : byPositionAgg.sterms().buckets().array()) {
                    Integer position = getIntegerFieldValue(bucket.key());
                    if (position == null) {
                        continue;
                    }
                    addPositionStat(resultList, position, bucket.aggregations());
                }
            }
        } catch (Exception e) {
            log.warn("广告埋点索引按广告位解析聚合结果失败,start:{},end:{}", startTime, endTime, e);
            return Collections.emptyList();
        }
        return resultList;
    }

    // 解析单个广告位桶的曝光PV/UV、点击PV并加入结果列表
    private void addPositionStat(List<AdPositionStatVO> resultList, Integer position, Map<String, Aggregate> aggs) {
        FilterAggregate exposureAgg = aggs.get("exposure").filter();
        long pv = exposureAgg.docCount();
        // uv 为 filter 聚合，memberId 为空的文档被排除，只统计有 memberId 的访客
        CardinalityAggregate uvAgg = exposureAgg.aggregations().get("uv").filter()
                .aggregations().get("uv_count").cardinality();
        long uv = uvAgg != null ? uvAgg.value() : 0L;
        long clicks = aggs.get("click").filter().docCount();

        AdPositionStatVO statVO = new AdPositionStatVO();
        statVO.setAdInfoPosition(position);
        statVO.setPv(pv);
        statVO.setUv(uv);
        statVO.setClicks(clicks);
        statVO.setCtr(calcCtr(pv, clicks));
        resultList.add(statVO);
    }

    @Override
    public List<AdStatVO> queryByAd(AdEventQueryDTO queryDTO) {
        LocalDateTime startTime = queryDTO.getStartTime();
        LocalDateTime endTime = queryDTO.getEndTime();
        // 0. 检查索引是否存在，过滤不存在的索引
        List<String> existingIndices = filterExistingIndices(resolveIndexPattern(startTime, endTime));
        if (existingIndices.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 构建基础查询
        Query boolQuery = buildBaseQuery(queryDTO);

        // 2. 构建聚合请求：按广告ID分组，top_hits取广告名称/广告位，嵌套曝光PV/UV、点击PV
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(existingIndices)
                .size(0)
                .query(boolQuery)
                .aggregations("by_ad", a -> a
                        .terms(t -> t.field("adId").size(1000)) // 广告数量可能较多，size适当放大
                        // 子聚合：取广告名称、广告位信息
                        .aggregations("top", sub -> sub
                                .topHits(th -> th.size(1)
                                        .source(so -> so.filter(f -> f.includes(List.of("adName", "adInfoPosition"))))))
                        // 子聚合：该广告曝光事件，doc_count即曝光PV，嵌套曝光UV
                        .aggregations("exposure", sub -> sub
                                .filter(f -> f.term(t -> t.field("eventType").value(AdEventType.AD_EXPOSURE.getCode())))
                                // memberId为空的访客不计入UV：仅统计memberId存在且非空字符串的文档
                                .aggregations("uv", u -> u
                                        .filter(f -> f.bool(b -> b
                                                .must(m -> m.exists(e -> e.field("memberId")))
                                                .mustNot(mn -> mn.term(t -> t.field("memberId").value("")))))
                                        .aggregations("uv_count", uc -> uc.cardinality(c -> c.field("memberId").precisionThreshold(40000)))))
                        )
                        // 子聚合：该广告点击事件，doc_count即点击PV
                        .aggregations("click", sub -> sub
                                .filter(f -> f.term(t -> t.field("eventType").value(AdEventType.AD_CLICK.getCode())))
                        )
        );

        // 3. 执行查询
        SearchResponse<Object> response;
        try {
            response = client.search(searchRequest, Object.class);
        } catch (Exception e) {
            log.warn("广告埋点索引按广告查询失败,start:{},end:{}", startTime, endTime, e);
            return Collections.emptyList();
        }

        // 4. 解析聚合结果
        List<AdStatVO> resultList = new ArrayList<>();
        if (response == null || response.aggregations() == null) {
            return resultList;
        }
        Aggregate byAdAgg = response.aggregations().get("by_ad");
        if (byAdAgg == null) {
            return resultList;
        }
        try {
            // adId为keyword字段正常返回sterms，兼容lterms作防御性兜底
            if (byAdAgg.isLterms()) {
                for (LongTermsBucket bucket : byAdAgg.lterms().buckets().array()) {
                    addAdStat(resultList, bucket.key(), bucket.aggregations());
                }
            } else if (byAdAgg.isSterms()) {
                for (StringTermsBucket bucket : byAdAgg.sterms().buckets().array()) {
                    Long adId = getLongFieldValue(bucket.key());
                    if (adId == null) {
                        continue;
                    }
                    addAdStat(resultList, adId, bucket.aggregations());
                }
            }
        } catch (Exception e) {
            log.warn("广告埋点索引按广告解析聚合结果失败,start:{},end:{}", startTime, endTime, e);
            return Collections.emptyList();
        }
        return resultList;
    }

    // 解析单个广告桶的曝光PV/UV、点击PV、top_hits信息并加入结果列表
    private void addAdStat(List<AdStatVO> resultList, Long adId, Map<String, Aggregate> aggs) {
        FilterAggregate exposureAgg = aggs.get("exposure").filter();
        long pv = exposureAgg.docCount();
        // uv 为 filter 聚合，memberId 为空的文档被排除，只统计有 memberId 的访客
        CardinalityAggregate uvAgg = exposureAgg.aggregations().get("uv").filter()
                .aggregations().get("uv_count").cardinality();
        long uv = uvAgg != null ? uvAgg.value() : 0L;
        long clicks = aggs.get("click").filter().docCount();

        AdStatVO statVO = new AdStatVO();
        statVO.setAdId(adId);
        statVO.setPv(pv);
        statVO.setUv(uv);
        statVO.setClicks(clicks);
        statVO.setCtr(calcCtr(pv, clicks));

        // 从top_hits中提取广告名称、广告位
        TopHitsAggregate topHitsAgg = aggs.get("top").topHits();
        List<Hit<JsonData>> topHits = topHitsAgg.hits().hits();
        if (!topHits.isEmpty() && topHits.get(0).source() != null) {
            Map<?, ?> sourceMap = topHits.get(0).source().to(Map.class);
            if (sourceMap != null) {
                Object adName = sourceMap.get("adName");
                if (adName != null) {
                    statVO.setAdName(adName.toString());
                }
                Object adInfoPosition = sourceMap.get("adInfoPosition");
                if (adInfoPosition instanceof Number) {
                    statVO.setAdInfoPosition(((Number) adInfoPosition).intValue());
                }
            }
        }
        resultList.add(statVO);
    }

    private Query buildBaseQuery(AdEventQueryDTO queryDTO) {

        List<Query> mustQueries = new ArrayList<>();

        // 广告事件类型必须匹配
        if (StringUtils.isNotBlank(queryDTO.getEventType())) {
            mustQueries.add(Query.of(q -> q.term(t -> t.field("eventType").value(queryDTO.getEventType()))));
        }

        // 广告ID过滤
        if (queryDTO.getAdIds() != null && !queryDTO.getAdIds().isEmpty()) {
            mustQueries.add(Query.of(q -> q.terms(
                t -> t.field("adId").terms(tv -> tv.value(queryDTO.getAdIds().stream().map(FieldValue::of).toList())))));
        }

        // 广告位序号过滤
        if (queryDTO.getAdInfoPositions() != null && !queryDTO.getAdInfoPositions().isEmpty()) {
            mustQueries.add(Query.of(q -> q.terms(
                t -> t.field("adInfoPosition").terms(tv -> tv.value(queryDTO.getAdInfoPositions().stream().map(p -> FieldValue.of(p.longValue())).toList())))));
        }

        if (queryDTO.getChannelIds() != null && !queryDTO.getChannelIds().isEmpty()) {
            mustQueries.add(Query.of(q -> q.terms(
                t -> t.field("channel").terms(tv -> tv.value(queryDTO.getChannelIds().stream().map(FieldValue::of).toList())))));
        }

        // 时间范围过滤
        mustQueries.add(Query.of(q -> q.range(
            r -> r.date(dr -> dr.field("timestamp").gte(formatDateTime(queryDTO.getStartTime())).lt(formatDateTime(queryDTO.getEndTime()))))));

        // 门店ID过滤（如果参数不为空）
        if (queryDTO.getStoreIds() != null && !queryDTO.getStoreIds().isEmpty()) {
            mustQueries.add(Query.of(q -> q.terms(
                t -> t.field("storeId").terms(tv -> tv.value(queryDTO.getStoreIds().stream().map(FieldValue::of).toList())))));
        }

        // 用户ID过滤
        if (queryDTO.getMemberId() != null) {
            mustQueries.add(Query.of(q -> q.term(t -> t.field("memberId").value(queryDTO.getMemberId()))));
        }

        return Query.of(q -> q.bool(b -> b.must(mustQueries)));
    }

    /**
     * ES range 查询时间格式：强制输出秒（如 2026-08-22T00:00:00+08:00）。
     * 显式 pattern 保证秒位恒定输出，与 timestamp mapping 的
     * strict_date_optional_time / yyyy-MM-dd'T'HH:mm:ss 格式对齐，
     * 避免依赖任何预定义 Formatter 的行为差异。
     * 注：预定义 Formatter（如 ISO_OFFSET_DATE_TIME）在秒为 0 时仍会输出秒，
     * 省略零秒是 OffsetDateTime.toString() 的行为，而非 ISO 系列 Formatter 的行为。
     */
    private static final DateTimeFormatter ES_RANGE_DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");

    // 辅助方法：格式化 LocalDateTime 为带时区偏移、必含秒的 ISO 格式字符串（统一 Asia/Shanghai 口径）
    private String formatDateTime(LocalDateTime dateTime) {
        ZonedDateTime zoned = dateTime.atZone(ZoneId.of("Asia/Shanghai"));
        return zoned.format(ES_RANGE_DATETIME_FORMAT);
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

    // 解析索引模式（示例：ad_event_logs_2023-10）
    // 索引名按写入侧的上海时区归月，这里用同一时区解析，避免月初/月末边界选错索引
    private List<String> resolveIndexPattern(LocalDateTime start, LocalDateTime end) {
        ZoneId zone = ZoneId.of("Asia/Shanghai");
        LocalDate startDate = start.atZone(zone).toLocalDate();
        LocalDate endDate = end.atZone(zone).toLocalDate();

        // 如果时间范围在同一个月内
        if (startDate.getYear() == endDate.getYear() && startDate.getMonth() == endDate.getMonth()) {
            return List.of("ad_event_logs_" + startDate.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        // 计算涉及的所有月份
        List<YearMonth> months = new ArrayList<>();
        YearMonth current = YearMonth.from(start.atZone(zone));
        YearMonth last = YearMonth.from(end.atZone(zone));

        while (!current.isAfter(last)) {
            months.add(current);
            current = current.plusMonths(1);
        }

        // 生成索引模式：ad_event_logs_2023-10,ad_event_logs_2023-11
        return months.stream().map(ym -> "ad_event_logs_" + ym.format(DateTimeFormatter.ofPattern("yyyy-MM"))).toList();
    }

    // 辅助方法：格式化日期边界（与趋势分桶的时区保持一致，用上海时区）
    private String formatDateForBound(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    // 计算点击率（口径：点击量/曝光量 * 100%，保留两位小数；曝光为0返回0）
    private Double calcCtr(long exposureCount, long clickCount) {
        if (exposureCount <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(clickCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(exposureCount), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Long getLongFieldValue(FieldValue fieldValue) {
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
                log.warn("解析广告ID失败, value={}", fieldValue.stringValue());
            }
        }
        return null;
    }

    private Integer getIntegerFieldValue(FieldValue fieldValue) {
        Long value = getLongFieldValue(fieldValue);
        return value != null ? value.intValue() : null;
    }
}
