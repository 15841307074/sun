package com.htyoudao.youdao.module.order.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 代取订单配置。
 */
@Getter
@Setter
@Component
@RefreshScope
@ConfigurationProperties(prefix = "errand.order")
public class ErrandOrderConfig {

    /**
     * 是否开启代取订单 22 点后限制下单，默认开启，保持历史逻辑。
     */
    private Boolean deadlineLimitEnabled = true;

    /**
     * 代取整单退款跨天限制白名单订单号。
     */
    private Set<String> refundCrossDayWhitelist = new HashSet<>();

    public boolean isDeadlineLimitEnabled() {
        return Boolean.TRUE.equals(deadlineLimitEnabled);
    }

    public boolean isRefundCrossDayWhitelist(String orderSn) {
        if (orderSn == null || refundCrossDayWhitelist == null || refundCrossDayWhitelist.isEmpty()) {
            return false;
        }
        return refundCrossDayWhitelist.stream()
                .filter(item -> item != null && !item.trim().isEmpty())
                .map(String::trim)
                .anyMatch(orderSn::equals);
    }
    

    public Set<String> getRefundCrossDayWhitelist() {
        return refundCrossDayWhitelist == null ? Collections.emptySet() : refundCrossDayWhitelist;
    }
}
