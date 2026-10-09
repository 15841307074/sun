package com.htyoudao.youdao.module.order.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkListener;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.htyoudao.youdao.module.order.dal.es.BzOrderPointsDocument;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Configuration
@RefreshScope
@Slf4j
public class BulkConfig {

    //批量操作的最大数量
    @Value("${elasticsearch.bulkActions:1000}")
    private Integer bulkActions;

    //批量操作的自动刷新时间间隔 单位秒
    @Value("${elasticsearch.flushInterval:30}")
    private Integer flushInterval;

    //并发批量请求数 0同步
    @Value("${elasticsearch.concurrentRequests:1}")
    private Integer concurrentRequests;

    @Resource
    private ElasticsearchClient client;


    @Bean("bulkIngester")
    public BulkIngester<BzOrderPointsDocument> bulkIngester() {
        BulkListener<BzOrderPointsDocument> bulkListener = new BulkListener<BzOrderPointsDocument>() {
            @Override
            public void beforeBulk(long l, BulkRequest bulkRequest, List<BzOrderPointsDocument> list) {

            }

            @Override
            public void afterBulk(long executionId, BulkRequest bulkRequest, List<BzOrderPointsDocument> list, BulkResponse bulkResponse) {
                if (bulkResponse.errors()) {
                    log.error("Bulk {} excuted with failures", executionId);
                } else {
                    log.info("序号{},批量推送成功，共耗时{}ms", executionId, bulkResponse.took());
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest bulkRequest, List<BzOrderPointsDocument> list, Throwable throwable) {
                log.error("Bulk {} 批量推送失败， 报错信息为{}", executionId, throwable.getMessage());
            }
        };
        return BulkIngester.of(builder -> builder
            .client(client)
            .maxOperations(bulkActions)//累计推送ES消息数量
            .maxConcurrentRequests(concurrentRequests)//请求ES最大并发数
            .flushInterval(flushInterval, TimeUnit.SECONDS)//刷新间隔
            .listener(bulkListener));
    }
}