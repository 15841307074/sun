package com.htyoudao.youdao.module.infra.service.group;

import java.util.*;
import jakarta.validation.*;
import com.htyoudao.youdao.module.infra.controller.admin.group.vo.*;
import com.htyoudao.youdao.module.infra.dal.dataobject.group.GroupDO;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;

/**
 * 群 Service 接口
 *
 * @author 超级管理员
 */
public interface GroupService {

    /**
     * 创建群
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createGroup(@Valid GroupSaveReqVO createReqVO);

    /**
     * 更新群
     *
     * @param updateReqVO 更新信息
     */
    void updateGroup(@Valid GroupSaveReqVO updateReqVO);

    /**
     * 删除群
     *
     * @param id 编号
     */
    void deleteGroup(Long id);

    /**
     * 获得群
     *
     * @param id 编号
     * @return 群
     */
    GroupDO getGroup(Long id);

    /**
     * 获得群分页
     *
     * @param pageReqVO 分页查询
     * @return 群分页
     */
    PageResult<GroupDO> getGroupPage(GroupPageReqVO pageReqVO);

}