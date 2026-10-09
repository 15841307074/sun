package com.htyoudao.youdao.module.analysis.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.json.JsonData;
import com.htyoudao.youdao.module.analysis.dal.es.EsField;
import com.htyoudao.youdao.module.analysis.dal.es.ScmOrderDetailDocument;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.AggregationContainer;
import org.springframework.data.elasticsearch.core.AggregationsContainer;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EsPurchaseQueryService {

    @Resource
    private ElasticsearchOperations operations;

    @Value
    public static class AmountAggResult {
        Map<Long, String> idToName;       // id -> name（warehouse/store）
        Map<Long, Double> currentAmount;  // 当前段
        Map<Long, Double> momAmount;      // 环比段
        Map<Long, Double> yoyAmount;      // 同比段（本方法不查，外部可塞）
    }

    /**
     * 查询：每个门店在区间内「发生过下单的自然日集合」
     * key   : storeId
     * value : 下单日期列表（LocalDate，升序、去重）
     */
    public Map<Long, List<LocalDate>> queryOrderDaysByStore(Collection<Long> storeIds, long startMs, long endExclusiveMs, Collection<Integer> invalidOrderStatus, Long warehouseId) {
        if (storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Query q = this.buildLastOrderQuery(storeIds, invalidOrderStatus, warehouseId, startMs, endExclusiveMs);

        // date_histogram：按天聚合（只返回有下单的天）
        Aggregation dayAgg = Aggregation.of(a -> a.dateHistogram(d -> d
                .field(EsField.ORDER_TIME_FIELD)
                .calendarInterval(CalendarInterval.Day)
                .minDocCount(1)
        ));

        Aggregation termsAgg = Aggregation.of(a -> a
                .terms(t -> t.field(EsField.STORE_ID).size(20000))
                .aggregations("days", dayAgg)
        );

        NativeQuery query = new NativeQueryBuilder()
                .withQuery(q)
                .withMaxResults(0)
                .withAggregation("t", termsAgg)
                .build();

        SearchHits<ScmOrderDetailDocument> hits = null;
        hits = operations.search(query, ScmOrderDetailDocument.class);

        Map<String, Aggregate> aggs = extractAggMap(hits);
        Aggregate t = aggs.get("t");

        if (!t.isLterms()) return Collections.emptyMap();

        Map<Long, List<LocalDate>> res = new HashMap<>();
        ZoneId zone = ZoneId.systemDefault();

        for (LongTermsBucket b : t.lterms().buckets().array()) {
            Long storeId = b.key();
            Aggregate daysAgg = b.aggregations().get("days");
            if (daysAgg == null || !daysAgg.isDateHistogram()) continue;

            List<LocalDate> days = new ArrayList<>();
            for (DateHistogramBucket db : daysAgg.dateHistogram().buckets().array()) {
                LocalDate d = Instant.ofEpochMilli(db.key())
                        .atZone(zone)
                        .toLocalDate();
                days.add(d);
            }

            if (!days.isEmpty()) {
                res.put(storeId, days.stream().distinct().sorted().toList());
            }
        }
        return res;
    }


    /**
     * 查询：按某个字段（warehouseId/storeId）terms 聚合，
     * 一次查出 current + mom（filters 切割）+ sum(orderAmount)
     */
    public AmountAggResult queryAmountByTermsWithMom(
            String termsField,
            String nameField,
            Collection<Long> storeIds,
            long currentStartMs, long currentEndExclusiveMs,
            long momStartMs, long momEndExclusiveMs,
            Collection<Integer> invalidOrderStatus,
            Collection<Long> activeStoreFilter // 可为空：按月剔除十天未进货门店时使用
    ) {
        if (storeIds == null || storeIds.isEmpty()) {
            return new AmountAggResult(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
        }

        // baseQuery：storeId in + (optional) activeStoreFilter + orderStatus not in + time in [momStart, currentEnd)
        Query baseQuery = buildBaseQuery(storeIds, invalidOrderStatus, activeStoreFilter, momStartMs, currentEndExclusiveMs);

        // period filters agg（current/mom）-> sum
        Aggregation periodAgg = Aggregation.of(a -> a
                .filters(f -> f.filters(ff -> ff.keyed(Map.of(
                        "current", Query.of(q -> q.range(r -> r
                                .date(n -> n
                                        .field(EsField.ORDER_TIME_FIELD)
                                        .gte(String.valueOf(currentStartMs))
                                        .lt(String.valueOf(currentEndExclusiveMs))
                                )
                        )),
                        "mom", Query.of(q -> q.range(r -> r
                                .date(n -> n
                                        .field(EsField.ORDER_TIME_FIELD)
                                        .gte(String.valueOf(momStartMs))
                                        .lt(String.valueOf(momEndExclusiveMs))
                                )
                        ))
                ))))
                .aggregations("amt",
                        Aggregation.of(a2 -> a2.sum(s -> s.field(EsField.ORDER_AMOUNT)))
                )
        );


        // name 子聚合（size=1）
        Aggregation nameAgg = Aggregation.of(a -> a
                .terms(t -> t.field(nameField).size(1))
        );

        // terms(termsField) -> name + period
        Aggregation termsAgg = Aggregation.of(a -> a
                .terms(t -> t.field(termsField).size(5000))
                .aggregations("name", nameAgg)
                .aggregations("period", periodAgg)
        );

        NativeQuery query = new NativeQueryBuilder()
                .withQuery(baseQuery)
                .withMaxResults(0)
                .withAggregation("t", termsAgg)
                .build();

        SearchHits<ScmOrderDetailDocument> hits = operations.search(query, ScmOrderDetailDocument.class);

        Map<Long, String> idToName = new HashMap<>();
        Map<Long, Double> curMap = new HashMap<>();
        Map<Long, Double> momMap = new HashMap<>();

        // 解析聚合（SDE 5.x 使用 ELC 的 Aggregate 类型）
        Map<String, Aggregate> aggs = extractAggMap(hits);
        Aggregate t = aggs.get("t");
        if (t == null || !t.isLterms()) {
            return new AmountAggResult(idToName, curMap, momMap, Collections.emptyMap());
        }

        for (LongTermsBucket bucket : t.lterms().buckets().array()) {

            Long id = bucket.key();
            // name
            String name = extractFirstStringTermKey(bucket.aggregations().get("name"));
            if (name != null) {
                idToName.put(id, name);
            }

            // period -> current/mom -> amt(sum)
            Aggregate period = bucket.aggregations().get("period");
            Map<String, Double> periodAmt = extractFiltersSum(period, "amt");

            curMap.put(id, periodAmt.getOrDefault("current", 0D));
            momMap.put(id, periodAmt.getOrDefault("mom", 0D));
        }

        return new AmountAggResult(idToName, curMap, momMap, Collections.emptyMap());
    }

    /**
     * 查询同比：terms(termsField) + sum(orderAmount)
     */
    public Map<Long, Double> queryAmountByTermsYoy(
            String termsField,
            String nameField,
            Collection<Long> storeIds,
            long yoyStartMs, long yoyEndExclusiveMs,
            Collection<Integer> invalidOrderStatus,
            Collection<Long> activeStoreFilter
    ) {
        if (storeIds == null || storeIds.isEmpty()) return Collections.emptyMap();

        Query q = buildBaseQuery(storeIds, invalidOrderStatus, activeStoreFilter, yoyStartMs, yoyEndExclusiveMs);

        Aggregation termsAgg = Aggregation.of(a -> a
                .terms(t -> t.field(termsField).size(5000))
                .aggregations("amt", Aggregation.of(a2 -> a2.sum(s -> s.field(EsField.ORDER_AMOUNT))))
        );

        NativeQuery query = new NativeQueryBuilder()
                .withQuery(q)
                .withMaxResults(0)
                .withAggregation("t", termsAgg)
                .build();

        SearchHits<ScmOrderDetailDocument> hits = operations.search(query, ScmOrderDetailDocument.class);
        Map<String, Aggregate> aggs = extractAggMap(hits);

        Aggregate t = aggs.get("t");
        if (t == null || !t.isLterms()) return Collections.emptyMap();

        Map<Long, Double> res = new HashMap<>();
        for (LongTermsBucket bucket : t.lterms().buckets().array()) {
            Long id = bucket.key();
            double amt = extractSum(bucket.aggregations().get("amt"));
            res.put(id, amt);
        }
        return res;
    }

    // -------------------- Query Builders --------------------

    /**
     * baseQuery：
     * - storeId in storeIds
     * - (optional) storeId in activeStoreFilter
     * - orderStatus not in invalidOrderStatus
     * - range(orderCreateTime) in [startMs, endExclusiveMs)
     */
    private Query buildBaseQuery(Collection<Long> storeIds,
                                 Collection<Integer> invalidOrderStatus,
                                 Collection<Long> activeStoreFilter,
                                 long startMs,
                                 long endExclusiveMs) {

        return Query.of(q -> q.bool(b -> {
                    // storeIds
                    b.filter(f -> f.terms(t -> t.field(EsField.STORE_ID)
                            .terms(v -> v.value(toFieldValueList(storeIds)))));

                    // activeStoreFilter（按月剔除十天未进货门店）
                    if (activeStoreFilter != null) {
                        b.filter(f -> f.terms(t -> t.field(EsField.STORE_ID)
                                .terms(v -> v.value(toFieldValueList(activeStoreFilter)))));
                    }

                    // time range
                    b.filter(f -> f.range(r -> r
                            .date(n -> n
                                    .field(EsField.ORDER_TIME_FIELD)
                                    .gte(String.valueOf(startMs))
                                    .lt(String.valueOf(endExclusiveMs))
                            )
                    ));

                    // orderStatus not in
                    if (invalidOrderStatus != null) {
                        for (Integer s : invalidOrderStatus) {
                            b.mustNot(mn -> mn.term(t -> t.field(EsField.ORDER_STATUS).value(s)));
                        }
                    }

                    return b;
                }

        ));
    }

    private Query buildLastOrderQuery(Collection<Long> storeIds,
                                      Collection<Integer> invalidOrderStatus,
                                      Long warehouseId,
                                      long startMs,
                                      long endExclusiveMs) {

        return Query.of(q -> q.bool(b -> {
            b.filter(f -> f.terms(t -> t.field(EsField.STORE_ID)
                    .terms(v -> v.value(toFieldValueList(storeIds)))));

            if (warehouseId != null) {
                b.filter(f -> f.term(t -> t.field(EsField.WAREHOUSE_ID).value(warehouseId)));
            }

            b.filter(f -> f.range(r -> r
                    .date(n -> n
                            .field(EsField.ORDER_TIME_FIELD)
                            .gte(String.valueOf(startMs))
                            .lt(String.valueOf(endExclusiveMs))
                    )
            ));


            if (invalidOrderStatus != null) {
                for (Integer s : invalidOrderStatus) {
                    b.mustNot(mn -> mn.term(t -> t.field(EsField.ORDER_STATUS).value(s)));
                }
            }
            return b;
        }));
    }

    // -------------------- helpers --------------------

    /**
     * SDE 5.4.x：SearchHits.getAggregations() 返回的是 AggregationsContainer，
     * 里面可以取到 ELC 原生的 Map<String, Aggregate>
     */
    @SuppressWarnings("unchecked")
    private Map<String, Aggregate> extractAggMap(SearchHits<?> hits) {
        if (hits == null || hits.getAggregations() == null) return Collections.emptyMap();

        Object container = hits.getAggregations();

        // 1) 常见：AggregationsContainer
        if (container instanceof AggregationsContainer<?> ac) {
            Object inner = ac.aggregations();

            // 1.1) inner 是 ElasticsearchAggregations
            if (inner instanceof ElasticsearchAggregations ea) {
                Object listOrMap = ea.aggregations(); // 你现场：这里返回 List
                if (listOrMap instanceof Map<?, ?> m) {
                    return (Map<String, Aggregate>) m;
                }
                if (listOrMap instanceof List<?> list) {
                    return listToAggMap(list);
                }
            }

            // 1.2) inner 直接是 Map
            if (inner instanceof Map<?, ?> m) {
                return (Map<String, Aggregate>) m;
            }

            // 1.3) inner 是 List
            if (inner instanceof List<?> list) {
                return listToAggMap(list);
            }
        }

        // 2) 少数：container 直接是 List
        if (container instanceof List<?> list) {
            return listToAggMap(list);
        }

        // 3) 兜底：反射（先判类型，不强转）
        try {
            Object inner = container.getClass().getMethod("aggregations").invoke(container);
            if (inner instanceof Map<?, ?> m) return (Map<String, Aggregate>) m;
            if (inner instanceof List<?> list) return listToAggMap(list);
        } catch (Exception ignore) {}

        return Collections.emptyMap();
    }

    /**
     * 把 Spring Data 的聚合“列表结构”转成 Map<String, Aggregate>
     * 兼容 list 元素为：带 getName()/getAggregation() 的对象
     */
    @SuppressWarnings("unchecked")
    private Map<String, Aggregate> listToAggMap(List<?> list) {
        if (list == null || list.isEmpty()) return Collections.emptyMap();

        Map<String, Aggregate> map = new HashMap<>();
        for (Object item : list) {
            if (item == null) continue;

            try {
                // Spring Data 聚合 wrapper 一般都有 getName()
                String name = ((ElasticsearchAggregation) item).aggregation().getName();

                // wrapper 里一般是 getAggregation() 或 getAggregate()
                Object aggObj =  ((ElasticsearchAggregation) item).aggregation().getAggregate();

                if (name != null && aggObj instanceof Aggregate a) {
                    map.put(name, a);
                }
            } catch (Exception ignore) {
                ignore.printStackTrace();
            }
        }
        return map;
    }

    private String extractFirstStringTermKey(Aggregate agg) {
        if (agg == null || !agg.isSterms()) return null;
        List<StringTermsBucket> buckets = agg.sterms().buckets().array();
        return buckets.isEmpty() ? null : buckets.get(0).key().stringValue();
    }

    private double extractSum(Aggregate agg) {
        if (agg == null || !agg.isSum()) return 0D;

        double v = agg.sum().value();   // 基本类型 double
        // 有些场景 ES 会返回 NaN（比如没命中数据），这里兜底
        if (Double.isNaN(v)) return 0D;
        return v;
    }

    private long extractMaxAsLong(Aggregate agg) {
        if (agg == null || !agg.isMax()) return 0L;

        double v = agg.max().value();   // 基本类型 double
        if (Double.isNaN(v)) return 0L;
        return (long) v;
    }

    /**
     * 从 filters 聚合里取 keyed buckets 的 sum
     * 返回：{"current": x, "mom": y}
     */
    private Map<String, Double> extractFiltersSum(Aggregate filtersAgg, String sumAggName) {
        if (filtersAgg == null || !filtersAgg.isFilters()) return Collections.emptyMap();

        FiltersAggregate fa = filtersAgg.filters();
        if (fa.buckets() == null || !fa.buckets().isKeyed()) return Collections.emptyMap();

        Map<String, Double> res = new HashMap<>();
        Map<String, FiltersBucket> keyed = fa.buckets().keyed();
        for (Map.Entry<String, FiltersBucket> e : keyed.entrySet()) {
            Aggregate sumAgg = e.getValue().aggregations().get(sumAggName);
            res.put(e.getKey(), extractSum(sumAgg));
        }
        return res;
    }

    private Long fieldValueToLong(FieldValue fv) {
        if (fv == null) return null;

        if (fv.isLong()) {
            return fv.longValue();
        }
        if (fv.isDouble()) {
            return (long) fv.doubleValue();
        }
        if (fv.isString()) {
            try {
                return Long.parseLong(fv.stringValue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private List<FieldValue> toFieldValueList(Collection<Long> ids) {
        return ids.stream()
                .map(FieldValue::of)
                .toList();
    }
}
