package com.htyoudao.youdao.module.order.core.calc.v1;

import com.alibaba.fastjson2.JSON;
import com.htyoudao.youdao.module.order.core.calc.v1.DTO.CalculateCacheDataDTO;
import com.htyoudao.youdao.module.order.dal.redis.RedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 缓存结算信息
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-25
 */
@Service
public class CalculateCacheService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 缓存结算信息
     *
     * @param data
     */
    public void cacheCalculateData(CalculateCacheDataDTO data) {
        this.setOrderCache(data);
    }

    /**
     * 缓存结算信息
     *
     * @param data
     */
    public void setOrderCache(CalculateCacheDataDTO data) {
        String calcJSON = JSON.toJSONString(data);

        String cacheKey = RedisKeyConstants.ORDER_CALC_CACHE + data.getMemberId();
        stringRedisTemplate.opsForValue().set(cacheKey, calcJSON, 30, TimeUnit.MINUTES);
    }

}
