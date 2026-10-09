package com.htyoudao.youdao.module.analysis.service.impl;


import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.*;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.AggregationType.SUM;

import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import co.elastic.clients.util.ObjectBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivitySeckillMemberVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisRecordsVO;
import com.htyoudao.youdao.module.analysis.service.dto.AggOrgDTO;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import com.htyoudao.youdao.module.analysis.service.dto.EventQueryDTO;
import com.htyoudao.youdao.module.analysis.service.dto.RangeDTO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisTopVO;
import com.htyoudao.youdao.module.analysis.dal.es.BzOrder;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.enums.EsDateFormat;
import com.htyoudao.youdao.module.analysis.enums.EventType;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig.AggregationType;
import com.htyoudao.youdao.module.analysis.service.IEsAggregationService;
import com.htyoudao.youdao.module.analysis.service.IEventService;

import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import jakarta.annotation.Resource;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


@Slf4j
@Service
public class EsAggregationServiceImpl implements IEsAggregationService {

    @Resource
    private ElasticsearchClient client;//8.5.1

    @Resource
    private IEventService eventService;

    @DubboReference
    private StoreApi storeApi;
    @DubboReference
    private ActivityApi activityApi;
    // 提取常量
    private static final String ACTIVITY_PRODUCTS_AGG = "activity_products";
    private static final String TOP_HITS_COMMODITY_NAME = "commodityName";

    @Override
    public Map<String, Double> batchAggregateMetrics(EsAggDTO esAggDTO, List<MetricsConfig> metrics) {

        Map<String, Double> result = new HashMap<>();

        for (MetricsConfig metric : metrics) {
            // 1. 创建 BoolQuery 构建器（组合多个条件）
            Builder builder = getBuilder(esAggDTO);
            Query query = metric.addTermsCondition(builder);
            SearchRequest searchRequest = SearchRequest.of(s -> s.size(0) // 不返回文档内容
                    .index("bz_order")
                    .query(query).aggregations(metric.getCode(), buildAggregation(metric)));

            try {
                SearchResponse<BzOrder> response = client.search(searchRequest, BzOrder.class);
                for (String key : response.aggregations().keySet()) {
                    Aggregate aggregate = response.aggregations().get(key);
                    double value = getValue(metric, aggregate);
                    result.put(key, value);
                }
                // 5. 解析结果
            } catch (IOException e) {
                log.error("ES 查询异常，request: {}", esAggDTO, e);
                throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
            }
        }
        return result;
    }

    @Override
    public Map<String, Double> batchAggregateMetricsWithUV(EsAggDTO esAggDTO, List<MetricsConfig> metrics) {
        Map<String, Double> stringDoubleMap = batchAggregateMetrics(esAggDTO, metrics);
        stringDoubleMap.put("uv", getUV(esAggDTO.getTimes(), esAggDTO.getStoreIds()).doubleValue());
        return stringDoubleMap;
    }

    private Long getUV(LocalDateTime[] times, List<Long> storeIds) {

        if (CollectionUtils.isEmpty(storeIds)){
            storeIds = new ArrayList<>(storeApi.getAllStoreIdByUser(SecurityFrameworkUtils.getLoginUserId())
                .getCheckedData());
        }
        
        EventQueryDTO queryDTO = new EventQueryDTO();
        queryDTO.setStoreIds(storeIds);
        queryDTO.setEventType(EventType.IN_STORE);
        queryDTO.setStartTime(times[0]);
        queryDTO.setEndTime(times[1]);
        return eventService.queryUV(queryDTO);
    }


    /**
     * 根据创建日期聚合查询
     *
     * @param type   1:天 2:小时
     * @param metric 指标
     * @return key:时间 value:指标值
     */
    @Override
    public Map<String, Double> analyzeByTimeGranularity(EsAggDTO esAggDTO, EsDateFormat type, MetricsConfig metric) {


        // 构建日期直方图聚合
        Aggregation dateHistogram = Aggregation.of(a -> a.dateHistogram(
                        dh -> dh.field("createTime")
                                .calendarInterval(type.getCalendarInterval())
                                .format(type.getFormat())
                                .timeZone("+08:00")) //设置为北京时间
                .aggregations(metric.getCode(), buildAggregation(metric)));

        // 构建搜索请求
        Builder builder = getBuilder(esAggDTO);
        SearchRequest request = SearchRequest.of(s ->
                s.size(0)
                        .aggregations("daily_stats", dateHistogram)
                        .query(metric.addTermsCondition(builder))
        );

        // 4. 执行查询
        try {
            SearchResponse<BzOrder> response = client.search(request, BzOrder.class);
            Map<String, Double> data = new HashMap<>();
            Aggregate dailyStats = response.aggregations().get("daily_stats");
            // 获取聚合对象
            List<DateHistogramBucket> timeSeries = dailyStats.dateHistogram().buckets().array();
            for (DateHistogramBucket bucket : timeSeries) {
                Aggregate aggregate = bucket.aggregations().get(metric.getCode());
                data.put(bucket.keyAsString(), getValue(metric, aggregate));
            }
            return data;
        } catch (IOException e) {
            log.error("ES 查询异常，request: {}, metric: {}", esAggDTO, metric.getCode(), e);
            throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
        }
    }


    @Override
    public PageResult<AnalysisTopVO> paginatedGroupAggregation(EsAggDTO esAggDTO,
                                                               List<MetricsConfig> metrics,
                                                               String groupField,
                                                               MetricsConfig orderMetric, SortOrder order,
                                                               Integer pageNum, Integer pageSize) {

        // 计算分页参数
        int from = (pageNum - 1) * pageSize;
        int requiredSize = from + pageSize;


        // 构建指标聚合
        Map<String, Aggregation> aggregations = metrics.stream()
                .collect(Collectors.toMap(
                        MetricsConfig::getCode,
                        this::buildFilterAggregation
                ));

        // 添加 storeId 子聚合
        aggregations.put("store_ids", Aggregation.of(a -> a
                .terms(t -> t
                        .field("storeId")
                        .size(10000)
                )
        ));

        // 构建主查询
        Query query = new Query.Builder().bool(getBuilder(esAggDTO).build()).build();

        // 构建排序规则
        TermsAggregation.Builder termBuilder = new TermsAggregation.Builder()
                .field(groupField)
                .size(requiredSize);
        if (orderMetric != null) {
            String name = orderMetric.getCode() + ">" + orderMetric.getCode(); //聚合排序
            termBuilder.order(new NamedValue<>(name, order));
        }

        SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
                .index("bz_order")
                .query(query)
                .aggregations(groupField,
                        a -> a.terms(termBuilder.build())
                                .aggregations(aggregations) // 添加指标聚合
                                .aggregations("bucket_sort_agg", bsa -> bsa
                                        .bucketSort(bs -> bs
                                                .from(from)
                                                .size(pageSize)
                                        )
                                )
                )
                .aggregations("total", total -> total
                        .cardinality(c -> c.field(groupField))
                ));

