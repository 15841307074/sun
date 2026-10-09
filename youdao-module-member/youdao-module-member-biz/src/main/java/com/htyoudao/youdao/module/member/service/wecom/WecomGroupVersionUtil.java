package com.htyoudao.youdao.module.member.service.wecom;

import jakarta.annotation.Resource;
import java.util.Objects;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


/**
 * 企业微信群版本号工具类
 * 用于统一处理版本号的验证和更新操作
 */
@Component
public class WecomGroupVersionUtil {

    // 缓存键设计（用于存储版本号）
    private static final String GROUP_VERSION_KEY = "wecom:group:version:%s";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 验证事件可靠性
     * @param chatId 群ID
     * @param beforeVersion 事件中的版本号
     * @return 是否可靠
     */
    public boolean validateEventReliability(String chatId, String beforeVersion) {
        // 获取存储的最新版本号
        String currentVersion = (String) redisTemplate.opsForValue().get(
                String.format(GROUP_VERSION_KEY, chatId)
        );

        // 版本号匹配 → 事件可靠
        return Objects.equals(currentVersion, beforeVersion);
    }

    /**
     * 更新版本号
     * @param chatId 群ID
     * @param afterVersion 新的版本号
     */
    public void updateVersion(String chatId, String afterVersion) {
        redisTemplate.opsForValue().set(
                String.format(GROUP_VERSION_KEY, chatId),
                afterVersion
        );
    }

    /**
     * 获取当前版本号
     * @param chatId 群ID
     * @return 当前版本号
     */
    public String getCurrentVersion(String chatId) {
        return (String) redisTemplate.opsForValue().get(
                String.format(GROUP_VERSION_KEY, chatId)
        );
    }
}
