package com.htyoudao.youdao.module.member.dal.redis.address;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import static com.htyoudao.youdao.module.member.dal.redis.RedisKeyConstants.MEMBER_ADDRESS_NAMESPACE;

@Repository
public class MemberAddressRedisDAO {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public void delete(String accessToken) {
        String redisKey = getNamespacedKey(accessToken);
        stringRedisTemplate.delete(redisKey);
    }

    // 获取带有命名空间的键
    private String getNamespacedKey(String key) {
        return MEMBER_ADDRESS_NAMESPACE + ":" + key;
    }
}
