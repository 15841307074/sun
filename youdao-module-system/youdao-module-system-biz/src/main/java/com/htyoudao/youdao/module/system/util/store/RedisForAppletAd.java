package com.htyoudao.youdao.module.system.util.store;

import cn.hutool.core.util.ObjectUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class RedisForAppletAd {

    @Resource
    RedisTemplate redisTemplate;

    public static String APPLET_AD = "APPLET_AD";

    public void delAllStore(Long businessId) {

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        Set<String> keys = redisTemplate.keys(APPLET_AD+businessId +":"+ "*");
        for (final String key : keys) {
            executorService.submit(() -> redisTemplate.delete(key));
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // 可以在这里添加日志记录等异常处理逻辑，比如记录删除操作被中断等情况
        }
    }


    /**
     * 删除指定的门店缓存key
     * @param businessId 商户ID
     * @param storeId 门店ID
     */
    public void delStoreById(Long businessId, Long storeId) {
        String key = APPLET_AD + businessId + ":" + storeId;
        redisTemplate.delete(key);
    }

    /**
     * 删除指定的门店缓存key
     * @param businessId 商户ID
     * @param storeId 门店ID
     */
    public void delStoreByIdExecute(Long businessId, Long storeId) {
        String key = APPLET_AD + businessId + ":" + storeId;
        redisTemplate.execute((RedisCallback<Long>) connection -> {
            return connection.del(key.getBytes());
        });
    }
}
