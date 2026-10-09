package com.htyoudao.youdao.module.member.service.wecom.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.module.member.controller.admin.wecom.WecomApiUtil;
import com.htyoudao.youdao.module.member.dal.dataobject.wecom.WecomGroupMemberDO;
import com.htyoudao.youdao.module.member.dal.mysql.wecom.WecomGroupMemberMapper;
import com.htyoudao.youdao.module.member.service.wecom.WecomGroupService;
import java.util.ArrayList;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import org.springframework.util.CollectionUtils;

/**
 * 企业微信群服务实现（数据库存储）
 */
@Slf4j
@Service("wecomGroupDatabaseService")
public class WecomGroupDatabaseServiceImpl implements WecomGroupService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private WecomApiUtil wecomApiUtil;

    @Autowired
    private WecomGroupMemberMapper wecomGroupMemberMapper;

    /**
     * 检查用户是否在群中
     */
    @Override
    public boolean isUserInGroup(String unionId, String chatId) {
        // 从数据库中检查
        Integer count = wecomGroupMemberMapper.countByChatIdAndUnionId(
                chatId, unionId
        );
        return count != null && count > 0;
    }




    /**
     * 处理可靠事件（直接使用回调数据更新）
     */
    @Override
    public void handleReliableEvent(String chatId, String changeType, List<String> memChangeList) {
        try {
            // 根据变更类型更新数据库中的成员列表
            if ("add_member".equals(changeType)) {
                for (String externalUserId : memChangeList) {
                    WecomGroupMemberDO wecomGroupMemberDO = new WecomGroupMemberDO();
                    wecomGroupMemberDO.setExternalUserId(externalUserId);
                    Optional.ofNullable(wecomApiUtil.getExternalContactDetail(externalUserId))
                        .map(detail -> detail.getJSONObject("external_contact"))
                        .map(contact -> contact.getString("unionid"))
                        .filter(StringUtils::isNotBlank)
                        .ifPresentOrElse(
                            wecomGroupMemberDO::setUnionId,
                            () -> {
                                wecomGroupMemberDO.setUnionId("");
                                log.warn("unionid为空，externalUserId: {}", externalUserId);
                            }
                        );
                    wecomGroupMemberDO.setChatId(chatId);
                    wecomGroupMemberMapper.insert(wecomGroupMemberDO);
                }

            } else if ("del_member".equals(changeType)) {
                wecomGroupMemberMapper.batchDeleteByChatIdAndUserIds(chatId, memChangeList);
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
        JSONObject response = wecomApiUtil.getGroupDetail(chatId);

        if (response == null){
            log.error("获取群详情失败，chatId: {}", chatId);
            throw new ServiceException(new ErrorCode(500,"获取群详情失败"));
        }

        // 检查错误码
        Integer errcode = response.getInteger("errcode");
        if (errcode != null && errcode != 0) {
            String errmsg = response.getString("errmsg");
            log.warn("获取群详情返回错误，chatId: {}, errcode: {}, errmsg: {}", chatId, errcode, errmsg);

            // 49008: 群主已经解散群聊
            if (errcode == 49008) {
                log.info("群聊已解散，删除群内所有成员，chatId: {}", chatId);
                // 删除该群的所有成员记录
                wecomGroupMemberMapper.delete(WecomGroupMemberDO::getChatId, chatId);
                // 返回空版本号
                return "";
            }

            throw new ServiceException(new ErrorCode(errcode, errmsg));
        }

        JSONObject groupDetail = response.getJSONObject("group_chat");
        if (groupDetail == null) {
            log.error("群详情数据为空，chatId: {}", chatId);
            throw new ServiceException(new ErrorCode(500, "群详情数据为空"));
        }

        JSONArray membersArray = groupDetail.getJSONArray("member_list");

        // 转换为List<String>格式
        List<WecomGroupMemberDO> membersList = new ArrayList<>();
        for (int i = 0; i < membersArray.size(); i++) {
            JSONObject member = membersArray.getJSONObject(i);
            WecomGroupMemberDO memberDO = new WecomGroupMemberDO();
            memberDO.setChatId(chatId);
            memberDO.setExternalUserId(member.getString("userid"));
            memberDO.setUnionId(member.getString("unionid"));
            memberDO.setMemberType(member.getInteger("type"));
            membersList.add(memberDO);
        }


        // 先删除所有该群的成员记录
        wecomGroupMemberMapper.delete(WecomGroupMemberDO::getChatId, chatId);

        // 批量插入新的成员记录
        wecomGroupMemberMapper.insertBatch(membersList);
        return groupDetail.getString("member_version");
    }

    /**
     * 判断用户是否在任意一个企业微信社群中
     */
    @Override
    public boolean isUserInAnyGroup(String unionId) {
        // 直接从数据库中检查
        Integer count = wecomGroupMemberMapper.countByUnionId(unionId);
        return count != null && count > 0;
    }

    /**
     * 获取当前成员列表
     */
    @Override
    public List<String> getCurrentMembers(String chatId) {
        // 1. 尝试从数据库获取
        List<WecomGroupMemberDO> memberDOs = wecomGroupMemberMapper.selectList(WecomGroupMemberDO::getChatId, chatId);

        if (CollectionUtils.isEmpty(memberDOs)){
            return List.of();
        }

        return memberDOs.stream().map(WecomGroupMemberDO::getExternalUserId).toList();
    }

}
