package com.htyoudao.youdao.module.promotion.service.activitySeckill;

import com.alibaba.fastjson.JSON;
import com.htyoudao.youdao.module.promotion.controller.admin.activitySeckill.vo.ActivitySeckillRespVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SeckillActivityCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String ACTIVITY_KEY_PREFIX = "seckill:activity:";
    
    // 缓存活动详情
    public void cacheActivity(ActivitySeckillRespVO activity) {
        String key = ACTIVITY_KEY_PREFIX + activity.getId();
        redisTemplate.opsForValue().set(key, JSON.toJSONString(activity));
    }

    // 获取活动详情
    public ActivitySeckillRespVO getActivity(Long activityId) {
        String key = ACTIVITY_KEY_PREFIX + activityId;
        String jsonStr = (String) redisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(jsonStr)){
            return null;
        }
        return JSON.parseObject(jsonStr, ActivitySeckillRespVO.class);
    }
    
    // 删除活动缓存
    public void removeActivity(String activityId) {
        String key = ACTIVITY_KEY_PREFIX + activityId;
        redisTemplate.delete(key);
    }
}