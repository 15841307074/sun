package com.htyoudao.youdao.module.analysis.service.impl;


import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.util.ObjectBuilder;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;

import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.OrderCreateTimeExcelRespVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.OrderStoreExcelRespVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActicitytyNjnzPageRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActivityNjnzRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ReportOrderDownloadReqVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityNjnzOrderVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityNjnzStorePageVO;
import com.htyoudao.youdao.module.analysis.dal.es.BzOrder;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder;
import com.htyoudao.youdao.module.analysis.enums.OrderExportField;
import com.htyoudao.youdao.module.analysis.service.IActivityAggregationService;
import com.htyoudao.youdao.module.analysis.service.IAggregationService;
import com.htyoudao.youdao.module.analysis.service.IEsAggregationService;
import com.htyoudao.youdao.module.system.api.org.dto.StoreOrgDTO;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.analysis.api.enums.ErrorCodeConstants.ORDER_QUERY_ERROR;
import static com.htyoudao.youdao.module.analysis.api.enums.ErrorCodeConstants.SCROLL_ORDER_QUERY_ERROR;
import static com.htyoudao.youdao.module.analysis.api.enums.LogRecordConstants.*;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.*;

@Slf4j
@Service
public class ActivityAggregationServiceImpl implements IActivityAggregationService {

    @Resource
    private IEsAggregationService service;
    @DubboReference
    private StoreApi storeApi;


    @Resource
    private ElasticsearchClient client;//8.5.1

    @Resource
    private IAggregationService aggregationService;

    @Resource
    private ExcelActionService excelActionService;

    private static final int SCROLL_BATCH_SIZE = 1000;

    private static final String SCROLL_KEEP_ALIVE = "5m";



