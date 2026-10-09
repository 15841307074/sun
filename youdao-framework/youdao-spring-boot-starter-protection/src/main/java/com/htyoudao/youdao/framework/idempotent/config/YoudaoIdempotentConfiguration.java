package com.htyoudao.youdao.framework.idempotent.config;

import com.htyoudao.youdao.framework.idempotent.core.aop.IdempotentAspect;
import com.htyoudao.youdao.framework.idempotent.core.keyresolver.impl.DefaultIdempotentKeyResolver;
import com.htyoudao.youdao.framework.idempotent.core.keyresolver.impl.ExpressionIdempotentKeyResolver;
import com.htyoudao.youdao.framework.idempotent.core.keyresolver.IdempotentKeyResolver;
import com.htyoudao.youdao.framework.idempotent.core.keyresolver.impl.UserIdempotentKeyResolver;
import com.htyoudao.youdao.framework.idempotent.core.redis.IdempotentRedisDAO;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import com.htyoudao.youdao.framework.redis.config.YoudaoRedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@AutoConfiguration(after = YoudaoRedisAutoConfiguration.class)
public class YoudaoIdempotentConfiguration {

    @Bean
    public IdempotentAspect idempotentAspect(List<IdempotentKeyResolver> keyResolvers, IdempotentRedisDAO idempotentRedisDAO) {
        return new IdempotentAspect(keyResolvers, idempotentRedisDAO);
    }

    @Bean
    public IdempotentRedisDAO idempotentRedisDAO(StringRedisTemplate stringRedisTemplate) {
        return new IdempotentRedisDAO(stringRedisTemplate);
    }

    // ========== 各种 IdempotentKeyResolver Bean ==========

    @Bean
    public DefaultIdempotentKeyResolver defaultIdempotentKeyResolver() {
        return new DefaultIdempotentKeyResolver();
    }

    @Bean
    public UserIdempotentKeyResolver userIdempotentKeyResolver() {
        return new UserIdempotentKeyResolver();
    }

    @Bean
    public ExpressionIdempotentKeyResolver expressionIdempotentKeyResolver() {
        return new ExpressionIdempotentKeyResolver();
    }

}
