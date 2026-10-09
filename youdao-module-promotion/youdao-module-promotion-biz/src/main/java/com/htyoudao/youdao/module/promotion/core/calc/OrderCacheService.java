package com.htyoudao.youdao.module.promotion.core.calc;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.htyoudao.youdao.module.promotion.api.usercoupon.DTO.CalculateCacheDataCopyDTO;
import com.htyoudao.youdao.module.promotion.dal.redis.RedisKeyConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author dht
 */
@Service
public class OrderCacheService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public CalculateCacheDataCopyDTO getOrderCache(Long memberId) {
        String cacheKey = RedisKeyConstants.ORDER_CALC_CACHE + memberId;
        String cacheJSON = stringRedisTemplate.opsForValue().get(cacheKey);
        if(ObjectUtil.isNull(cacheJSON)){
            return new CalculateCacheDataCopyDTO();
        }
        return JSON.parseObject(cacheJSON, CalculateCacheDataCopyDTO.class);
    }
}
