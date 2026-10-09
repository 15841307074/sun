package com.htyoudao.youdao.module.infra.service.groupmember;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.infra.controller.admin.groupmember.vo.GroupMemberPageReqVO;
import com.htyoudao.youdao.module.infra.controller.admin.groupmember.vo.GroupMemberSaveReqVO;
import com.htyoudao.youdao.module.infra.dal.dataobject.groupmember.GroupMemberDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Set;

/**
 * 群成员 Service 接口
 *
 * @author 超级管理员
 */
public interface GroupMemberService {

    /**
     * 创建群成员
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createGroupMember(@Valid GroupMemberSaveReqVO createReqVO);

    /**
     * 创建组成员批处理
     *
     * @param memberIds 成员ID
     * @param groupId   群组ID
     */
    void createGroupMemberBatch(Set<Long> memberIds, Long groupId);

    /**
     * 更新群成员
     *
     * @param updateReqVO 更新信息
     */
    void updateGroupMember(@Valid GroupMemberSaveReqVO updateReqVO);

    /**
     * 删除群成员
     *
     * @param id 编号
     */
    void deleteGroupMember(Long id);

    /**
     * 获得群成员
     *
     * @param id 编号
     * @return 群成员
     */
    GroupMemberDO getGroupMember(Long id);

    /**
     * 获得群成员分页
     *
     * @param pageReqVO 分页查询
     * @return 群成员分页
     */
    PageResult<GroupMemberDO> getGroupMemberPage(GroupMemberPageReqVO pageReqVO);

    /**
     * 按组id查找用户id
     *
     * @param groupId 群组ID
     * @return {@link List }<{@link Long }>
     */
    List<Long> findUserIdsByGroupId(Long groupId);

    /**
     * 按id和用户id查找
     *
     * @param groupId 群组ID
     * @param userId  用户ID
     * @return {@link GroupMemberDO }
     */
    GroupMemberDO findByIdAndUserId(Long groupId, Long userId);

    /**
     * 按用户id查找
     *
     * @param loginUserId 登录用户id
     * @return {@link List }<{@link GroupMemberDO }>
     */
    List<GroupMemberDO> findByUserId(Long loginUserId);

    /**
     * 获取昵称
     *
     * @param member 成员
     * @return {@link String }
     */
    String getShowNickName(GroupMemberDO member);
}
