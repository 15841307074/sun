package com.htyoudao.youdao.framework.mybatis.config;

import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.htyoudao.youdao.framework.common.constants.RedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * 定制雪花ID
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-13
 */
public class SnowflakeConfig {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Value("${spring.application.name}")
    private String applicationName;

    @Bean
    public IdentifierGenerator idGenerator() throws Exception {
        //以模块名称作为key，最大不超过32
        Long inc = stringRedisTemplate.opsForValue().increment(RedisKeyConstants.SAAS_SNOWFLAKE_WORKERID + applicationName);

        long workerId = inc % 32;
        // 数据中心ID可根据环境变量或命名空间设置
        long datacenterId = generateDatacenterId(applicationName);
        return new DefaultIdentifierGenerator(workerId, datacenterId);
    }

    /**
     * 根据应用名称生成 datacenterId (0-31)
     */
    private long generateDatacenterId(String appName) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(appName.getBytes());

        // 取哈希值的前 4 字节转换为整数
        int hash = ByteBuffer.wrap(digest).getInt();

        // 取绝对值并模 32，确保范围在 0-31
        return Math.abs(hash) % 32;
    }

    public static void main(String[] args) {
        SnowflakeConfig config = new SnowflakeConfig();

        // 模拟不同应用名称
        String[] appNames = {"infra-server", "order-server", "commodity-server", "pay-server", "system-server",
                "coupon-server", "analysis-server", "integral-server", "schedule-server"};
        Set<Long> ids = new HashSet<>();

        for (String name : appNames) {
            long id = 0;
            try {
                id = config.generateDatacenterId(name);
                System.out.println("应用名称：" + name + ", 数据中心ID：" + id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ids.add(id);
        }
        /**
         * 应用名称：infra-server, 数据中心ID：16
         * 应用名称：order-server, 数据中心ID：1
         * 应用名称：commodity-server, 数据中心ID：13
         * 应用名称：pay-server, 数据中心ID：21
         * 应用名称：system-server, 数据中心ID：2
         * 应用名称：coupon-server, 数据中心ID：30
         * 应用名称：analysis-server, 数据中心ID：5
         * 应用名称：integral-server, 数据中心ID：31
         * 应用名称：schedule-server, 数据中心ID：6
         */
    }
}
