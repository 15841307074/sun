package com.htyoudao.youdao.module.infra.service.group;

import cn.hutool.core.collection.CollUtil;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.infra.controller.admin.group.vo.GroupPageReqVO;
import com.htyoudao.youdao.module.infra.controller.admin.group.vo.GroupSaveReqVO;
import com.htyoudao.youdao.module.infra.dal.dataobject.group.GroupDO;
import com.htyoudao.youdao.module.infra.dal.mysql.group.GroupMapper;
import com.htyoudao.youdao.module.infra.service.groupmember.GroupMemberService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.infra.enums.ErrorCodeConstants.GROUP_NOT_EXISTS;

/**
 * 群 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class GroupServiceImpl implements GroupService {

    @Resource
    private GroupMemberService groupMemberService;
    @Resource
    private GroupMapper groupMapper;

    @Override
    public Long createGroup(GroupSaveReqVO createReqVO) {
        // 插入
        GroupDO group = BeanUtils.toBean(createReqVO, GroupDO.class);
        group.setOwnerId(SecurityFrameworkUtils.getLoginUserId());
        groupMapper.insert(group);

        // 设置群成员
        Set<Long> memberIds = createReqVO.getMemberIds();
        if (CollUtil.isNotEmpty(memberIds)) {
            groupMemberService.createGroupMemberBatch(memberIds, group.getId());
        }
        // 返回
        return group.getId();
    }

    @Override
    public void updateGroup(GroupSaveReqVO updateReqVO) {
        // 校验存在
        validateGroupExists(updateReqVO.getId());
        // 更新
        GroupDO updateObj = BeanUtils.toBean(updateReqVO, GroupDO.class);
        groupMapper.updateById(updateObj);
    }

    @Override
    public void deleteGroup(Long id) {
        // 校验存在
        validateGroupExists(id);
        // 删除
        groupMapper.deleteById(id);
    }

    private void validateGroupExists(Long id) {
        if (groupMapper.selectById(id) == null) {
            throw exception(GROUP_NOT_EXISTS);
        }
    }

    @Override
    public GroupDO getGroup(Long id) {
        return groupMapper.selectById(id);
    }

    @Override
    public PageResult<GroupDO> getGroupPage(GroupPageReqVO pageReqVO) {
        return groupMapper.selectPage(pageReqVO);
    }

}
