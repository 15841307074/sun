package com.htyoudao.youdao.module.infra.service.groupmessage;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.date.LocalDateTimeUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.framework.websocket.core.constants.IMConstants;
import com.htyoudao.youdao.framework.websocket.core.constants.RedisKeyConstants;
import com.htyoudao.youdao.framework.websocket.core.message.GroupMessage;
import com.htyoudao.youdao.framework.websocket.core.message.UserInfo;
import com.htyoudao.youdao.framework.websocket.core.sender.WebSocketMessageSender;
import com.htyoudao.youdao.module.infra.controller.admin.groupmessage.vo.GroupMessagePageReqVO;
import com.htyoudao.youdao.module.infra.controller.admin.groupmessage.vo.GroupMessageRespVO;
import com.htyoudao.youdao.module.infra.controller.admin.groupmessage.vo.GroupMessageSaveReqVO;
import com.htyoudao.youdao.module.infra.dal.dataobject.groupmember.GroupMemberDO;
import com.htyoudao.youdao.module.infra.dal.dataobject.groupmessage.GroupMessageDO;
import com.htyoudao.youdao.module.infra.dal.mysql.groupmessage.GroupMessageMapper;
import com.htyoudao.youdao.module.infra.enums.websocket.MessageCodeEnum;
import com.htyoudao.youdao.module.infra.enums.websocket.MessageStatusEnum;
import com.htyoudao.youdao.module.infra.service.groupmember.GroupMemberService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.infra.enums.ErrorCodeConstants.*;

