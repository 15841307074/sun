package com.htyoudao.youdao.module.analysis.service.impl;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;

import cn.hutool.core.util.NumberUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation.Builder;
import co.elastic.clients.elasticsearch._types.aggregations.Buckets;
import co.elastic.clients.elasticsearch._types.aggregations.CardinalityAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.CompositeAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.CompositeAggregationSource;
import co.elastic.clients.elasticsearch._types.aggregations.CompositeBucket;
import co.elastic.clients.elasticsearch._types.aggregations.FilterAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.aggregations.SumAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.ValueCountAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.ValueType;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.util.NamedValue;
import com.alibaba.fastjson.JSON;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.ProductPageDownloadExcelVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisTopVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.product.ProductResult;
import com.htyoudao.youdao.module.analysis.dal.es.BzOrder;
import com.htyoudao.youdao.module.analysis.dal.es.BzOrderProduct;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.enums.EventType;
import com.htyoudao.youdao.module.analysis.enums.ProductMetricsConfigNew;
import com.htyoudao.youdao.module.analysis.enums.ProductMetricsConfigNew.AggregationType;
import com.htyoudao.youdao.module.analysis.service.IEventService;
import com.htyoudao.youdao.module.analysis.service.IProductAggerationService;
import com.htyoudao.youdao.module.analysis.service.dto.EventQueryDTO;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.commodity.api.DTO.AfterOrderSimpleDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.CommodityHiddenFilterDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSpuCountDTO;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreSimpleResDto;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class ProductAggerationServiceImplNew implements IProductAggerationService {

    @Resource
    private ElasticsearchClient client;//8.5.1

    @Resource
    private IEventService eventService;

    @DubboReference
    private CommodityApi commodityApi;

    @DubboReference
    private StoreApi storeApi;


    @Override
    public PageResult<ProductResult> getProductPage(ProductPageRequest request) {
        if (isPurchaseQuery(request)) {
            return getPurchaseProductPage(request);
        }

        // 计算分页参数
        int from = (request.getPageNo() - 1) * request.getPageSize();
        int requiredSize = from + request.getPageSize();
        String groupField = "commodityId";

        List<ProductMetricsConfigNew> metricsConfigs = request.getMetrics().stream()
            .map(ProductMetricsConfigNew::getEnumByCode).filter(Objects::nonNull).toList();

        //设置查询条件
        BoolQuery.Builder builder = getBuilder(request);

        Query query = new Query.Builder().bool(builder.build()).build();

        // 构建排序规则
        TermsAggregation.Builder termBuilder = new TermsAggregation.Builder()
            .field(groupField)
            .size(requiredSize);
        SortOrder sortOrder = Objects.equals(request.getSortOrder(), "asc") ? SortOrder.Asc : SortOrder.Desc;

        if (StringUtils.isNotBlank(request.getSortBy())) {
            String name = request.getSortBy()+ ">" + request.getSortBy(); //聚合排序
            termBuilder.order(new NamedValue<>(name, sortOrder));
        }

        // 构建指标聚合
        Map<String, Aggregation> aggregations = metricsConfigs.stream()
            .collect(Collectors.toMap(
                ProductMetricsConfigNew::getCode,
                this::buildFilterAggregation
            ));


        SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
            .index("bz_order_product")
            .query(query)
            .aggregations(groupField,
                a -> a.terms(termBuilder.build())
                    .aggregations(aggregations) // 添加指标聚合
                    .aggregations("bucket_sort_agg", bsa -> bsa
                        .bucketSort(bs -> bs
                            .from(from)
                            .size(request.getPageSize())
                        )
                    )
            )
            .aggregations("total", total -> total
                .cardinality(c -> c.field("commodityId"))
            ));


        SearchResponse<BzOrderProduct> response = null;
        try {
            response = client.search(searchRequest, BzOrderProduct.class);
        } catch (Exception e) {
            log.error("product page es request failed, request={}", JSON.toJSONString(request), e);
            throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
        }

        Long total = response.aggregations().get("total").cardinality().value();
        Aggregate aggregate = response.aggregations().get(groupField);
        List<ProductResult> extracted = extracted(metricsConfigs, aggregate);
        PageResult<ProductResult> mapPageResult = new PageResult<>();
        mapPageResult.setTotal(total);
        mapPageResult.setList(extracted);

        return mapPageResult;
    }

    private List<ProductResult> extracted(List<ProductMetricsConfigNew> metrics, Aggregate aggregate) {
        List<ProductResult> list = new ArrayList<>();

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

    private ProductResult processBucket(Object rawKey, Map<String, Aggregate> aggs, List<ProductMetricsConfigNew> metrics) {
        TreeMap<String, Object> resultMap = new TreeMap<>();

        for (ProductMetricsConfigNew metric : metrics) {
            String metricKey = metric.getCode();
            aggs.get(metricKey);
            FilterAggregate filter = aggs.get(metricKey).filter();
            Aggregate subAgg = filter.aggregations().get(metricKey);
            resultMap.put(metricKey, getValue(metric, subAgg));
        }

        ProductResult item = JSON.parseObject(JSON.toJSONString(resultMap), ProductResult.class);

        // 直接从 rawKey 设置 commodityId，避免 JSON 序列化精度丢失
        if (rawKey instanceof Long) {
            item.setCommodityId((Long) rawKey);
        } else if (rawKey instanceof String) {
            try {
                item.setCommodityId(Long.parseLong((String) rawKey));
            } catch (NumberFormatException e) {
                log.warn("无法将 rawKey 转换为 Long: {}", rawKey);
            }
        }

        //复购率
        if (item.getRepurchaseUserCount() != null && item.getCustomerCount() != null && item.getCustomerCount() != 0){
            item.setRepurchaseRate(NumberUtil.div(item.getRepurchaseUserCount(), item.getCustomerCount()));
        }

        //新客占比
        if (item.getNewCustomerCount() != null && item.getCustomerCount() != null && item.getCustomerCount() != 0){
            item.setNewCustomerRate(NumberUtil.div(item.getNewCustomerCount(), item.getCustomerCount()));
        }

        return item;
    }



    @Override
    public PageResult<ProductResult> getProductPageByCursor(ProductPageRequest request) {
        if (isPurchaseQuery(request)) {
            return getPurchaseProductPageByCursor(request);
        }
        String groupField = "commodityId";

        List<ProductMetricsConfigNew> metricsConfigs = request.getMetrics().stream()
            .map(ProductMetricsConfigNew::getEnumByCode).filter(Objects::nonNull).toList();

        // 设置查询条件
        BoolQuery.Builder builder = getBuilder(request);
        Query query = new Query.Builder().bool(builder.build()).build();

        // 构建 composite 聚合源
        List<Map<String, CompositeAggregationSource>> compositeSources = new ArrayList<>();

        // 主要分组字段：商品ID
        compositeSources.add(Map.of(groupField, CompositeAggregationSource.of(cas -> cas
            .terms(t -> t
                .field(groupField)
                .missingBucket(false)
                .valueType(ValueType.Long)
            )
        )));

        // 如果有排序字段，添加为第二个聚合源
        if (StringUtils.isNotBlank(request.getSortBy())) {
            SortOrder sortOrder = Objects.equals(request.getSortOrder(), "asc") ? SortOrder.Asc : SortOrder.Desc;
            compositeSources.add(Map.of("sort_field", CompositeAggregationSource.of(cas -> cas
                .terms(t -> t
                    .field(request.getSortBy())
                    .order(sortOrder)
                    .missingBucket(false)
                )
            )));
        }

        // 构建指标聚合
        Map<String, Aggregation> aggregations = metricsConfigs.stream()
            .collect(Collectors.toMap(
                ProductMetricsConfigNew::getCode,
                this::buildFilterAggregation
            ));

        // 构建 composite 聚合
        Aggregation compositeAgg = Aggregation.of(a -> a
            .composite(c -> {
                var cb = c.sources(compositeSources).size(request.getPageSize());
                // 如果有游标，设置 after
                if (request.getAfterKey() != null && !request.getAfterKey().isEmpty()) {
                    // 验证 afterKey 格式 - 至少应包含 commodityId
                    if (!request.getAfterKey().containsKey("commodityId")) {
                        log.warn("Invalid afterKey format: missing required field 'commodityId'. afterKey: {}", request.getAfterKey());
                        throw exception(ErrorCodeConstants.ES_QUERY_ERROR, "无效的游标格式");
                    }

                    // 需要将 Map<String, Object> 转换为 Map<String, FieldValue>
                    Map<String, FieldValue> afterKeyMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : request.getAfterKey().entrySet()) {
                        Object value = entry.getValue();
                        FieldValue fieldValue;

                        // 根据值类型创建对应的 FieldValue
                        if (value == null) {
                            // 跳过 null 值，ES composite after 不支持 null
                            log.warn("Skipping null value for key: {}", entry.getKey());
                            continue;
                        } else if (value instanceof Long) {
                            fieldValue = FieldValue.of((Long) value);
                        } else if (value instanceof Integer) {
                            fieldValue = FieldValue.of(((Integer) value).longValue());
                        } else if (value instanceof Double) {
                            fieldValue = FieldValue.of((Double) value);
                        } else if (value instanceof Float) {
                            fieldValue = FieldValue.of(((Float) value).doubleValue());
                        } else if (value instanceof Boolean) {
                            fieldValue = FieldValue.of((Boolean) value);
                        } else if (value instanceof String) {
                            fieldValue = FieldValue.of((String) value);
                        } else {
                            // 对于其他类型，尝试转换为字符串
                            log.debug("Converting unknown type {} to string for key: {}", value.getClass().getSimpleName(), entry.getKey());
                            fieldValue = FieldValue.of(value.toString());
                        }

                        afterKeyMap.put(entry.getKey(), fieldValue);
                    }

                    if (!afterKeyMap.isEmpty()) {
                        log.debug("Using cursor pagination with afterKey: {}", afterKeyMap.keySet());
                        cb.after(afterKeyMap);
                    } else {
                        log.warn("afterKey conversion resulted in empty map, skipping cursor");
                    }
                }
                return cb;
            })
            .aggregations(aggregations) // 分别添加指标聚合
        );

        SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
            .index("bz_order_product")
            .query(query)
            .aggregations("composite_agg", compositeAgg)
            .aggregations("total", total -> total
                .cardinality(c -> c.field(groupField))
            ));

        SearchResponse<BzOrderProduct> response = null;
        try {
            response = client.search(searchRequest, BzOrderProduct.class);
        } catch (Exception e) {
            log.error("cursor pagination request failed, request={}", JSON.toJSONString(request), e);
            throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
        }

        Long total = response.aggregations().get("total").cardinality().value();
        CompositeAggregate compositeAggregate = response.aggregations().get("composite_agg").composite();

        List<ProductResult> extracted = extractedFromComposite(metricsConfigs, compositeAggregate);

        PageResult<ProductResult> mapPageResult = new PageResult<>();
        mapPageResult.setTotal(total);
        mapPageResult.setList(extracted);

        // 设置下一页游标
        if (compositeAggregate.afterKey() != null && !compositeAggregate.afterKey().isEmpty()) {
            // 将 Map<String, FieldValue> 转换为 Map<String, Object>
            Map<String, Object> afterKeyMap = new HashMap<>();
            for (Map.Entry<String, FieldValue> entry : compositeAggregate.afterKey().entrySet()) {
                FieldValue fieldValue = entry.getValue();
                Object value;

                // 根据 FieldValue 的类型提取实际值
                if (fieldValue.isLong()) {
                    value = fieldValue.longValue();
                } else if (fieldValue.isString()) {
                    value = fieldValue.stringValue();
                } else if (fieldValue.isDouble()) {
                    value = fieldValue.doubleValue();
                } else if (fieldValue.isBoolean()) {
                    value = fieldValue.booleanValue();
                } else {
                    // fallback 为字符串
                    value = fieldValue.toString();
                }

                afterKeyMap.put(entry.getKey(), value);
            }
            mapPageResult.setAfterKey(afterKeyMap);
        }

        return mapPageResult;
    }

    /**
     * 从 composite 聚合结果中提取产品数据
     */
    private List<ProductResult> extractedFromComposite(List<ProductMetricsConfigNew> metrics, CompositeAggregate compositeAggregate) {
        List<ProductResult> list = new ArrayList<>();

        for (CompositeBucket bucket : compositeAggregate.buckets().array()) {
            Map<String, FieldValue> key = bucket.key();
            Object commodityId = key.get("commodityId").longValue(); // 获取商品ID

            list.add(processBucket(commodityId, bucket.aggregations(), metrics));
        }

        return list;
    }

    @Override
    public PageResult<ProductResult> realTimeProductPage(ProductPageRequest request) {
        PageResult<ProductResult> resultPageResult = getProductPage(request);

        List<ProductResult> results = resultPageResult.getList();

        fillRealtimeMetrics(request, results);


        return resultPageResult;
    }

    private boolean isPurchaseQuery(ProductPageRequest request) {
        return Objects.equals(request.getIsSingle(), 3);
    }

    private PageResult<ProductResult> getPurchaseProductPage(ProductPageRequest request) {
        List<PurchaseProductRow> rows = listSortedPurchaseProducts(request);
        int from = Math.max((request.getPageNo() - 1) * request.getPageSize(), 0);
        int to = Math.min(from + request.getPageSize(), rows.size());
        List<ProductResult> currentPage = from >= rows.size() ? List.of()
            : rows.subList(from, to).stream().map(PurchaseProductRow::result).toList();

        PageResult<ProductResult> pageResult = new PageResult<>();
        pageResult.setTotal((long) rows.size());
        pageResult.setList(currentPage);
        return pageResult;
    }

    private PageResult<ProductResult> getPurchaseProductPageByCursor(ProductPageRequest request) {
        List<PurchaseProductRow> rows = listSortedPurchaseProducts(request);
        return buildPurchaseProductCursorPage(request, rows);
    }

    private PageResult<ProductResult> getPurchaseProductPageByCursorWithAfterOrders(ProductPageRequest request,
        List<AfterOrderSimpleDTO> afterOrders) {
        return buildPurchaseProductCursorPage(request, listSortedPurchaseProducts(request, afterOrders));
    }

    private PageResult<ProductResult> buildPurchaseProductCursorPage(ProductPageRequest request,
        List<PurchaseProductRow> rows) {
        int start = 0;
        if (!CollectionUtils.isEmpty(request.getAfterKey()) && request.getAfterKey().containsKey("afterId")) {
            Long afterId = Long.valueOf(String.valueOf(request.getAfterKey().get("afterId")));
            for (int i = 0; i < rows.size(); i++) {
                if (Objects.equals(rows.get(i).afterId(), afterId)) {
                    start = i + 1;
                    break;
                }
            }
        }

        int end = Math.min(start + request.getPageSize(), rows.size());
        List<PurchaseProductRow> currentPage = start >= rows.size() ? List.of() : rows.subList(start, end);

        PageResult<ProductResult> pageResult = new PageResult<>();
        pageResult.setTotal((long) rows.size());
        pageResult.setList(currentPage.stream().map(PurchaseProductRow::result).toList());
        if (end < rows.size() && !currentPage.isEmpty()) {
            pageResult.setAfterKey(Map.of("afterId", currentPage.get(currentPage.size() - 1).afterId()));
        }
        return pageResult;
    }

    private List<PurchaseProductRow> listSortedPurchaseProducts(ProductPageRequest request) {
        List<AfterOrderSimpleDTO> afterOrders = listFilteredAfterOrders(request);
        return listSortedPurchaseProducts(request, afterOrders);
    }

    private List<PurchaseProductRow> listSortedPurchaseProducts(ProductPageRequest request,
        List<AfterOrderSimpleDTO> afterOrders) {
        if (CollectionUtils.isEmpty(afterOrders)) {
            return List.of();
        }

        Map<Long, AfterOrderSimpleDTO> uniqueAfterOrders = afterOrders.stream()
            .collect(Collectors.toMap(AfterOrderSimpleDTO::getCommodityId, item -> item, (left, right) -> left));

        Map<Long, ProductResult> salesMap = queryPurchaseSalesMap(request,
            new ArrayList<>(uniqueAfterOrders.keySet()));

        Comparator<PurchaseProductRow> comparator = buildPurchaseComparator(request);
        return uniqueAfterOrders.values().stream()
            .map(item -> new PurchaseProductRow(item.getAfterId(), buildPurchaseProductResult(item, salesMap.get(item.getCommodityId()))))
            .sorted(comparator)
            .toList();
    }

    private List<AfterOrderSimpleDTO> listFilteredAfterOrders(ProductPageRequest request) {
        List<AfterOrderSimpleDTO> checkedData = commodityApi.getAfterOrderSimpleList().getCheckedData();
        return listFilteredAfterOrders(request, checkedData);
    }

    private List<AfterOrderSimpleDTO> listFilteredAfterOrders(ProductPageRequest request,
        List<AfterOrderSimpleDTO> checkedData) {
        if (CollectionUtils.isEmpty(checkedData)) {
            return List.of();
        }
        return checkedData.stream()
            .filter(item -> item.getCommodityId() != null)
            .filter(item -> StringUtils.isBlank(request.getGoodsName()) || StringUtils.contains(item.getCommodityName(), request.getGoodsName()))
            .filter(item -> CollectionUtils.isEmpty(request.getCommodityIds()) || request.getCommodityIds().contains(item.getCommodityId()))
            .toList();
    }

    private ProductResult buildPurchaseProductResult(AfterOrderSimpleDTO item, ProductResult sales) {
        ProductResult result = new ProductResult();
        result.setCommodityId(item.getCommodityId());
        result.setGoodsName(item.getCommodityName());
        result.setGoodsImage(item.getThumbnailUrl());
        result.setIsPurchase(1);
        result.setSalesAmount(0D);
        result.setSalesVolume(0);
        result.setSingleSalesVolume(0);
        result.setAllSalesVolume(0);
        result.setOrderCount(0);
        result.setCustomerCount(0L);
        result.setRepurchaseUserCount(0L);
        result.setNewCustomerCount(0L);

        if (sales != null) {
            result.setSalesAmount(sales.getSalesAmount());
            result.setSalesVolume(sales.getSalesVolume());
            result.setSingleSalesVolume(sales.getSingleSalesVolume());
            result.setAllSalesVolume(sales.getAllSalesVolume());
            result.setOrderCount(sales.getOrderCount());
            result.setCustomerCount(sales.getCustomerCount());
            result.setRepurchaseUserCount(sales.getRepurchaseUserCount());
            result.setRepurchaseRate(sales.getRepurchaseRate());
            result.setNewCustomerRate(sales.getNewCustomerRate());
            result.setNewCustomerCount(sales.getNewCustomerCount());
        }
        return result;
    }

    private Comparator<PurchaseProductRow> buildPurchaseComparator(ProductPageRequest request) {
        Comparator<PurchaseProductRow> comparator = switch (normalizePurchaseSortBy(request.getSortBy())) {
            case "salesAmount" -> Comparator.comparingDouble(row -> defaultDouble(row.result().getSalesAmount()));
            case "salesVolume" -> Comparator.comparingLong(row -> defaultLong(row.result().getSalesVolume()));
            case "allSalesVolume" -> Comparator.comparingLong(row -> defaultLong(row.result().getAllSalesVolume()));
            default -> Comparator.comparing(PurchaseProductRow::afterId, Comparator.nullsLast(Long::compareTo));
        };

        if (!Objects.equals(request.getSortOrder(), "asc")) {
            comparator = comparator.reversed();
        }

        return comparator.thenComparing(PurchaseProductRow::afterId, Comparator.nullsLast(Long::compareTo));
    }

    private String normalizePurchaseSortBy(String sortBy) {
        return switch (StringUtils.defaultString(sortBy)) {
            case "salesAmount", "allSalesVolume" -> sortBy;
            default -> "";
        };
    }

    private long defaultLong(Number value) {
        return value == null ? 0L : value.longValue();
    }

    private double defaultDouble(Number value) {
        return value == null ? 0D : value.doubleValue();
    }

    private Map<Long, ProductResult> queryPurchaseSalesMap(ProductPageRequest request, List<Long> commodityIds) {
        if (CollectionUtils.isEmpty(commodityIds)) {
            return Map.of();
        }
        ProductPageRequest queryRequest = new ProductPageRequest();
        queryRequest.setCurrentTimeStart(request.getCurrentTimeStart());
        queryRequest.setCurrentTimeEnd(request.getCurrentTimeEnd());
        queryRequest.setSortBy("");
        queryRequest.setSortOrder(request.getSortOrder());
        queryRequest.setStoreIds(request.getStoreIds());
        queryRequest.setGoodsName(null);
        queryRequest.setIsSingle(3);
        queryRequest.setRealTime(false);
        queryRequest.setCommodityIds(commodityIds);
        queryRequest.setMetrics(request.getMetrics());
        queryRequest.setRangeHours(request.getRangeHours());
        queryRequest.setStatType(request.getStatType());
        queryRequest.setChannelIds(request.getChannelIds());
        queryRequest.setCouponIds(request.getCouponIds());
        queryRequest.setActivityIds(request.getActivityIds());
        queryRequest.setPageNo(1);
        queryRequest.setPageSize(commodityIds.size());

        return getEsProductPage(queryRequest).getList().stream()
            .peek(item -> {
                item.setIsPurchase(1);
            })
            .collect(Collectors.toMap(ProductResult::getCommodityId, item -> item, (left, right) -> right));
    }

    private record PurchaseProductRow(Long afterId, ProductResult result) {
    }

    private PageResult<ProductResult> getEsProductPage(ProductPageRequest request) {
        int from = (request.getPageNo() - 1) * request.getPageSize();
        int requiredSize = from + request.getPageSize();
        String groupField = "commodityId";

        List<ProductMetricsConfigNew> metricsConfigs = request.getMetrics().stream()
            .map(ProductMetricsConfigNew::getEnumByCode).filter(Objects::nonNull).toList();

        BoolQuery.Builder builder = getBuilder(request);
        Query query = new Query.Builder().bool(builder.build()).build();

        TermsAggregation.Builder termBuilder = new TermsAggregation.Builder()
            .field(groupField)
            .size(requiredSize);

        Map<String, Aggregation> aggregations = metricsConfigs.stream()
            .collect(Collectors.toMap(ProductMetricsConfigNew::getCode, this::buildFilterAggregation));

        SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
            .index("bz_order_product")
            .query(query)
            .aggregations(groupField,
                a -> a.terms(termBuilder.build())
                    .aggregations(aggregations)
                    .aggregations("bucket_sort_agg", bsa -> bsa
                        .bucketSort(bs -> bs
                            .from(from)
                            .size(request.getPageSize())
                        )
                    )
            )
            .aggregations("total", total -> total
                .cardinality(c -> c.field("commodityId"))
            ));

        SearchResponse<BzOrderProduct> response;
        try {
            response = client.search(searchRequest, BzOrderProduct.class);
        } catch (Exception e) {
            log.error("purchase sales es request failed, request={}", JSON.toJSONString(request), e);
            throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
        }

        PageResult<ProductResult> pageResult = new PageResult<>();
        pageResult.setTotal(response.aggregations().get("total").cardinality().value());
        pageResult.setList(extracted(metricsConfigs, response.aggregations().get(groupField)));
        return pageResult;
    }

    @Override
    public List<ProductPageDownloadExcelVO> productPageDownload(ProductPageRequest request) {
        List<ProductResult> currentPageProducts = getProductPage(request).getList();
        if (CollectionUtils.isEmpty(currentPageProducts)) {
            return List.of();
        }

        List<Long> targetStoreIds = request.getStoreIds();
        List<Long> currentPageCommodityIds = currentPageProducts.stream().map(ProductResult::getCommodityId).toList();
        String timeLabel = buildExportTimeLabel(request);
        List<ProductPageDownloadExcelVO> result = new ArrayList<>();
        Map<Long, String> storeNameMap = buildStoreNameMap(targetStoreIds);
        Map<Long, Long> saleStoreCountMap = buildSaleStoreCountMap(currentPageProducts, targetStoreIds, request.getMetrics());
        List<AfterOrderSimpleDTO> afterOrders = isPurchaseQuery(request) ? listFilteredAfterOrders(request) : List.of();

        for (Long storeId : targetStoreIds) {
            ProductPageRequest storeRequest = copyForSingleStore(request, storeId);
            storeRequest.setCommodityIds(currentPageCommodityIds);
            storeRequest.setPageNo(1);
            storeRequest.setPageSize(currentPageCommodityIds.size());
            String storeName = storeNameMap.getOrDefault(storeId, String.valueOf(storeId));
            List<ProductResult> pageList = buildStoreDownloadProducts(storeRequest, currentPageProducts, afterOrders);
            fillRealtimeMetrics(storeRequest, pageList, false);
            applySaleStoreCount(pageList, saleStoreCountMap);

            for (ProductResult item : pageList) {
                ProductPageDownloadExcelVO vo = new ProductPageDownloadExcelVO();
                vo.setGoodsName(item.getGoodsName());
                vo.setCategoryName(item.getCategoryName());
                vo.setIsSingle(item.getIsSingle());
                vo.setStoreName(storeName);
                vo.setTime(timeLabel);
                vo.setSalesVolume(item.getSalesVolume());
                vo.setSingleSalesVolume(item.getSingleSalesVolume());
                vo.setAllSalesVolume(item.getAllSalesVolume());
                vo.setSalesAmount(item.getSalesAmount());
                vo.setOrderCount(item.getOrderCount());
                vo.setCustomerCount(item.getCustomerCount());
                vo.setRepurchaseUserCount(item.getRepurchaseUserCount());
                vo.setRepurchaseRate(item.getRepurchaseRate() == null ? 0.0 : item.getRepurchaseRate().doubleValue());
                vo.setNewCustomerCount(item.getNewCustomerCount());
                vo.setNewCustomerRate(item.getNewCustomerRate() == null ? 0.0 : item.getNewCustomerRate().doubleValue());
                vo.setClickUserCount(item.getClickUserCount());
                vo.setAddCartUserCount(item.getAddCartUserCount());
                vo.setStoreCount(item.getStoreCount());
                result.add(vo);
            }
        }

        return result;
    }

    @Override
    public Map<Long, List<ProductResult>> getProductDownloadDataByStore(ProductPageRequest request) {
        if (CollectionUtils.isEmpty(request.getStoreIds())) {
            return Map.of();
        }

        List<ProductMetricsConfigNew> metricsConfigs = request.getMetrics().stream()
            .map(ProductMetricsConfigNew::getEnumByCode)
            .filter(Objects::nonNull)
            .toList();

        BoolQuery.Builder builder = getBuilder(request);
        Query query = new Query.Builder().bool(builder.build()).build();
        Map<String, Aggregation> aggregations = metricsConfigs.stream()
            .collect(Collectors.toMap(ProductMetricsConfigNew::getCode, this::buildFilterAggregation));

        Map<Long, List<ProductResult>> result = new HashMap<>();
        Map<String, FieldValue> afterKey = null;

        do {
            SearchRequest searchRequest = buildProductDownloadSearchRequest(query, aggregations, afterKey);
            SearchResponse<BzOrderProduct> response;
            try {
                response = client.search(searchRequest, BzOrderProduct.class);
            } catch (IOException e) {
                log.error("product download es request failed, request={}", JSON.toJSONString(request), e);
                throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
            }

            CompositeAggregate compositeAggregate = response.aggregations().get("download_composite").composite();
            appendProductDownloadBuckets(metricsConfigs, compositeAggregate, result);
            afterKey = compositeAggregate.afterKey();
        } while (afterKey != null && !afterKey.isEmpty());

        if (request.getMetrics().contains("clickUserCount") || request.getMetrics().contains("addCartUserCount")) {
            fillDownloadRealtimeMetrics(request, result);
        }

        return result;
    }

    private SearchRequest buildProductDownloadSearchRequest(Query query, Map<String, Aggregation> aggregations,
        Map<String, FieldValue> afterKey) {
        List<Map<String, CompositeAggregationSource>> sources = new ArrayList<>();
        sources.add(Map.of("storeId", CompositeAggregationSource.of(cas -> cas.terms(t -> t
            .field("storeId")
            .missingBucket(false)
            .valueType(ValueType.Long)))));
        sources.add(Map.of("commodityId", CompositeAggregationSource.of(cas -> cas.terms(t -> t
            .field("commodityId")
            .missingBucket(false)
            .valueType(ValueType.Long)))));

        return SearchRequest.of(s -> s.size(0)
            .index("bz_order_product")
            .query(query)
            .aggregations("download_composite", a -> a
                .composite(c -> {
                    var builder = c.sources(sources).size(1000);
                    if (afterKey != null && !afterKey.isEmpty()) {
                        builder.after(afterKey);
                    }
                    return builder;
                })
                .aggregations(aggregations)
            ));
    }

    private void appendProductDownloadBuckets(List<ProductMetricsConfigNew> metricsConfigs,
        CompositeAggregate compositeAggregate, Map<Long, List<ProductResult>> result) {
        for (CompositeBucket bucket : compositeAggregate.buckets().array()) {
            Map<String, FieldValue> key = bucket.key();
            Long storeId = getCompositeLongValue(key.get("storeId"));
            Long commodityId = getCompositeLongValue(key.get("commodityId"));
            if (storeId == null || commodityId == null) {
                continue;
            }
            ProductResult item = processBucket(commodityId, bucket.aggregations(), metricsConfigs);
            result.computeIfAbsent(storeId, ignored -> new ArrayList<>()).add(item);
        }
    }

    private Long getCompositeLongValue(FieldValue fieldValue) {
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
                log.warn("failed to parse composite key to long, value={}", fieldValue.stringValue());
            }
        }
        return null;
    }

    private void fillDownloadRealtimeMetrics(ProductPageRequest request, Map<Long, List<ProductResult>> storeProductMap) {
        if (CollectionUtils.isEmpty(storeProductMap) || CollectionUtils.isEmpty(request.getMetrics())) {
            return;
        }

        Map<Long, Map<String, Long>> clickUvMap = request.getMetrics().contains("clickUserCount")
            ? batchQueryDownloadEventUv(request, storeProductMap, EventType.CLICK_PRODUCT) : Map.of();
        Map<Long, Map<String, Long>> addCartUvMap = request.getMetrics().contains("addCartUserCount")
            ? batchQueryDownloadEventUv(request, storeProductMap, EventType.ADD_CART) : Map.of();

        for (Map.Entry<Long, List<ProductResult>> entry : storeProductMap.entrySet()) {
            Map<String, Long> storeClickUvMap = clickUvMap.getOrDefault(entry.getKey(), Map.of());
            Map<String, Long> storeAddCartUvMap = addCartUvMap.getOrDefault(entry.getKey(), Map.of());
            for (ProductResult item : entry.getValue()) {
                String commodityId = String.valueOf(item.getCommodityId());
                if (request.getMetrics().contains("clickUserCount")) {
                    item.setClickUserCount(storeClickUvMap.getOrDefault(commodityId, 0L));
                }
                if (request.getMetrics().contains("addCartUserCount")) {
                    item.setAddCartUserCount(storeAddCartUvMap.getOrDefault(commodityId, 0L));
                }
            }
        }
    }

    private Map<Long, Map<String, Long>> batchQueryDownloadEventUv(ProductPageRequest request,
        Map<Long, List<ProductResult>> storeProductMap, EventType eventType) {
        List<String> commodityIds = storeProductMap.values().stream()
            .flatMap(List::stream)
            .map(item -> String.valueOf(item.getCommodityId()))
            .distinct()
            .toList();
        if (CollectionUtils.isEmpty(commodityIds)) {
            return Map.of();
        }

        EventQueryDTO queryDTO = new EventQueryDTO();
        queryDTO.setStoreIds(new ArrayList<>(storeProductMap.keySet()));
        queryDTO.setEventType(eventType);
        queryDTO.setStartTime(request.getCurrentTimeStart());
        queryDTO.setEndTime(request.getCurrentTimeEnd());
        queryDTO.setEventIds(commodityIds);
        return eventService.batchQueryUVByStoreAndEvent(queryDTO);
    }

    private List<ProductResult> buildStoreDownloadProducts(ProductPageRequest storeRequest,
        List<ProductResult> currentPageProducts, List<AfterOrderSimpleDTO> afterOrders) {
        List<ProductResult> storeProducts = isPurchaseQuery(storeRequest)
            ? getPurchaseProductPageByCursorWithAfterOrders(storeRequest, afterOrders).getList()
            : getProductPage(storeRequest).getList();

        Map<Long, ProductResult> storeProductMap = storeProducts.stream()
            .collect(Collectors.toMap(ProductResult::getCommodityId, item -> item, (left, right) -> right));

        List<ProductResult> result = new ArrayList<>(currentPageProducts.size());
        for (ProductResult currentPageProduct : currentPageProducts) {
            ProductResult product = buildEmptyStoreProduct(currentPageProduct);
            ProductResult storeProduct = storeProductMap.get(currentPageProduct.getCommodityId());
            if (storeProduct != null) {
                mergeStoreProductMetrics(product, storeProduct);
            }
            result.add(product);
        }
        return result;
    }

    private ProductResult buildEmptyStoreProduct(ProductResult baseProduct) {
        ProductResult result = new ProductResult();
        result.setCommodityId(baseProduct.getCommodityId());
        result.setGoodsName(baseProduct.getGoodsName());
        result.setCategoryName(baseProduct.getCategoryName());
        result.setGoodsImage(baseProduct.getGoodsImage());
        result.setIsSingle(baseProduct.getIsSingle());
        result.setIsPurchase(baseProduct.getIsPurchase());
        result.setSalesAmount(0D);
        result.setSalesVolume(0);
        result.setSingleSalesVolume(0);
        result.setAllSalesVolume(0);
        result.setOrderCount(0);
        result.setCustomerCount(0L);
        result.setRepurchaseUserCount(0L);
        result.setNewCustomerCount(0L);
        return result;
    }

    private void mergeStoreProductMetrics(ProductResult target, ProductResult source) {
        target.setSalesAmount(source.getSalesAmount());
        target.setSalesVolume(source.getSalesVolume());
        target.setSingleSalesVolume(source.getSingleSalesVolume());
        target.setAllSalesVolume(source.getAllSalesVolume());
        target.setIsPurchase(source.getIsPurchase());
        target.setOrderCount(source.getOrderCount());
        target.setCustomerCount(source.getCustomerCount());
        target.setRepurchaseUserCount(source.getRepurchaseUserCount());
        target.setRepurchaseRate(source.getRepurchaseRate());
        target.setNewCustomerCount(source.getNewCustomerCount());
        target.setNewCustomerRate(source.getNewCustomerRate());
    }


    private BoolQuery.Builder getBuilder(ProductPageRequest request) {
        // 1. 创建 BoolQuery 构建器
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        LocalDateTime[] times =  new LocalDateTime[]{request.getCurrentTimeStart(),request.getCurrentTimeEnd()};
        List<Long> storeIds = request.getStoreIds();

        if (log.isDebugEnabled()) {
            log.debug(
                "build product es query, currentTimeStart={}, currentTimeEnd={}, storeIds={}, commodityIds={}, channelIds={}, couponIds={}, activityIds={}, metrics={}, hasNullStoreId={}, hasNullCommodityId={}, hasNullChannelId={}, hasNullCouponId={}, hasNullActivityId={}",
                request.getCurrentTimeStart(),
                request.getCurrentTimeEnd(),
                request.getStoreIds(),
                request.getCommodityIds(),
                request.getChannelIds(),
                request.getCouponIds(),
                request.getActivityIds(),
                request.getMetrics(),
                request.getStoreIds() != null && request.getStoreIds().stream().anyMatch(Objects::isNull),
                request.getCommodityIds() != null && request.getCommodityIds().stream().anyMatch(Objects::isNull),
                request.getChannelIds() != null && request.getChannelIds().stream().anyMatch(Objects::isNull),
                request.getCouponIds() != null && request.getCouponIds().stream().anyMatch(Objects::isNull),
                request.getActivityIds() != null && request.getActivityIds().stream().anyMatch(Objects::isNull)
            );
        }

        //1.设置business条件
        boolBuilder.filter(
            m -> m.term(t -> t.field("businessId").value(FieldValue.of(BusinessContextHolder.getRequiredBusinessId()))));

        // 2. 处理时间范围条件
        boolBuilder.filter(m -> m.range(
            r -> r.date(dr -> dr.field("createTime").gte(formatDateTime(times[0])).lte(formatDateTime(times[1])))));

        // 3. 处理 门店 ID 条件
        if (!CollectionUtils.isEmpty(storeIds)) {
            if (log.isDebugEnabled()) {
                log.debug("product es terms field=storeId, values={}, hasNull={}", storeIds,
                    storeIds.stream().anyMatch(Objects::isNull));
            }
            List<FieldValue> storeFieldValues = toFieldValues(storeIds);
            if (!storeFieldValues.isEmpty()) {
                boolBuilder.filter(m -> m.terms(
                    t -> t.field("storeId").terms(tv -> tv.value(storeFieldValues))));
            }
        } else {
            Set<Long> dataPermissionStoreIds = storeApi.getAllStoreIdByUser(SecurityFrameworkUtils.getLoginUserId())
                .getCheckedData();
            if (CollectionUtils.isEmpty(dataPermissionStoreIds)) {
                throw exception(ErrorCodeConstants.DATA_PERMISSION_IS_EMPTY);
            }
            if (!CollectionUtils.isEmpty(dataPermissionStoreIds)) {
                if (log.isDebugEnabled()) {
                    log.debug("product es terms field=storeId(dataPermission), values={}, hasNull={}",
                        dataPermissionStoreIds, dataPermissionStoreIds.stream().anyMatch(Objects::isNull));
                }
                List<FieldValue> storeFieldValues = toFieldValues(dataPermissionStoreIds);
                if (!storeFieldValues.isEmpty()) {
                    boolBuilder.filter(m -> m.terms(t -> t.field("storeId")
                        .terms(tv -> tv.value(storeFieldValues))));
                }
            }
        }

        //4.设置自定义时间段
        if (!CollectionUtils.isEmpty(request.getRangeHours())){
            Set<Integer> hours = request.getRangeHours().get(0).calcHours();
            if (log.isDebugEnabled()) {
                log.debug("product es terms field=star, values={}, hasNull={}", hours,
                    hours.stream().anyMatch(Objects::isNull));
            }
            List<FieldValue> hourFieldValues = toFieldValues(hours);
            if (!hourFieldValues.isEmpty()) {
                boolBuilder.filter(m -> m.terms(t -> t.field("star")
                    .terms(tv -> tv.value(hourFieldValues))));
            }
        }

        if (isPurchaseQuery(request)) {
            boolBuilder.filter(mm -> mm.term(t -> t.field("isPurchase").value(1)));
        } else {
            if (request.getIsSingle() != null) {
                boolBuilder.must(mm -> mm.term(t -> t.field("isSingle").value(request.getIsSingle())));
            }
        }
        applyHiddenCommodityFilter(boolBuilder, request);
        if (StringUtils.isNotBlank(request.getGoodsName())) {
            boolBuilder.must(mm -> mm.matchPhrase(t -> t.field("goodsName").query(request.getGoodsName())));
        }
        if (!CollectionUtils.isEmpty(request.getCommodityIds())) {
            if (log.isDebugEnabled()) {
                log.debug("product es terms field=commodityId, values={}, hasNull={}", request.getCommodityIds(),
                    request.getCommodityIds().stream().anyMatch(Objects::isNull));
            }
            List<FieldValue> commodityFieldValues = toFieldValues(request.getCommodityIds());
            if (!commodityFieldValues.isEmpty()) {
                boolBuilder.filter(mm -> mm.terms(t -> t.field("commodityId")
                    .terms(tv -> tv.value(commodityFieldValues))));
            }
        }

        // 营销分析相关筛选
        // 1. 统计类型筛选
        if (StringUtils.isNotBlank(request.getStatType())) {
            //boolBuilder.filter(mm -> mm.term(t -> t.field("isSon").value(0)));
            switch (request.getStatType()) {
                case "coupon":
                    // 优惠券：activityDiscountAmount != 0
                    boolBuilder.must(mm -> mm.range(r -> r.number(nr -> nr.field("activityDiscountAmount").gt(0.0))));
                    break;
                case "activity":
                    // 营销活动：promotionDiscountAmount != 0
                    boolBuilder.must(mm -> mm.range(r -> r.number(nr -> nr.field("promotionDiscountAmount").gt(0.0))));
                    break;
                case "all":
                    boolBuilder.must(mm -> mm.range(r -> r.number(nr -> nr.field("allDiscountAmount").gt(0.0))));
                    break;
                default:
                    // 未知类型，不添加额外过滤条件
                    break;
            }
        }

        // 2. 统计渠道筛选
        if (!CollectionUtils.isEmpty(request.getChannelIds())) {
            if (log.isDebugEnabled()) {
                log.debug("product es terms field=channel, values={}, hasNull={}", request.getChannelIds(),
                    request.getChannelIds().stream().anyMatch(Objects::isNull));
            }
            List<FieldValue> channelFieldValues = toFieldValues(request.getChannelIds());
            if (!channelFieldValues.isEmpty()) {
                boolBuilder.filter(mm -> mm.terms(t -> t.field("channel")
                    .terms(tv -> tv.value(channelFieldValues))));
            }
        }

        // 3. 优惠券筛选
        if (!CollectionUtils.isEmpty(request.getCouponIds())) {
            if (log.isDebugEnabled()) {
                log.debug("product es terms field=couponId, values={}, hasNull={}", request.getCouponIds(),
                    request.getCouponIds().stream().anyMatch(Objects::isNull));
            }
            List<FieldValue> couponFieldValues = toFieldValues(request.getCouponIds());
            if (!couponFieldValues.isEmpty()) {
                boolBuilder.filter(mm -> mm.terms(t -> t.field("couponId")
                    .terms(tv -> tv.value(couponFieldValues))));
            }
        }

        // 4. 营销活动筛选
        if (!CollectionUtils.isEmpty(request.getActivityIds())) {
            if (log.isDebugEnabled()) {
                log.debug("product es terms field=activityId, values={}, hasNull={}", request.getActivityIds(),
                    request.getActivityIds().stream().anyMatch(Objects::isNull));
            }
            List<FieldValue> activityFieldValues = toFieldValues(request.getActivityIds());
            if (!activityFieldValues.isEmpty()) {
                boolBuilder.filter(mm -> mm.terms(t -> t.field("activityId")
                    .terms(tv -> tv.value(activityFieldValues))));
            }
        }


        return boolBuilder;
    }

    private void applyHiddenCommodityFilter(BoolQuery.Builder boolBuilder, ProductPageRequest request) {
        if (!Objects.equals(request.getIsSingle(), 1) && !Objects.equals(request.getIsSingle(), 2)) {
            return;
        }

        CommodityHiddenFilterDTO hiddenFilter = commodityApi.filterHiddenCommodityIds().getCheckedData();
        if (hiddenFilter == null || hiddenFilter.getIsHidden() == null) {
            return;
        }

        List<FieldValue> commodityIds = CollectionUtils.isEmpty(hiddenFilter.getCommodityIds())
            ? List.of() : toFieldValues(hiddenFilter.getCommodityIds());
        if (Objects.equals(hiddenFilter.getIsHidden(), 1)) {
            if (!commodityIds.isEmpty()) {
                boolBuilder.mustNot(mm -> mm.terms(t -> t.field("commodityId")
                    .terms(tv -> tv.value(commodityIds))));
            }
            return;
        }

        if (Objects.equals(hiddenFilter.getIsHidden(), 0)) {
            if (commodityIds.isEmpty()) {
                boolBuilder.filter(mm -> mm.matchNone(mn -> mn));
            } else {
                boolBuilder.filter(mm -> mm.terms(t -> t.field("commodityId")
                    .terms(tv -> tv.value(commodityIds))));
            }
        }
    }



    // 辅助方法：格式化 LocalDateTime 为 ISO 格式字符串
    private String formatDateTime(LocalDateTime dateTime) {
        ZonedDateTime zoned = dateTime.atZone(ZoneId.of("Asia/Shanghai"));
        return zoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private List<FieldValue> toFieldValues(Collection<?> values) {
        return values.stream()
            .filter(Objects::nonNull)
            .map(value -> FieldValue.of(JsonData.of(value)))
            .toList();
    }


    private Map<Long, Long> querySaleStoreCountMap(List<ProductResult> list, Set<Long> storeIds) {
        Set<Long> commodityIds = list.stream().map(ProductResult::getCommodityId).collect(Collectors.toSet());
        List<StoreSpuCountDTO> checkedData = commodityApi.saleStoreCountByCommodityIds(commodityIds, storeIds)
            .getCheckedData();
        return checkedData.stream()
            .collect(Collectors.toMap(StoreSpuCountDTO::getCommodityId, StoreSpuCountDTO::getStoreCount));
    }

    private void applySaleStoreCount(List<ProductResult> list, Map<Long, Long> storeCountMap) {
        for (ProductResult productResult : list) {
            Long storeCount = storeCountMap.getOrDefault(productResult.getCommodityId(), 0L);
            productResult.setStoreCount(storeCount);
        }
    }

    private Map<Long, Long> buildSaleStoreCountMap(List<ProductResult> list, List<Long> storeIds, Set<String> metrics) {
        if (CollectionUtils.isEmpty(list) || CollectionUtils.isEmpty(storeIds) || CollectionUtils.isEmpty(metrics)
            || !metrics.contains("storeCount")) {
            return Map.of();
        }
        return querySaleStoreCountMap(list, new HashSet<>(storeIds));
    }

    /**
     * 通用事件统计设置方法
     *
     * @param request    请求参数
     * @param results    产品结果列表
     * @param eventType  事件类型
     * @param setter     设置结果的函数
     */
    private void setEventCount(ProductPageRequest request, List<ProductResult> results, EventType eventType, 
                              BiConsumer<ProductResult, Long> setter) {
        if (CollectionUtils.isEmpty(results)) {
            return;
        }
        
        // 批量查询事件UV
        List<String> commodityIds = results.stream()
                .map(item -> String.valueOf(item.getCommodityId()))
                .toList();
        
        EventQueryDTO queryDTO = new EventQueryDTO();
        queryDTO.setStoreIds(request.getStoreIds());
        queryDTO.setEventType(eventType);
        queryDTO.setStartTime(request.getCurrentTimeStart());
        queryDTO.setEndTime(request.getCurrentTimeEnd());
        queryDTO.setEventIds(commodityIds);
        
        Map<String, Long> uvMap = eventService.batchQueryUV(queryDTO);
        
        // 设置事件用户数
        for (ProductResult item : results) {
            String commodityIdStr = String.valueOf(item.getCommodityId());
            setter.accept(item, uvMap.getOrDefault(commodityIdStr, 0L));
        }
    }

    private void fillRealtimeMetrics(ProductPageRequest request, List<ProductResult> results) {
        fillRealtimeMetrics(request, results, true);
    }

    private void fillRealtimeMetrics(ProductPageRequest request, List<ProductResult> results, boolean includeStoreCount) {
        try {
            if (CollectionUtils.isEmpty(results) || CollectionUtils.isEmpty(request.getMetrics())) {
                return;
            }
            if (includeStoreCount && request.getMetrics().contains("storeCount") && !CollectionUtils.isEmpty(request.getStoreIds())) {
                applySaleStoreCount(results, querySaleStoreCountMap(results, new HashSet<>(request.getStoreIds())));
            }
            if (request.getMetrics().contains("clickUserCount")) {
                setEventCount(request, results, EventType.CLICK_PRODUCT, ProductResult::setClickUserCount);
            }
            if (request.getMetrics().contains("addCartUserCount")) {
                setEventCount(request, results, EventType.ADD_CART, ProductResult::setAddCartUserCount);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    private ProductPageRequest copyForSingleStore(ProductPageRequest request, Long storeId) {
        ProductPageRequest storeRequest = new ProductPageRequest();
        storeRequest.setCurrentTimeStart(request.getCurrentTimeStart());
        storeRequest.setCurrentTimeEnd(request.getCurrentTimeEnd());
        storeRequest.setSortBy(request.getSortBy());
        storeRequest.setSortOrder(request.getSortOrder());
        storeRequest.setStoreIds(List.of(storeId));
        storeRequest.setGoodsName(request.getGoodsName());
        storeRequest.setIsSingle(request.getIsSingle());
        storeRequest.setRealTime(request.getRealTime());
        storeRequest.setCommodityIds(request.getCommodityIds());
        storeRequest.setMetrics(new HashSet<>(request.getMetrics()));
        storeRequest.setRangeHours(request.getRangeHours());
        storeRequest.setStatType(request.getStatType());
        storeRequest.setChannelIds(request.getChannelIds());
        storeRequest.setCouponIds(request.getCouponIds());
        storeRequest.setActivityIds(request.getActivityIds());
        storeRequest.setPageNo(1);
        storeRequest.setPageSize(500);
        return storeRequest;
    }

    private String buildExportTimeLabel(ProductPageRequest request) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (Boolean.TRUE.equals(request.getRealTime())) {
            return request.getCurrentTimeEnd().format(dateTimeFormatter);
        }
        return request.getCurrentTimeStart().format(dateFormatter) + " - "
            + request.getCurrentTimeEnd().format(dateFormatter);
    }

    private Map<Long, String> buildStoreNameMap(List<Long> storeIds) {
        if (CollectionUtils.isEmpty(storeIds)) {
            return Map.of();
        }
        List<StoreSimpleResDto> stores = storeApi.getStoreSimpleResDtoList(storeIds).getCheckedData();
        if (CollectionUtils.isEmpty(stores)) {
            return Map.of();
        }
        return stores.stream().collect(Collectors.toMap(StoreSimpleResDto::getStoreId, StoreSimpleResDto::getStoreName,
            (left, right) -> left));
    }



    private Aggregation buildFilterAggregation(ProductMetricsConfigNew metricsConfig) {
        return buildFilterAggregation(metricsConfig, false);
    }

    private Aggregation buildFilterAggregation(ProductMetricsConfigNew metricsConfig, boolean filterMainProduct) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        if (filterMainProduct) {
            boolBuilder.filter(mm -> mm.term(t -> t.field("isSon").value(0)));
        }
        Query query = metricsConfig.addTermsCondition(boolBuilder);
        return new Builder()
            .filter(f -> f.bool(query.bool()))
            .aggregations(Map.of(
                metricsConfig.getCode(),
                buildAggregation(metricsConfig)
            ))
            .build();
    }



    private Aggregation buildAggregation(ProductMetricsConfigNew param) {
        switch (param.getAggType()) {
            case SIZE:
                return Aggregation.of(a -> a.terms(t -> t.field(param.getField()).size(1)));
            case SUM:
                return Aggregation.of(a -> a.sum(s -> s.field(param.getField())));
            case VALUE_COUNT:
                return Aggregation.of(a -> a.valueCount(s -> s.field(param.getField())));
            case CARDINALITY:
                return Aggregation.of(a -> a.cardinality(m -> m.field(param.getField())));
            case MIN_COUNT:
                return Aggregation.of(a -> a.terms(t -> t.field(param.getField())
                    .minDocCount(2).size(100000)));
            case HIT:
                return Aggregation.of(a -> a.topHits(th -> th
                    .size(1)  // 仅取第一个文档的
                    .source(so -> so
                        .filter(f -> f
                            .includes(param.getField())  // 只包含 storeName 字段
                        )
                    )
                ));
            default:
                throw new IllegalArgumentException("Unsupported aggregation type: " + param.getAggType());
        }
    }

    /**
     * 批量聚合指标
     * @param request 产品页面请求参数
     * @param metrics 指标列表
     * @return 聚合结果映射
     */
    public Map<String, Double> batchAggregateMetrics(ProductPageRequest request, List<ProductMetricsConfigNew> metrics) {
        Map<String, Double> result = new HashMap<>();

        for (ProductMetricsConfigNew metric : metrics) {
            // 创建 BoolQuery 构建器
            BoolQuery.Builder builder = getBuilder(request);
            Query query = new Query.Builder().bool(builder.build()).build();

            // 构建搜索请求
            SearchRequest searchRequest = SearchRequest.of(s -> s.size(0) // 不返回文档内容
                .index("bz_order_product")
                .query(query).aggregations(metric.getCode(), buildFilterAggregation(metric,
                    shouldFilterMainProduct(request, metric))));

            try {
                SearchResponse<BzOrderProduct> response = client.search(searchRequest, BzOrderProduct.class);
                for (String key : response.aggregations().keySet()) {
                    Aggregate aggregate = response.aggregations().get(key);
                    Double value = (Double) getMetricValue(metric, aggregate);
                    result.put(key, value);
                }
            } catch (IOException e) {
                log.error("ES 查询异常，request: {}", request, e);
                throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
            }
        }
        return result;
    }

    private boolean shouldFilterMainProduct(ProductPageRequest request, ProductMetricsConfigNew metric) {
        return request != null
            && StringUtils.isNotBlank(request.getStatType());
    }

    private Object getMetricValue(ProductMetricsConfigNew metric, Aggregate aggregate) {
        if (aggregate != null && aggregate.isFilter()) {
            Aggregate subAgg = aggregate.filter().aggregations().get(metric.getCode());
            return getValue(metric, subAgg);
        }
        return getValue(metric, aggregate);
    }

    /**
     * 获取聚合值
     * @param param 指标配置
     * @param aggregate 聚合结果
     * @return 聚合值（可能是 Double 或 String）
     */
    private Object getValue(ProductMetricsConfigNew param, Aggregate aggregate) {
        switch (param.getAggType()) {
            case SIZE: {
                if (aggregate.isLterms()) {
                    var buckets = aggregate.lterms().buckets().array();
                    return buckets.isEmpty() ? 0.0 : buckets.get(0).key();
                } else {
                    var buckets = aggregate.sterms().buckets().array();
                    return buckets.isEmpty() ? 0.0 : 1.0; // 字符串类型返回 1.0 表示存在
                }
            }
            case SUM: {
                SumAggregate sum = aggregate.sum();
                return sum != null ? sum.value() : 0.0;
            }
            case VALUE_COUNT: {
                ValueCountAggregate valueCount = aggregate.valueCount();
                return valueCount != null ? valueCount.value() : 0.0;
            }
            case CARDINALITY: {
                CardinalityAggregate cardinality = aggregate.cardinality();
                return cardinality != null ? cardinality.value() : 0.0;
            }
            case HIT: {
                // 从 topHits 中提取字段值
                var topHits = aggregate.topHits();
                if (topHits != null && topHits.hits() != null && !topHits.hits().hits().isEmpty()) {
                    var hit = topHits.hits().hits().get(0);
                    if (hit.source() != null) {
                        var sourceMap = hit.source().to(java.util.Map.class);
                        Object value = sourceMap.get(param.getField());
                        return value != null ? value : "";
                    }
                }
                return "";
            }
            case MIN_COUNT: {
                if (aggregate.isLterms()) {
                    return aggregate.lterms().buckets().array().size();
                } else {
                    return aggregate.sterms().buckets().array().size();
                }
            }
            default:
                throw new IllegalArgumentException("Unsupported aggregation type: " + param.getAggType());
        }
    }

    /**
     * 根据门店ID分组查询
     * @param request 产品页面请求参数
     * @return 门店聚合结果列表
     */
    @Override
    public PageResult<AnalysisTopVO> getStoreAggregation(ProductPageRequest request,String groupField) {
        // 计算分页参数
        int from = (request.getPageNo() - 1) * request.getPageSize();
        int requiredSize = from + request.getPageSize();


        List<ProductMetricsConfigNew> metricsConfigs = request.getMetrics().stream()
            .map(ProductMetricsConfigNew::getEnumByCode).filter(Objects::nonNull).toList();

        // 设置查询条件
        BoolQuery.Builder builder = getBuilder(request);
        Query query = new Query.Builder().bool(builder.build()).build();

        // 构建排序规则
        TermsAggregation.Builder termBuilder = new TermsAggregation.Builder()
            .field(groupField)
            .size(requiredSize);
        SortOrder sortOrder = Objects.equals(request.getSortOrder(), "asc") ? SortOrder.Asc : SortOrder.Desc;

        if (StringUtils.isNotBlank(request.getSortBy())) {
            String name = request.getSortBy() + ">" + request.getSortBy(); // 聚合排序
            termBuilder.order(new NamedValue<>(name, sortOrder));
        }

        // 构建指标聚合
        Map<String, Aggregation> aggregations = metricsConfigs.stream()
            .collect(Collectors.toMap(
                ProductMetricsConfigNew::getCode,
                metricsConfig -> buildFilterAggregation(metricsConfig, shouldFilterMainProduct(request, metricsConfig))
            ));

        // 添加 storeId 子聚合
        aggregations.put("store_ids", Aggregation.of(a -> a
            .terms(t -> t
                .field("storeId")
                .size(10000)
            )
        ));


        SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
            .index("bz_order_product")
            .query(query)
            .aggregations(groupField,
                a -> a.terms(termBuilder.build())
                    .aggregations(aggregations) // 添加指标聚合
                    .aggregations("bucket_sort_agg", bsa -> bsa
                        .bucketSort(bs -> bs
                            .from(from)
                            .size(requiredSize)
                        )
                    )
            )
            .aggregations("total", total -> total
                .cardinality(c -> c.field(groupField))
            ));

        SearchResponse<BzOrderProduct> response = null;
        try {
            response = client.search(searchRequest, BzOrderProduct.class);
            Long total = response.aggregations().get("total").cardinality().value();
            Aggregate aggregate = response.aggregations().get(groupField);
            List<AnalysisTopVO> marketingShopDataVOS = extractedStoreAggregation(metricsConfigs, aggregate);
            PageResult<AnalysisTopVO> mapPageResult = new PageResult<>();
            mapPageResult.setTotal(total);
            mapPageResult.setList(marketingShopDataVOS);
            return mapPageResult;
        } catch (IOException e) {
            log.error("", e);
            throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
        }

    }

    /**
     * 提取门店聚合结果
     * @param metrics 指标列表
     * @param aggregate 聚合结果
     * @return 门店聚合结果列表
     */
    private List<AnalysisTopVO> extractedStoreAggregation(List<ProductMetricsConfigNew> metrics, Aggregate aggregate) {
        List<AnalysisTopVO> list = new ArrayList<>();

        if (aggregate.isLterms()) {
            Buckets<LongTermsBucket> buckets = aggregate.lterms().buckets();
            for (LongTermsBucket bucket : buckets.array()) {
                list.add(processStoreBucket(bucket.key(), bucket.aggregations(), metrics));
            }
        } else if (aggregate.isSterms()) {
            Buckets<StringTermsBucket> buckets = aggregate.sterms().buckets();
            for (StringTermsBucket bucket : buckets.array()) {
                list.add(processStoreBucket(bucket.key(), bucket.aggregations(), metrics));
            }
        }

        return list;
    }

    private AnalysisTopVO processStoreBucket(Object rawKey, Map<String, Aggregate> aggs, List<ProductMetricsConfigNew> metrics) {
        String key = rawKey.toString(); // 无论是 Long 还是 String，都转成 String 存储

        if (rawKey instanceof FieldValue) {
            key = ((FieldValue) rawKey).stringValue();
        }

        TreeMap<String, Double> resultMap = new TreeMap<>();
        String name = "";
        List<Long> storeIds = new ArrayList<>();
        for (ProductMetricsConfigNew metric : metrics) {
            String metricKey = metric.getCode();
            aggs.get(metricKey);
            Aggregate storeIdsAgg = aggs.get("store_ids");
            if (storeIdsAgg != null && storeIdsAgg.isLterms()) {
                Buckets<LongTermsBucket> storeBuckets = storeIdsAgg.lterms().buckets();
                for (LongTermsBucket storeBucket : storeBuckets.array()) {
                    storeIds.add(storeBucket.key());
                }
            }

            FilterAggregate filter = aggs.get(metricKey).filter();
            Aggregate subAgg = filter.aggregations().get(metricKey);
            if (metric.getAggType().equals(AggregationType.HIT)) {
                name = getHitName(metrics, subAgg);
            } else {
                resultMap.put(metricKey, (Double) getValue(metric, subAgg));
            }
        }
        AnalysisTopVO vo = new AnalysisTopVO();
        vo.setKey(key);
        vo.setName(name);
        vo.setValues(resultMap);
        vo.setStoreIds(storeIds);
        return vo;
    }

    private String getHitName(List<ProductMetricsConfigNew> metrics, Aggregate aggregate) {
        ProductMetricsConfigNew hitParam = metrics.stream()
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
                    case "storeName":
                        hitName = order.getStoreName();
                        break;
                    default:
                        break;
                }
            }
        }
        return hitName;
    }

}
