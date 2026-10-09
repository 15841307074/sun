package com.htyoudao.youdao.module.analysis.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.CardinalityAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.SumAggregate;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.htyoudao.youdao.module.analysis.dal.es.ScmOrderDetailDocument;
import com.htyoudao.youdao.module.analysis.service.IScfOrderFullService;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class ScfOrderFullServiceImpl implements IScfOrderFullService {

    @Resource
    private ElasticsearchClient client;

    @Override
    public Double getGylOrderAmount(List<Long> storeIds, LocalDateTime timeStart, LocalDateTime timeEnd) {
        // 1. 参数校验
        if (CollectionUtils.isEmpty(storeIds)) {
            log.warn("storeIds参数为空");
            return 0.0;
        }

        Query query = getQuery(storeIds, timeStart, timeEnd);

        // 2. 构建聚合请求
        SearchRequest searchRequest = SearchRequest.of(s -> s.index("scm_order_full")
            .size(0) // 不需要返回原始文档
            .query(query)
            .aggregations("orderAmount", a -> a.sum(c -> c.field("commodityAmount"))
            ));
        // 3. 执行查询
        SearchResponse<ScmOrderDetailDocument> response = null;
        try {
            response = client.search(searchRequest, ScmOrderDetailDocument.class);
        } catch (Exception e) {
            return 0.0;
        }

        SumAggregate orderAmount = response.aggregations().get("orderAmount").sum();

        // 处理没有匹配数据的情况
        return orderAmount != null ? orderAmount.value() : 0.0;
    }



    @Override
    public List<ScmOrderDetailDocument> gylOrderList(List<Long> storeIds, LocalDateTime timeStart,
        LocalDateTime timeEnd) {
        // 1. 参数校验
        if (CollectionUtils.isEmpty(storeIds)) {
            log.warn("storeIds参数为空");
            return Collections.emptyList();
        }

        Query query = getQuery(storeIds, timeStart, timeEnd);

        // 2. 构建聚合请求
        SearchRequest searchRequest = SearchRequest.of(s ->
            s.index("scm_order_full")
            .query(query)
            .sort(sort -> sort.field(f -> f
                .field("createTime")
                .order(SortOrder.Desc)
            ))
        );
        // 3. 执行查询
        SearchResponse<ScmOrderDetailDocument> response = null;
        try {
            response = client.search(searchRequest, ScmOrderDetailDocument.class);
        } catch (Exception e) {
            return List.of();
        }

        // 6. 提取文档列表
        return response.hits().hits().stream()
            .map(Hit::source)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }


    // 辅助方法：格式化 LocalDateTime 为 ISO 格式字符串
    private String formatDateTime(LocalDateTime dateTime) {
        ZonedDateTime zoned = dateTime.atZone(ZoneId.of("Asia/Shanghai"));
        return zoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }


    private Query getQuery(List<Long> storeIds, LocalDateTime timeStart, LocalDateTime timeEnd) {

        List<Integer> notStatus = List.of(7,70,0);

        return Query.of(q -> q.bool(b -> b
            .must(m -> m.range(r -> r.date(dr -> dr.field("createTime")
                .gte(formatDateTime(timeStart))
                .lt(formatDateTime(timeEnd)))))
            .must(m -> m.terms(t -> t.field("storeId").terms(tv -> tv.value(
                storeIds.stream().map(FieldValue::of).toList()))))
            .mustNot(m -> m.terms(t -> t.field("orderStatus").terms(tv -> tv.value(
                notStatus.stream().map(FieldValue::of).toList())))))
        );
    }
}
