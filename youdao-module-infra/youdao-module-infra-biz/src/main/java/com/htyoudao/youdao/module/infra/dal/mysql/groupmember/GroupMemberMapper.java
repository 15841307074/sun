package com.htyoudao.youdao.module.infra.dal.mysql.groupmember;

import java.util.*;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.infra.dal.dataobject.groupmember.GroupMemberDO;
import org.apache.ibatis.annotations.Mapper;
import com.htyoudao.youdao.module.infra.controller.admin.groupmember.vo.*;

/**
 * 群成员 Mapper
 *
 * @author 超级管理员
 */
@Mapper
public interface GroupMemberMapper extends BaseMapperX<GroupMemberDO> {

    default PageResult<GroupMemberDO> selectPage(GroupMemberPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<GroupMemberDO>()
                .eqIfPresent(GroupMemberDO::getGroupId, reqVO.getGroupId())
                .eqIfPresent(GroupMemberDO::getUserId, reqVO.getUserId())
                .likeIfPresent(GroupMemberDO::getUserNickName, reqVO.getUserNickName())
                .likeIfPresent(GroupMemberDO::getRemarkNickName, reqVO.getRemarkNickName())
                .eqIfPresent(GroupMemberDO::getHeadImage, reqVO.getHeadImage())
                .likeIfPresent(GroupMemberDO::getRemarkGroupName, reqVO.getRemarkGroupName())
                .eqIfPresent(GroupMemberDO::getIsDnd, reqVO.getIsDnd())
                .eqIfPresent(GroupMemberDO::getQuit, reqVO.getQuit())
                .betweenIfPresent(GroupMemberDO::getQuitTime, reqVO.getQuitTime())
                .betweenIfPresent(GroupMemberDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(GroupMemberDO::getId));
    }

}