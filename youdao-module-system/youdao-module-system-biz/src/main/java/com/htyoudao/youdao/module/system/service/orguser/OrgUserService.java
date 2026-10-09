package com.htyoudao.youdao.module.system.service.orguser;

import java.util.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserChargedReqVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserMoveReqVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserNum;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageRespVO;
import jakarta.validation.*;
import com.htyoudao.youdao.module.system.controller.admin.orguser.vo.*;
import com.htyoudao.youdao.module.system.dal.dataobject.orguser.OrgUserDO;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.pojo.PageParam;
import jakarta.validation.constraints.NotNull;

/**
 * 组织和用户关联 Service 接口
 *
 * @author 零零玖零
 */
public interface OrgUserService {

    /**
     * 创建组织和用户关联
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOrgUser(@Valid OrgUserSaveReqVO createReqVO);

    /**
     * 更新组织和用户关联
     *
     * @param updateReqVO 更新信息
     */
    void updateOrgUser(@Valid OrgUserSaveReqVO updateReqVO);

    /**
     * 删除组织和用户关联
     *
     * @param id 编号
     */
    void deleteOrgUser(Long id);

    /**
     * 获得组织和用户关联
     *
     * @param id 编号
     * @return 组织和用户关联
     */
    OrgUserDO getOrgUser(Long id);

    /**
     * 获得组织和用户关联分页
     *
     * @param pageReqVO 分页查询
     * @return 组织和用户关联分页
     */
    PageResult<OrgUserDO> getOrgUserPage(OrgUserPageReqVO pageReqVO);

    /**
     * 获得组织和用户关联
     *
     * @param userId userId
     * @return 组织ids
     */
    List<Long> selectUserOrgIds(Long userId);

    /**
     * 设置负责人
     * @param orgUserChargedReqVO orgUserChargedReqVO
     * @return int
     */
    int setCharged(OrgUserChargedReqVO orgUserChargedReqVO);

    /**
     * 设置负责人
     * @param id id
     * @param userId userId
     * @return int
     */
    int setChargedByUpdate(Long id, Long userId);


    /**
     * 撤销负责人
     * @param orgUserChargedReqVO orgUserChargedReqVO
     * @return int
     */
    int unCharged(OrgUserChargedReqVO orgUserChargedReqVO);

    /**
     * 根据组织id获取用户列表
     * @param pageReqVO pageReqVO
     */
    Page<OrgUserPageRespVO> getUserListByOrgId(com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageReqVO pageReqVO);

    /**
     * 批量移除
     * @param orgMoveReqVO pageReqVO
     * @return int
     */
    int deleteBatchOrgUser(OrgUserMoveReqVO orgMoveReqVO);

    /**
     * 批量新增
     * @param list orgUserSaveReqVO
     * @return int
     */
    Boolean insertBatch(List<OrgUserSaveReqVO> list);

    /**
     * 更新时通过组织查询当前节点人员
     * @param orgId orgId
     * @return int
     */
    List<com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserRespVO> getUserListByUpdate(Long orgId);

    /**
     * 批量新增
     * @param orgUserSaveReqVO orgUserSaveReqVO
     * @return int
     */
    Boolean saveBatch(OrgUserSaveReqVO orgUserSaveReqVO);

    /**
     * 获取节点人员数量
     * @param unionIds orgIds
     * @return Map
     */
    List<OrgUserNum> getNodeUserNum(Collection<Long> unionIds);

    /**
     * 判断是否有组织人员
     * @param id orgId
     * @return Boolean
     */
    Boolean hasOrgUser(Long id);

    /**
     * 获得组织和用户/项目关联
     *
     * @param userId userId
     * @return 组织ids
     */
    List<Long> selectUserOrgIdsByBusinessId(Long userId,Long businessId);

    /**
     * 设置组织人员可见
     * @param orgUserChargedReqVO orgUserChargedReqVO
     * @return int
     */
    int orgSetUserIsVisible(OrgUserChargedReqVO orgUserChargedReqVO);

    /**
     * 设置组织人员不可见
     * @param orgUserChargedReqVO orgUserChargedReqVO
     * @return int
     */
    int orgSetUserUnVisible(OrgUserChargedReqVO orgUserChargedReqVO);

    Long getOrgLeaderIdByOrgId(Long orgId);
}