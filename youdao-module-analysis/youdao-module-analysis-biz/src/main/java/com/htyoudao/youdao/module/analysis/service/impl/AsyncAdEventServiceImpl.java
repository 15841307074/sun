package com.htyoudao.youdao.module.analysis.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.module.analysis.dal.es.AdEvent;
import com.htyoudao.youdao.module.analysis.framework.config.kafka.KafkaProducerConfigurationProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 异步广告事件服务（Kafka 版）
 * 生产端：HTTP 线程异步发送事件到 Kafka（非阻塞），立即返回
 * 消费端：@KafkaListener 批量消费消息，按月份分组批量写入 ES
 */
@Slf4j
@Service("asyncAdEventService")
public class AsyncAdEventServiceImpl {

    @Resource
    private ElasticsearchClient client;

    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;

    @Resource
    private ObjectMapper objectMapper;

    /** Kafka生产者配置（发送开关） */
    @Resource
    private KafkaProducerConfigurationProperties producerProperties;

    /** Kafka Topic，读取 kafka.topic.ad-event 配置 */
    @Value("${kafka.topic.ad-event:ad_event}")
    private String topic;

    /** 累计发送失败数 */
    private final AtomicLong totalSendFailed = new AtomicLong(0);

    /** 发送失败日志限流：上次输出时间戳，避免高并发下日志风暴 */
    private final AtomicLong lastFailLogTime = new AtomicLong(0);

    /** 跳过发送日志限流，每秒最多1条 */
    private final AtomicLong lastSkipLogTime = new AtomicLong(0);

    /** 累计刷盘数 */
    private final AtomicLong totalFlushed = new AtomicLong(0);

    /** AdEvent 列表的反序列化类型（新数组格式消息） */
    private static final TypeReference<List<AdEvent>> AD_EVENT_LIST_TYPE = new TypeReference<List<AdEvent>>() {};

    /**
     * 非阻塞发送到 Kafka（替代原内存队列 offer），发送为异步操作，不阻塞 HTTP 线程。
     * 内部复用 {@link #offerBatch(List)}，单条事件同样以 JSON 数组格式发送
     */
    public boolean offer(AdEvent event) {
        return offerBatch(Collections.singletonList(event));
    }

    /**
     * 批量非阻塞发送到 Kafka：整批事件一次序列化为 JSON 数组，单次 send 发送，不阻塞 HTTP 线程
     *
     * @param events 事件列表
     * @return 入参为 null/空返回 true（无事件可发，视为成功）；发送开关关闭返回 true（视为接收成功）；
     * 序列化失败返回 false；已提交异步发送返回 true
     */
    public boolean offerBatch(List<AdEvent> events) {
        if (events == null || events.isEmpty()) {
            // 无事件需要发送，视为成功
            return true;
        }
        // 发送开关关闭时直接返回成功（与空列表语义一致），不序列化不发送
        if (Boolean.FALSE.equals(producerProperties.getAdEventEnabled())) {
            // 限流日志：每秒最多1条，避免高并发日志风暴
            long now = System.currentTimeMillis();
            long last = lastSkipLogTime.get();
            if (now - last >= 1000 && lastSkipLogTime.compareAndSet(last, now)) {
                log.info("跳过发送，本批={}条", events.size());
            }
            return true;
        }
        try {
            String json = objectMapper.writeValueAsString(events);
            kafkaTemplate.send(topic, json)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            totalSendFailed.incrementAndGet();
                            long now = System.currentTimeMillis();
                            long last = lastFailLogTime.get();
                            if (now - last >= 1000 && lastFailLogTime.compareAndSet(last, now)) {
                                log.warn("广告事件批量发送Kafka失败，批次大小={}，累计失败={}",
                                        events.size(), totalSendFailed.get(), ex);
                            }
                        }
                    });
            return true;
        } catch (Exception e) {
            log.warn("广告事件批量序列化失败，批次大小={}", events.size(), e);
            return false;
        }
    }

    /**
     * 批量消费，批量写入 ES（使用埋点专用容器工厂，独立消费组）
     */
    @KafkaListener(topics = "${kafka.topic.ad-event:ad_event}",
            groupId = "${kafka.consumer.groupId:analysis-ad-event-group}",
            containerFactory = "adEventKafkaListenerContainerFactory")
    public void consume(List<String> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<AdEvent> events = new ArrayList<>(records.size());
        for (String json : records) {
            try {
                String trimmed = json == null ? "" : json.trim();
                if (trimmed.startsWith("[")) {
                    // 新格式：JSON 数组（offerBatch 批量发送的消息）
                    events.addAll(objectMapper.readValue(trimmed, AD_EVENT_LIST_TYPE));
                } else if (trimmed.startsWith("{")) {
                    // 旧格式：单个 JSON 对象（灰度期 topic 中残留的旧消息）
                    events.add(objectMapper.readValue(trimmed, AdEvent.class));
                } else {
                    // 既非数组也非对象，视为非法消息直接跳过
                    log.warn("广告事件消息格式非法，跳过: {}", json);
                }
            } catch (Exception e) {
                log.warn("广告事件消息解析失败，跳过: {}", json, e);
            }
        }
        flushBatch(events);
    }

    /**
     * 批量刷盘到 ES
     */
    private void flushBatch(List<AdEvent> events) {
        if (events.isEmpty()) {
            return;
        }

        try {
            // 按月份分组，同月的事件写入同一索引（批次可能跨月，不能只取第一条的时间）
            Map<String, List<AdEvent>> grouped = events.stream()
                    .collect(Collectors.groupingBy(e -> buildIndexName(e.getTimestamp())));

            long startTime = System.currentTimeMillis();
            boolean hasError = false;
            for (Map.Entry<String, List<AdEvent>> entry : grouped.entrySet()) {
                String indexName = entry.getKey();
                List<AdEvent> batch = entry.getValue();

                BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
                for (AdEvent event : batch) {
                    bulkBuilder.operations(op -> op.create(idx -> idx
                            .index(indexName)
                            .document(event)));
                }

                BulkResponse response = client.bulk(bulkBuilder.build());
                if (response.errors()) {
                    hasError = true;
                    log.warn("批量写入ES部分失败，索引={}，批次大小={}，错误项数={}",
                            indexName, batch.size(), response.items().stream().filter(i -> i.error() != null).count());
                }
            }
            long elapsed = System.currentTimeMillis() - startTime;
            if (!hasError) {
                log.debug("批量写入ES成功，批次大小={}，耗时={}ms，累计刷盘={}",
                        events.size(), elapsed, totalFlushed.addAndGet(events.size()));
            } else {
                totalFlushed.addAndGet(events.size());
                log.warn("批量写入ES完成但有失败项，批次大小={}，耗时={}ms", events.size(), elapsed);
            }
        } catch (Exception e) {
            log.error("批量写入ES异常，批次大小={}", events.size(), e);
        }
    }

    /**
     * 构建索引名称
     */
    private String buildIndexName(Date timestamp) {
        String format = DateFormatUtils.format(timestamp, "yyyy-MM");
        return "ad_event_logs_" + format;
    }
}
