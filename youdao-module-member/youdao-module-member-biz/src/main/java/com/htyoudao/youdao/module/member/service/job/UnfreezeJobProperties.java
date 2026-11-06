package com.htyoudao.youdao.module.member.service.job;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;

/** 冻结积分任务专用动态配置，不刷新其他定时任务所在的服务。 */
@Component
@RefreshScope
public class UnfreezeJobProperties {
    @Autowired
    private ConfigurableEnvironment environment;
    @Value("${member.points.unfreeze.max-workers:2}")
    private int maxWorkers = 2;
    @Value("${member.points.unfreeze.batch-size:200}")
    private int batchSize = 200;
    @Value("${member.points.unfreeze.max-duration-seconds:300}")
    private long maxDurationSeconds = 300;
    @Value("${member.points.unfreeze.max-pages:100}")
    private int maxPages = 100;
    @Value("${member.points.unfreeze.page-pause-millis:50}")
    private long pagePauseMillis = 50;
    @Value("${member.points.unfreeze.daily-stop-enabled:true}")
    private boolean dailyStopEnabled = true;
    @Value("${member.points.unfreeze.end-hour:6}")
    private int endHour = 6;

    /** 每次触发固定一次配置快照，动态刷新从下一次触发生效；保护上限不可被配置突破。 */
    public Snapshot snapshot() {
        return new Snapshot(Math.max(1, Math.min(8, maxWorkers)), Math.max(1, Math.min(500, batchSize)),
                Math.max(1, Math.min(900, maxDurationSeconds)), Math.max(1, Math.min(100, maxPages)),
                Math.max(0, Math.min(1000, pagePauseMillis)), dailyStopEnabled,
                Math.max(1, Math.min(24, endHour)));
    }

    /** 临时诊断仅输出指定配置项，不打印完整配置、令牌或连接信息。 */
    public String diagnostics() {
        String key = "member.points.unfreeze.max-workers";
        String source = "not-found";
        for (PropertySource<?> candidate : environment.getPropertySources()) {
            if (!"configurationProperties".equals(candidate.getName()) && candidate.containsProperty(key)) {
                source = diagnosticSource(candidate, key);
                break;
            }
        }
        return "configProbe=unfreeze-limit8-v1, environmentMaxWorkers=" + environment.getProperty(key, "<unset>")
                + ", injectedMaxWorkers=" + maxWorkers + ", codeMaxWorkersLimit=8, workerPropertySource=" + source
                + ", environmentDurationSeconds=" + environment.getProperty("member.points.unfreeze.max-duration-seconds", "<unset>")
                + ", injectedDurationSeconds=" + maxDurationSeconds
                + ", activeProfiles=" + java.util.Arrays.toString(environment.getActiveProfiles())
                + ", nacosNamespace=" + environment.getProperty("spring.cloud.nacos.config.namespace", "<default>")
                + ", nacosGroup=" + environment.getProperty("spring.cloud.nacos.config.group", "DEFAULT_GROUP")
                + ", executorAppName=" + environment.getProperty("xxl.job.executor.app-name",
                        environment.getProperty("xxl.job.executor.appname", "<unset>"))
                + ", executorIpConfigured=" + environment.getProperty("xxl.job.executor.ip", "<auto>")
                + ", executorPortConfigured=" + environment.getProperty("xxl.job.executor.port", "<auto>")
                + ", autoDetectedIp=" + com.xxl.job.core.util.IpUtil.getIp();
    }

    private String diagnosticSource(PropertySource<?> source, String key) {
        if (source instanceof CompositePropertySource composite) {
            for (PropertySource<?> nested : composite.getPropertySources()) {
                if (nested.containsProperty(key)) return diagnosticSource(nested, key);
            }
        }
        // 只显示已知来源名称，避免其他配置源名称携带连接地址或凭据。
        for (String known : java.util.List.of("member-server.yaml", "application-common.yaml", "datasource.yaml",
                "dubbo.yaml", "systemEnvironment", "systemProperties", "commandLineArgs")) {
            if (source.getName().contains(known)) return known + "(" + source.getClass().getSimpleName() + ")";
        }
        return source.getClass().getSimpleName();
    }

    public record Snapshot(int maxWorkers, int batchSize, long maxDurationSeconds, int maxPages,
                           long pagePauseMillis, boolean dailyStopEnabled, int endHour) { }
}
