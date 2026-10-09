package com.htyoudao.youdao.module.member.dal.redis.map;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import static com.htyoudao.youdao.module.member.dal.redis.RedisKeyConstants.MEMBER_MAP_MAIYUN_ERRORS;

@Slf4j
@Repository
public class MemberMapRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void incrementMaiyunError(String api, int errorCode) {
        incrementMaiyunError(api, String.valueOf(errorCode));
    }

    public void incrementMaiyunTimeout(String api) {
        incrementMaiyunError(api, "timeout");
    }

    private void incrementMaiyunError(String api, String errorCode) {
        try {
            stringRedisTemplate.opsForHash().increment(
                    MEMBER_MAP_MAIYUN_ERRORS + api, errorCode, 1L);
        } catch (RuntimeException e) {
            log.warn("迈云地图错误次数记录失败，接口={}，错误码={}", api, errorCode, e);
        }
    }
}