/**
 * 群消息 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class GroupMessageServiceImpl implements GroupMessageService {

    @Resource
    private GroupMemberService groupMemberService;
    @Resource
    private GroupMessageMapper groupMessageMapper;
    @Resource
    private WebSocketMessageSender webSocketMessageSender;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Long sendGroupMessage(GroupMessageSaveReqVO sendReqVO) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        GroupMemberDO member = groupMemberService.findByIdAndUserId(sendReqVO.getGroupId(), loginUserId);
        if (member == null || member.getQuit()) {
            throw exception(GROUP_QUIT);
        }
        Integer terminal = WebFrameworkUtils.getTerminal();
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(sendReqVO.getGroupId());
        Set<Long> receiverIds = userIds.stream()
                .filter(id -> !Objects.equals(id, loginUserId))
                .collect(Collectors.toSet());
        // 插入
        GroupMessageDO groupMessage = BeanUtils.toBean(sendReqVO, GroupMessageDO.class);
        groupMessage.setSendId(loginUserId);
        groupMessage.setSendTime(LocalDateTime.now());
        groupMessage.setSendNickName(groupMemberService.getShowNickName(member));
        groupMessage.setStatus(MessageStatusEnum.PENDING.getCode());
        groupMessageMapper.insert(groupMessage);

        // 群发消息
        GroupMessageRespVO respMessage = BeanUtils.toBean(groupMessage, GroupMessageRespVO.class);
        GroupMessage<GroupMessageRespVO> messageInfo = new GroupMessage<>();
        messageInfo.setSender(new UserInfo(loginUserId, terminal));
        messageInfo.setReceiverIds(receiverIds);
        messageInfo.setSendResult(false);
        messageInfo.setData(respMessage);
        webSocketMessageSender.sendGroupMessage(messageInfo);

        // 返回
        return groupMessage.getId();
    }

    @Override
    public void revokeGroupMessage(Long id) {
        // 校验存在
        GroupMessageDO message = groupMessageMapper.selectById(id);
        validateGroupMessageExists(message);
        Long groupId = message.getGroupId();
        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        GroupMemberDO member = groupMemberService.findByIdAndUserId(groupId, loginUserId);
        if (member == null || Boolean.TRUE.equals(member.getQuit())) {
            throw exception(GROUP_QUIT);
        }
        // 更新消息状态
        message.setStatus(MessageStatusEnum.REVOKE.getCode());
        groupMessageMapper.updateById(message);

        // 生成一条撤回消息
        GroupMessageDO revokeMessage = new GroupMessageDO();
        revokeMessage.setStatus(MessageStatusEnum.PENDING.getCode());
        revokeMessage.setType(MessageCodeEnum.REVOKE.getCode());
        revokeMessage.setGroupId(groupId);
        revokeMessage.setSendId(loginUserId);
        revokeMessage.setSendNickName(groupMemberService.getShowNickName(member));
        revokeMessage.setContent(id.toString());
        revokeMessage.setSendTime(LocalDateTime.now());
        groupMessageMapper.insert(revokeMessage);

        // 推送消息
        List<Long> userIds = groupMemberService.findUserIdsByGroupId(groupId);
        GroupMessageRespVO respMessage = BeanUtils.toBean(revokeMessage, GroupMessageRespVO.class);
        GroupMessage<GroupMessageRespVO> messageInfo = new GroupMessage<>();
        messageInfo.setSender(new UserInfo(loginUserId, WebFrameworkUtils.getTerminal()));
        messageInfo.setReceiverIds(new HashSet<>(userIds));
        messageInfo.setData(respMessage);
        webSocketMessageSender.sendGroupMessage(messageInfo);
    }

    /**
     * 验证组消息
     *
     * @param message 消息
     */
    private void validateGroupMessageExists(GroupMessageDO message) {
        if (message == null) {
            throw exception(GROUP_MESSAGE_NOT_EXISTS);
        }
        if (!message.getSendId().equals(WebFrameworkUtils.getLoginUserId())) {
            throw exception(GROUP_MESSAGE_NOT_YOURS);
        }
        boolean timeout = LocalDateTimeUtils.beforeNow(
                LocalDateTimeUtils.addTime(Duration.ofSeconds(IMConstants.ALLOW_RECALL_SECOND)));
        if (timeout) {
            throw exception(GROUP_MESSAGE_TIMEOUT);
        }
    }

    @Override
    public GroupMessageDO getGroupMessage(Long id) {
        return groupMessageMapper.selectById(id);
    }

    @Override
    public PageResult<GroupMessageDO> getGroupMessagePage(GroupMessagePageReqVO pageReqVO) {
        return groupMessageMapper.selectPage(pageReqVO);
    }

    @Override
    public void readGroupMessage(Long groupId) {

        // 查询最后一条的id
        GroupMessageDO message = groupMessageMapper.selectOne(Wrappers.<GroupMessageDO>lambdaQuery()
                .select(GroupMessageDO::getId)
                .eq(GroupMessageDO::getGroupId, groupId)
                .orderByDesc(GroupMessageDO::getId)
                .last("limit 1"));
        if (message == null) {
            return;
        }

        // 推送消息给自己的其他终端,同步清空会话列表中的未读数量
        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        GroupMessageRespVO respMessage = new GroupMessageRespVO();
        respMessage.setType(MessageCodeEnum.READ.getCode());
        respMessage.setSendTime(LocalDateTime.now());
        respMessage.setSendId(loginUserId);
        respMessage.setGroupId(groupId);
        GroupMessage<GroupMessageRespVO> sendMessage = new GroupMessage<>();
        sendMessage.setSender(new UserInfo(loginUserId, WebFrameworkUtils.getTerminal()));
        sendMessage.setSendResult(true);
        sendMessage.setData(respMessage);
        webSocketMessageSender.sendGroupMessage(sendMessage);

        // 记录已读消息位置
        String key = String.format("%s:%d", RedisKeyConstants.IM_GROUP_READ_POSITION, groupId);
        stringRedisTemplate.opsForHash().put(key, loginUserId, message.getId());

    }

    @Override
    public List<GroupMessageRespVO> loadOfflineMessage(Long minId) {
        Long loginUserId = WebFrameworkUtils.getLoginUserId();
        // 查询用户加入的群组
        List<GroupMemberDO> members = groupMemberService.findByUserId(loginUserId);
        if (CollUtil.isEmpty(members)) {
            return Collections.emptyList();
        }
        Map<Long, GroupMemberDO> memberMap = CollStreamUtil.toIdentityMap(members, GroupMemberDO::getGroupId);
        Set<Long> groupIds = memberMap.keySet();
        // 只能拉取最近30天的消息，并且不能超过10000条
        LocalDateTime minDate = LocalDateTimeUtils.minusTime(Duration.ofDays(30L));
        List<GroupMessageDO> messages = groupMessageMapper.selectList(Wrappers.<GroupMessageDO>lambdaQuery()
                .gt(GroupMessageDO::getId, minId)
                .gt(GroupMessageDO::getSendTime, minDate)
                .in(GroupMessageDO::getGroupId, groupIds)
                .orderByDesc(GroupMessageDO::getId)
                .last("limit 10000"));
        // 对消息进行分组
        if (CollUtil.isEmpty(messages)) {
            return Collections.emptyList();
        }
        Map<Long, List<GroupMessageDO>> messageMap = messages.stream()
                .collect(Collectors.groupingBy(GroupMessageDO::getGroupId));
        LinkedList<GroupMessageRespVO> respMessages = new LinkedList<>();
        for (Map.Entry<Long, List<GroupMessageDO>> entry : messageMap.entrySet()) {
            Long groupId = entry.getKey();
            List<GroupMessageDO> groupMessages = entry.getValue();
            String key = String.format("%s:%d", RedisKeyConstants.IM_GROUP_READ_POSITION, groupId);
            Object o = stringRedisTemplate.opsForHash().get(key, loginUserId);
            long readMaxId = Objects.isNull(o) ? -1 : (Long) o;
            for (GroupMessageDO m : groupMessages) {
                // 排除加群前的消息
                GroupMemberDO member = memberMap.get(m.getGroupId());
                if (m.getSendTime().isBefore(member.getCreateTime())) {
                    continue;
                }
                GroupMessageRespVO respMessage = BeanUtils.toBean(m, GroupMessageRespVO.class);
                respMessage.setStatus(readMaxId >= m.getId() ? MessageStatusEnum.READ.getCode()
                        : MessageStatusEnum.PENDING.getCode());
                respMessages.add(respMessage);
            }
        }
        // 根据id排序
        return respMessages.stream().sorted(Comparator.comparing(GroupMessageRespVO::getId))
                .collect(Collectors.toList());
    }

}
