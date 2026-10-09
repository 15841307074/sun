package com.htyoudao.youdao.module.infra.service.groupmessage;

import jakarta.validation.*;
import com.htyoudao.youdao.module.infra.controller.admin.groupmessage.vo.*;
import com.htyoudao.youdao.module.infra.dal.dataobject.groupmessage.GroupMessageDO;
import com.htyoudao.youdao.framework.common.pojo.PageResult;

import java.util.List;

/**
 * 群消息 Service 接口
 *
 * @author 超级管理员
 */
public interface GroupMessageService {

    /**
     * 创建群消息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long sendGroupMessage(@Valid GroupMessageSaveReqVO createReqVO);

    /**
     * 撤销组消息
     *
     * @param id 编号
     */
    void revokeGroupMessage(Long id);

    /**
     * 获得群消息
     *
     * @param id 编号
     * @return 群消息
     */
    GroupMessageDO getGroupMessage(Long id);

    /**
     * 获得群消息分页
     *
     * @param pageReqVO 分页查询
     * @return 群消息分页
     */
    PageResult<GroupMessageDO> getGroupMessagePage(GroupMessagePageReqVO pageReqVO);

    /**
     * 阅读群消息
     *
     * @param groupId 群组ID
     */
    void readGroupMessage(Long groupId);

    /**
     * 加载离线消息
     *
     * @param minId 最小ID
     * @return {@link List }<{@link GroupMessageRespVO }>
     */
    List<GroupMessageRespVO> loadOfflineMessage(Long minId);
}
