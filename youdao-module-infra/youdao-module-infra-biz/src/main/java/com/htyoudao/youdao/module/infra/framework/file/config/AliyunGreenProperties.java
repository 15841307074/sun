package com.htyoudao.youdao.module.infra.framework.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.Set;

@ConfigurationProperties(prefix = "youdao.aliyun-green")
@RefreshScope
@Data
public class AliyunGreenProperties {

    /**
     * 是否启用上传前图片鉴黄。
     */
    private boolean enabled = true;

    private String regionId = "cn-shanghai";

    private String endpoint = "green-cip.cn-shanghai.aliyuncs.com";

    private String accessKeyId;

    private String accessKeySecret;

    /**
     * 图片审核增强版服务，由内容安全控制台的规则配置决定检测范围。
     */
    private String service = "baselineCheck";

    /**
     * 命中这些风险等级时拒绝图片。
     */
//    private Set<String> blockedRiskLevels = Set.of("medium", "high");
    private Set<String> blockedRiskLevels = Set.of("high");

    /**
     * 是否使用内容安全临时 OSS 的内网地址。
     *
     * 只有同时满足以下条件才建议开启：
     *
     *   - 应用部署在阿里云 ECS、ACK 等云上环境
     *   - 应用与内容安全服务接入地域一致，例如都是上海
     *   - 运行环境能够访问阿里云 VPC 内网域名
     *   - 希望流量走阿里云内网
     */
    private boolean internal;

}
