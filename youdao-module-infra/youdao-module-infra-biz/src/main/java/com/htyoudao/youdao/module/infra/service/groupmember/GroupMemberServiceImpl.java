package com.htyoudao.youdao.module.infra.service.groupmember;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.websocket.core.constants.RedisKeyConstants;
import com.htyoudao.youdao.module.infra.controller.admin.groupmember.vo.GroupMemberPageReqVO;
import com.htyoudao.youdao.module.infra.controller.admin.groupmember.vo.GroupMemberSaveReqVO;
import com.htyoudao.youdao.module.infra.dal.dataobject.groupmember.GroupMemberDO;
import com.htyoudao.youdao.module.infra.dal.mysql.groupmember.GroupMemberMapper;
import com.htyoudao.youdao.module.system.api.user.AdminUserApi;
import com.htyoudao.youdao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.infra.enums.ErrorCodeConstants.GROUP_MEMBER_NOT_EXISTS;

/**
 * 群成员 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
@CacheConfig(cacheNames = RedisKeyConstants.IM_CACHE_GROUP_MEMBER_ID)
public class GroupMemberServiceImpl implements GroupMemberService {

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private GroupMemberMapper groupMemberMapper;

    @Override
    public Long createGroupMember(GroupMemberSaveReqVO createReqVO) {
        // 插入
        GroupMemberDO groupMember = BeanUtils.toBean(createReqVO, GroupMemberDO.class);
        groupMemberMapper.insert(groupMember);
        // 返回
        return groupMember.getId();
    }

    @Override
    public void createGroupMemberBatch(Set<Long> memberIds, Long groupId) {
        // 查询组员信息
        List<AdminUserRespDTO> userList = adminUserApi.getUserList(memberIds).getCheckedData();
        if (CollUtil.isNotEmpty(userList)) {
            List<GroupMemberDO> members = userList.stream()
                    .map(user -> {
                        GroupMemberDO member = new GroupMemberDO();
                        member.setGroupId(groupId);
                        member.setUserId(user.getId());
                        member.setHeadImage(user.getAvatar());
                        member.setUserNickName(user.getNickname());
                        return member;
                    })
                    .toList();
            groupMemberMapper.insertBatch(members);
        }
    }

    @Override
    public void updateGroupMember(GroupMemberSaveReqVO updateReqVO) {
        // 校验存在
        validateGroupMemberExists(updateReqVO.getId());
        // 更新
        GroupMemberDO updateObj = BeanUtils.toBean(updateReqVO, GroupMemberDO.class);
        groupMemberMapper.updateById(updateObj);
    }

    @Override
    public void deleteGroupMember(Long id) {
        // 校验存在
        validateGroupMemberExists(id);
        // 删除
        groupMemberMapper.deleteById(id);
    }

    private void validateGroupMemberExists(Long id) {
        if (groupMemberMapper.selectById(id) == null) {
            throw exception(GROUP_MEMBER_NOT_EXISTS);
        }
    }

    @Override
    public GroupMemberDO getGroupMember(Long id) {
        return groupMemberMapper.selectById(id);
    }

    @Override
    public PageResult<GroupMemberDO> getGroupMemberPage(GroupMemberPageReqVO pageReqVO) {
        return groupMemberMapper.selectPage(pageReqVO);
    }

    @Cacheable(key = "#groupId")
    @Override
    public List<Long> findUserIdsByGroupId(Long groupId) {
        LambdaQueryWrapper<GroupMemberDO> memberWrapper = Wrappers.lambdaQuery();
        memberWrapper.eq(GroupMemberDO::getGroupId, groupId).eq(GroupMemberDO::getQuit, false)
                .select(GroupMemberDO::getUserId);
        List<GroupMemberDO> members = groupMemberMapper.selectList(memberWrapper);
        return members.stream().map(GroupMemberDO::getUserId).collect(Collectors.toList());
    }

    @Override
    public GroupMemberDO findByIdAndUserId(Long groupId, Long userId) {
        return groupMemberMapper.selectOne(GroupMemberDO::getGroupId, groupId, GroupMemberDO::getUserId, userId);
    }

    @Override
    public List<GroupMemberDO> findByUserId(Long loginUserId) {
        return groupMemberMapper.selectList(GroupMemberDO::getUserId, loginUserId, GroupMemberDO::getQuit, false);
    }

    @Override
    public String getShowNickName(GroupMemberDO member) {
        return StrUtil.blankToDefault(member.getRemarkNickName(), member.getUserNickName());
    }

}
