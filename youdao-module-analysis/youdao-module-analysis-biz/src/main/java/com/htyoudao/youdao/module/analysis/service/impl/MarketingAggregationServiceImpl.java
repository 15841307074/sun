package com.htyoudao.youdao.module.analysis.service.impl;

import static com.htyoudao.youdao.module.analysis.enums.OrderStatusConstants.VALID_ORDER_STATES;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.Buckets;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.MarketingRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisTopVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.marketing.MarketingChannelDataVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.marketing.MarketingOverviewVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.marketing.MarketingShopDataVO;
import com.htyoudao.youdao.module.analysis.dal.es.BzOrder;
import com.htyoudao.youdao.module.analysis.enums.EventType;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import com.htyoudao.youdao.module.analysis.enums.ProductMetricsConfigNew;
import com.htyoudao.youdao.module.analysis.service.IEsAggregationService;
import com.htyoudao.youdao.module.analysis.service.IEventService;
import com.htyoudao.youdao.module.analysis.service.IMarketingAggregationService;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import com.htyoudao.youdao.module.analysis.service.dto.EventQueryDTO;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.activity.VO.ActivityChannelNameDataRespVO;
import com.htyoudao.youdao.module.promotion.api.goodcoupon.GoodCouponApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreSimpleResDto;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class MarketingAggregationServiceImpl implements IMarketingAggregationService {

    @Resource
    private ProductAggerationServiceImplNew productAggerationService;

    @Resource
    private IEventService eventService;

    @Resource
    private IEsAggregationService esAggregationService;

    @Resource
    private ElasticsearchClient client;

    @DubboReference
    private ActivityApi activityApi;

    @DubboReference
    private GoodCouponApi goodCouponApi;

    @DubboReference
    private StoreApi storeApi;


    @Override
    public MarketingOverviewVO getMarketingOverview(MarketingRequest request) {
        // 将 MarketingRequest 转换为 ProductPageRequest
        ProductPageRequest productRequest = convertToProductPageRequest(request);

        // 定义需要聚合的指标
        List<ProductMetricsConfigNew> metrics = List.of(
            ProductMetricsConfigNew.CUSTOMER_COUNT,//下单人数
            ProductMetricsConfigNew.ORDER_COUNT,//活动订单数
            ProductMetricsConfigNew.SALES_VOLUME,//购买商品件数
            ProductMetricsConfigNew.ACTIVITY_DISCOUNT_AMOUNT,
            ProductMetricsConfigNew.PROMOTION_DISCOUNT_AMOUNT,
            ProductMetricsConfigNew.ALL_ACTIVITY_DISCOUNT,//活动优惠
            ProductMetricsConfigNew.PAY_AMOUNT//活动销售额
        );

        // 调用 batchAggregateMetrics 方法获取聚合结果
        Map<String, Double> aggregateResult = Map.of();

        // 构建 MarketingOverviewVO
        MarketingOverviewVO overview = new MarketingOverviewVO(aggregateResult);
        OrderMetrics orderMetrics = queryOrderMetrics(request);
        overview.setOrderCount(orderMetrics.orderCount());
        overview.setCustomerCount(orderMetrics.customerCount());
        overview.setSalesVolume(orderMetrics.salesVolume());
        overview.setActivityDiscountAmount(orderMetrics.activityDiscountAmount());
        overview.setPromotionDiscountAmount(orderMetrics.promotionDiscountAmount());
        overview.setAllDiscountAmount(orderMetrics.allDiscountAmount());
        overview.setSalesAmount(orderMetrics.salesAmount());

        // 链接点击次数&链接点击人数
        if (request.getShowUV()) {
            EventQueryDTO queryDTO = getEventQueryDTO(request);
            Long uv = eventService.queryUV(queryDTO);
            Long pv = eventService.queryPV(queryDTO);
            overview.setLinkClickCount(pv);
            overview.setLinkClickUserCount(uv);
        }

        overview.setRate();
        return overview;
    }


    private static EventQueryDTO getEventQueryDTO(MarketingRequest request) {
        EventQueryDTO queryDTO = new EventQueryDTO();
        queryDTO.setStoreIds(request.getStoreIds());
        queryDTO.setStartTime(request.getCurrentTimeStart());
        queryDTO.setEndTime(request.getCurrentTimeEnd());
        queryDTO.setEventTypes(List.of(EventType.COUPON_VIEW, EventType.ACTIVITY));
        queryDTO.setChannelIds(request.getChannelIds());
        if (!CollectionUtils.isEmpty(request.getActivityIds())) {
            queryDTO.setEventTypes(List.of(EventType.ACTIVITY));
            queryDTO.setEventIds(request.getActivityIds().stream().map(String::valueOf).toList());
        }
        if (!CollectionUtils.isEmpty(request.getCouponIds())) {
            queryDTO.setEventTypes(List.of(EventType.COUPON_VIEW));
            queryDTO.setEventIds(request.getCouponIds().stream().map(String::valueOf).toList());
        }

        if (Objects.equals(request.getStatType(),"coupon")){
            queryDTO.setEventTypes(List.of(EventType.COUPON_VIEW));
        }
        if (Objects.equals(request.getStatType(),"activity")){
            queryDTO.setEventTypes(List.of(EventType.ACTIVITY));
        }
        queryDTO.setTrackTotalHits(true);
        return queryDTO;
    }

    /**
     * 获取优惠券领取事件查询DTO
     *
     * @param request 营销请求参数
     * @return 优惠券领取事件查询DTO
     */
    private static EventQueryDTO getCouponClaimEventQueryDTO(MarketingRequest request) {
        EventQueryDTO queryDTO = new EventQueryDTO();
        queryDTO.setStoreIds(request.getStoreIds());
        queryDTO.setStartTime(request.getCurrentTimeStart());
        queryDTO.setEndTime(request.getCurrentTimeEnd());
        queryDTO.setEventTypes(List.of(EventType.COUPON_CLAIM));
        queryDTO.setChannelIds(request.getChannelIds());
        if (!CollectionUtils.isEmpty(request.getCouponIds())) {
            queryDTO.setEventIds(request.getCouponIds().stream().map(String::valueOf).toList());
        }
        return queryDTO;
    }

    /**
     * 设置优惠券使用比率
     * @param shopDataVO 门店数据VO
     * @param couponReceiveCount 优惠券领取数量
     */
    /**
     * 设置优惠券使用比率（MarketingChannelDataVO）
     *
     * @param channelDataVO      渠道数据VO
     * @param couponReceiveCount 优惠券领取数量
     */
    private static void setCouponUseRate(MarketingChannelDataVO channelDataVO, Long couponReceiveCount) {
        if (channelDataVO == null) {
            return;
        }

        channelDataVO.setCouponReceiveCount(couponReceiveCount != null ? couponReceiveCount.intValue() : 0);

        Integer couponUseCount = channelDataVO.getCouponUseCount();
        int receiveCount = channelDataVO.getCouponReceiveCount();

        if (receiveCount > 0 && couponUseCount != null) {
            double useRate = (double) couponUseCount / receiveCount;
            channelDataVO.setCouponUseRate(String.format("%.2f%%", useRate * 100));
        } else {
            channelDataVO.setCouponUseRate("0%");
        }
    }

    /**
     * 设置优惠券使用比率（MarketingShopDataVO）
     *
     * @param shopDataVO         门店数据VO
     * @param couponReceiveCount 优惠券领取数量
     */
    private static void setCouponUseRate(MarketingShopDataVO shopDataVO, Long couponReceiveCount) {
        if (shopDataVO == null) {
            return;
        }

        shopDataVO.setCouponReceiveCount(couponReceiveCount != null ? couponReceiveCount : 0L);

        Integer couponUseCount = shopDataVO.getCouponUseCount();
        Long receiveCount = shopDataVO.getCouponReceiveCount();

        if (receiveCount != null && receiveCount > 0 && couponUseCount != null) {
            double useRate = (double) couponUseCount / receiveCount;
            shopDataVO.setCouponUseRate(String.format("%.2f%%", useRate * 100));
        } else {
            shopDataVO.setCouponUseRate("0%");
        }
    }


    @Override
    public PageResult<MarketingShopDataVO> getMarketingShopData(MarketingRequest request) {
        if (request != null) {
            return buildShopDataFromOrderMetrics(request);
        }
        // 将 MarketingRequest 转换为 ProductPageRequest
        ProductPageRequest productRequest = convertToProductPageRequest(request);

        // 优惠券使用数量
        productRequest.getMetrics().add(ProductMetricsConfigNew.COUPON_USE_COUNT.getCode());
        // 门店名称
        productRequest.getMetrics().add(ProductMetricsConfigNew.STORE_NAMES.getCode());

        if (isOrderMetricSort(request)) {
            return buildShopDataFromOrderMetrics(request);
        }

        PageResult<AnalysisTopVO> storeAggregation = productAggerationService.getStoreAggregation(productRequest,
            "storeId");
        List<AnalysisTopVO> list = storeAggregation.getList();
        if (CollectionUtils.isEmpty(list)) {
            return buildShopDataFromOrderMetrics(request);
        }
        List<MarketingShopDataVO> data = new ArrayList<>();
        List<Long> resultStoreIds = list.stream()
            .filter(Objects::nonNull)
            .flatMap(item -> CollectionUtils.isEmpty(item.getStoreIds()) ? Stream.empty() : item.getStoreIds().stream())
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        Map<Long, Long> couponReceiveCountMap = queryCouponReceiveCountMap(request, resultStoreIds);
        Map<Long, OrderMetrics> orderMetricsMap = queryOrderMetricsByStore(request, resultStoreIds);

        for (AnalysisTopVO analysisTopVO : list) {
            MarketingShopDataVO shopDataVO = new MarketingShopDataVO(analysisTopVO.getValues());
            shopDataVO.setStoreId(analysisTopVO.getStoreIds().get(0));
            shopDataVO.setStoreName(analysisTopVO.getName());
            OrderMetrics orderMetrics = orderMetricsMap.getOrDefault(shopDataVO.getStoreId(), OrderMetrics.empty());
            shopDataVO.setOrderCount(orderMetrics.orderCount());
            shopDataVO.setCustomerCount(orderMetrics.customerCount());
            shopDataVO.setSalesVolume(orderMetrics.salesVolume());
            shopDataVO.setActivityDiscountAmount(orderMetrics.activityDiscountAmount());
            shopDataVO.setPromotionDiscountAmount(orderMetrics.promotionDiscountAmount());
            shopDataVO.setAllDiscountAmount(orderMetrics.allDiscountAmount());
            shopDataVO.setCouponUseCount(orderMetrics.couponUseCount());
            shopDataVO.setSalesAmount(orderMetrics.salesAmount());
            data.add(shopDataVO);

            // 链接点击次数&链接点击人数
            if (request.getShowUV()) {
                EventQueryDTO queryDTO = getEventQueryDTO(request);
                // 添加当前门店ID到查询条件
                queryDTO.setStoreIds(List.of(shopDataVO.getStoreId()));
                Long pv = eventService.queryPV(queryDTO);
                shopDataVO.setLinkClickCount(pv);
                Long uv = eventService.queryUV(queryDTO);
                shopDataVO.setLinkClickUserCount(uv);
            }

            Long couponReceiveCount = couponReceiveCountMap.getOrDefault(shopDataVO.getStoreId(), 0L);

            // 设置优惠券使用比率
            setCouponUseRate(shopDataVO, couponReceiveCount);

            // set rate
            shopDataVO.setRate();
        }

        PageResult<MarketingShopDataVO> pageResult = new PageResult<>();
        pageResult.setTotal(storeAggregation.getTotal());
        pageResult.setList(data);
        return pageResult;
    }

    private PageResult<MarketingShopDataVO> buildShopDataFromOrderMetrics(MarketingRequest request) {
        Map<Long, OrderMetrics> orderMetricsMap = queryOrderMetricsByStore(request, null);
        List<Long> storeIds = new ArrayList<>(orderMetricsMap.keySet());
        Map<Long, String> storeNameMap = buildStoreNameMap(storeIds);
        Map<Long, Long> couponReceiveCountMap = queryCouponReceiveCountMap(request, storeIds);

        List<MarketingShopDataVO> allData = new ArrayList<>(storeIds.size());
        for (Long storeId : storeIds) {
            MarketingShopDataVO shopDataVO = new MarketingShopDataVO(null);
            shopDataVO.setStoreId(storeId);
            shopDataVO.setStoreName(storeNameMap.get(storeId));

            OrderMetrics orderMetrics = orderMetricsMap.getOrDefault(storeId, OrderMetrics.empty());
            shopDataVO.setOrderCount(orderMetrics.orderCount());
            shopDataVO.setCustomerCount(orderMetrics.customerCount());
            shopDataVO.setSalesVolume(orderMetrics.salesVolume());
            shopDataVO.setActivityDiscountAmount(orderMetrics.activityDiscountAmount());
            shopDataVO.setPromotionDiscountAmount(orderMetrics.promotionDiscountAmount());
            shopDataVO.setAllDiscountAmount(orderMetrics.allDiscountAmount());
            shopDataVO.setCouponUseCount(orderMetrics.couponUseCount());
            shopDataVO.setSalesAmount(orderMetrics.salesAmount());

            setCouponUseRate(shopDataVO, couponReceiveCountMap.getOrDefault(storeId, 0L));
            shopDataVO.setRate();
            allData.add(shopDataVO);
        }

        sortMarketingShopData(allData, request);
        List<MarketingShopDataVO> pageData = pageMarketingShopData(allData, request);
        for (MarketingShopDataVO shopDataVO : pageData) {
            setCouponUseRate(shopDataVO, couponReceiveCountMap.getOrDefault(shopDataVO.getStoreId(), 0L));
            if (Boolean.TRUE.equals(request.getShowUV())) {
                EventQueryDTO queryDTO = getEventQueryDTO(request);
                queryDTO.setStoreIds(List.of(shopDataVO.getStoreId()));
                shopDataVO.setLinkClickCount(eventService.queryPV(queryDTO));
                shopDataVO.setLinkClickUserCount(eventService.queryUV(queryDTO));
                shopDataVO.setRate();
            }
        }

        PageResult<MarketingShopDataVO> pageResult = new PageResult<>();
        pageResult.setTotal((long) allData.size());
        pageResult.setList(pageData);
        return pageResult;
    }

    private OrderMetrics queryOrderMetrics(MarketingRequest request) {
        Query query = buildMarketingOrderQuery(request, null);
        SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
            .index("bz_order")
            .query(query)
            .aggregations("orderCount", Aggregation.of(a -> a.cardinality(c -> c.field("orderId"))))
            .aggregations("customerCount", Aggregation.of(a -> a.cardinality(c -> c.field("memberId"))))
            .aggregations("salesVolume", buildOrderProductCountAggregation())
            .aggregations("activityDiscountAmount", Aggregation.of(a -> a.sum(c -> c.field("activityDiscountAmount"))))
            .aggregations("promotionDiscountAmount", Aggregation.of(a -> a.sum(c -> c.field("promotionDiscountAmount"))))
            .aggregations("allDiscountAmount", Aggregation.of(a -> a.sum(c -> c.field("allDiscountAmount"))))
            .aggregations("couponUseCount", buildCouponUseCountAggregation())
            .aggregations("payAmount", Aggregation.of(a -> a.sum(c -> c.field("payAmount")))));
        try {
            SearchResponse<BzOrder> response = client.search(searchRequest, BzOrder.class);
            return new OrderMetrics(
                getCardinalityValue(response.aggregations().get("orderCount")),
                getCardinalityValue(response.aggregations().get("customerCount")),
                (int) getSumValue(response.aggregations().get("salesVolume")),
                getSumValue(response.aggregations().get("activityDiscountAmount")),
                getSumValue(response.aggregations().get("promotionDiscountAmount")),
                getSumValue(response.aggregations().get("allDiscountAmount")),
                getFilterCardinalityValue(response.aggregations().get("couponUseCount"), "couponUseOrderCount"),
                getSumValue(response.aggregations().get("payAmount"))
            );
        } catch (IOException e) {
            throw new RuntimeException("ES query bz_order marketing metrics error", e);
        }
    }

    private Map<Long, OrderMetrics> queryOrderMetricsByStore(MarketingRequest request, List<Long> storeIds) {
        Query query = buildMarketingOrderQuery(request, storeIds);
        int aggregationSize = CollectionUtils.isEmpty(storeIds) ? 10000 : storeIds.size();
        SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
            .index("bz_order")
            .query(query)
            .aggregations("storeId", a -> a.terms(t -> t.field("storeId").size(aggregationSize))
                .aggregations("orderCount", Aggregation.of(aa -> aa.cardinality(c -> c.field("orderId"))))
                .aggregations("customerCount", Aggregation.of(aa -> aa.cardinality(c -> c.field("memberId"))))
                .aggregations("salesVolume", buildOrderProductCountAggregation())
                .aggregations("activityDiscountAmount", Aggregation.of(aa -> aa.sum(c -> c.field("activityDiscountAmount"))))
                .aggregations("promotionDiscountAmount", Aggregation.of(aa -> aa.sum(c -> c.field("promotionDiscountAmount"))))
                .aggregations("allDiscountAmount", Aggregation.of(aa -> aa.sum(c -> c.field("allDiscountAmount"))))
                .aggregations("couponUseCount", buildCouponUseCountAggregation())
                .aggregations("payAmount", Aggregation.of(aa -> aa.sum(c -> c.field("payAmount"))))));
        try {
            SearchResponse<BzOrder> response = client.search(searchRequest, BzOrder.class);
            Aggregate aggregate = response.aggregations().get("storeId");
            if (aggregate == null || !aggregate.isLterms()) {
                return Map.of();
            }
            Buckets<LongTermsBucket> buckets = aggregate.lterms().buckets();
            Map<Long, OrderMetrics> result = new LinkedHashMap<>(buckets.array().size());
            for (LongTermsBucket bucket : buckets.array()) {
                result.put(bucket.key(), new OrderMetrics(
                    getCardinalityValue(bucket.aggregations().get("orderCount")),
                    getCardinalityValue(bucket.aggregations().get("customerCount")),
                    (int) getSumValue(bucket.aggregations().get("salesVolume")),
                    getSumValue(bucket.aggregations().get("activityDiscountAmount")),
                    getSumValue(bucket.aggregations().get("promotionDiscountAmount")),
                    getSumValue(bucket.aggregations().get("allDiscountAmount")),
                    getFilterCardinalityValue(bucket.aggregations().get("couponUseCount"), "couponUseOrderCount"),
                    getSumValue(bucket.aggregations().get("payAmount"))
                ));
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("ES query bz_order marketing store metrics error", e);
        }
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

    private void mergeProductMetrics(MarketingRequest request, List<MarketingShopDataVO> data) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        List<Long> storeIds = data.stream()
            .map(MarketingShopDataVO::getStoreId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (CollectionUtils.isEmpty(storeIds)) {
            return;
        }
        ProductPageRequest productRequest = convertToProductPageRequest(request);
        productRequest.setStoreIds(storeIds);
        productRequest.setSortBy(null);
        productRequest.setSortOrder(null);
        productRequest.setPageNo(1);
        productRequest.setPageSize(storeIds.size());
        productRequest.getMetrics().add(ProductMetricsConfigNew.COUPON_USE_COUNT.getCode());
        productRequest.getMetrics().add(ProductMetricsConfigNew.STORE_NAMES.getCode());

        PageResult<AnalysisTopVO> storeAggregation = productAggerationService.getStoreAggregation(productRequest,
            "storeId");
        if (storeAggregation == null || CollectionUtils.isEmpty(storeAggregation.getList())) {
            return;
        }
        Map<Long, MarketingShopDataVO> productMetricMap = new HashMap<>();
        for (AnalysisTopVO analysisTopVO : storeAggregation.getList()) {
            if (analysisTopVO == null || CollectionUtils.isEmpty(analysisTopVO.getStoreIds())) {
                continue;
            }
            productMetricMap.put(analysisTopVO.getStoreIds().get(0), new MarketingShopDataVO(analysisTopVO.getValues()));
        }
        for (MarketingShopDataVO target : data) {
            MarketingShopDataVO source = productMetricMap.get(target.getStoreId());
            if (source == null) {
                continue;
            }
            target.setActivityDiscountAmount(source.getActivityDiscountAmount());
            target.setPromotionDiscountAmount(source.getPromotionDiscountAmount());
            target.setAllDiscountAmount(source.getAllDiscountAmount());
            target.setCouponUseCount(source.getCouponUseCount());
        }
    }

    private void sortMarketingShopData(List<MarketingShopDataVO> data, MarketingRequest request) {
        if (CollectionUtils.isEmpty(data) || request == null || !StringUtils.hasText(request.getSortBy())) {
            return;
        }
        Comparator<MarketingShopDataVO> comparator = buildShopDataComparator(request.getSortBy().trim());
        if (comparator == null) {
            return;
        }
        boolean asc = "asc".equalsIgnoreCase(request.getSortOrder());
        data.sort(asc ? comparator : comparator.reversed());
    }

    private Comparator<MarketingShopDataVO> buildShopDataComparator(String sortField) {
        return switch (sortField) {
            case "customerCount" -> Comparator.comparing(MarketingShopDataVO::getCustomerCount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "orderCount" -> Comparator.comparing(MarketingShopDataVO::getOrderCount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "salesVolume" -> Comparator.comparing(MarketingShopDataVO::getSalesVolume,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "customerUnitPrice" -> Comparator.comparing(MarketingShopDataVO::getCustomerUnitPrice,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "activityDiscountAmount" -> Comparator.comparing(MarketingShopDataVO::getActivityDiscountAmount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "promotionDiscountAmount" -> Comparator.comparing(MarketingShopDataVO::getPromotionDiscountAmount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "allDiscountAmount" -> Comparator.comparing(MarketingShopDataVO::getAllDiscountAmount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "payAmount", "salesAmount" -> Comparator.comparing(MarketingShopDataVO::getSalesAmount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "couponReceiveCount" -> Comparator.comparing(MarketingShopDataVO::getCouponReceiveCount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "couponUseCount" -> Comparator.comparing(MarketingShopDataVO::getCouponUseCount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            default -> null;
        };
    }

    private List<MarketingShopDataVO> pageMarketingShopData(List<MarketingShopDataVO> data, MarketingRequest request) {
        if (CollectionUtils.isEmpty(data)) {
            return List.of();
        }
        int pageNo = request.getPageNo() == null || request.getPageNo() < 1 ? 1 : request.getPageNo();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? data.size() : request.getPageSize();
        int fromIndex = Math.min((pageNo - 1) * pageSize, data.size());
        int toIndex = Math.min(fromIndex + pageSize, data.size());
        return new ArrayList<>(data.subList(fromIndex, toIndex));
    }

    private boolean isOrderMetricSort(MarketingRequest request) {
        if (request == null || !StringUtils.hasText(request.getSortBy())) {
            return false;
        }
        return switch (request.getSortBy().trim()) {
            case "orderCount", "customerCount", "salesVolume", "payAmount", "salesAmount", "customerUnitPrice" -> true;
            default -> false;
        };
    }

    private Aggregation buildOrderProductCountAggregation() {
        return Aggregation.of(a -> a.sum(s -> s.field("tableWareNum")));
    }

    private Aggregation buildCouponUseCountAggregation() {
        return Aggregation.of(a -> a.filter(f -> f.exists(e -> e.field("couponId")))
            .aggregations("couponUseOrderCount", aa -> aa.cardinality(c -> c.field("orderId"))));
    }

    private Query buildMarketingOrderQuery(MarketingRequest request, List<Long> overrideStoreIds) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        boolBuilder.filter(m -> m.term(t -> t.field("businessId").value(FieldValue.of(BusinessContextHolder.getRequiredBusinessId()))));
        boolBuilder.filter(m -> m.range(r -> r.date(dr -> dr.field("createTime")
            .gte(formatDateTime(request.getCurrentTimeStart()))
            .lte(formatDateTime(request.getCurrentTimeEnd())))));

        List<Long> storeIds = CollectionUtils.isEmpty(overrideStoreIds) ? request.getStoreIds() : overrideStoreIds;
        if (CollectionUtils.isEmpty(storeIds)) {
            Set<Long> dataPermissionStoreIds = storeApi.getAllStoreIdByUser(SecurityFrameworkUtils.getLoginUserId())
                .getCheckedData();
            addTermsFilter(boolBuilder, "storeId", dataPermissionStoreIds);
        } else {
            addTermsFilter(boolBuilder, "storeId", storeIds);
        }

        addTermsFilter(boolBuilder, "orderState", VALID_ORDER_STATES);
        addTermsFilter(boolBuilder, "couponId", request.getCouponIds());
        addTermsFilter(boolBuilder, "channel", request.getChannelIds());

        if (!CollectionUtils.isEmpty(request.getActivityIds())) {
            boolBuilder.must(m -> m.nested(n -> n.path("product")
                .query(q -> q.bool(b -> b.filter(f -> f.terms(t -> t.field("product.activityId")
                    .terms(tv -> tv.value(request.getActivityIds().stream()
                        .filter(Objects::nonNull)
                        .map(value -> FieldValue.of(JsonData.of(value)))
                        .toList()))))))));
        }

        if (StringUtils.hasText(request.getStatType())) {
            switch (request.getStatType()) {
                case "coupon" -> {
                    if (CollectionUtils.isEmpty(request.getCouponIds())) {
                        boolBuilder.must(m -> m.range(r -> r.number(nr -> nr.field("activityDiscountAmount").gt(0.0))));
                    }
                }
                case "activity" -> {
                    if (CollectionUtils.isEmpty(request.getActivityIds())) {
                        boolBuilder.must(m -> m.nested(n -> n.path("product")
                            .query(q -> q.bool(b -> b.filter(f -> f.range(r -> r.number(nr -> nr
                                .field("product.promotionDiscountAmount").gt(0.0))))))));
                    }
                }
                case "all" -> {
                    if (CollectionUtils.isEmpty(request.getCouponIds()) && CollectionUtils.isEmpty(request.getActivityIds())) {
                        boolBuilder.must(m -> m.range(r -> r.number(nr -> nr.field("allDiscountAmount").gt(0.0))));
                    }
                }
                default -> {
                }
            }
        }
        return new Query.Builder().bool(boolBuilder.build()).build();
    }

    private void addTermsFilter(BoolQuery.Builder boolBuilder, String field, Iterable<?> values) {
        if (values == null) {
            return;
        }
        List<FieldValue> fieldValues = StreamSupport.stream(values.spliterator(), false)
            .filter(Objects::nonNull)
            .map(value -> FieldValue.of(JsonData.of(value)))
            .toList();
        if (!fieldValues.isEmpty()) {
            boolBuilder.filter(m -> m.terms(t -> t.field(field).terms(tv -> tv.value(fieldValues))));
        }
    }

    private int getCardinalityValue(Aggregate aggregate) {
        return aggregate != null && aggregate.isCardinality() ? Math.toIntExact(aggregate.cardinality().value()) : 0;
    }

    private double getSumValue(Aggregate aggregate) {
        return aggregate != null && aggregate.isSum() ? aggregate.sum().value() : 0.0;
    }

    private int getNestedValueCount(Aggregate aggregate, String aggregationName) {
        if (aggregate == null || !aggregate.isNested()) {
            return 0;
        }
        Aggregate subAggregate = aggregate.nested().aggregations().get(aggregationName);
        return subAggregate != null && subAggregate.isSum()
            ? (int) subAggregate.sum().value() : 0;
    }

    private int getFilterCardinalityValue(Aggregate aggregate, String aggregationName) {
        if (aggregate == null || !aggregate.isFilter()) {
            return 0;
        }
        return getCardinalityValue(aggregate.filter().aggregations().get(aggregationName));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        ZonedDateTime zoned = dateTime.atZone(ZoneId.of("Asia/Shanghai"));
        return zoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private record OrderMetrics(Integer orderCount, Integer customerCount, Integer salesVolume,
                                Double activityDiscountAmount, Double promotionDiscountAmount,
                                Double allDiscountAmount, Integer couponUseCount, Double salesAmount) {
        private static OrderMetrics empty() {
            return new OrderMetrics(0, 0, 0, 0.0, 0.0, 0.0, 0, 0.0);
        }
    }

    private Map<Long, Long> queryCouponReceiveCountMap(MarketingRequest request, List<Long> storeIds) {
        if (request == null || CollectionUtils.isEmpty(storeIds)) {
            return Map.of();
        }
        EventQueryDTO queryDTO = getCouponClaimEventQueryDTO(request);
        queryDTO.setStoreIds(storeIds);
        Map<Long, Long> result = eventService.queryPVByStore(queryDTO);
        return result == null ? Map.of() : result;
    }

    @Override
    public List<MarketingChannelDataVO> getMarketingChannelData(MarketingRequest request) {
        ProductPageRequest productRequest = convertToProductPageRequest(request);
        // 优惠券使用数量
        productRequest.getMetrics().add(ProductMetricsConfigNew.COUPON_USE_COUNT.getCode());
        productRequest.setSortBy(null);
        productRequest.setSortOrder(null);


        //渠道归类
        String groupField = "channel";
        PageResult<AnalysisTopVO> storeAggregation = productAggerationService.getStoreAggregation(productRequest, groupField);
        List<AnalysisTopVO> list = storeAggregation.getList();
        List<MarketingChannelDataVO> data = new ArrayList<>();

        // 渠道类型对应渠道 id 列表
        Map<Long, String> channelIdMap = buildChannelIdMap();
        for (AnalysisTopVO analysisTopVO : list) {
            MarketingChannelDataVO channelDataVO = new MarketingChannelDataVO(analysisTopVO.getValues());
            Long channelId = parseChannelId(analysisTopVO.getKey());
            channelDataVO.setChannelId(channelId);
            channelDataVO.setChannelType(channelIdMap.getOrDefault(channelDataVO.getChannelId(),"历史渠道数据"));
            data.add(channelDataVO);
        }

        appendMissingChannelData(request, data, channelIdMap);

        for (MarketingChannelDataVO channelDataVO : data) {
            // 链接点击次数
            if (request.getShowUV()) {
                if (channelDataVO.getChannelId() != null) {
                    EventQueryDTO queryDTO = getEventQueryDTO(request);
                    queryDTO.setChannelIds(List.of(channelDataVO.getChannelId()));
                    Long pv = eventService.queryPV(queryDTO);
                    channelDataVO.setLinkClickCount(pv);
                } else {
                    channelDataVO.setLinkClickCount(0L);
                }
            }

            // 优惠券领取数量
            Long couponReceiveCount = 0L;
            if (channelDataVO.getChannelId() != null) {
                EventQueryDTO couponClaimQueryDTO = getCouponClaimEventQueryDTO(request);
                couponClaimQueryDTO.setChannelIds(List.of(channelDataVO.getChannelId()));
                couponReceiveCount = eventService.queryPV(couponClaimQueryDTO);
            }

            // 设置优惠券使用比率
            setCouponUseRate(channelDataVO, couponReceiveCount);

            // set rate
            channelDataVO.setRate();
        }

        // 计算渠道销售额占比
        calculateChannelSalesRatio(request, data);

        sortMarketingChannelData(data, request);

        // 设置合计
        appendTotalChannelData(data);

        return data;
    }

    private void sortMarketingChannelData(List<MarketingChannelDataVO> data, MarketingRequest request) {
        if (CollectionUtils.isEmpty(data) || request == null) {
            return;
        }

        String sortField = request.getSortBy();
        String sortOrder = request.getSortOrder();

        if (!StringUtils.hasText(sortField) && StringUtils.hasText(sortOrder)
            && !"asc".equalsIgnoreCase(sortOrder) && !"desc".equalsIgnoreCase(sortOrder)) {
            sortField = sortOrder;
            sortOrder = null;
        }
        if (!StringUtils.hasText(sortField)) {
            sortField = "salesAmount";
            sortOrder = "desc";
        }
        if (!StringUtils.hasText(sortOrder)) {
            sortOrder = "desc";
        }

        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        Comparator<MarketingChannelDataVO> comparator = buildChannelDataComparator(sortField.trim());
        if (comparator == null) {
            sortField = "salesAmount";
            asc = false;
            comparator = buildChannelDataComparator(sortField);
            if (comparator == null) {
                return;
            }
        }

        data.sort(asc ? comparator : comparator.reversed());
    }

    private Comparator<MarketingChannelDataVO> buildChannelDataComparator(String sortField) {
        return switch (sortField) {
            case "channelType" -> Comparator.comparing(MarketingChannelDataVO::getChannelType,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "linkClickCount" -> Comparator.comparing(MarketingChannelDataVO::getLinkClickCount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "couponReceiveCount" -> Comparator.comparing(MarketingChannelDataVO::getCouponReceiveCount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "couponUseCount" -> Comparator.comparing(MarketingChannelDataVO::getCouponUseCount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "couponUseRate" -> Comparator.comparing(item -> parsePercent(item.getCouponUseRate()));
            case "customerCount" -> Comparator.comparing(MarketingChannelDataVO::getCustomerCount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "orderCount" -> Comparator.comparing(MarketingChannelDataVO::getOrderCount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "salesVolume" -> Comparator.comparing(MarketingChannelDataVO::getSalesVolume,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "customerUnitPrice" -> Comparator.comparing(MarketingChannelDataVO::getCustomerUnitPrice,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "activityDiscountAmount" -> Comparator.comparing(MarketingChannelDataVO::getActivityDiscountAmount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "promotionDiscountAmount" -> Comparator.comparing(MarketingChannelDataVO::getPromotionDiscountAmount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "allDiscountAmount" -> Comparator.comparing(MarketingChannelDataVO::getAllDiscountAmount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "channelOrderAmount" -> Comparator.comparing(MarketingChannelDataVO::getChannelOrderAmount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "salesAmount" -> Comparator.comparing(MarketingChannelDataVO::getSalesAmount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "channelRate" -> Comparator.comparing(item -> parsePercent(item.getChannelRate()));
            case "linkClickUserCount" -> Comparator.comparing(MarketingChannelDataVO::getLinkClickUserCount,
                Comparator.nullsLast(Comparator.naturalOrder()));
            case "conversionRate" -> Comparator.comparing(item -> parsePercent(item.getConversionRate()));
            case "conversionPayRate" -> Comparator.comparing(item -> parsePercent(item.getConversionPayRate()));
            default -> null;
        };
    }

    private Double parsePercent(String value) {
        if (!StringUtils.hasText(value)) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.replace("%", "").trim());
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    /**
     * 将 MarketingRequest 转换为 ProductPageRequest
     *
     * @param marketingRequest 营销请求参数
     * @return 产品页面请求参数
     */
    private ProductPageRequest convertToProductPageRequest(MarketingRequest marketingRequest) {
        ProductPageRequest productRequest = new ProductPageRequest();
        productRequest.setCurrentTimeStart(marketingRequest.getCurrentTimeStart());
        productRequest.setCurrentTimeEnd(marketingRequest.getCurrentTimeEnd());
        productRequest.setStoreIds(marketingRequest.getStoreIds());
        productRequest.setStatType(marketingRequest.getStatType());
        productRequest.setChannelIds(marketingRequest.getChannelIds());
        // 设置基础指标
        List<ProductMetricsConfigNew> metrics = List.of(
            ProductMetricsConfigNew.CUSTOMER_COUNT,//下单人数
            ProductMetricsConfigNew.ORDER_COUNT,//活动订单数
            ProductMetricsConfigNew.SALES_VOLUME,//购买商品件数
            ProductMetricsConfigNew.ACTIVITY_DISCOUNT_AMOUNT,
            ProductMetricsConfigNew.PROMOTION_DISCOUNT_AMOUNT,
            ProductMetricsConfigNew.ALL_ACTIVITY_DISCOUNT,//活动优惠
            ProductMetricsConfigNew.PAY_AMOUNT//活动销售额
        );
        productRequest.setMetrics(metrics.stream().map(ProductMetricsConfigNew::getCode).collect(Collectors.toSet()));
        productRequest.setActivityIds(marketingRequest.getActivityIds());
        productRequest.setChannelIds(marketingRequest.getChannelIds());
        productRequest.setCouponIds(marketingRequest.getCouponIds());
        productRequest.setSortBy(marketingRequest.getSortBy());
        productRequest.setSortOrder(marketingRequest.getSortOrder());
        return productRequest;
    }

    /**
     * 计算渠道销售额占比
     *
     * @param request       营销请求参数
     * @param channelDataVO 渠道数据VO
     */
    private void calculateChannelSalesRatio(MarketingRequest request, List<MarketingChannelDataVO> channelDataVO) {
        if (request == null || CollectionUtils.isEmpty(channelDataVO)) {
            return;
        }

        // 创建 EsAggDTO 对象
        Double totalSalesAmount = 0.0;
        for (MarketingChannelDataVO marketingChannelDataVO : channelDataVO) {

            EsAggDTO esAggDTO = new EsAggDTO();
            esAggDTO.setTimes(new LocalDateTime[]{request.getCurrentTimeStart(), request.getCurrentTimeEnd()});
            esAggDTO.setStoreIds(request.getStoreIds());
            if (marketingChannelDataVO.getChannelId() == null) {
                marketingChannelDataVO.setChannelOrderAmount(0.0);
                continue;
            }
            esAggDTO.setChannel(Math.toIntExact(marketingChannelDataVO.getChannelId()));

            Map<String, Double> aggregateResult = esAggregationService.batchAggregateMetrics(esAggDTO, List.of(MetricsConfig.PAY_AMOUNTS));
            Double channelSalesAmount = aggregateResult.getOrDefault("payAmount", 0.0);
            totalSalesAmount += channelSalesAmount;
            marketingChannelDataVO.setChannelOrderAmount(channelSalesAmount);
        }

        for (MarketingChannelDataVO marketingChannelDataVO : channelDataVO) {
            if (totalSalesAmount > 0) {
                double channelOrderAmount = marketingChannelDataVO.getChannelOrderAmount() == null ? 0.0 : marketingChannelDataVO.getChannelOrderAmount();
                double channelRate = (channelOrderAmount / totalSalesAmount) * 100;
                marketingChannelDataVO.setChannelRate(String.format("%.2f%%", channelRate));
            } else {
                marketingChannelDataVO.setChannelRate("0.00%");
            }
        }

    }

    private void appendMissingChannelData(MarketingRequest request,
                                          List<MarketingChannelDataVO> data,
                                          Map<Long, String> channelIdMap) {
        if (CollectionUtils.isEmpty(channelIdMap)) {
            return;
        }

        Set<Long> existingChannelIds = data.stream()
            .map(MarketingChannelDataVO::getChannelId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(HashSet::new));

        Set<Long> targetChannelIds;
        if (CollectionUtils.isEmpty(request.getChannelIds())) {
            targetChannelIds = channelIdMap.keySet();
        } else {
            targetChannelIds = request.getChannelIds().stream()
                .filter(channelIdMap::containsKey)
                .collect(Collectors.toCollection(HashSet::new));
        }

        for (Long channelId : targetChannelIds) {
            if (existingChannelIds.contains(channelId)) {
                continue;
            }
            MarketingChannelDataVO emptyChannelData = new MarketingChannelDataVO(null);
            emptyChannelData.setChannelId(channelId);
            emptyChannelData.setChannelType(channelIdMap.get(channelId));
            data.add(emptyChannelData);
        }
    }

    private Map<Long, String> buildChannelIdMap() {
        Map<Long, String> channelIdMap = new LinkedHashMap<>();
        CommonResult<List<ActivityChannelNameDataRespVO>> channelResp = activityApi.getChannelList();
        if (channelResp == null || CollectionUtils.isEmpty(channelResp.getCheckedData())) {
            return channelIdMap;
        }
        for (ActivityChannelNameDataRespVO channel : channelResp.getCheckedData()) {
            channelIdMap.put(channel.getId(), channel.getName());
        }
        return channelIdMap;
    }

    private Long parseChannelId(String channelKey) {
        if (!StringUtils.hasText(channelKey)) {
            return null;
        }
        try {
            return Long.valueOf(channelKey);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void appendTotalChannelData(List<MarketingChannelDataVO> data) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }

        MarketingChannelDataVO total = new MarketingChannelDataVO(null);
        total.setChannelType("合计");
        total.setChannelRate("100.00%");

        long totalLinkClickCount = 0L;
        int totalCouponReceiveCount = 0;
        int totalCouponUseCount = 0;
        int totalCustomerCount = 0;
        int totalOrderCount = 0;
        int totalSalesVolume = 0;
        double totalActivityDiscountAmount = 0.0;
        double totalPromotionDiscountAmount = 0.0;
        double totalAllDiscountAmount = 0.0;
        double totalChannelOrderAmount = 0.0;
        double totalSalesAmount = 0.0;

        for (MarketingChannelDataVO item : data) {
            totalLinkClickCount += item.getLinkClickCount() == null ? 0L : item.getLinkClickCount();
            totalCouponReceiveCount += item.getCouponReceiveCount() == null ? 0 : item.getCouponReceiveCount();
            totalCouponUseCount += item.getCouponUseCount() == null ? 0 : item.getCouponUseCount();
            totalCustomerCount += item.getCustomerCount() == null ? 0 : item.getCustomerCount();
            totalOrderCount += item.getOrderCount() == null ? 0 : item.getOrderCount();
            totalSalesVolume += item.getSalesVolume() == null ? 0 : item.getSalesVolume();
            totalActivityDiscountAmount += item.getActivityDiscountAmount() == null ? 0.0 : item.getActivityDiscountAmount();
            totalPromotionDiscountAmount += item.getPromotionDiscountAmount() == null ? 0.0 : item.getPromotionDiscountAmount();
            totalAllDiscountAmount += item.getAllDiscountAmount() == null ? 0.0 : item.getAllDiscountAmount();
            totalChannelOrderAmount += item.getChannelOrderAmount() == null ? 0.0 : item.getChannelOrderAmount();
            totalSalesAmount += item.getSalesAmount() == null ? 0.0 : item.getSalesAmount();
        }

        total.setLinkClickCount(totalLinkClickCount);
        total.setCouponReceiveCount(totalCouponReceiveCount);
        total.setCouponUseCount(totalCouponUseCount);
        total.setCustomerCount(totalCustomerCount);
        total.setOrderCount(totalOrderCount);
        total.setSalesVolume(totalSalesVolume);
        total.setActivityDiscountAmount(totalActivityDiscountAmount);
        total.setPromotionDiscountAmount(totalPromotionDiscountAmount);
        total.setAllDiscountAmount(totalAllDiscountAmount);
        total.setChannelOrderAmount(totalChannelOrderAmount);
        total.setSalesAmount(totalSalesAmount);

        if (totalCouponReceiveCount > 0) {
            double couponUseRate = (double) totalCouponUseCount / totalCouponReceiveCount;
            total.setCouponUseRate(String.format("%.2f%%", couponUseRate * 100));
        } else {
            total.setCouponUseRate("0.00%");
        }

        total.setRate();
        data.add(total);
    }


}
