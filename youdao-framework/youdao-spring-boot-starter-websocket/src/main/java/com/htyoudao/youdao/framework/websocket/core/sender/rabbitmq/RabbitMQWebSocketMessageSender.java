package com.htyoudao.youdao.framework.websocket.core.sender.rabbitmq;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.htyoudao.youdao.framework.common.enums.TerminalEnum;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.websocket.core.constants.RedisKeyConstants;
import com.htyoudao.youdao.framework.websocket.core.message.GroupMessage;
import com.htyoudao.youdao.framework.websocket.core.message.PrivateMessage;
import com.htyoudao.youdao.framework.websocket.core.message.SendResult;
import com.htyoudao.youdao.framework.websocket.core.message.UserInfo;
import com.htyoudao.youdao.framework.websocket.core.sender.AbstractWebSocketMessageSender;
import com.htyoudao.youdao.framework.websocket.core.sender.WebSocketMessageSender;
import com.htyoudao.youdao.framework.websocket.core.session.WebSocketSessionManager;
import com.htyoudao.youdao.module.infra.enums.MessageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.*;

/**
 * 基于 RabbitMQ 的 {@link WebSocketMessageSender} 实现类
 *
 * @author 0090
 */
@Slf4j
public class RabbitMQWebSocketMessageSender extends AbstractWebSocketMessageSender {

    private final RabbitTemplate rabbitTemplate;

    private final DirectExchange directExchange;

    private final StringRedisTemplate stringRedisTemplate;

    private final String routingKeyPrefix;

    public RabbitMQWebSocketMessageSender(WebSocketSessionManager sessionManager,
                                          StringRedisTemplate stringRedisTemplate,
                                          RabbitTemplate rabbitTemplate,
                                          DirectExchange directExchange, String routingKeyPrefix) {
        super(sessionManager);
        this.stringRedisTemplate = stringRedisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.directExchange = directExchange;
        this.routingKeyPrefix = routingKeyPrefix;
    }

    @Override
    public <T> void sendPrivateMessage(PrivateMessage<T> message) {
        // 根据每个终端所连的server进行分组
        Map<String, UserInfo> sendMap = new HashMap<>();
        for (Integer terminal : TerminalEnum.IM_ARRAYS) {
            String key = String.format("%s:%d:%d", RedisKeyConstants.IM_MAX_SERVER_ID, message.getReceiverId(), terminal);
            sendMap.put(key, new UserInfo(message.getReceiverId(), terminal));
        }
        // 格式:map<服务器id,list<接收方>>
        Map<String, Set<Long>> serverMap = new HashMap<>();
        List<UserInfo> offLineUsers = new LinkedList<>();
        // 批量拉取
        buildServerMapAndUsers(sendMap, offLineUsers, serverMap);

        // 逐个server发送
        for (Map.Entry<String, Set<Long>> entry : serverMap.entrySet()) {
            // 推送消息
            sendRabbitMQMessage(MessageTypeEnum.PRIVATE_MESSAGE.getType(), message, entry.getKey());
        }
    }

    @Override
    public <T> void sendGroupMessage(GroupMessage<T> message) {
        // 根据群聊每个成员所连的server，进行分组
        Map<String, UserInfo> sendMap = getStringUserInfoMap(message);
        // 格式:map<服务器id,list<接收方>>
        Map<String, Set<Long>> serverMap = new HashMap<>();
        List<UserInfo> offLineUsers = new LinkedList<>();
        buildServerMapAndUsers(sendMap, offLineUsers, serverMap);

        // 逐个server发送
        for (Map.Entry<String, Set<Long>> entry : serverMap.entrySet()) {
            GroupMessage<T> sendMessage = new GroupMessage<>();
            sendMessage.setSender(message.getSender());
            sendMessage.setReceiverIds(entry.getValue());
            sendMessage.setSendResult(message.getSendResult());
            sendMessage.setData(message.getData());
            // 推送消息
            sendRabbitMQMessage(MessageTypeEnum.GROUP_MESSAGE.getType(), sendMessage, entry.getKey());
        }

        // 对离线用户回复消息状态
        if (message.getSendResult() && !offLineUsers.isEmpty()) {
            List<SendResult<T>> results = new LinkedList<>();
            for (UserInfo offLineUser : offLineUsers) {
                SendResult<T> result = new SendResult<>();
                result.setSender(message.getSender());
                result.setReceiver(offLineUser);
                // result.setCode(IMSendCode.NOT_ONLINE.code());
                result.setData(message.getData());
                results.add(result);
            }
            // listenerMulticaster.multicast(IMListenerType.GROUP_MESSAGE, results);
        }
    }

    /**
     * 获取key-用户信息映射
     *
     * @param message 消息
     * @return {@link Map }<{@link String }, {@link UserInfo }>
     */
    private static <T> Map<String, UserInfo> getStringUserInfoMap(GroupMessage<T> message) {
        Map<String, UserInfo> sendMap = new HashMap<>();
        Set<Long> receiverIds = message.getReceiverIds();
        for (Integer terminal : message.getTerminals()) {
            receiverIds.forEach(id -> {
                String key = String.format("%s:%d:%d", RedisKeyConstants.IM_MAX_SERVER_ID, id, terminal);
                sendMap.put(key, new UserInfo(id, terminal));
            });
        }
        return sendMap;
    }

    /**
     * 构建服务器map和离线用户
     *
     * @param sendMap      发送map
     * @param offLineUsers 离线用户
     * @param serverMap    服务器map
     */
    private void buildServerMapAndUsers(Map<String, UserInfo> sendMap, List<UserInfo> offLineUsers, Map<String, Set<Long>> serverMap) {
        // 批量拉取
        List<String> serverIds = stringRedisTemplate.opsForValue().multiGet(sendMap.keySet());
        if (CollUtil.isEmpty(serverIds)) {
            offLineUsers.addAll(sendMap.values());
        } else {
            int idx = 0;
            for (Map.Entry<String, UserInfo> entry : sendMap.entrySet()) {
                String serverId = serverIds.get(idx++);
                if (StrUtil.isBlank(serverId)) {
                    // 加入离线列表
                    offLineUsers.add(entry.getValue());
                } else {
                    Set<Long> set = serverMap.computeIfAbsent(serverId, o -> new HashSet<>());
                    set.add(entry.getValue().getId());
                }
            }
        }
    }

    /**
     * 发送rabbitMQ message
     *
     * @param messageType 消息类型
     * @param message     消息
     * @param routingKey  路由密钥
     */
    private <T> void sendRabbitMQMessage(String messageType, T message, String routingKey) {
        // 推送消息
        RabbitMQWebSocketMessage mqMessage = new RabbitMQWebSocketMessage()
                .setMessageType(messageType).setMessageContent(JsonUtils.toJsonString(message));
        rabbitTemplate.convertAndSend(directExchange.getName(), routingKeyPrefix + routingKey, mqMessage);
    }

}
