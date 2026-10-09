package com.htyoudao.youdao.framework.tracer.core.metrics;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import io.micrometer.common.lang.NonNull;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import jakarta.annotation.Resource;

import javax.sql.DataSource;

/**
 * Druid 连接池指标绑定器：将 Druid 指标注册到 Micrometer
 */
public class DataSourcePoolMetricsBinder implements MeterBinder {

    @Resource
    private DataSource dataSource;

    @Override
    public void bindTo(@NonNull MeterRegistry registry) {
        // 使用的动态数据源，master默认使用了druid连接池
        // 当存在非动态数据源的 DataSource bean（如 ClickHouseConfig 的 HikariDataSource）时，
        // DynamicRoutingDataSource 不会被创建，此时跳过连接池指标绑定
        /*if (!(dataSource instanceof DynamicRoutingDataSource ds)) {
            return;
        }*/
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        bindDruid(registry, ds);
        // 如果配置了sharding数据源，才集成hikari指标
        ItemDataSource shardingDataSource = (ItemDataSource) ds.getDataSource(DsNameConstants.SHARDING);
        // 由于找不到数据源时会返回主数据源，需要判断是否是sharding
        if (DsNameConstants.SHARDING.equals(shardingDataSource.getName())) {
            bindHikari(registry, shardingDataSource);
        }

    }

    private void bindHikari(MeterRegistry registry, ItemDataSource shardingDataSource) {
        // 通用标签：区分不同数据源
        io.micrometer.core.instrument.Tags tags = io.micrometer.core.instrument.Tags.of("datasource", "hikari");

        if (shardingDataSource.getRealDataSource() instanceof HikariDataSource hikariDataSource) {
            // 获取 Hikari 监控接口（核心）
            HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
            if (poolMXBean == null) {
                return;
            }

            // 1. 活跃连接数（正在使用的连接）
            Gauge.builder("hikari.pool.active.connections", poolMXBean, HikariPoolMXBean::getActiveConnections)
                    .description("活跃连接数")
                    .tags(tags)
                    .baseUnit("connections")
                    .register(registry);

            // 2. 空闲连接数（池中空闲的连接）
            Gauge.builder("hikari.pool.idle.connections", poolMXBean, HikariPoolMXBean::getIdleConnections)
                    .description("空闲连接数")
                    .tags(tags)
                    .baseUnit("connections")
                    .register(registry);

            // 3. 总连接数（活跃 + 空闲）
            Gauge.builder("hikari.pool.total.connections", poolMXBean, HikariPoolMXBean::getTotalConnections)
                    .description("总连接数")
                    .tags(tags)
                    .baseUnit("connections")
                    .register(registry);

            // 4. 等待连接的线程数（因池满等待的线程）
            Gauge.builder("hikari.pool.threads.awaiting.connections", poolMXBean, HikariPoolMXBean::getThreadsAwaitingConnection)
                    .description("等待连接的线程数")
                    .tags(tags)
                    .baseUnit("threads")
                    .register(registry);
        }
    }

    private static void bindDruid(MeterRegistry registry, DynamicRoutingDataSource ds) {
        ItemDataSource itemDataSource = (ItemDataSource) ds.getDataSource(DsNameConstants.MASTER);

        if (itemDataSource.getRealDataSource() instanceof DruidDataSource druidDataSource) {
            // 通用标签：区分不同数据源
            io.micrometer.core.instrument.Tags tags = io.micrometer.core.instrument.Tags.of("datasource", "druid");

            // 1. 活跃连接数（正在使用的连接）
            Gauge.builder("druid.pool.active", druidDataSource, DruidDataSource::getActiveCount)
                    .description("活跃连接数")
                    .tags(tags)
                    .baseUnit("connections")
                    .register(registry);

            // 2. 池中总连接数（活跃 + 空闲）
            Gauge.builder("druid.pool.total", druidDataSource, DruidDataSource::getPoolingCount)
                    .description("总连接数")
                    .tags(tags)
                    .baseUnit("connections")
                    .register(registry);

            // 3. 等待连接的线程数（因池满等待的线程）
            Gauge.builder("druid.pool.waiting", druidDataSource, DruidDataSource::getWaitThreadCount)
                    .description("等待连接的线程数")
                    .tags(tags)
                    .baseUnit("threads")
                    .register(registry);

            // 3. 最大连接数（配置的 maxActive）
            Gauge.builder("druid.pool.max", druidDataSource, DruidDataSource::getMaxActive)
                    .description("最大连接数")
                    .tags(tags)
                    .baseUnit("connections")
                    .register(registry);

            // 5. 最小空闲连接数（配置的 minIdle）
            Gauge.builder("druid.pool.min.idle", druidDataSource, DruidDataSource::getMinIdle)
                    .description("最小空闲连接数")
                    .tags(tags)
                    .baseUnit("connections")
                    .register(registry);
        }
    }
}
