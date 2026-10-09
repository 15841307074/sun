package com.htyoudao.youdao.module.order.service.order;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation.Builder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation.Builder.ContainerBuilder;
import co.elastic.clients.elasticsearch._types.aggregations.Buckets;
import co.elastic.clients.elasticsearch._types.aggregations.CompositeAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.CompositeAggregationSource;
import co.elastic.clients.elasticsearch._types.aggregations.CompositeBucket;
import co.elastic.clients.elasticsearch._types.aggregations.FilterAggregate;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.analysis.ProductSonRequest;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.analysis.ProductSonResult;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductSonDO;
import com.htyoudao.youdao.module.order.dal.es.BzOrderProductDocument;
import com.htyoudao.youdao.module.order.dal.mysql.BzOrderProductSonMapper;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2024-10-08
 */
@DS(DsNameConstants.SHARDING)
@Slf4j
@Service
public class BzOrderProductSonServiceImpl extends ServiceImpl<BzOrderProductSonMapper, BzOrderProductSonDO> implements
    BzOrderProductSonService {

    @Resource
    private ElasticsearchClient elasticsearchClient;


    @Override
    public List<BzOrderProductSonDO> getOrderProductSonList(List<Long> productIds, LocalDateTime[] createTimes) {
        if (CollectionUtils.isEmpty(productIds)) {
            return new ArrayList<>();
        }
        return baseMapper.selectList(productIds, createTimes);
    }


    /**
     * 根据条件聚合商品销量
     *
     * @return 商品销售聚合结果列表
     */
    @Override
    public List<ProductSonResult> productSonList(ProductSonRequest productSonRequest) {

        try {
            // 构建查询请求
            SearchRequest searchRequest = buildSearchRequest(
                productSonRequest.getCommodityId(), productSonRequest.getStoreIds(),
                productSonRequest.getCurrentTimeStart(), productSonRequest.getCurrentTimeEnd()
            );

            // 执行查询
            SearchResponse<BzOrderProductDocument> response = elasticsearchClient.search(
                searchRequest, BzOrderProductDocument.class
            );

            // 处理结果
            return processAggregationResults(response);

        } catch (IOException e) {
            log.error("ES查询失败", e);
            throw new RuntimeException("ES查询异常", e);
        }
    }

    private SearchRequest buildSearchRequest(String parentCommodityId, List<Long> storeIds, LocalDateTime startTime,
        LocalDateTime endTime) {

        // 构建查询条件
        Query boolQuery = buildBoolQuery(parentCommodityId, storeIds, startTime, endTime);

        // 构建聚合
        ContainerBuilder containerBuilder = new Builder().composite(c -> {
            CompositeAggregationSource source = CompositeAggregationSource.of(
                cs -> cs.terms(t -> t.field("commodityId")));
            c.sources(List.of(Map.of("commodityId", source)));
            c.size(300);
            return c;
        });
        containerBuilder.aggregations("goodsName", Aggregation.of(a ->
            a.terms(t -> t.field("goodsName.keyword")
                .size(1)
            )));
        containerBuilder.aggregations("goodsImage", Aggregation.of(a ->
            a.terms(t -> t.field("goodsImage")
                .size(1)
            )));
        containerBuilder.aggregations("goodsNum", Aggregation.of(a ->
            a.sum(t -> t.field("goodsNum")
            )));
        return SearchRequest.of(s -> s
            .size(0) // 不返回具体文档，只返回聚合结果
            .query(boolQuery)
            .aggregations("products", containerBuilder.build())
        );
    }

    private Query buildBoolQuery(String parentCommodityId, List<Long> storeIds, LocalDateTime startTime,
        LocalDateTime endTime) {

        List<Query> mustQueries = new ArrayList<>();

        // parentCommodityId 必须条件
        mustQueries.add(Query.of(q -> q
            .term(t -> t.field("parentCommodityId").value(parentCommodityId))
        ));

        // storeIds 条件
        if (storeIds != null && !storeIds.isEmpty()) {
            mustQueries.add(Query.of(q -> q
                .terms(t -> t.field("storeId").terms(ts -> ts
                    .value(storeIds.stream()
                        .map(FieldValue::of)
                        .collect(Collectors.toList()))
                ))
            ));
        }

        // 时间范围条件
        if (startTime != null && endTime != null) {
            mustQueries.add(Query.of(q ->
                q.range(
                    r -> r.date(dr ->
                        dr.field("createTime")
                            .gte(formatDateTime(startTime))
                            .lte(formatDateTime(endTime))
                    ))));
        }

        return Query.of(q -> q.bool(b -> b.must(mustQueries)));
    }


    // 辅助方法：格式化 LocalDateTime 为 ISO 格式字符串
    private String formatDateTime(LocalDateTime dateTime) {
        ZonedDateTime zoned = dateTime.atZone(ZoneId.of("Asia/Shanghai"));
        return zoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }


    /**
     * 处理聚合结果
     */
    private List<ProductSonResult> processAggregationResults(SearchResponse<BzOrderProductDocument> response) {
        CompositeAggregate compositeAgg = response.aggregations().get("products").composite();

        Buckets<CompositeBucket> buckets = compositeAgg.buckets();
        return buckets.array().stream().map(this::getProductResult).collect(Collectors.toList());
    }

    private ProductSonResult getProductResult(CompositeBucket bucket) {
        ProductSonResult item = new ProductSonResult();

        item.setCommodityId(bucket.key().get("commodityId").longValue());

        Aggregate nameAgg = getAggregate(bucket, "goodsName");
        item.setGoodsName(getHit1Value(nameAgg));

        Aggregate imageAgg = getAggregate(bucket, "goodsImage");
        item.setGoodsImage(getHit1Value(imageAgg));

        //销售额
        Aggregate salesAgg = getAggregate(bucket, "goodsNum");
        if (salesAgg != null) {
            item.setSalesVolume(salesAgg.sum().value());
        }

        return item;
    }

    private String getHit1Value(Aggregate goodsAgg) {
        if (goodsAgg != null) {
            if (goodsAgg.isLterms()) {
                return goodsAgg.lterms().buckets().array().get(0).key() + "";
            } else {
                return goodsAgg.sterms().buckets().array().get(0).key().stringValue();
            }
        }

        return "";
    }

    private Long getHit1LongValue(Aggregate goodsAgg) {
        if (goodsAgg != null) {
            if (goodsAgg.isLterms()) {
                return goodsAgg.lterms().buckets().array().get(0).key();
            } else {
                return Long.valueOf(goodsAgg.sterms().buckets().array().get(0).key().stringValue());
            }
        }
        return null;
    }

    private static Aggregate getAggregate(CompositeBucket bucket, String key) {
        Aggregate aggregate = bucket.aggregations().get(key);
        if (aggregate == null) {
            return null;
        }

        return aggregate;
//        FilterAggregate filter = aggregate.filter();
//        if (filter == null) {
//            return null;
//        }
//        if (CollectionUtils.isEmpty(filter.aggregations())) {
//            return null;
//        }
//        return filter.aggregations().get(key);
    }


}
