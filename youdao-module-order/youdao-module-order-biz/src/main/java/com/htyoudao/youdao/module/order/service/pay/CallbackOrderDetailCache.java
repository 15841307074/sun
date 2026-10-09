package com.htyoudao.youdao.module.order.service.pay;

import com.alibaba.fastjson2.JSON;
import com.htyoudao.youdao.module.order.dal.DTO.OrderDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * 仅保存下单时的商品明细，不保存订单状态和支付状态。
 */
@Slf4j
@Component
@RefreshScope
public class CallbackOrderDetailCache {

    private final StringRedisTemplate redis;
    private final long ttlSeconds;

    public CallbackOrderDetailCache(StringRedisTemplate redis,
                                    @Value("${order.pay.callback-detail-cache-ttl-seconds:180}") long ttlSeconds) {
        this.redis = redis;
        this.ttlSeconds = ttlSeconds;
    }

    public void putAfterCommit(Long businessId, String orderSn, OrderDetailDTO detail) {
        if (ttlSeconds <= 0 || businessId == null
                || !TransactionSynchronizationManager.isActualTransactionActive()
                || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        try {
            if (!valid(orderSn, detail)) {
                log.warn("回调明细快照不完整，跳过缓存 orderSn={}", orderSn);
                return;
            }
            // 保存回填主键后的对象快照；不能在 afterCommit 中重新查询数据库。
            String snapshot = JSON.toJSONString(detail);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        redis.opsForValue().set(key(businessId, orderSn), snapshot, Duration.ofSeconds(ttlSeconds));
                    } catch (RuntimeException e) {
                        log.warn("订单已提交，回调明细缓存写入失败 orderSn={}", orderSn, e);
                    }
                }
            });
        } catch (RuntimeException e) {
            log.warn("回调明细快照生成失败 orderSn={}", orderSn, e);
        }
    }

    /**
     * 返回 null 表示未命中，调用方按原逻辑查库；不续期，避免长期使用旧快照。
     */
    public OrderDetailDTO get(Long businessId, String orderSn) {
        if (ttlSeconds <= 0 || businessId == null) {
            log.warn("【notify】回调明细回源MySQL reason={}, businessId={}, orderSn={}",
                    ttlSeconds <= 0 ? "CACHE_DISABLED" : "BUSINESS_ID_MISSING", businessId, orderSn);
            return null;
        }
        try {
            String value = redis.opsForValue().get(key(businessId, orderSn));
            if (value == null) {
                log.warn("【notify】回调明细回源MySQL reason=CACHE_MISS, businessId={}, orderSn={}", businessId, orderSn);
                return null;
            }
            OrderDetailDTO detail = JSON.parseObject(value, OrderDetailDTO.class);
            if (!valid(orderSn, detail)) {
                log.warn("【notify】回调明细回源MySQL reason=CACHE_INVALID, businessId={}, orderSn={}", businessId, orderSn);
                return null;
            }
            log.info("【notify】成功拿到订单明细缓存 {}", orderSn);
            return detail;
        } catch (RuntimeException e) {
            log.warn("【notify】回调明细回源MySQL reason=CACHE_READ_ERROR, businessId={}, orderSn={}", businessId, orderSn, e);
            return null;
        }
    }

    private static boolean valid(String orderSn, OrderDetailDTO detail) {
        if (orderSn == null || detail == null || detail.getBzOrderDO() != null
                || detail.getProductDOList() == null || detail.getProductDOList().isEmpty()
                || detail.getProductSonDOList() == null || detail.getPurchaseDOList() == null) {
            return false;
        }
        Set<Long> parentIds = new HashSet<>();
        for (var product : detail.getProductDOList()) {
            if (product == null || product.getOrderProductId() == null
                    || !orderSn.equals(product.getOrderSn()) || !parentIds.add(product.getOrderProductId())) {
                return false;
            }
        }
        for (var son : detail.getProductSonDOList()) {
            if (son == null || son.getOrderProductId() == null || !orderSn.equals(son.getOrderSn())
                    || !parentIds.contains(son.getParentGoodsId())) {
                return false;
            }
        }
        return detail.getPurchaseDOList().stream()
                .allMatch(item -> item != null && orderSn.equals(item.getOrderSn()));
    }

    private static String key(Long businessId, String orderSn) {
        return "order:callback:detail:v1:" + businessId + ":" + orderSn;
    }
}
