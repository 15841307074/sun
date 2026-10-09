package com.htyoudao.youdao.module.bpm.service.learningtask;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** 学习任务统计必须读取营销埋点并按项目、任务类型和任务ID隔离。 */
class LearningTaskAccessQueryTest {
    @Test
    void statisticsUsesMarketingEventsAndManagerIdentity() {
        SearchRequest request = LearningTaskAccessServiceImpl.buildStatisticsRequest(10L, 2098319195630198785L);
        assertEquals(List.of("event_logs_*"), request.index());
        Map<String, TermQuery> filters = request.query().bool().filter().stream()
                .map(query -> query.term()).collect(Collectors.toMap(TermQuery::field, query -> query));
        assertEquals(3, filters.size());
        assertEquals(10L, filters.get("businessId").value().longValue());
        assertEquals("learning_task", filters.get("eventType").value().stringValue());
        assertEquals("2098319195630198785", filters.get("eventId").value().stringValue());
        assertEquals("memberId", request.aggregations().get("uv").cardinality().field());
        assertTrue(request.trackTotalHits().enabled());
        assertTrue(request.allowNoIndices());
        assertFalse(request.allowPartialSearchResults());
    }
}