    public Map<String, Double> query(ActivityNjnzRequestVO requestVO) {
        Map<String, Double> result = new HashMap<>();
        List<MetricsConfig> metrics = List.of(PAY_AMOUNTS, OFFER_AMOUNT, COMMODITY_COUNT,ORDER_NUMBER, CUSTOMER_COUNTS, AVERAGE_PAYMENTS);
        Long activityId = requestVO.getActivityId();

        for (MetricsConfig metric : metrics) {
            try {
                // 1. 构建查询条件
                Builder boolBuilder = new Builder();
                boolBuilder.filter(m -> m.term(
                        t -> t.field("businessId")
                                .value(FieldValue.of(BusinessContextHolder.getBusinessId()))
                ));

                boolBuilder.filter(m -> m.term(
                        t -> t.field("orderState")
                                .value(60)
                ));
                boolBuilder.must(Query.of(q -> getProductQuery(activityId, q)));

                // 特殊处理商品数量聚合
                if (metric == COMMODITY_COUNT) {
                    // 2. 构建嵌套聚合请求
                    SearchRequest searchRequest = SearchRequest.of(s -> s
                            .size(0)
                            .query(metric.addTermsCondition(boolBuilder))
                            .aggregations("matching_products", a -> a
                                    .nested(n -> n.path("product"))
                                    .aggregations("activity_products", a2 -> a2
                                            .filter(f -> f
                                                    .term(t -> t
                                                            .field("product.activityId")
                                                            .value(FieldValue.of(activityId))
                                                    )
                                            )
                                            .aggregations("product_count", a3 -> a3
                                                    .sum(v -> v.field("product.goodsNum"))
                                            )
                                    )
                            )
                    );




                    // 3. 执行查询并处理结果
                    SearchResponse<BzOrder> response = client.search(searchRequest, BzOrder.class);
                    NestedAggregate nestedAgg = response.aggregations().get("matching_products").nested();
                    if (nestedAgg != null) {
                        FilterAggregate filterAgg = nestedAgg.aggregations().get("activity_products").filter();
                        if (filterAgg != null) {
//                            SumAggregate productCount = filterAgg.aggregations().get("product_count").sum();
                            Aggregate totalGoodsNum = filterAgg.aggregations().get("product_count");
                            double value = totalGoodsNum.sum().value();
                            result.put(metric.getCode(), value);
                        } else {
                            result.put(metric.getCode(), 0.0);
                        }
                    } else {
                        result.put(metric.getCode(), 0.0);
                    }
                } else {
                    // 其他指标原有处理逻辑
                    SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
                            .query(metric.addTermsCondition(boolBuilder))
                            .aggregations(buildAggregationStructure(metric)));
                    SearchResponse<BzOrder> response = client.search(searchRequest, BzOrder.class);
                    processAggregationResults(response, metric, result);
                }


//                processAggregationResults(response, metric, result);
            } catch (IOException e) {
                throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
            }
        }
        return result;
    }




    private Map<String, Aggregation> buildAggregationStructure(MetricsConfig metric) {
        Map<String, Aggregation> aggs = new HashMap<>();

        if (metric.getField().contains(".")) {
            // 嵌套字段处理
            String[] parts = metric.getField().split("\\.");
            String nestedPath = parts[0];
            String nestedField = parts[1];

            aggs.put(metric.getCode(), Aggregation.of(a -> a.nested(n -> n.path(nestedPath))
                    .aggregations("inner_agg", buildSimpleAggregation(metric, metric.getField()))));
        } else {
            // 普通字段处理
            aggs.put(metric.getCode(), buildSimpleAggregation(metric,metric.getField()));
        }

        return aggs;
    }

    private void processAggregationResults(SearchResponse<BzOrder> response,
                                           MetricsConfig metric,
                                           Map<String, Double> result) {
        Aggregate rootAggregate = response.aggregations().get(metric.getCode());

        if (metric.getField().contains(".")) {
            // 处理嵌套聚合结果
//            if (rootAggregate.isNested()) {
//
//            }
            NestedAggregate nestedAgg = rootAggregate.nested();
            Aggregate innerAgg = nestedAgg.aggregations().get("inner_agg");
            result.put(metric.getCode(), formatValue(getAggregationValue(innerAgg, metric)));
        } else {
            // 处理普通聚合结果
            result.put(metric.getCode(), formatValue(getAggregationValue(rootAggregate, metric)));
        }
    }

    private double getAggregationValue(Aggregate aggregate, MetricsConfig metric) {
        return switch (metric.getAggType()) {
            case SUM -> aggregate.sum().value();
            case AVG -> aggregate.avg().value();
            case COUNT -> aggregate.valueCount().value();
            case MAX -> aggregate.max().value();
            case MIN -> aggregate.min().value();
            case CARDINALITY -> aggregate.cardinality().value();
            default -> 0.0;
        };
    }

    private double formatValue(double value) {
        return Double.parseDouble(String.format("%.2f", value));
    }

    private Aggregation buildSimpleAggregation(MetricsConfig param, String fieldName) {
        return switch (param.getAggType()) {
            case SUM -> Aggregation.of(a -> a.sum(s -> s.field(fieldName)));
            case AVG -> Aggregation.of(a -> a.avg(avg -> avg.field(fieldName)));
            case COUNT -> Aggregation.of(a -> a.valueCount(vc -> vc.field(fieldName)));
            case MAX -> Aggregation.of(a -> a.max(m -> m.field(fieldName)));
            case MIN -> Aggregation.of(a -> a.min(m -> m.field(fieldName)));
            case CARDINALITY -> Aggregation.of(a -> a.cardinality(m -> m.field(fieldName)));
            case HIT -> Aggregation.of(a -> a.topHits(th -> th.size(1)
                    .source(so -> so.filter(f -> f.includes(fieldName)))));
            case MIN_COUNT -> Aggregation.of(a -> a.terms(t -> t.field(fieldName)
                    .minDocCount(2).size(10000)));
        };
    }


    // 从完整字段名中提取嵌套字段名
    private String getNestedFieldName(String fullFieldName) {
        return fullFieldName.substring(fullFieldName.indexOf(".") + 1);
    }

    @Override
    public PageResult<ActivityNjnzStorePageVO> activityNjnzStorePage(ActicitytyNjnzPageRequestVO requestVO) {
        final List<MetricsConfig> metrics = List.of(
                 STORE_NAMES,PAY_AMOUNTS,OFFER_AMOUNT,COMMODITY_COUNT,ORDER_NUMBER,CUSTOMER_COUNTS,AVERAGE_PAYMENTS
        );
        return aggregationService.activityGroupPage(requestVO, "storeId", metrics, true);
    }

    @Override
    @LogRecord(type = PROMOTION_ACTIVITYNJNZ_TYPE, subType = PROMOTION_ACTIVITYNJNZ_EXPORT_TYPE, bizNo = "{{#activityNjnz.activityId}}", success = PROMOTION_ACTIVITYNJNZ_EXPORT_SUCCESS)
    public Boolean exportData(ActicitytyNjnzPageRequestVO requestVO) {
        final List<MetricsConfig> metrics = List.of(
                STORE_NAMES,PAY_AMOUNTS,OFFER_AMOUNT,COMMODITY_COUNT,ORDER_NUMBER,CUSTOMER_COUNTS,AVERAGE_PAYMENTS
        );
        LogRecordContext.putVariable("activityNjnz", requestVO);
        return aggregationService.activityData(requestVO, "storeId", metrics, true);
    }

    @Override
    @LogRecord(type = PROMOTION_ACTIVITYNJNZ_TYPE, subType = PROMOTION_ACTIVITYNJNZ_STORE_EXPORT_TYPE, bizNo = "{{#activityNjnz.activityId}}", success = PROMOTION_ACTIVITYNJNZ_STORE_EXPORT_SUCCESS)
    public void exportStoreData(ActicitytyNjnzPageRequestVO requestVO) {
        if(StringUtils.isEmpty(requestVO.getBeforeTimeStart())){
            throw exception(ErrorCodeConstants.ES_DATE_ERROR);
        }
        LogRecordContext.putVariable("activityNjnz", requestVO);


        LocalDateTime startTime = requestVO.getBeforeTimeStart();
        LocalDateTime endTime = requestVO.getBeforeTimeEnd();
        boolean timeRangeValid = isTimeRangeValid(startTime, endTime);
        if(!timeRangeValid){
            throw exception(ErrorCodeConstants.ES_CHANGE_DATE_ERROR);
        }
        Long storeId = requestVO.getStoreId();
        List<Long> longList = new ArrayList<>();
        longList.add(storeId);
        requestVO.setStoreIds(longList);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        Set<String> fields = new HashSet<>();
        fields.add("storeName");
        fields.add("orderSn");
        fields.add("orderType");
        fields.add("paymentName");
        fields.add("orderFrom");
        fields.add("createTime");
        fields.add("orderState");
        fields.add("goodsName");
        fields.add("goodsAmount");
        fields.add("goodsNum");
        fields.add("goodsSubtotal");
        fields.add("promotionDiscountAmount");
        fields.add("commodityPrice");
        fields.add("activityName");

        CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(storeId);
        StoreDTO data = storeByStoreId.getData();

        String fileName = startTime.format(formatter) + "至" + endTime.format(formatter)+data.getStoreName()+"门店活动明细";
        excelActionService.exportAsyncExcel(OrderStoreExcelRespVO.class,requestVO, param -> this.queryStoreOrderByCreateTime(requestVO), fileName, fields);

    }

    private List<OrderStoreExcelRespVO> queryStoreOrderByCreateTime(ActicitytyNjnzPageRequestVO requestVO) {
        Query boolQuery = buildBaseQuery(requestVO);

        SearchResponse<BzOrder> initialResponse = null;
        // 2. 初始化滚动查询
        try{
            initialResponse = client.search(s -> s
                            .index("bz_order")
                            .query(q -> q.bool(b -> b.must(boolQuery)))
                            .size(SCROLL_BATCH_SIZE)
                            .scroll(sc -> sc.time(SCROLL_KEEP_ALIVE))
                            .trackTotalHits(t -> t.enabled(true))
                            .sort(sort -> sort
                                    .field(f -> f
                                            // 按创建时间排序
                                            .field("createTime")
                                            // 升序排列
                                            .order(SortOrder.Asc)
                                    )
                            )
                            .sort(sort -> sort
                                    .field(f -> f
                                            // 按店铺ID排序
                                            .field("storeId")
                                            // 升序排列
                                            .order(SortOrder.Asc)
                                    )
                            ),
                    BzOrder.class
            );
        }catch (Exception e){
            throw exception(ORDER_QUERY_ERROR);
        }


        // 3. 处理结果
        List<BzOrder> allOrders = new ArrayList<>();
        String scrollId = initialResponse.scrollId();
        assert initialResponse.hits().total() != null;
        long total = initialResponse.hits().total().value();
        log.info("开始处理订单数据，总计: {}", total);

        try {
            // 处理第一批结果
            processResponse(initialResponse, allOrders);

            // 继续滚动获取剩余结果
            while (allOrders.size() < total) {
                String finalScrollId = scrollId;
                ScrollResponse<BzOrder> scrollResponse = client.scroll(s -> s
                                .scrollId(finalScrollId)
                                .scroll(sc -> sc.time(SCROLL_KEEP_ALIVE)),
                        BzOrder.class
                );

                List<Hit<BzOrder>> hits = scrollResponse.hits().hits();
                if (hits.isEmpty()) break;

                processResponse(initialResponse, allOrders);
                scrollId = scrollResponse.scrollId();

                log.debug("已处理 {}/{} 条订单", allOrders.size(), total);
            }

//            List<OrderStoreExcelRespVO> bean = BeanCopyUtils.copyBeanList(allOrders, OrderStoreExcelRespVO.class);

            List<OrderStoreExcelRespVO> bean = new ArrayList<>();
            for (BzOrder allOrder : allOrders) {
                if(allOrder.getProduct().size()>1){
                    for (BzOrder.Product product : allOrder.getProduct()) {
                        if(!StringUtils.isEmpty(product.getActivityId())){
                            OrderStoreExcelRespVO orderStoreExcelRespVO = new OrderStoreExcelRespVO();
                            BeanUtils.copyProperties(allOrder,orderStoreExcelRespVO);
                            orderStoreExcelRespVO.setGoodsName(product.getGoodsName());
                            orderStoreExcelRespVO.setGoodsAmount(product.getGoodsAmount());
                            orderStoreExcelRespVO.setGoodsNum(product.getGoodsNum());

                            Double bb =product.getGoodsAmount() * product.getGoodsNum();
                            orderStoreExcelRespVO.setGoodsSubtotal(bb);
                            Double cc =product.getActivityDiscountAmount()+product.getPromotionDiscountAmount();
                            if(bb-cc>=0){
                                orderStoreExcelRespVO.setCommodityPrice(bb-cc);
                            }else{
                                orderStoreExcelRespVO.setCommodityPrice(0.00);
                            }

                          //  orderStoreExcelRespVO.setActivityName(product.getActivityName());
                            orderStoreExcelRespVO.setPromotionDiscountAmount(product.getPromotionDiscountAmount());
                            bean.add(orderStoreExcelRespVO);
                        }
                    }

                }else{
                    OrderStoreExcelRespVO orderStoreExcelRespVO = new OrderStoreExcelRespVO();
                    BeanUtils.copyProperties(allOrder,orderStoreExcelRespVO);
                    orderStoreExcelRespVO.setGoodsName(allOrder.getProduct().get(0).getGoodsName());
                    orderStoreExcelRespVO.setGoodsAmount(allOrder.getProduct().get(0).getGoodsAmount());
                    orderStoreExcelRespVO.setGoodsNum(allOrder.getProduct().get(0).getGoodsNum());
                    Double bb = allOrder.getProduct().get(0).getGoodsAmount() * allOrder.getProduct().get(0).getGoodsNum();
                    orderStoreExcelRespVO.setGoodsSubtotal(bb);
                    Double cc =allOrder.getProduct().get(0).getActivityDiscountAmount()+allOrder.getProduct().get(0).getPromotionDiscountAmount();
                    if(bb-cc>=0){
                        orderStoreExcelRespVO.setCommodityPrice(bb-cc);
                    }else{
                        orderStoreExcelRespVO.setCommodityPrice(0.00);
                    }
//                    orderStoreExcelRespVO.setCouponName(allOrder.getProduct().get(0).getCouponName());
//                    orderStoreExcelRespVO.setActivityDiscountAmount(allOrder.getProduct().get(0).getActivityDiscountAmount());
//                    orderStoreExcelRespVO.setActivityName(allOrder.getProduct().get(0).getActivityName());
                    orderStoreExcelRespVO.setPromotionDiscountAmount(allOrder.getProduct().get(0).getPromotionDiscountAmount());
                    bean.add(orderStoreExcelRespVO);
                }



            }

            return bean;
        } catch (Exception e){
            throw exception(SCROLL_ORDER_QUERY_ERROR);
        }finally {
            // 清理滚动上下文
            if (scrollId != null) {
                try {
                    String finalScrollId1 = scrollId;
                    client.clearScroll(c -> c.scrollId(finalScrollId1));
                } catch (Exception e) {
                    log.warn("清除滚动上下文失败", e);
                }
            }
            log.info("订单数据处理完成，共处理 {} 条", allOrders.size());
        }


    }
    private Query buildBaseQuery(ActicitytyNjnzPageRequestVO reqVO) {
        List<Query> mustQueries = new ArrayList<>();
        List<Integer> orderList = new ArrayList<>();
        orderList.add(60);

        // 时间范围条件
        mustQueries.add(buildTimeRangeQuery(reqVO));

        mustQueries.add(Query.of(q -> q
                .nested(n -> n
                        .path("product")  // 嵌套对象的路径
                        .query(nq -> nq  // 嵌套查询内容
                                .bool(b -> b  // 可以在嵌套查询中使用bool组合多个条件
                                        .must(m -> m.term(t -> t.field("product.activityId").value(reqVO.getActivityId())))
                                ))
                        )
                ));

        // 门店条件
        mustQueries.add(Query.of(q -> q
                .terms(t -> t
                        .field("storeId")
                        .terms(t2 -> t2
                                .value(reqVO.getStoreIds().stream()
                                        .map(FieldValue::of)
                                        .collect(Collectors.toList()))
                        )
                )
        ));

        // 门店条件
        mustQueries.add(Query.of(q -> q
                .terms(t -> t
                        .field("orderState")
                        .terms(t2 -> t2
                                .value(orderList.stream()
                                        .map(FieldValue::of)
                                        .collect(Collectors.toList()))
                        )
                )
        ));




        return Query.of(q -> q.bool(b -> b.must(mustQueries)));
    }

    /**
     * 构建时间范围查询
     */
    private Query buildTimeRangeQuery(ActicitytyNjnzPageRequestVO reqVO) {
        if (reqVO.getBeforeTimeStart() == null || reqVO.getBeforeTimeEnd() == null) {
            return Query.of(q -> q.matchAll(m -> m));
        }
        LocalDateTime startTime = reqVO.getBeforeTimeStart();
        LocalDateTime endTime = reqVO.getBeforeTimeEnd();
        return Query.of(m -> m.range(
                r -> r.date(dr -> dr.field("createTime").gte(formatDateTime(startTime)).lte(formatDateTime(endTime)))));

    }
    // 辅助方法：格式化 LocalDateTime 为 ISO 格式字符串
    private String formatDateTime(LocalDateTime dateTime) {
        ZonedDateTime zoned = dateTime.atZone(ZoneId.of("Asia/Shanghai"));
        return zoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * 处理每条订单的商品
     * @param response response
     * @param resultList  resultList
     */
    private void processResponse(SearchResponse<BzOrder> response, List<BzOrder> resultList) {
        for (Hit<BzOrder> hit : response.hits().hits()) {
            if (hit.source() != null) {
                // 计算商品总数
                BzOrder order = hit.source();
                if (order.getProduct() != null) {
                    List<BzOrder.Product> products = order.getProduct();
                    int totalGoods = products.stream()
                            .mapToInt(BzOrder.Product::getGoodsNum)
                            .sum();
                    order.setProductNum(totalGoods);
                    StringBuilder sb = new StringBuilder();
                    for (BzOrder.Product product : products) {
                        sb.append(product.getGoodsName()).append("*").append(product.getGoodsNum()).append(";");
                    }
                    order.setProductName(sb.toString());
                }
                resultList.add(order);
            }
        }
    }

    private static ObjectBuilder<Query> getProductQuery(Long activityId, Query.Builder m) {
        return m.nested(n -> n.path("product").query(nq -> nq.bool(nb -> {
            if (activityId != null) {
                nb.must(mm -> mm.term(t -> t.field("product.activityId").value(activityId)));
            }
            return nb;
        })));
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
                            .size(10000)));
        };
    }
    public boolean isTimeRangeValid(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return false;
        }
        long daysBetween = ChronoUnit.DAYS.between(start, end);
        return daysBetween <= 61 && daysBetween >= 0;
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
                if (aggregate.isLterms()){
                    yield (double) aggregate.lterms().buckets().array().size();
                }else {
                    yield (double) aggregate.sterms().buckets().array().size();
                }
            }
        };
    }

}
