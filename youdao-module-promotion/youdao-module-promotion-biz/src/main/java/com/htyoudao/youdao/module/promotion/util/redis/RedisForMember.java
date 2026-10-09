package com.htyoudao.youdao.module.promotion.util.redis;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.htyoudao.youdao.framework.common.enums.UserTypeEnum;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class RedisForMember {

    private static StringRedisTemplate stringRedisTemplate;

    @Resource
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        RedisForMember.stringRedisTemplate = stringRedisTemplate;
    }

    private static String OAUTH2_ACCESS_TOKEN = "oauth2_access_token:%s";

    private static String formatKey(String accessToken) {
        return String.format(OAUTH2_ACCESS_TOKEN, accessToken);
    }

    public static Map<String, String> getMemberFromRedis(String token){
        String redisKey = formatKey(token);
        OAuth2AccessTokenDO oAuth2AccessTokenDO = JsonUtils.parseObject(stringRedisTemplate.opsForValue().get(redisKey), OAuth2AccessTokenDO.class);
        if (ObjectUtil.isEmpty(oAuth2AccessTokenDO)){
            return null;
        }
        return oAuth2AccessTokenDO.getUserInfo();
    }
    @Data
    public static class OAuth2AccessTokenDO {

        /**
         * 编号，数据库递增
         */
        @TableId
        private Long id;
        /**
         * 访问令牌
         */
        private String accessToken;
        /**
         * 刷新令牌
         */
        private String refreshToken;
        /**
         * 用户编号
         */
        private Long userId;
        /**
         * 用户类型
         * 枚举 {@link UserTypeEnum}
         */
        private Integer userType;
        /**
         * 用户信息
         */
        @TableField(typeHandler = JacksonTypeHandler.class)
        private Map<String, String> userInfo;
        /**
         * 客户端编号
         *
         */
        private String clientId;
        /**
         * 授权范围
         */
        @TableField(typeHandler = JacksonTypeHandler.class)
        private List<String> scopes;
        /**
         * 过期时间
         */
        private LocalDateTime expiresTime;

    }
}
