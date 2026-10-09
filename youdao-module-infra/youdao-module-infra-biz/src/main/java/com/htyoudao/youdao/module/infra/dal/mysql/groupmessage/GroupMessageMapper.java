package com.htyoudao.youdao.module.infra.dal.mysql.groupmessage;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.infra.controller.admin.groupmessage.vo.GroupMessagePageReqVO;
import com.htyoudao.youdao.module.infra.dal.dataobject.groupmessage.GroupMessageDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 群消息 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface GroupMessageMapper extends BaseMapperX<GroupMessageDO> {

    default PageResult<GroupMessageDO> selectPage(GroupMessagePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<GroupMessageDO>()
                .eqIfPresent(GroupMessageDO::getTmpId, reqVO.getTmpId())
                .eqIfPresent(GroupMessageDO::getGroupId, reqVO.getGroupId())
                .eqIfPresent(GroupMessageDO::getSendId, reqVO.getSendId())
                .likeIfPresent(GroupMessageDO::getSendNickName, reqVO.getSendNickName())
                .eqIfPresent(GroupMessageDO::getContent, reqVO.getContent())
                .eqIfPresent(GroupMessageDO::getAtUserIds, reqVO.getAtUserIds())
                .eqIfPresent(GroupMessageDO::getReceipt, reqVO.getReceipt())
                .eqIfPresent(GroupMessageDO::getReceiptOk, reqVO.getReceiptOk())
                .eqIfPresent(GroupMessageDO::getType, reqVO.getType())
                .eqIfPresent(GroupMessageDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(GroupMessageDO::getSendTime, reqVO.getSendTime())
                .betweenIfPresent(GroupMessageDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(GroupMessageDO::getId));
    }

}
