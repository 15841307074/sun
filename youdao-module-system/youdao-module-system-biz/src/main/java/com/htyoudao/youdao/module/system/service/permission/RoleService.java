package com.htyoudao.youdao.module.system.service.permission;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageRespVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.UserRoleVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleRespVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleSaveReqVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleSimpleByBusinessRespVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleSortVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleUserPageReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.RoleDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * 角色 Service 接口
 *
 * @author 0090
 */
public interface RoleService {

    /**
     * 创建角色
     *
     * @param createReqVO 创建角色信息
     * @param type 角色类型
     * @return 角色编号
     */
    Long createRole(@Valid RoleSaveReqVO createReqVO, Integer type);

    /**
     * 更新角色
     *
     * @param updateReqVO 更新角色信息
     */
    void updateRole(@Valid RoleSaveReqVO updateReqVO);

    PageResult<OrgUserPageRespVO> getUserListWithRole(RoleUserPageReqVO pageReqVO);

    /**
     * 删除角色
     *
     * @param id 角色编号
     */
    void deleteRole(Long id);

    /**
     * 设置角色的数据权限
     *
     * @param id 角色编号
     * @param dataScope 数据范围
     * @param dataScopeDeptIds 部门编号数组
     */
    void updateRoleDataScope(Long id, Integer dataScope, Set<Long> dataScopeDeptIds);

    RoleDO validateRoleForUpdate(Long id);

    /**
     * 获得角色
     *
     * @param id 角色编号
     * @return 角色
     */
    RoleDO getRole(Long id);

    /**
     * 获得角色，从缓存中
     *
     * @param id 角色编号
     * @return 角色
     */
    RoleDO getRoleFromCache(Long id);

    /**
     * 获得角色列表
     *
     * @param ids 角色编号数组
     * @return 角色列表
     */
    List<RoleDO> getRoleList(Collection<Long> ids);

    /**
     * 获得角色数组，从缓存中
     *
     * @param ids 角色编号数组
     * @return 角色数组
     */
    List<RoleDO> getRoleListFromCache(Collection<Long> ids);

    /**
     * 获得角色列表
     *
     * @param statuses 筛选的状态
     * @return 角色列表
     */
    List<RoleDO> getRoleListByStatus(Collection<Integer> statuses);

    /**
     * 获得所有角色列表
     *
     * @return 角色列表
     */
    List<RoleDO> getRoleList();


    /**
     * 获得所有角色列表
     *
     * @return 角色列表
     */
    List<RoleDO> getRoleList(Long businessId);

    /**
     * 获得角色分页
     *
     * @param reqVO 角色分页查询
     * @return 角色分页结果
     */
    PageResult<RoleRespVO> getRolePage(RolePageReqVO reqVO);

    /**
     * 判断角色编号数组中，是否有管理员
     *
     * @param ids 角色编号数组
     * @return 是否有管理员
     */
    boolean hasAnySuperAdmin(Collection<Long> ids);

    /**
     * 校验角色们是否有效。如下情况，视为无效：
     * 1. 角色编号不存在
     * 2. 角色被禁用
     *
     * @param ids 角色编号数组
     */
    void validateRoleList(Collection<Long> ids);

    /**
     * 根据用户id数组，获得角色名称数组
     * @param userIds userIds
     * @return 角色名称数组
     */
    List<UserRoleVO> getRoleNamesByUserIds(Set<Long> userIds);

    /**
     * 根据用户id数组，获得角色名称数组
     * @param userIds userIds
     * @return 角色名称数组
     */
    List<UserRoleVO> getRoleNamesByUserIdsV2(Set<Long> userIds);

    void sortRole(@Valid List<RoleSortVO> sortReqVOS);

}
