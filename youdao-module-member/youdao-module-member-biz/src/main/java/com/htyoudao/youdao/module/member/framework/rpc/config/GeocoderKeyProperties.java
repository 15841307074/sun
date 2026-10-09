package com.htyoudao.youdao.module.member.framework.rpc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import java.util.Map;

@Data
@Component // 确保Spring将此类作为组件管理
@RefreshScope // 支持配置动态刷新
@ConfigurationProperties(prefix = "geocoder-key") // 前缀与Nacos配置根键一致
public class GeocoderKeyProperties {

    /**
     * 是否启用迈云地图接口；默认关闭，逆地理和提示词搜索继续使用腾讯接口
     */
    private boolean maiyunEnabled = false;

    /**
     * 迈云位置服务 API Key
     */
    private String maiyunKey;

    /**
     * 迈云位置服务请求来源标识，对应请求体中的 from 字段
     */
    private Integer maiyunFrom = 3;

    /**
     * 各业务的腾讯地图 key 集合
     */
    private Map<Long, String> config; // 映射geocoderKey下的所有键值对

}
