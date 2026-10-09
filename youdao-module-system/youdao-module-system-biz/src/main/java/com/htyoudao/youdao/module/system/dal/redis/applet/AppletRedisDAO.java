package com.htyoudao.youdao.module.system.dal.redis.applet;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.util.collection.CollectionUtils;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.htyoudao.youdao.module.system.util.string.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.htyoudao.youdao.module.system.dal.redis.RedisKeyConstants.*;

/**
 * {@link OAuth2AccessTokenDO} 的 RedisDAO
 *
 * @author 0090
 */
@Repository
public class AppletRedisDAO {

    @Resource
    RedisTemplate<String, String> redisTemplate;

    public void delByAppletPageLocation(Long appletPageId, Integer appletPageLocation, Long businessId) {
        if (ObjectUtil.hasNull(appletPageId, businessId)) {
            throw new ServiceException(500, "参数不能为空");
        }

        String key = APPLET_PAGE_ALL_MANAGEMENT + "_" + businessId + ":" + appletPageLocation + "_" + appletPageId;

        // 单个key删除 → 直接用delete，最简单、最稳定
        redisTemplate.delete(key);
    }

    public void addByAppletPageLocation(Long appletPageId, Integer appletPageLocation, String appletPageInfo, Long businessId) {
        if (ObjectUtil.hasNull(appletPageId, businessId, appletPageLocation) || !StringUtils.hasText(appletPageInfo)) {
            throw new ServiceException(500, "参数不能为空");
        }

        String key = APPLET_PAGE_ALL_MANAGEMENT +"_"+businessId + ":"+appletPageLocation +"_"+appletPageId ;

        redisTemplate.opsForValue().set(key, appletPageInfo);
    }

    public String getByAppletPageLocation(Long appletPageId, Integer appletPageLocation, Long businessId) {
        if (ObjectUtil.hasNull(appletPageId, businessId, appletPageLocation) ) {
            throw new ServiceException(500, "参数不能为空");
        }

        String key = APPLET_PAGE_ALL_MANAGEMENT +"_"+businessId + ":"+appletPageLocation +"_"+appletPageId ;

        return redisTemplate.opsForValue().get(key);
    }

    public String getStr(String key) {
        if (ObjectUtil.hasNull(key) ) {
            throw new ServiceException(500, "参数不能为空");
        }

        return redisTemplate.opsForValue().get(key);
    }

    public void setStr(String key, String value) {
        if (ObjectUtil.hasNull(key) ) {
            throw new ServiceException(500, "参数不能为空");
        }

        redisTemplate.opsForValue().set(key, value);
    }

    public void multiSet(Map<String, String> dataMap) {
        if (CollectionUtil.isEmpty(dataMap)) {
            return;
        }
        redisTemplate.opsForValue().multiSet(dataMap);
    }

    /**
     * ZSet 添加元素（去重 + 删除后重新加入会回到队尾）
     * @param key 队列key
     * @param value 元素值
     */
    public void zAdd(String key, String value) {
        // 存在则不操作，保证顺序不变
        if (redisTemplate.opsForZSet().score(key, value) != null) {
            return;
        }
        // 用时间戳作为score，保证追加到队尾
        redisTemplate.opsForZSet().add(key, value, System.currentTimeMillis());
    }

    /**
     * ZSet 删除元素
     */
    public void zRemove(String key, String value) {
        redisTemplate.opsForZSet().remove(key, value);
    }

    /**
     * 获取队列全部元素（保持插入顺序）
     */
    public Set<String> zGetAll(String key) {
        return redisTemplate.opsForZSet().range(key, 0, -1);
    }

    /**
     * 获取队列全部元素（保持倒序）
     */
    public Set<String> zGetAllReverse(String key) {
        return redisTemplate.opsForZSet().reverseRange(key, 0, -1);
    }

    /**
     * 获取最后一个元素（最新加入的那个）
     */
    public String zGetLast(String key) {
        Set<String> set = redisTemplate.opsForZSet().reverseRange(key, 0, 0);
        return CollectionUtil.isEmpty(set) ? null : set.iterator().next();
    }

    /**
     * 获取倒数第 N 个元素
     * index=1 → 倒数第1个（最新）
     * index=2 → 倒数第2个
     */
    public String zGetLast(String key, int index) {
        if (index < 1) {
            return null;
        }
        Set<String> set = redisTemplate.opsForZSet().reverseRange(key, index - 1, index - 1);
        return CollectionUtil.isEmpty(set) ? null : set.iterator().next();
    }

    /**
     * 获取队列长度
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

}
