package com.htyoudao.youdao.module.commodity.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "businessid")
public class BusinessProjectMappingConfig {

    public Map<String, Integer> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, Integer> mapping) {
        this.mapping = mapping;
    }

    /**
     * key: business_id
     * value: project_code
     */
    private Map<String, Integer> mapping = new HashMap<>();

    public Integer getProjectCode(String businessId) {
        return mapping.get(businessId);
    }
}

