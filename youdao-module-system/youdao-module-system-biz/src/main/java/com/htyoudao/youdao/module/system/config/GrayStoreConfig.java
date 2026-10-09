package com.htyoudao.youdao.module.system.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "gray.stores")
public class GrayStoreConfig {

    private Map<Long, Set<String>> config = new HashMap<>();

    // 获取指定门店的可见MemberIds集合
    public Set<String> getMemberIdsByStore(Long storeId) {
        return config.get(storeId);
    }
}
