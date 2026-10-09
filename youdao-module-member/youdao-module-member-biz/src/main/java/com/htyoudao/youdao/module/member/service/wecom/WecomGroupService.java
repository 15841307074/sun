package com.htyoudao.youdao.module.member.service.wecom;

import java.io.IOException;
import java.util.List;


/**
 * 企业微信群服务接口
 */
public interface WecomGroupService {

    /**
     * 检查用户是否在群中
     */
    boolean isUserInGroup(String unionId, String chatId);

    /*
     * 处理可靠事件（直接使用回调数据更新）
     */
    void handleReliableEvent(String chatId, String changeType, List<String> memChangeList);

    /**
     * 判断用户是否在任意一个企业微信社群中
     */
    boolean isUserInAnyGroup(String unionId) ;

    /**
     * 处理不可靠事件
     * @param chatId 群ID
     * @return 群版本号
     */
    String handleUnreliableEvent(String chatId) throws IOException;

    List<String> getCurrentMembers(String chatId);
}