        // 3. 执行搜索请求
        try {
            SearchResponse<BzOrder> response = client.search(searchRequest, BzOrder.class);
            Long total = response.aggregations().get("total").cardinality().value();
            Aggregate aggregate = response.aggregations().get(groupField);
            List<AnalysisTopVO> data = extracted(metrics, aggregate);
            PageResult<AnalysisTopVO> mapPageResult = new PageResult<>();
            mapPageResult.setTotal(total);
            mapPageResult.setList(data);
            return mapPageResult;
        } catch (IOException e) {
            log.error("ES 查询异常，request: {}", esAggDTO, e);
            throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
        }


    }

    @Override
    public PageResult<AnalysisTopVO> paginatedRangeAggregation(EsAggDTO esAggDTO,
                                                               List<AggOrgDTO> customGroups,
                                                               List<MetricsConfig> metrics, String groupField, MetricsConfig orderMetric, SortOrder order, int pageNum,
                                                               int pageSize) {


        Map<String, Aggregation> filterAggs = new LinkedHashMap<>();

        for (int i = 0; i < customGroups.size(); i++) {
            List<Long> group = customGroups.get(i).getStoreIds();
            String key = "group_" + i;

            Query filterQuery = Query.of(q -> q.terms(t ->
                    t.field(groupField).terms(tv -> tv.value(group.stream().map(FieldValue::of).toList()))
            ));


            // 构建指标聚合
            Map<String, Aggregation> subAggs = metrics.stream()
                    .collect(Collectors.toMap(
                            MetricsConfig::getCode,
                            this::buildFilterAggregation
                    ));


            Aggregation filterAgg = Aggregation.of(a -> a
                    .filter(f -> f.bool(b -> b
                            .must(filterQuery)
                    ))
                    .aggregations(subAggs)
            );

            filterAggs.put(key, filterAgg);
        }

        // 构建主查询
        Query query = new Query.Builder().bool(getBuilder(esAggDTO).build()).build();

        SearchRequest searchRequest = SearchRequest.of(s -> s
                .size(0)
                .index("bz_order")
                .query(query)  // 主查询，排除 userId = 0 等基础过滤
                .aggregations(filterAggs)
        ); // 每个自定义 group 一个 filter 聚合


        // 3. 执行搜索请求
        SearchResponse<BzOrder> response = null;
        try {
            response = client.search(searchRequest, BzOrder.class);
        } catch (IOException e) {
            log.error("", e);
            throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
        }
        Map<String, Aggregate> aggResult = response.aggregations();

        List<AnalysisTopVO> resultList = new ArrayList<>();

        for (int i = 0; i < customGroups.size(); i++) {
            String key = "group_" + i;
            List<Long> storeIds = customGroups.get(i).getStoreIds();

            Aggregate agg = aggResult.get(key);
            if (agg == null || !agg.isFilter()) continue;

            FilterAggregate filterAgg = agg.filter();
            Map<String, Aggregate> subAggs = filterAgg.aggregations();

            TreeMap<String, Double> valueMap = new TreeMap<>();

            for (MetricsConfig metric : metrics) {
                Aggregate metricAgg = subAggs.get(metric.getCode())
                        .filter().aggregations().get(metric.getCode());
                valueMap.put(metric.getCode(), getValue(metric, metricAgg));
            }

            AnalysisTopVO vo = new AnalysisTopVO();
            vo.setKey(customGroups.get(i).getOrgId().toString());
            vo.setName(customGroups.get(i).getOrgName());
            vo.setValues(valueMap);
            vo.setStoreIds(storeIds);
            resultList.add(vo);
        }

        //排序处理
        if (orderMetric != null) {
            Comparator<AnalysisTopVO> comparator = Comparator.comparing(
                    vo -> vo.getValues().getOrDefault(orderMetric.getCode(), 0.0)
            );

            if (order.equals(SortOrder.Desc)) {
                comparator = comparator.reversed();
            }

            resultList = resultList.stream()
                    .sorted(comparator)
                    .toList();
        }


        //分页处理
        int from = (pageNum - 1) * pageSize;
        List<AnalysisTopVO> pagedList = resultList.stream()
                .skip(from)
                .limit(pageSize)
                .toList();

        PageResult<AnalysisTopVO> pageResult = new PageResult<>();
        pageResult.setTotal((long) resultList.size());
        pageResult.setList(pagedList);

        return pageResult;
    }

    @Override
    public PageResult<AnalysisTopVO> paginatedGroupAggregation(EsAggDTO esAggDTO, List<MetricsConfig> metrics, String groupField, Integer pageNum, Integer pageSize) {
        // 计算分页参数
        int from = (pageNum - 1) * pageSize;
        int requiredSize = from + pageSize;


        // 构建指标聚合
        Map<String, Aggregation> aggregations = metrics.stream()
                .collect(Collectors.toMap(
                        MetricsConfig::getCode,
                        metric -> {
                            if ("offerAmount".equals(metric.getCode())) {
                                // 特殊处理嵌套字段
                                return Aggregation.of(a -> a
                                        .nested(n -> n.path("product"))
                                        .aggregations("promotion_agg",
                                                nestedAgg -> nestedAgg.sum(s -> s.field("product.promotionDiscountAmount"))
                                        ));
                            } else if ("commodityCount".equals(metric.getCode())) {
                                return Aggregation.of(a -> a
                                        .nested(n -> n.path("product"))
                                        .aggregations("activity_products", a2 -> a2
                                                .filter(f -> f.term(t -> t
                                                        .field("product.activityId")
                                                        .value(FieldValue.of(esAggDTO.getActivityId()))))
                                                .aggregations("total_goods_num", a3 -> a3  // 修改聚合名称
                                                        .sum(s -> s.field("product.goodsNum")))  // 改用 sum 聚合
                                        ));
                            } else {
                                return buildFilterAggregation(metric);
                            }
                        }
                ));

        // 添加 storeId 子聚合
        aggregations.put("store_ids", Aggregation.of(a -> a
                .terms(t -> t
                        .field("storeId")
                        .size(10000)
                )
        ));


        // 构建主查询
        Query query = new Query.Builder().bool(getBuilderTwo(esAggDTO).build()).build();

        // 构建排序规则
        TermsAggregation.Builder termBuilder = new TermsAggregation.Builder()
                .field(groupField)
                .size(requiredSize);

        SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
                .index("bz_order")
                .query(query)
                .aggregations(groupField,
                        a -> a.terms(termBuilder.build())
                                .aggregations(aggregations) // 添加指标聚合
                                .aggregations("bucket_sort_agg", bsa -> bsa
                                        .bucketSort(bs -> bs
                                                .from(from)
                                                .size(pageSize)
                                        )
                                )
                )
                .aggregations("total", total -> total
                        .cardinality(c -> c.field(groupField))
                ));

        // 3. 执行搜索请求
        try {
            SearchResponse<BzOrder> response = client.search(searchRequest, BzOrder.class);
            Long total = response.aggregations().get("total").cardinality().value();
            Aggregate aggregate = response.aggregations().get(groupField);

            List<AnalysisTopVO> data = extracted(metrics, aggregate);
            PageResult<AnalysisTopVO> mapPageResult = new PageResult<>();
            mapPageResult.setTotal(total);
            mapPageResult.setList(data);
            return mapPageResult;
        } catch (IOException e) {
            log.error("ES 查询异常，request: {}", esAggDTO, e);
            throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
        }
    }

    @Override
    public PageResult<ActivitySeckillMemberVO> paginatedGroupAggregation(EsAggDTO esAggDTO, Integer pageNum, List<MetricsConfig> metrics, Integer pageSize) {
        // 1. 定义分组字段（按会员ID分组，获取会员维度数据）
        String groupField = "orderSn";

        // 3. 计算分页参数
        int from = (pageNum - 1) * pageSize;
        int requiredSize = from + pageSize;

        // 4. 构建指标聚合
        Map<String, Aggregation> aggregations = metrics.stream()
                .collect(Collectors.toMap(
                        MetricsConfig::getCode,
                        metric -> {
                            // 处理嵌套字段（如商品名称）
                            if (metric.getField().contains("product.")) {
                                return Aggregation.of(a -> a
                                        .nested(n -> n.path("product"))
                                        .aggregations("activity_products", a2 -> a2
                                                .filter(f -> f
                                                        .term(t -> t
                                                                .field("product.activityId")
                                                                .value(FieldValue.of(esAggDTO.getActivityId()))
                                                        )
                                                )
                                                .aggregations(metric.getCode(), buildAggregation(metric))
                                        )

                                );
                            } else {
                                return buildFilterAggregation(metric);
                            }
                        }
                ));


        // 构建主查询
        Query query = new Query.Builder().bool(getBuilderTwo(esAggDTO).build()).build();

        // 构建排序规则
        TermsAggregation.Builder termBuilder = new TermsAggregation.Builder()
                .field(groupField)
                .size(requiredSize);

        SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
                .index("bz_order")
                .query(query)
                .aggregations(groupField,
                        a -> a.terms(termBuilder.build())
                                .aggregations(aggregations) // 添加指标聚合
                                .aggregations("bucket_sort_agg", bsa -> bsa
                                        .bucketSort(bs -> bs
                                                .from(from)
                                                .size(pageSize)
                                        )
                                )
                )
                .aggregations("total", total -> total
                        .cardinality(c -> c.field(groupField))
                ));

        // 8. 执行搜索请求
        try {
            SearchResponse<BzOrder> response = client.search(searchRequest, BzOrder.class);
            Long total = response.aggregations().get("total").cardinality().value();
            Aggregate aggregate = response.aggregations().get(groupField);

            // 9. 解析结果为AnalysisRecordsVO列表
            List<ActivitySeckillMemberVO> data = extractedMemberSeckill(metrics, aggregate);

            PageResult<ActivitySeckillMemberVO> pageResult = new PageResult<>();
            pageResult.setTotal(total);
            pageResult.setList(data);
            return pageResult;
        } catch (IOException e) {
            log.error("ES 查询异常，request: {}", esAggDTO, e);
            throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
        }
    }

    @Override
    public PageResult<AnalysisTopVO> paginatedGrouSeckillpAggregation(EsAggDTO esAggDTO, List<MetricsConfig> metrics, String groupField, Integer pageNum, Integer pageSize, int type) {
        // 计算分页参数
        int from = (pageNum - 1) * pageSize;
        int requiredSize = from + pageSize;


        // 构建指标聚合
        Map<String, Aggregation> aggregations = metrics.stream()
                .collect(Collectors.toMap(
                        MetricsConfig::getCode,
                        metric -> {
                            if ("offerAmount".equals(metric.getCode())) {
                                // 特殊处理嵌套字段
                                return Aggregation.of(a -> a
                                        .nested(n -> n.path("product"))
                                        .aggregations("promotion_agg",
                                                nestedAgg -> nestedAgg.sum(s -> s.field("product.promotionDiscountAmount"))
                                        ));
                            } else if ("commodityCount".equals(metric.getCode())) {
                                return Aggregation.of(a -> a
                                        .nested(n -> n.path("product"))
                                        .aggregations("activity_products", a2 -> a2
                                                .filter(f -> f.term(t -> t
                                                        .field("product.activityId")
                                                        .value(FieldValue.of(esAggDTO.getActivityId()))))
                                                .aggregations("total_goods_num", a3 -> a3  // 修改聚合名称
                                                        .sum(s -> s.field("product.goodsNum")))  // 改用 sum 聚合
                                        ));
                            } else {
                                return buildFilterAggregation(metric);
                            }
                        }
                ));

        // 添加 storeId 子聚合
        aggregations.put("store_ids", Aggregation.of(a -> a
                .terms(t -> t
                        .field(groupField)
                        .size(10000)
                )
        ));


        // 构建主查询
        Query query = new Query.Builder().bool(getBuilderChannel(esAggDTO, type).build()).build();

        // 构建排序规则
        TermsAggregation.Builder termBuilder = new TermsAggregation.Builder()
                .field(groupField)
                .size(requiredSize);

        SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
                .index("bz_order")
                .query(query)
                .aggregations(groupField,
                        a -> a.terms(termBuilder.build())
                                .aggregations(aggregations) // 添加指标聚合
                                .aggregations("bucket_sort_agg", bsa -> bsa
                                        .bucketSort(bs -> bs
                                                .from(from)
                                                .size(pageSize)
                                        )
                                )
                )
                .aggregations("total", total -> total
                        .cardinality(c -> c.field(groupField))
                ));

        // 3. 执行搜索请求
        try {
            SearchResponse<BzOrder> response = client.search(searchRequest, BzOrder.class);
            Long total = response.aggregations().get("total").cardinality().value();
            Aggregate aggregate = response.aggregations().get(groupField);

            List<AnalysisTopVO> data = extractedSeckill(esAggDTO, metrics, aggregate);
            PageResult<AnalysisTopVO> mapPageResult = new PageResult<>();
            mapPageResult.setTotal(total);
            mapPageResult.setList(data);
            return mapPageResult;
        } catch (IOException e) {
            log.error("ES 查询异常，request: {}", esAggDTO, e);
            throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
        }
    }

    /**
     * 解析字符串类型的Terms聚合桶
     */
    private List<AnalysisRecordsVO> parseStringTermsBuckets(List<MetricsConfig> metrics, List<StringTermsBucket> buckets) {
        return buckets.stream()
                .map(bucket -> buildAnalysisRecordsVO(metrics, bucket.aggregations()))
                .collect(Collectors.toList());
    }

    /**
     * 解析长整型的Terms聚合桶
     */
    private List<AnalysisRecordsVO> parseLongTermsBuckets(List<MetricsConfig> metrics, List<LongTermsBucket> buckets) {
        return buckets.stream()
                .map(bucket -> buildAnalysisRecordsVO(metrics, bucket.aggregations()))
                .collect(Collectors.toList());
    }

    /**
     * 构建AnalysisRecordsVO对象
     */
    private AnalysisRecordsVO buildAnalysisRecordsVO(List<MetricsConfig> metrics, Map<String, Aggregate> aggregations) {
        AnalysisRecordsVO recordsVO = new AnalysisRecordsVO();
        TreeMap<String, Double> values = new TreeMap<>();

        for (MetricsConfig metric : metrics) {
            String metricCode = metric.getCode();
            Object metricValue = getMetricValue(aggregations, metric);

            switch (metricCode) {
                case "STORE_NAMES":
                    recordsVO.setStoreName(metricValue != null ? metricValue.toString() : null);
                    break;
                case "MEMBER_NAME":
                    recordsVO.setMemberName(metricValue != null ? metricValue.toString() : null);
                    break;
                case "MEMBER_MOBILE":
                    recordsVO.setMemberMobile(metricValue != null ? metricValue.toString() : null);
                    break;
                case "PAY_TIME":
                    recordsVO.setPayTime(formatPayTime(metricValue));
                    break;
                case "COMMODITY_NAME":
                    recordsVO.setCommodityName(metricValue != null ? metricValue.toString() : null);
                    break;
                case "PAY_AMOUNTS":
                    if (metricValue instanceof Number) {
                        double amount = ((Number) metricValue).doubleValue();
                        recordsVO.setPayAmount(amount);
                        values.put(metricCode, amount);
                    }
                    break;
                case "CHANNEL":
                    recordsVO.setChannelName(metricValue != null ? metricValue.toString() : null);
                    break;
                default:
                    break;
            }
        }

        recordsVO.setValues(values);
        return recordsVO;
    }

    /**
     * 获取指标聚合的值（处理普通和嵌套聚合）
     */
    private Object getMetricValue(Map<String, Aggregate> aggregations, MetricsConfig metric) {
        String code = metric.getCode();
        // 处理嵌套聚合（如商品相关字段）
        if (metric.getField().contains("product.")) {
            Aggregate nestedAgg = aggregations.get(code);
            if (nestedAgg != null && nestedAgg.nested() != null) {
                Aggregate innerAgg = nestedAgg.nested().aggregations().get(code);
                return extractValueFromAggregate(innerAgg);
            }
        } else {
            // 处理普通聚合
            Aggregate agg = aggregations.get(code);
            return extractValueFromAggregate(agg);
        }
        return null;
    }


    /**
     * 格式化支付时间为"yyyy-MM-dd HH:mm:ss"
     */
    private String formatPayTime(Object timeValue) {
        if (timeValue == null) {
            return null;
        }
        try {
            long timestamp;
            if (timeValue instanceof Number) {
                timestamp = ((Number) timeValue).longValue();
            } else if (timeValue instanceof String) {
                timestamp = Long.parseLong((String) timeValue);
            } else {
                return timeValue.toString();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            log.warn("支付时间格式化失败: {}", timeValue, e);
            return timeValue.toString();
        }
    }


    @Override
    public List<AnalysisTopVO> paginatedGroupAggregationList(EsAggDTO esAggDTO, List<MetricsConfig> metrics, String groupField) {
        // 构建指标聚合
        Map<String, Aggregation> aggregations = metrics.stream()
                .collect(Collectors.toMap(
                        MetricsConfig::getCode,
                        metric -> {
                            if ("offerAmount".equals(metric.getCode())) {
                                // 特殊处理嵌套字段
                                return Aggregation.of(a -> a
                                        .nested(n -> n.path("product"))
                                        .aggregations("promotion_agg",
                                                nestedAgg -> nestedAgg.sum(s -> s.field("product.promotionDiscountAmount"))
                                        ));
                            } else if ("commodityCount".equals(metric.getCode())) {
                                return Aggregation.of(a -> a
                                        .nested(n -> n.path("product"))
                                        .aggregations("activity_products", a2 -> a2
                                                .filter(f -> f.term(t -> t
                                                        .field("product.activityId")
                                                        .value(FieldValue.of(esAggDTO.getActivityId()))))
                                                .aggregations("total_goods_num", a3 -> a3  // 修改聚合名称
                                                        .sum(s -> s.field("product.goodsNum")))  // 改用 sum 聚合
                                        ));
                            } else {
                                return buildFilterAggregation(metric);
                            }
                        }
                ));

        // 添加 storeId 子聚合
        aggregations.put("store_ids", Aggregation.of(a -> a
                .terms(t -> t
                        .field(groupField)
                        .size(10000)
                )
        ));

        // 构建主查询
        Query query = new Query.Builder().bool(getBuilderTwo(esAggDTO).build()).build();

        // 构建排序规则
        TermsAggregation.Builder termBuilder = new TermsAggregation.Builder()
                .field(groupField)
                .size(10000);

        SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
                .index("bz_order")
                .query(query)
                .aggregations(groupField,
                        a -> a.terms(termBuilder.build())
                                .aggregations(aggregations) // 添加指标聚合
                                .aggregations("bucket_sort_agg", bsa -> bsa
                                        .bucketSort(bs -> bs
                                                .from(0)
                                                .size(10000)
                                        )
                                )
                )
                .aggregations("total", total -> total
                        .cardinality(c -> c.field(groupField))
                ));

        // 3. 执行搜索请求
        try {
            SearchResponse<BzOrder> response = client.search(searchRequest, BzOrder.class);
//            Long total = response.aggregations().get("total").cardinality().value();
            Aggregate aggregate = response.aggregations().get(groupField);
            List<AnalysisTopVO> data = extracted(metrics, aggregate);
            return data;
        } catch (IOException e) {
            log.error("ES 查询异常，request: {}", esAggDTO, e);
            throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
        }
    }

    @Override
    public List<AnalysisTopVO> paginatedSeckillGroupAggregationList(EsAggDTO esAggDTO, List<MetricsConfig> metrics, String groupField) {
        // 构建指标聚合
        Map<String, Aggregation> aggregations = metrics.stream()
                .collect(Collectors.toMap(
                        MetricsConfig::getCode,
                        metric -> {
                            if ("offerAmount".equals(metric.getCode())) {
                                // 特殊处理嵌套字段
                                return Aggregation.of(a -> a
                                        .nested(n -> n.path("product"))
                                        .aggregations("promotion_agg",
                                                nestedAgg -> nestedAgg.sum(s -> s.field("product.promotionDiscountAmount"))
                                        ));
                            } else if ("commodityCount".equals(metric.getCode())) {
                                return Aggregation.of(a -> a
                                        .nested(n -> n.path("product"))
                                        .aggregations("activity_products", a2 -> a2
                                                .filter(f -> f.term(t -> t
                                                        .field("product.activityId")
                                                        .value(FieldValue.of(esAggDTO.getActivityId()))))
                                                .aggregations("total_goods_num", a3 -> a3  // 修改聚合名称
                                                        .sum(s -> s.field("product.goodsNum")))  // 改用 sum 聚合
                                        ));
                            } else {
                                return buildFilterAggregation(metric);
                            }
                        }
                ));

        // 添加 storeId 子聚合
        aggregations.put("store_ids", Aggregation.of(a -> a
                .terms(t -> t
                        .field(groupField)
                        .size(10000)
                )
        ));

        // 构建主查询
        Query query = new Query.Builder().bool(getBuilderTwo(esAggDTO).build()).build();

        // 构建排序规则
        TermsAggregation.Builder termBuilder = new TermsAggregation.Builder()
                .field(groupField)
                .size(10000);

        SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
                .index("bz_order")
                .query(query)
                .aggregations(groupField,
                        a -> a.terms(termBuilder.build())
                                .aggregations(aggregations) // 添加指标聚合
                                .aggregations("bucket_sort_agg", bsa -> bsa
                                        .bucketSort(bs -> bs
                                                .from(0)
                                                .size(10000)
                                        )
                                )
                )
                .aggregations("total", total -> total
                        .cardinality(c -> c.field(groupField))
                ));

        // 3. 执行搜索请求
        try {
            SearchResponse<BzOrder> response = client.search(searchRequest, BzOrder.class);
//            Long total = response.aggregations().get("total").cardinality().value();
            Aggregate aggregate = response.aggregations().get(groupField);
            List<AnalysisTopVO> data = extractedSeckill(esAggDTO, metrics, aggregate);
            return data;
        } catch (IOException e) {
            log.error("ES 查询异常，request: {}", esAggDTO, e);
            throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
        }
    }

    @Override
    public Map<Long, Double> frequencyAggGroup(EsAggDTO dto, MetricsConfig groupField) {
        Map<Long, Double> resultMap = new HashMap<>();

        // 构建基础 query
        Query query = new Query.Builder()
                .bool(getBuilder(dto)
                        .mustNot(m -> m.term(e -> e.field("memberId").value("0")))//过滤掉点餐机订单
                        .build()).build();


        // terms 聚合，每个用户就是一个桶，bucket.docCount = 该用户的订单数
        Aggregation userAgg = Aggregation.of(a -> a
                .terms(t -> t
                        .field("memberId") // 下单用户字段
                        .size(10000) // 限制聚合数量
                )
                .aggregations(groupField.getCode(), buildFilterAggregation(groupField))
        );

        // 构建 search request
        SearchRequest request = SearchRequest.of(s -> s
                .size(0)
                .index("bz_order")
                .query(query)
                .aggregations("user_frequency_agg", userAgg)
        );

        try {
            SearchResponse<BzOrder> response = client.search(request, BzOrder.class);

            Aggregate userFrequencyAgg = response.aggregations().get("user_frequency_agg");
            List<LongTermsBucket> userBuckets = userFrequencyAgg.lterms().buckets().array();

            for (LongTermsBucket bucket : userBuckets) {
                long orderCount = bucket.docCount(); // 每个用户的下单频次
                // 获取实际指标值（如支付金额）
                Aggregate subAgg = bucket.aggregations()
                        .get(groupField.getCode()).filter()
                        .aggregations().get(groupField.getCode());

                double value = getValue(groupField, subAgg);
                // 同一范围的合并统计
                resultMap.merge(orderCount, value, Double::sum);
            }

            return resultMap;
        } catch (IOException e) {
            log.error("frequencyAggGroup failed", e);
            throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
        }
    }


    @Override
    public Map<String, Double> rangeList(EsAggDTO request,
                                         String rangeField, MetricsConfig groupField,
                                         List<RangeDTO> list) {

        Query query = new Query.Builder().bool(getBuilder(request).build()).build();

        List<AggregationRange> rangeList = new ArrayList<>();
        for (RangeDTO rangeDTO : list) {
            AggregationRange aggregationRange = AggregationRange.of(
                    r -> r.key(rangeDTO.getName())
                            .from(rangeDTO.getFrom())
                            .to(rangeDTO.getTo())
            );
            rangeList.add(aggregationRange);
        }

        Aggregation aggregation = Aggregation.of(
                r -> r.range(range -> range.field(rangeField)
                                .ranges(rangeList))
                        .aggregations(groupField.getCode(), buildFilterAggregation(groupField))
        );


        SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
                .index("bz_order")
                .query(query)
                .aggregations("range_agg", aggregation)
        );

        // 3. 执行搜索请求
        try {
            SearchResponse<BzOrder> response = client.search(searchRequest, BzOrder.class);
            List<RangeBucket> rangeBuckets = response.aggregations().get("range_agg")
                    .range().buckets().array();

            Map<String, Double> resultMap = new HashMap<>();
            for (RangeBucket rangeBucket : rangeBuckets) {
                Aggregate aggregate = rangeBucket.aggregations()
                        .get(groupField.getCode())
                        .filter().aggregations()
                        .get(groupField.getCode());
                resultMap.put(rangeBucket.key(), getValue(groupField, aggregate));
            }

            return resultMap;

        } catch (IOException e) {
            log.error("ES 查询异常，request: {}", request, e);
            throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
        }
    }


    // 构建过滤聚合辅助方法
    private Aggregation buildFilterAggregation(MetricsConfig metricsConfig) {
        Builder boolBuilder = new Builder();
        Query query = metricsConfig.addTermsCondition(boolBuilder);
        return new Aggregation.Builder()
                .filter(f -> f.bool(query.bool()))
                .aggregations(Map.of(
                        metricsConfig.getCode(),
                        buildAggregation(metricsConfig)
                ))
                .build();
    }

    private List<AnalysisTopVO> extracted(List<MetricsConfig> metrics, Aggregate aggregate) {
        List<AnalysisTopVO> list = new ArrayList<>();

        if (aggregate.isLterms()) {
            Buckets<LongTermsBucket> buckets = aggregate.lterms().buckets();
            for (LongTermsBucket bucket : buckets.array()) {
                list.add(processBucket(bucket.key(), bucket.aggregations(), metrics));
            }
        } else if (aggregate.isSterms()) {
            Buckets<StringTermsBucket> buckets = aggregate.sterms().buckets();
            for (StringTermsBucket bucket : buckets.array()) {
                list.add(processBucket(bucket.key(), bucket.aggregations(), metrics));
            }
        }

        return list;
    }

    private List<AnalysisTopVO> extractedSeckill(EsAggDTO esAggDTO, List<MetricsConfig> metrics, Aggregate aggregate) {
        List<AnalysisTopVO> list = new ArrayList<>();

        if (aggregate.isLterms()) {
            Buckets<LongTermsBucket> buckets = aggregate.lterms().buckets();
            for (LongTermsBucket bucket : buckets.array()) {
                list.add(processBucketSeckill(esAggDTO, bucket.key(), bucket.aggregations(), metrics));
            }
        } else if (aggregate.isSterms()) {
            Buckets<StringTermsBucket> buckets = aggregate.sterms().buckets();
            for (StringTermsBucket bucket : buckets.array()) {
                list.add(processBucketSeckill(esAggDTO, bucket.key(), bucket.aggregations(), metrics));
            }
        }

        return list;
    }

    private List<ActivitySeckillMemberVO> extractedMemberSeckill(List<MetricsConfig> metrics, Aggregate aggregate) {
        List<ActivitySeckillMemberVO> list = new ArrayList<>();

        if (aggregate.isLterms()) {
            Buckets<LongTermsBucket> buckets = aggregate.lterms().buckets();
            for (LongTermsBucket bucket : buckets.array()) {
                list.add(processBucketMemberSeckill(bucket.key(), bucket.aggregations(), metrics));
            }
        } else if (aggregate.isSterms()) {
            Buckets<StringTermsBucket> buckets = aggregate.sterms().buckets();
            for (StringTermsBucket bucket : buckets.array()) {
                list.add(processBucketMemberSeckill( bucket.key(), bucket.aggregations(), metrics));
            }
        }

        return list;
    }
    private AnalysisTopVO processBucket(Object rawKey, Map<String, Aggregate> aggs, List<MetricsConfig> metrics) {
        String key = rawKey.toString(); // 无论是 Long 还是 String，都转成 String 存储

        if (rawKey instanceof FieldValue) {
            key = ((FieldValue) rawKey).stringValue();
        }

        TreeMap<String, Double> resultMap = new TreeMap<>();
        String name = "";

        for (MetricsConfig metric : metrics) {
            String metricKey = metric.getCode();
            aggs.get(metricKey);
            if ("offerAmount".equals(metricKey)) {
                // 处理嵌套聚合结果
                Aggregate nestedAgg = aggs.get(metricKey);
                if (nestedAgg != null && nestedAgg.isNested()) {
                    Aggregate promotionAgg = nestedAgg.nested().aggregations().get("promotion_agg");
                    if (promotionAgg != null && promotionAgg.isSum()) {
                        double value = promotionAgg.sum().value();
                        resultMap.put(metricKey, value);
                    }
                }
            } else if ("commodityCount".equals(metricKey)) {
                // 处理嵌套聚合结果
                Aggregate nestedAgg = aggs.get(metricKey);
                if (nestedAgg != null && nestedAgg.isNested()) {
                    FilterAggregate filterAgg = nestedAgg.nested().aggregations().get("activity_products").filter();
//                    Aggregate promotionAgg = nestedAgg.nested().aggregations().get("promotion_agg");
                    if (filterAgg != null) {
                        Aggregate totalGoodsNum = filterAgg.aggregations().get("total_goods_num");
                        double value = totalGoodsNum.sum().value();
                        resultMap.put(metricKey, value);
                    } else {
                        resultMap.put(metricKey, 0.0);
                    }

//                    if (promotionAgg != null && promotionAgg.isValueCount()) {
//                        boolean valueCount = promotionAgg.isValueCount();
//                        resultMap.put(metricKey,0.00);
//                    }
                }
            } else {
                FilterAggregate filter = aggs.get(metricKey).filter();
                Aggregate subAgg = filter.aggregations().get(metricKey);

                if (metric.getAggType().equals(AggregationType.HIT)) {
                    name = getHitName(metrics, subAgg);
                } else {
                    resultMap.put(metricKey, getValue(metric, subAgg));
                }
            }

        }
        List<Long> storeIds = new ArrayList<>();
        Aggregate storeIdsAgg = aggs.get("store_ids");
        if (storeIdsAgg != null && storeIdsAgg.isLterms()) {
            Buckets<LongTermsBucket> storeBuckets = storeIdsAgg.lterms().buckets();
            for (LongTermsBucket storeBucket : storeBuckets.array()) {
                storeIds.add(storeBucket.key());
            }
        }

        AnalysisTopVO vo = new AnalysisTopVO();
        vo.setKey(key);
        vo.setName(name);
        vo.setValues(resultMap);
        vo.setStoreIds(storeIds);
        return vo;
    }

    private AnalysisTopVO processBucketSeckill(EsAggDTO esAggDTO, Object rawKey, Map<String, Aggregate> aggs, List<MetricsConfig> metrics) {
        String key = rawKey.toString(); // 无论是 Long 还是 String，都转成 String 存储

        if (rawKey instanceof FieldValue) {
            key = ((FieldValue) rawKey).stringValue();
        }

        TreeMap<String, Double> resultMap = new TreeMap<>();
        String name = "";

        for (MetricsConfig metric : metrics) {
            String metricKey = metric.getCode();
            aggs.get(metricKey);
            if ("offerAmount".equals(metricKey)) {
                // 处理嵌套聚合结果
                Aggregate nestedAgg = aggs.get(metricKey);
                if (nestedAgg != null && nestedAgg.isNested()) {
                    Aggregate promotionAgg = nestedAgg.nested().aggregations().get("promotion_agg");
                    if (promotionAgg != null && promotionAgg.isSum()) {
                        double value = promotionAgg.sum().value();
                        resultMap.put(metricKey, value);
                    }
                }
            } else if ("commodityCount".equals(metricKey)) {
                // 处理嵌套聚合结果
                Aggregate nestedAgg = aggs.get(metricKey);
                if (nestedAgg != null && nestedAgg.isNested()) {
                    FilterAggregate filterAgg = nestedAgg.nested().aggregations().get("activity_products").filter();
//                    Aggregate promotionAgg = nestedAgg.nested().aggregations().get("promotion_agg");
                    if (filterAgg != null) {
                        Aggregate totalGoodsNum = filterAgg.aggregations().get("total_goods_num");
                        double value = totalGoodsNum.sum().value();
                        resultMap.put(metricKey, value);
                    } else {
                        resultMap.put(metricKey, 0.0);
                    }

                }
            }else {
                FilterAggregate filter = aggs.get(metricKey).filter();
                Aggregate subAgg = filter.aggregations().get(metricKey);

                if (metric.getAggType().equals(AggregationType.HIT)) {
                    name = getHitName(metrics, subAgg);
                } else {
                    resultMap.put(metricKey, getValue(metric, subAgg));
                }
            }

        }
        EventQueryDTO queryDTO = new EventQueryDTO();
        queryDTO.setEventId(esAggDTO.getActivityId().toString());
        queryDTO.setEventType(EventType.ACTIVITY);
        queryDTO.setStartTime(esAggDTO.getTimes()[0]);
        queryDTO.setEndTime(esAggDTO.getTimes()[1]);

        resultMap.put(TOTAL_VISITORS.getCode(), eventService.queryUV(queryDTO).doubleValue());
        Double customerCounts = resultMap.get(CUSTOMER_COUNTS.getCode());
        Double totalVisitors = resultMap.get(TOTAL_VISITORS.getCode());
        if (customerCounts != null && totalVisitors != null && totalVisitors > 0) {
            resultMap.put(CONVERSION_RATE.getCode(), customerCounts / totalVisitors);
        } else {
            resultMap.put(CONVERSION_RATE.getCode(), 0.0);
        }
        List<Long> storeIds = new ArrayList<>();
        Aggregate storeIdsAgg = aggs.get("store_ids");
        if (storeIdsAgg != null && storeIdsAgg.isLterms()) {
            Buckets<LongTermsBucket> storeBuckets = storeIdsAgg.lterms().buckets();
            for (LongTermsBucket storeBucket : storeBuckets.array()) {
                storeIds.add(storeBucket.key());
            }
        }
        AnalysisTopVO vo = new AnalysisTopVO();
        vo.setKey(key);
        vo.setName(name);
        vo.setValues(resultMap);
        vo.setStoreIds(storeIds);
        return vo;
    }


    private ActivitySeckillMemberVO processBucketMemberSeckill(Object rawKey,
                                                               Map<String, Aggregate> aggs,
                                                               List<MetricsConfig> metrics) {
        ActivitySeckillMemberVO vo = new ActivitySeckillMemberVO();

         for (MetricsConfig metric : metrics) {
             String metricKey = metric.getCode();
            // 每次循环重新定义变量，避免跨循环污染
            String currentValue = null;
            Aggregate subAgg = null;

            // 处理聚合数据，提取当前指标对应的值
            if ("commodityName".equals(metricKey)) {
                currentValue = handleCommodityName(aggs, metric);
            } else {
                // 处理其他指标，统一空指针防护
                Aggregate metricAgg = aggs.get(metricKey);
                if (metricAgg != null) {
                    FilterAggregate filter = metricAgg.filter();
                    if (filter != null) {
                        subAgg = filter.aggregations().get(metricKey);
                        // 处理HIT类型的聚合
                        if (AggregationType.HIT.equals(metric.getAggType()) && subAgg != null) {
                            currentValue = getMemberHitName(metric, subAgg);
                        }
                    }
                }
            }

            // 根据指标键赋值到VO
            assignToVO(vo, metricKey, currentValue, subAgg, metric);
        }

        return vo;
    }

    // 单独处理商品名称的聚合逻辑
    private String handleCommodityName(Map<String, Aggregate> aggs, MetricsConfig metric) {
        Aggregate nestedAgg = aggs.get("commodityName");
        if (nestedAgg == null || !nestedAgg.isNested()) {
            return null;
        }

        FilterAggregate filterAgg = nestedAgg.nested().aggregations().get(ACTIVITY_PRODUCTS_AGG).filter();
        if (filterAgg == null) {
            return null;
        }

        Aggregate topHitsAgg = filterAgg.aggregations().get(TOP_HITS_COMMODITY_NAME);
        return topHitsAgg != null ? getCommaSeparatedGoodsNames(topHitsAgg) : null;
    }
    /**
     * 从topHits聚合结果中提取商品名称（适配嵌套结构）
     */
    public String getCommaSeparatedGoodsNames(Aggregate topHitsAgg) {
        if (topHitsAgg == null) {
            return null;
        }

        try {
            // 1. 将Aggregate转为JSON字符串
            String jsonStr = topHitsAgg.toString(); // 假设toString()返回完整JSON

            // 2. 解析JSON字符串
            JsonNode rootNode =  new ObjectMapper().readTree(extractPureJson(jsonStr));

            // 3. 定位到hits数组：hits -> hits
            JsonNode outerHits = rootNode.get("hits");
            if (outerHits == null) {
                return null;
            }

            JsonNode hitsArray = outerHits.get("hits");
            if (hitsArray == null || !hitsArray.isArray()) {
                return null;
            }

            // 4. 遍历数组提取goodsName
            StringBuilder goodsNames = new StringBuilder();
            Iterator<JsonNode> iterator = hitsArray.elements();
            while (iterator.hasNext()) {
                JsonNode hitNode = iterator.next();
                JsonNode sourceNode = hitNode.get("_source");
                if (sourceNode == null) {
                    continue;
                }

                JsonNode goodsNameNode = sourceNode.get("goodsName");
                if (goodsNameNode != null && !goodsNameNode.isNull()) {
                    String goodsName = goodsNameNode.asText().trim();
                    if (!goodsName.isEmpty()) {
                        if (goodsNames.length() > 0) {
                            goodsNames.append(",");
                        }
                        goodsNames.append(goodsName);
                    }
                }
            }

            return goodsNames.length() > 0 ? goodsNames.toString() : null;

        } catch (Exception e) {
            // 处理JSON解析异常
            e.printStackTrace(); // 实际项目中建议使用日志框架
            return null;
        }
    }
    private String extractPureJson(String rawString) {
        // 找到JSON的起始位置（第一个{或[）
        int startIndex = -1;
        for (int i = 0; i < rawString.length(); i++) {
            if ("{[".contains(String.valueOf(rawString.charAt(i)))) {
                startIndex = i;
                break;
            }
        }

        // 找不到有效的JSON起始字符
        if (startIndex == -1) {
            return null;
        }

        // 截取从起始字符到结尾的部分作为JSON
        return rawString.substring(startIndex);
    }
    // 统一处理VO赋值逻辑
    private void assignToVO(ActivitySeckillMemberVO vo, String metricKey,
                            String currentValue, Aggregate subAgg, MetricsConfig metric) {
        switch (metricKey) {
            case "storeName":
                vo.setStoreName(currentValue);
                break;
            case "memberName":
                vo.setMemberName(currentValue);
                break;
            case "memberMobile":
                vo.setMemberMobile(currentValue);
                break;
            case "payTime":
                vo.setPayTime(currentValue);
                break;
            case "commodityName":
                vo.setCommodityName(currentValue);
                break;
            case "payAmount":
                if (subAgg != null) {
                    double payAmount = getValue(metric, subAgg);
                    vo.setPayAmount(payAmount);
                }
                break;
            case "channelName":
                vo.setChannelName(currentValue);
                break;
            // 可添加默认分支处理未知指标
            default:
                // 日志记录未知指标，便于后续维护
                // log.warn("未处理的指标键: {}", metricKey);
                break;
        }
    }
    private double processFilterAggregate(FilterAggregate filterAgg) {
        // 实现Filter聚合的特殊处理逻辑
        // 例如：计算满足过滤条件的文档数
        return filterAgg.docCount();
    }

    private String getHitName(List<MetricsConfig> metrics, Aggregate aggregate) {
        MetricsConfig hitParam = metrics.stream()
                .filter(m -> m.getAggType().equals(AggregationType.HIT))
                .findFirst()
                .orElse(null);
        String hitName = "";

        if (hitParam != null) {
            Hit<JsonData> hit = aggregate.topHits().hits().hits().get(0);
            JsonData source = hit.source();
            if (source != null) {
                BzOrder order = source.to(BzOrder.class);
                switch (hitParam.getField()) {
                    case "storeName" -> hitName = order.getStoreName();
                    //渠道
                    case "evaluateState" -> hitName = activityApi.selectChannelName(order.getEvaluateState()).getData();
                    case "memberName" -> hitName = order.getMemberName();
                    case "takeAwayTel" ->
                            hitName = order.getTakeAwayTel() != null ? order.getTakeAwayTel() : order.getTakeAwayTel();
                    case "payTime" -> hitName = SimpleDateFormat.getDateTimeInstance().format(order.getCreateTime());
                    case "commodityName" ->
                            order.getProduct().stream().map(BzOrder.Product::getGoodsName).collect(Collectors.joining(","));
                }
            }
        }
        return hitName;
    }
    private String getMemberHitName(MetricsConfig metrics, Aggregate aggregate) {
        String hitName = "";
        // 创建指定格式的SimpleDateFormat
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
// 格式化日期
        if (metrics != null) {
            Hit<JsonData> hit = aggregate.topHits().hits().hits().get(0);
            JsonData source = hit.source();
            if (source != null) {
                BzOrder order = source.to(BzOrder.class);
                switch (metrics.getField()) {
                    case "storeName" -> hitName = order.getStoreName();
                    //渠道
                    case "evaluateState" -> hitName = activityApi.selectChannelName(order.getEvaluateState()).getData();
                    case "memberName" -> hitName = order.getMemberName();
                    case "takeAwayTel" ->
                            hitName = order.getTakeAwayTel() != null ? order.getTakeAwayTel() : order.getTakeAwayTel();
                    case "createTime" -> hitName =  sdf.format(order.getCreateTime());
                }
            }
        }
        return hitName;
    }

    private Builder getBuilder(EsAggDTO request) {
        // 1. 创建 BoolQuery 构建器
        Builder boolBuilder = new Builder();

        LocalDateTime[] times = request.getTimes();
        List<Long> storeIds = request.getStoreIds();
        List<Integer> orderFroms = request.getOrderFroms();

        //设置business条件
        boolBuilder.filter(m -> m.term(
                t -> t.field("businessId")
                        .value(FieldValue.of(BusinessContextHolder.getBusinessId()))
        ));

        // 2. 处理时间范围条件
        if (request.getSelectNoShow()) {
            boolBuilder.must(q -> q.bool(b -> b
                    .should(s -> s.range(r -> r.date(dr ->
                            dr.field("createTime")
                                    .gte(formatDateTime(times[0]))
                                    .lte(formatDateTime(times[1]))
                    )))
                    .should(s -> s.term(t -> t
                            .field("noShow")
                            .value(1)
                    ))
                    .minimumShouldMatch("1")
            ));
        } else {
            boolBuilder.filter(m -> m.range(
                    r -> r.date(dr -> dr.field("createTime").gte(formatDateTime(times[0])).lte(formatDateTime(times[1])))));
        }

        // 3. 处理 门店 ID 条件
        if (!CollectionUtils.isEmpty(storeIds)) {
            boolBuilder.filter(m -> m.terms(
                    t -> t.field("storeId").terms(tv -> tv.value(storeIds.stream().map(FieldValue::of).toList()))));
        } else {
            Set<Long> dataPermissionStoreIds = storeApi.getAllStoreIdByUser(SecurityFrameworkUtils.getLoginUserId())
                    .getCheckedData();
            if (CollectionUtils.isEmpty(dataPermissionStoreIds)) {
                throw exception(ErrorCodeConstants.DATA_PERMISSION_IS_EMPTY);
            }
            if (!CollectionUtils.isEmpty(dataPermissionStoreIds)) {
                boolBuilder.filter(m -> m.terms(t -> t.field("storeId")
                        .terms(tv -> tv.value(dataPermissionStoreIds.stream().map(FieldValue::of).toList()))));
            }
        }

        //4.处理 订单来源 条件
        if (!CollectionUtils.isEmpty(orderFroms)) {
            boolBuilder.filter(m -> m.terms(
                    t -> t.field("orderFrom").terms(tv -> tv.value(orderFroms.stream().map(FieldValue::of).toList()))));
        }

        //5.处理 是否新客 expressId   条件
        if (request.getExpressId() != null) {
            boolBuilder.filter(m -> m.term(
                    t -> t.field("expressId")
                            .value(FieldValue.of(request.getExpressId()))
            ));
        }

        //5.处理 是否会员 isSettlement 条件
        if (request.getIsSettlement() != null) {
            boolBuilder.filter(m -> m.term(
                    t -> t.field("isSettlement").value(FieldValue.of(request.getIsSettlement()))
            ));
        }

        // 模糊查询城市名称
        if (!StringUtils.isEmpty(request.getCityName())) {
            boolBuilder.must(mm -> mm.matchPhrase(t -> t.field("city").query(request.getCityName())));
        }

        // 模糊查询门店名称
        if (!StringUtils.isEmpty(request.getStoreName())) {
            boolBuilder.must(mm -> mm.matchPhrase(t -> t.field("storeName").query(request.getStoreName())));
        }

        if (ObjectUtil.isNotEmpty(request.getChannel())) {
            boolBuilder.filter(m -> m.term(
                t -> t.field("evaluateState")
                    .value(request.getChannel())
            ));
        }

        if (ObjectUtil.isNotEmpty(request.getChannelType())) {
            boolBuilder.filter(m -> m.term(
                t -> t.field("channelType")
                    .value(request.getChannelType())
            ));
        }
        return boolBuilder;
    }


    private Builder getBuilderTwo(EsAggDTO request) {
        // 1. 创建 BoolQuery 构建器
        Builder boolBuilder = new Builder();


        List<Long> storeIds = request.getStoreIds();
        Long activityId = request.getActivityId();

        //设置business条件
        boolBuilder.filter(m -> m.term(
                t -> t.field("businessId")
                        .value(FieldValue.of(BusinessContextHolder.getBusinessId()))
        ));


        if (request.getTimes() != null) {
            LocalDateTime[] times = request.getTimes();
            // 2. 处理时间范围条件
            boolBuilder.filter(m -> m.range(
                    r -> r.date(dr -> dr.field("createTime").gte(formatDateTime(times[0])).lte(formatDateTime(times[1])))));
        }
        boolBuilder.must(Query.of(q -> getProductQuery(activityId, q)));

        if (!CollectionUtils.isEmpty(request.getOrderS())) {
            boolBuilder.filter(m -> m.terms(
                    t -> t.field("orderState").terms(tv -> tv.value(request.getOrderS().stream().map(FieldValue::of).toList()))));
        }

        // 3. 处理 门店 ID 条件
        if (!CollectionUtils.isEmpty(storeIds)) {
            boolBuilder.filter(m -> m.terms(
                    t -> t.field("storeId").terms(tv -> tv.value(storeIds.stream().map(FieldValue::of).toList()))));
        } else {
            Set<Long> dataPermissionStoreIds = storeApi.getAllStoreIdByUser(SecurityFrameworkUtils.getLoginUserId())
                    .getCheckedData();
            if (CollectionUtils.isEmpty(dataPermissionStoreIds)) {
                throw exception(ErrorCodeConstants.DATA_PERMISSION_IS_EMPTY);
            }
            if (!CollectionUtils.isEmpty(dataPermissionStoreIds)) {
                boolBuilder.filter(m -> m.terms(t -> t.field("storeId")
                        .terms(tv -> tv.value(dataPermissionStoreIds.stream().map(FieldValue::of).toList()))));
            }
        }
        // 模糊查询门店名称
        if (!StringUtils.isEmpty(request.getStoreName())) {
            boolBuilder.must(m -> m.wildcard(mt ->
                    mt.field("storeName").value("*" + request.getStoreName() + "*")
            ));
        }
        if (ObjectUtil.isNotEmpty(request.getStoreId())) {
            boolBuilder.filter(m -> m.term(
                    t -> t.field("storeId")
                            .value(request.getStoreId())
            ));
        }
        if (ObjectUtil.isNotEmpty(request.getChannel())) {
            boolBuilder.filter(m -> m.term(
                    t -> t.field("evaluateState")
                            .value(request.getChannel())
            ));
        }

        if (ObjectUtil.isNotEmpty(request.getChannelType())) {
            boolBuilder.filter(m -> m.term(
                t -> t.field("channelType")
                    .value(request.getChannelType())
            ));
        }
        if (ObjectUtil.isNotEmpty(request.getMemberText())) {
            /*boolBuilder.should(m -> m.wildcard(mt ->
                    mt.field("memberName").value("*" + request.getMemberText() + "*")
            ));*/
            boolBuilder.filter(m -> m.term(mt ->
                    mt.field("takeAwayTel").value( request.getMemberText() )
            ));
          //  boolBuilder.minimumShouldMatch("1");
        }
        if (ObjectUtil.isNotEmpty(request.getCommodityId())) {
            boolBuilder.filter(m -> m.nested(
                    nested -> nested
                            .path("product")  // 嵌套对象的路径（即 product 字段）
                            .query(nq -> nq.term(t -> t.field("product.commodityId").value(request.getCommodityId())))
            ));
        }
        return boolBuilder;
    }

    private static ObjectBuilder<Query> getProductQuery(Long activityId, Query.Builder m) {
        return m.nested(n -> n.path("product").query(nq -> nq.bool(nb -> {
            if (activityId != null) {
                nb.must(mm -> mm.term(t -> t.field("product.activityId").value(activityId)));
            }
            return nb;
        })));
    }


    // 辅助方法：格式化 LocalDateTime 为 ISO 格式字符串
    private String formatDateTime(LocalDateTime dateTime) {
        ZonedDateTime zoned = dateTime.atZone(ZoneId.of("Asia/Shanghai"));
        return zoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * 折线图 和 指标查询 使用的 构建聚合，不同指标的条件 在query中拼接
     */
    private Aggregation buildAggregation(MetricsConfig param) {
        return switch (param.getAggType()) {
            case SUM -> Aggregation.of(a -> a.sum(s -> s.field(param.getField())));
            case AVG -> Aggregation.of(a -> a.avg(avg -> avg.field(param.getField())));
            case COUNT -> Aggregation.of(a -> a.valueCount(vc -> vc.field(param.getField())));
            case MAX -> Aggregation.of(a -> a.max(m -> m.field(param.getField())));
            case MIN -> Aggregation.of(a -> a.min(m -> m.field(param.getField())));
            case CARDINALITY -> Aggregation.of(a -> a.cardinality(m -> m.field(param.getField())));
            case HIT -> Aggregation.of(a -> a.topHits(th -> th
                    .size(1)  // 仅取第一个文档的
                    .source(so -> so
                            .filter(f -> f
                                    .includes(param.getField())  // 只包含 storeName 字段
                            )
                    )
            ));
            case MIN_COUNT -> Aggregation.of(agg -> agg.terms(t ->
                    t.field(param.getField())
                            .minDocCount(2)
                            .size(1000000)));
        };
    }


    // 获取聚合值
    private Double getValue(MetricsConfig param, Aggregate aggregate) {
        return switch (param.getAggType()) {
            case SUM -> {
                SumAggregate sum = aggregate.sum();
                yield sum != null ? sum.value() : 0.0;
            }
            case AVG -> {
                AvgAggregate avg = aggregate.avg();
                yield avg != null ? avg.value() : 0.0;
            }
            case COUNT -> {
                ValueCountAggregate count = aggregate.valueCount();
                yield count != null ? count.value() : 0.0;
            }
            case MAX -> {
                MaxAggregate max = aggregate.max();
                yield max != null ? max.value() : 0.0;
            }
            case MIN -> {
                MinAggregate min = aggregate.min();
                yield min != null ? min.value() : 0.0;
            }
            case CARDINALITY -> {
                CardinalityAggregate cardinality = aggregate.cardinality();
                yield cardinality != null ? cardinality.value() : 0.0;
            }
            case HIT -> {
                yield 0.0;
            }
            case MIN_COUNT -> {
                if (aggregate.isLterms()) {
                    yield (double) aggregate.lterms().buckets().array().size();
                } else {
                    yield (double) aggregate.sterms().buckets().array().size();
                }
            }
        };
    }

    /**
     * 解析聚合结果为 AnalysisRecordsVO 列表
     */
    private List<AnalysisRecordsVO> parseToAnalysisRecords(List<MetricsConfig> metrics, Aggregate aggregate) {
        List<AnalysisRecordsVO> recordsList = new ArrayList<>();

        // 处理长整型Terms聚合（如memberId为Long类型）
        if (aggregate.isLterms()) {
            Buckets<LongTermsBucket> buckets = aggregate.lterms().buckets();
            for (LongTermsBucket bucket : buckets.array()) {
                recordsList.add(buildAnalysisRecordsVO(metrics, bucket.key(), bucket.aggregations()));
            }
        }
        // 处理字符串型Terms聚合（如storeName为String类型）
        else if (aggregate.isSterms()) {
            Buckets<StringTermsBucket> buckets = aggregate.sterms().buckets();
            for (StringTermsBucket bucket : buckets.array()) {
                recordsList.add(buildAnalysisRecordsVO(metrics, bucket.key(), bucket.aggregations()));
            }
        }

        return recordsList;
    }

    /**
     * 构建单个 AnalysisRecordsVO 对象
     */
    private AnalysisRecordsVO buildAnalysisRecordsVO(List<MetricsConfig> metrics, Object
            groupKey, Map<String, Aggregate> aggregations) {
        AnalysisRecordsVO recordsVO = new AnalysisRecordsVO();
        TreeMap<String, Double> values = new TreeMap<>(); // 用于存储数值型指标

        // 设置分组键（如memberId、storeId等）

        // 解析每个指标
        for (MetricsConfig metric : metrics) {
            String metricCode = metric.getCode();
            Aggregate metricAgg = getMetricAggregate(aggregations, metric);
            if (metricAgg == null) {
                continue;
            }

            // 根据指标类型提取值并设置到VO
            switch (metricCode) {
                case "STORE_NAMES":
                    recordsVO.setStoreName(extractStringValue(metricAgg));
                    break;
                case "MEMBER_NAME":
                    recordsVO.setMemberName(extractStringValue(metricAgg));
                    break;
                case "MEMBER_MOBILE":
                    recordsVO.setMemberMobile(extractStringValue(metricAgg));
                    break;
                case "PAY_TIME":
                    recordsVO.setPayTime(formatPayTime(extractStringValue(metricAgg)));
                    break;
                case "COMMODITY_NAME":
                    recordsVO.setCommodityName(extractStringValue(metricAgg));
                    break;
                case "PAY_AMOUNTS":
                    double payAmount = getValue(metric, metricAgg);
                    recordsVO.setPayAmount(payAmount);
                    values.put(metricCode, payAmount);
                    break;
                case "CHANNEL_NAMES":
                    recordsVO.setChannelName(extractStringValue(metricAgg));
                    break;
                // 其他指标可在此扩展
                default:
                    // 处理数值型指标
                    if (isNumericMetric(metric)) {
                        values.put(metricCode, getValue(metric, metricAgg));
                    }
                    break;
            }
        }

        // 设置数值指标集合
        recordsVO.setValues(values);

        return recordsVO;
    }

    /**
     * 提取指标对应的聚合结果（处理嵌套聚合情况）
     */
    private Aggregate getMetricAggregate(Map<String, Aggregate> aggregations, MetricsConfig metric) {
        String metricCode = metric.getCode();
        Aggregate agg = aggregations.get(metricCode);
        if (agg == null) {
            return null;
        }

        // 处理嵌套聚合（如商品相关指标）
        if ("offerAmount".equals(metricCode)) {
            return agg.nested().aggregations().get("promotion_agg");
        } else if ("commodityCount".equals(metricCode)) {
            return agg.nested()
                    .aggregations().get("activity_products")
                    .filter().aggregations().get("total_goods_num");
        }

        // 普通聚合直接返回（如果有filter嵌套需特殊处理）
        if (agg.isFilter()) {
            return agg.filter().aggregations().get(metricCode);
        }
        return agg;
    }

    /**
     * 从聚合结果中提取字符串值
     */
    private String extractStringValue(Aggregate aggregate) {
        // 处理值计数聚合（ValueCount）
        if (aggregate.valueCount() != null) {
            return String.valueOf(aggregate.valueCount().value());
        }
        // 处理字符串 Terms 聚合（StringTermsAggregate）
        else if (aggregate.sterms() != null) {
            Buckets<StringTermsBucket> buckets = aggregate.sterms().buckets();
            if (!buckets.array().isEmpty()) {
                return buckets.array().get(0).key().toString();
            }
        }
        // 处理长整型 Terms 聚合（LongTermsAggregate）
        else if (aggregate.lterms() != null) {
            Buckets<LongTermsBucket> buckets = aggregate.lterms().buckets();
            if (!buckets.array().isEmpty()) {
                return buckets.array().get(0).key() + "";
            }
        }
        // 其他聚合类型默认返回 null
        return null;
    }

    /**
     * 格式化支付时间
     */
    private String formatPayTime(String timeStr) {
        if (StringUtils.isBlank(timeStr)) {
            return null;
        }
        try {
            // 根据实际时间格式调整（示例：yyyy-MM-dd HH:mm:ss）
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(timeStr);
            return zonedDateTime.format(outputFormatter);
        } catch (Exception e) {
            log.warn("时间格式化失败: {}", timeStr, e);
            return timeStr;
        }
    }

    /**
     * 判断是否为数值型指标
     */
    private boolean isNumericMetric(MetricsConfig metric) {
        AggregationType type = metric.getAggType();
        return type == SUM || type == AggregationType.AVG
                || type == AggregationType.MAX || type == AggregationType.MIN;
    }

    /**
     * 提取门店ID列表
     */
    private List<Long> extractStoreIds(Map<String, Aggregate> aggregations) {
        Aggregate storeAgg = aggregations.get("store_ids");
        if (storeAgg == null || !storeAgg.isLterms()) {
            return Collections.emptyList();
        }
        return storeAgg.lterms().buckets().array().stream()
                .map(LongTermsBucket::key)
                .collect(Collectors.toList());
    }

    /**
     * 从聚合结果中提取具体值（根据聚合类型适配）
     */
    private Object extractValueFromAggregate(Aggregate agg) {
        if (agg == null) {
            return null;
        }
        // 适配常见聚合类型（根据实际buildAggregation实现调整）
        if (agg.max() != null) {
            return agg.max().value(); // max聚合取最大值
        } else if (agg.sum() != null) {
            return agg.sum().value(); // sum聚合取总和
        } else if (agg.avg() != null) {
            return agg.avg().value(); // avg聚合取平均值
        }
        return null;
    }

    private Builder getBuilderChannel(EsAggDTO request, int type) {
        // 1. 创建 BoolQuery 构建器
        Builder boolBuilder = new Builder();


        List<Long> storeIds = request.getStoreIds();
        Long activityId = request.getActivityId();

        //设置business条件
        boolBuilder.filter(m -> m.term(
                t -> t.field("businessId")
                        .value(FieldValue.of(BusinessContextHolder.getBusinessId()))
        ));


        if (request.getTimes() != null) {
            LocalDateTime[] times = request.getTimes();
            // 2. 处理时间范围条件
            boolBuilder.filter(m -> m.range(
                    r -> r.date(dr -> dr.field("createTime").gte(formatDateTime(times[0])).lte(formatDateTime(times[1])))));
        }
        boolBuilder.must(Query.of(q -> getProductQuery(activityId, q)));

        if (!CollectionUtils.isEmpty(request.getOrderS())) {
            boolBuilder.filter(m -> m.terms(
                    t -> t.field("orderState").terms(tv -> tv.value(request.getOrderS().stream().map(FieldValue::of).toList()))));
        }

        // 3. 处理 门店 ID 条件
        if (type == 1) {
            if (!CollectionUtils.isEmpty(storeIds)) {
                boolBuilder.filter(m -> m.terms(
                        t -> t.field("storeId").terms(tv -> tv.value(storeIds.stream().map(FieldValue::of).toList()))));
            } else {
                Set<Long> dataPermissionStoreIds = storeApi.getAllStoreIdByUser(SecurityFrameworkUtils.getLoginUserId())
                        .getCheckedData();
                if (CollectionUtils.isEmpty(dataPermissionStoreIds)) {
                    throw exception(ErrorCodeConstants.DATA_PERMISSION_IS_EMPTY);
                }
                if (!CollectionUtils.isEmpty(dataPermissionStoreIds)) {
                    boolBuilder.filter(m -> m.terms(t -> t.field("storeId")
                            .terms(tv -> tv.value(dataPermissionStoreIds.stream().map(FieldValue::of).toList()))));
                }
            }
        }
        // 模糊查询门店名称
        if (!StringUtils.isEmpty(request.getStoreName())) {
            boolBuilder.must(m -> m.wildcard(mt ->
                    mt.field("storeName").value("*" + request.getStoreName() + "*")
            ));
        }
        return boolBuilder;
    }
}