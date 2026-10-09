package com.htyoudao.youdao.framework.redis.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.htyoudao.youdao.framework.redis.core.handler.KeyPrefixHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Redis 配置类
 * @author lqman
 */
@Slf4j
@AutoConfiguration(before = RedissonAutoConfigurationV2.class) // 目的：使用自己定义的 RedisTemplate Bean
@EnableConfigurationProperties(YoudaoRedissonProperties.class)
public class YoudaoRedisAutoConfiguration {

    @Resource
    private YoudaoRedissonProperties youdaoRedissonProperties;

    @Bean
    public RedissonAutoConfigurationCustomizer redissonCustomizer() {
        return config -> {
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
            ObjectMapper om = new ObjectMapper();
            om.registerModule(javaTimeModule);
            om.setTimeZone(TimeZone.getDefault());
            om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            // 指定序列化输入的类型，类必须是非final修饰的。序列化时将对象全类名一起保存下来
            om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
//            LoggerFactory.useSlf4jLogging(true);
//            FuryCodec furyCodec = new FuryCodec();
//            CompositeCodec codec = new CompositeCodec(StringCodec.INSTANCE, furyCodec, furyCodec);
            TypedJsonJacksonCodec jsonCodec = new TypedJsonJacksonCodec(Object.class, om);
            // 组合序列化 key 使用 String 内容使用通用 json 格式
            CompositeCodec codec = new CompositeCodec(StringCodec.INSTANCE, jsonCodec, jsonCodec);
            config.setThreads(youdaoRedissonProperties.getThreads())
                    .setNettyThreads(youdaoRedissonProperties.getNettyThreads())
                    // 缓存 Lua 脚本 减少网络传输(redisson 大部分的功能都是基于 Lua 脚本实现)
                    .setUseScriptCache(true)
                    .setCodec(codec);
            YoudaoRedissonProperties.SingleServerConfig singleServerConfig = youdaoRedissonProperties.getSingleServerConfig();
            if (ObjectUtil.isNotNull(singleServerConfig)) {
                // 使用单机模式
                config.useSingleServer()
                        //设置redis key前缀
                        .setNameMapper(new KeyPrefixHandler(youdaoRedissonProperties.getKeyPrefix()))
                        .setTimeout(singleServerConfig.getTimeout())
                        .setClientName(singleServerConfig.getClientName())
                        .setIdleConnectionTimeout(singleServerConfig.getIdleConnectionTimeout())
                        .setSubscriptionConnectionPoolSize(singleServerConfig.getSubscriptionConnectionPoolSize())
                        .setConnectionMinimumIdleSize(singleServerConfig.getConnectionMinimumIdleSize())
                        .setConnectionPoolSize(singleServerConfig.getConnectionPoolSize());
            }
            // 集群配置方式
            YoudaoRedissonProperties.ClusterServersConfig clusterServersConfig = youdaoRedissonProperties.getClusterServersConfig();
            if (ObjectUtil.isNotNull(clusterServersConfig)) {
                config.useClusterServers()
                        //设置redis key前缀
                        .setNameMapper(new KeyPrefixHandler(youdaoRedissonProperties.getKeyPrefix()))
                        .setTimeout(clusterServersConfig.getTimeout())
                        .setClientName(clusterServersConfig.getClientName())
                        .setIdleConnectionTimeout(clusterServersConfig.getIdleConnectionTimeout())
                        .setSubscriptionConnectionPoolSize(clusterServersConfig.getSubscriptionConnectionPoolSize())
                        .setMasterConnectionMinimumIdleSize(clusterServersConfig.getMasterConnectionMinimumIdleSize())
                        .setMasterConnectionPoolSize(clusterServersConfig.getMasterConnectionPoolSize())
                        .setSlaveConnectionMinimumIdleSize(clusterServersConfig.getSlaveConnectionMinimumIdleSize())
                        .setSlaveConnectionPoolSize(clusterServersConfig.getSlaveConnectionPoolSize())
                        .setReadMode(clusterServersConfig.getReadMode())
                        .setSubscriptionMode(clusterServersConfig.getSubscriptionMode());
            }
            log.info("初始化 redis 配置");
        };
    }
    /**
     * 创建 RedisTemplate Bean，使用 JSON 序列化方式
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建 RedisTemplate 对象
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置 RedisConnection 工厂。😈 它就是实现多种 Java Redis 客户端接入的秘密工厂。感兴趣的胖友，可以自己去撸下。
        template.setConnectionFactory(factory);
        // 使用 String 序列化方式，序列化 KEY 。
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        // 使用 JSON 序列化方式（库是 Jackson ），序列化 VALUE 。
        template.setValueSerializer(buildRedisSerializer());
        template.setHashValueSerializer(buildRedisSerializer());
        return template;
    }

    public static RedisSerializer<?> buildRedisSerializer() {
        RedisSerializer<Object> json = RedisSerializer.json();
        // 解决 LocalDateTime 的序列化
        ObjectMapper objectMapper = (ObjectMapper) ReflectUtil.getFieldValue(json, "mapper");
        objectMapper.registerModules(new JavaTimeModule());
        return json;
    }

}
