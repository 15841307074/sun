package com.htyoudao.youdao.module.bpm.service.learningtask;

import com.htyoudao.youdao.module.bpm.dal.mysql.learningtask.LearningTaskMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 在内存数据库执行真实 Mapper SQL，验证门店范围、任务状态与游标分页。 */
class LearningTaskOrderQueryTest {

    @Test
    void matchesAllManagerStoresWithoutDuplicateTasksOrOtherUsers() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:learning_order;MODE=MySQL")) {
            try (Statement sql = connection.createStatement()) {
                sql.execute("CREATE TABLE bpm_learning_task (task_id BIGINT PRIMARY KEY, business_id BIGINT, deleted INT, "
                        + "online_status INT, order_limit_type INT, order_limit_days INT, start_time TIMESTAMP, end_time TIMESTAMP, "
                        + "apply_scope INT, store_scope INT)");
                sql.execute("CREATE TABLE bpm_learning_task_store (task_id BIGINT, business_id BIGINT, store_id BIGINT, deleted INT)");
                sql.execute("CREATE TABLE bpm_learning_task_tag (task_id BIGINT, business_id BIGINT, tag_id BIGINT, deleted INT)");
                sql.execute("CREATE TABLE system_store_tag (business_id BIGINT, store_id BIGINT, tag_id BIGINT, deleted INT)");
                sql.execute("CREATE TABLE system_store_info (store_id BIGINT, business_id BIGINT, user_id BIGINT, store_status INT, deleted INT)");
                sql.execute("INSERT INTO system_store_info VALUES (100,10,7,0,0),(200,10,7,0,0),(300,10,8,0,0),"
                        + "(400,10,7,1,0),(500,10,7,0,1),(600,11,7,0,0)");
                // 全部门店、指定门店、按标签三种范围均可命中；任务结束时间不用于免除补学。
                for (int id = 1; id <= 14; id++) {
                    sql.execute("INSERT INTO bpm_learning_task VALUES (" + id
                            + ",10,0,1,1,3,'2026-09-01 00:00:00','2026-09-05 00:00:00',1,1)");
                }
                sql.execute("UPDATE bpm_learning_task SET store_scope=2 WHERE task_id IN (2,4,11,12,13,14)");
                sql.execute("UPDATE bpm_learning_task SET apply_scope=2 WHERE task_id IN (3,5)");
                sql.execute("INSERT INTO bpm_learning_task_store VALUES (2,10,100,0),(2,10,200,0),(4,10,200,0),"
                        + "(11,10,300,0),(12,10,400,0),(13,10,500,0),(14,10,600,0)");
                sql.execute("INSERT INTO bpm_learning_task_tag VALUES (3,10,20,0),(5,10,21,0)");
                sql.execute("INSERT INTO system_store_tag VALUES (10,100,20,0),(10,100,21,1),(11,100,21,0)");
                sql.execute("UPDATE bpm_learning_task SET online_status=0 WHERE task_id=6");
                sql.execute("UPDATE bpm_learning_task SET start_time='2026-10-01 00:00:00' WHERE task_id=7");
                sql.execute("UPDATE bpm_learning_task SET business_id=11 WHERE task_id=8");
                sql.execute("UPDATE bpm_learning_task SET deleted=1 WHERE task_id=9");
                sql.execute("UPDATE bpm_learning_task SET order_limit_type=0 WHERE task_id=10");
            }
            assertEquals(List.of(1L, 2L, 3L, 4L), query(connection, null, 500));
            assertEquals(List.of(1L, 2L), query(connection, null, 2));
            assertEquals(List.of(3L, 4L), query(connection, 2L, 2));
            assertEquals(List.of(), query(connection, 4L, 500));
            // 负责人仍有两家正常门店，但没有一家命中剩余限订货任务时，不返回待校验任务。
            try (Statement sql = connection.createStatement()) {
                sql.execute("UPDATE bpm_learning_task SET online_status=0 WHERE task_id IN (1,2,3,4)");
            }
            assertEquals(List.of(), query(connection, null, 500));
        }
    }

    private List<Long> query(Connection connection, Long afterTaskId, int batchSize) throws Exception {
        Select annotation = LearningTaskMapper.class.getMethod("selectOrderLimitedTasks", Long.class, Long.class,
                LocalDateTime.class, Long.class, int.class).getAnnotation(Select.class);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("businessId", 10L); parameters.put("userId", 7L);
        parameters.put("now", LocalDateTime.of(2026, 9, 16, 12, 0));
        parameters.put("afterTaskId", afterTaskId); parameters.put("batchSize", batchSize);
        BoundSql sql = new XMLLanguageDriver().createSqlSource(new Configuration(),
                String.join(" ", annotation.value()), Map.class).getBoundSql(parameters);
        try (PreparedStatement statement = connection.prepareStatement(sql.getSql())) {
            for (int i = 0; i < sql.getParameterMappings().size(); i++) {
                statement.setObject(i + 1, parameters.get(sql.getParameterMappings().get(i).getProperty()));
            }
            List<Long> result = new ArrayList<>();
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) result.add(rows.getLong("task_id"));
            }
            return result;
        }
    }
}
