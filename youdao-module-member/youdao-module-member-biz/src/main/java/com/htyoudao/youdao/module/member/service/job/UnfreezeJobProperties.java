package com.htyoudao.youdao.module.member.service.job;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/** 冻结积分任务专用动态配置，不刷新其他定时任务所在的服务。 */
@Component
@RefreshScope
public class UnfreezeJobProperties {
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

    public record Snapshot(int maxWorkers, int batchSize, long maxDurationSeconds, int maxPages,
                           long pagePauseMillis, boolean dailyStopEnabled, int endHour) { }
}
