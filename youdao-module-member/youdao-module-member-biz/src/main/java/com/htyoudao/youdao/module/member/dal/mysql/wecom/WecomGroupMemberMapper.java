package com.htyoudao.youdao.module.member.dal.mysql.wecom;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.member.dal.dataobject.wecom.WecomGroupMemberDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import org.apache.ibatis.annotations.Select;

/**
 * 企业微信群成员关系Mapper
 */
@Mapper
public interface WecomGroupMemberMapper extends BaseMapperX<WecomGroupMemberDO> {

    /**
     * 批量删除群成员关系
     */
    default int batchDeleteByChatIdAndUserIds(String chatId, List<String> userIds){
        LambdaQueryWrapperX<WecomGroupMemberDO> queryWrapperX = new LambdaQueryWrapperX<>();
        queryWrapperX.eq(WecomGroupMemberDO::getChatId, chatId);
        queryWrapperX.in(WecomGroupMemberDO::getExternalUserId, userIds);
        return this.delete(queryWrapperX);
    }

    /**
     * 批量新增群成员关系
     */
    default void batchInsertByChatIdAndUserIds(String chatId, List<String> userIds){
        if (chatId == null || userIds == null || userIds.isEmpty()){
            return;
        }

        for (String userId : userIds) {
            //插入新记录
            WecomGroupMemberDO memberDO = new WecomGroupMemberDO();
            memberDO.setChatId(chatId);
            memberDO.setExternalUserId(userId);
            this.insert(memberDO);
        }
    }

    /**
     * 检查用户是否在群中
     */
    @Select("SELECT COUNT(1) FROM wecom_group_member WHERE chat_id = #{chatId} AND union_id = #{unionId} and deleted = 0")
    Integer countByChatIdAndUnionId(@Param("chatId") String chatId, @Param("unionId") String unionId);

    /**
     * 检查用户是否在任意群中
     */
    @Select("SELECT COUNT(1) FROM wecom_group_member WHERE union_id = #{unionId} and deleted = 0")
    Integer countByUnionId(@Param("unionId") String unionId);
}
