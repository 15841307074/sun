package com.htyoudao.youdao.module.commodity.util;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@Component
public class TruncateTableUtil {
    private static final Logger log = LoggerFactory.getLogger(TruncateTableUtil.class);

    // 数据源（Spring 自动注入）
    private final DataSource dataSource;


    // 允许 TRUNCATE 的表名白名单（避免误操作核心表）
    private final List<String> ALLOWED_TABLES = Arrays.asList(
            "commodity_store_category", // 你的业务表
            "commodity_store_spu", "commodity_store_sku","commodity_store_group","commodity_store_single"  // 其他允许清空的表
    );

    // 构造器注入数据源
    public TruncateTableUtil(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 安全 TRUNCATE 全表（核心方法）
     * @param tableName 要清空的表名（小写，与数据库一致）
     * @throws SQLException 执行失败抛出异常
     */
    public void safeTruncateTable(String tableName) throws SQLException {
        // 1. 前置校验（安全屏障）
        // 校验表名是否在白名单
        Assert.isTrue(ALLOWED_TABLES.contains(tableName.toLowerCase()),
                "表 " + tableName + " 不在允许 TRUNCATE 的白名单中，禁止操作");


        // 2. 执行 TRUNCATE
        String sql = "TRUNCATE TABLE " + tableName;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            log.info("开始执行 TRUNCATE，表名：{}", tableName);
            long startTime = System.currentTimeMillis();

            ps.executeUpdate(); // 执行 TRUNCATE

            long cost = System.currentTimeMillis() - startTime;
            log.info("✅ TRUNCATE 执行完成，表名：{}，耗时：{}ms", tableName, cost);

        } catch (SQLException e) {
            log.error("❌ TRUNCATE 执行失败，表名：{}", tableName, e);
            throw e; // 抛出异常，让上层感知失败
        }
    }

    /**
     * 重载方法：适配 MyBatis-Plus 实体类（自动获取表名）
     * @param entityClass 实体类（需加 @TableName 注解）
     */
    public <T> void safeTruncateTable(Class<T> entityClass) throws SQLException {
        // 获取实体类对应的表名（适配 MyBatis-Plus 注解）
        com.baomidou.mybatisplus.annotation.TableName tableNameAnnotation = entityClass.getAnnotation(com.baomidou.mybatisplus.annotation.TableName.class);
        Assert.notNull(tableNameAnnotation, "实体类未添加 @TableName 注解");

        String tableName = tableNameAnnotation.value();
        safeTruncateTable(tableName);
    }
}
