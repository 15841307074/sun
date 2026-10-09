package com.htyoudao.youdao.module.member.service.wecom.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.htyoudao.youdao.module.member.controller.admin.wecom.WecomApiUtil;
import com.htyoudao.youdao.module.member.service.wecom.WecomGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 企业微信群服务实现（Redis Set存储）
 */
@Slf4j
@Service("wecomGroupRedisService")
public class WecomGroupRedisServiceImpl implements WecomGroupService {

    // 缓存键设计
    private static final String GROUP_MEMBERS_KEY = "wecom:group:members:%s";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private WecomApiUtil wecomApiUtil;

    /**
     * 检查用户是否在群中
     */
    @Override
    public boolean isUserInGroup(String userId, String chatId) {
        Boolean isMember = redisTemplate.opsForSet().isMember(
                String.format(GROUP_MEMBERS_KEY, chatId),
                userId
        );
        return isMember != null && isMember;
    }


    /**
     * 处理可靠事件（直接使用回调数据更新）
     */
    @Override
    public void handleReliableEvent(String chatId, String changeType, List<String> memChangeList) {
        try {
            // 直接使用Redis Set的增量更新命令
            if ("add_member".equals(changeType)) {
                // 添加成员
                redisTemplate.opsForSet().add(
                        String.format(GROUP_MEMBERS_KEY, chatId),
                        memChangeList.toArray()
                );
            } else if ("del_member".equals(changeType)) {
                // 删除成员
                redisTemplate.opsForSet().remove(
                        String.format(GROUP_MEMBERS_KEY, chatId),
                        memChangeList.toArray()
                );
            }
            
        } catch (Exception e) {
            log.error("处理可靠事件失败", e);
        }
    }

    /**
     * 处理不可靠事件（调用API获取完整数据）
     */
    @Override
    public String handleUnreliableEvent(String chatId) throws IOException {
        // 调用API获取最新群详情（包含成员列表和版本号）
        JSONObject groupDetail = wecomApiUtil.getGroupDetail(chatId);
        JSONArray membersArray = groupDetail.getJSONArray("member_list");

        // 转换为List<String>格式
        List<String> membersList = new ArrayList<>();
        for (int i = 0; i < membersArray.size(); i++) {
            JSONObject member = membersArray.getJSONObject(i);
            membersList.add(member.getString("userid"));
        }

        // 更新Redis Set缓存
        redisTemplate.delete(String.format(GROUP_MEMBERS_KEY, chatId));
        if (!membersList.isEmpty()) {
            redisTemplate.opsForSet().add(
                String.format(GROUP_MEMBERS_KEY, chatId),
                membersList
            );
        }

        return groupDetail.getString("member_version");
    }

    /**
     * 判断用户是否在任意一个企业微信社群中
     */
    @Override
    public boolean isUserInAnyGroup(String userId) {
        // 获取所有客户群列表
        Set<String> groupList = null;
        try {
            groupList = wecomApiUtil.getGroupList();
        } catch (IOException e) {
            log.error("", e);
            return false;
        }
        for (String chatId : groupList) {
            try {
                // 检查用户是否在当前群中
                if (isUserInGroup(userId, chatId)) {
                    return true; // 一旦找到，立即返回
                }
            } catch (Exception e) {
                log.error("检查用户是否在群中失败", e);
            }
        }

        // 遍历完所有群，用户不在任何群中
        return false;
    }

    /**
     * 获取当前成员列表
     */
    public List<String> getCurrentMembers(String chatId){
        // 1. 尝试从Redis Set获取
        Set<Object> cachedMembers = redisTemplate.opsForSet().members(
                String.format(GROUP_MEMBERS_KEY, chatId)
        );
        
        if (cachedMembers != null && !cachedMembers.isEmpty()) {
            // 如果缓存存在，转换为List并返回
            return cachedMembers.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }

        return List.of();
    }

}
