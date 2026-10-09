package com.htyoudao.youdao.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Supplier;
import com.alibaba.nacos.shaded.com.google.common.base.Suppliers;
import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.util.collection.CollectionUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.system.api.permission.dto.DeptDataPermissionRespDTO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.permission.PermissionCopyRoleReqVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.permission.PermissionMoveUserRoleReqVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.permission.PermissionRemoveUserRoleReqVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleSaveReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.MenuDO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.RoleDO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.RoleMenuDO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.UserRoleDO;
import com.htyoudao.youdao.module.system.dal.mysql.businessuser.BusinessUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.permission.RoleMenuMapper;
import com.htyoudao.youdao.module.system.dal.mysql.permission.UserRoleMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreUserMapper;
import com.htyoudao.youdao.module.system.dal.redis.RedisKeyConstants;
import com.htyoudao.youdao.module.system.enums.permission.DataScopeEnum;
import com.htyoudao.youdao.module.system.enums.permission.RoleTypeEnum;
import com.htyoudao.youdao.module.system.service.dept.DeptService;
import com.htyoudao.youdao.module.system.service.org.OrgService;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertSet;
import static com.htyoudao.youdao.framework.common.util.json.JsonUtils.toJsonString;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.ROLE_MENU_NOT_EXISTS;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.ROLE_NOT_EXISTS;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.ROLE_SYSTEM_CONNOT_REMOVE_USER;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.ROLE_USER_DUPLICATE;

/**
 * 权限 Service 实现类
 *
 * @author 0090
 */
@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;
    @Resource
    private DeptService deptService;
    @Resource
    private OrgService orgService;
    @Resource
    private SystemStoreInfoMapper systemStoreInfoMapper;
    @Resource
    private SystemStoreUserMapper systemStoreUserMapper;
    @Resource
    private BusinessUserMapper businessUserMapper;

    @Override
    public boolean hasAnyPermissions(Long userId, String... permissions) {
        // 如果为空，说明已经有权限
        if (ArrayUtil.isEmpty(permissions)) {
            return true;
        }

        // 获得当前登录的角色。如果为空，说明没有权限
        List<RoleDO> roles = getEnableUserRoleListByUserIdFromCache(userId,BusinessContextHolder.getBusinessId());
        if (CollUtil.isEmpty(roles)) {
            return false;
        }

        // 情况一：遍历判断每个权限，如果有一满足，说明有权限
        for (String permission : permissions) {
            if (hasAnyPermission(roles, permission)) {
                return true;
            }
        }

        // 情况二：如果是超管，也说明有权限
        return roleService.hasAnySuperAdmin(convertSet(roles, RoleDO::getId));
    }

    /**
     * 判断指定角色，是否拥有该 permission 权限
     *
     * @param roles 指定角色数组
     * @param permission 权限标识
     * @return 是否拥有
     */
    private boolean hasAnyPermission(List<RoleDO> roles, String permission) {
        List<Long> menuIds = menuService.getMenuIdListByPermissionFromCache(permission);
        // 采用严格模式，如果权限找不到对应的 Menu 的话，也认为没有权限
        if (CollUtil.isEmpty(menuIds)) {
            return false;
        }

        // 判断是否有权限
        Set<Long> roleIds = convertSet(roles, RoleDO::getId);
        for (Long menuId : menuIds) {
            // 获得拥有该菜单的角色编号集合
            Set<Long> menuRoleIds = getSelf().getMenuRoleIdListByMenuIdFromCache(menuId);
            // 如果有交集，说明有权限
            if (CollUtil.containsAny(menuRoleIds, roleIds)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAnyRoles(Long userId, String... roles) {
        // 如果为空，说明已经有权限
        if (ArrayUtil.isEmpty(roles)) {
            return true;
        }

        // 获得当前登录的角色。如果为空，说明没有权限
        List<RoleDO> roleList = getEnableUserRoleListByUserIdFromCache(userId, BusinessContextHolder.getBusinessId());
        if (CollUtil.isEmpty(roleList)) {
            return false;
        }

        // 判断是否有角色
        Set<String> userRoles = convertSet(roleList, RoleDO::getCode);
        return CollUtil.containsAny(userRoles, Sets.newHashSet(roles));
    }

    // ========== 角色-菜单的相关方法  ==========

    @Override
    @DSTransactional // 多数据源，使用 @DSTransactional 保证本地事务，以及数据源的切换
    @Caching(evict = {
            @CacheEvict(value = RedisKeyConstants.MENU_ROLE_ID_LIST,
            allEntries = true),
            @CacheEvict(value = RedisKeyConstants.PERMISSION_MENU_ID_LIST,
            allEntries = true) // allEntries 清空所有缓存，主要一次更新涉及到的 menuIds 较多，反倒批量会更快
    })
    public void assignRoleMenu(Long roleId, Set<Long> menuIds) {
        // 获得角色拥有菜单编号
        Set<Long> dbMenuIds = convertSet(roleMenuMapper.selectListByRoleId(roleId), RoleMenuDO::getMenuId);
        // 计算新增和删除的菜单编号
        Set<Long> menuIdList = CollUtil.emptyIfNull(menuIds);
        Collection<Long> createMenuIds = CollUtil.subtract(menuIdList, dbMenuIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbMenuIds, menuIdList);
        // 执行新增和删除。对于已经授权的菜单，不用做任何处理
        if (CollUtil.isNotEmpty(createMenuIds)) {
            roleMenuMapper.insertBatch(CollectionUtils.convertList(createMenuIds, menuId -> {
                RoleMenuDO entity = new RoleMenuDO();
                entity.setRoleId(roleId);
                entity.setMenuId(menuId);
                return entity;
            }));
        }
        if (CollUtil.isNotEmpty(deleteMenuIds)) {
            roleMenuMapper.deleteListByRoleIdAndMenuIds(roleId, deleteMenuIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Caching(evict = {
            @CacheEvict(value = RedisKeyConstants.MENU_ROLE_ID_LIST,
                    allEntries = true), // allEntries 清空所有缓存，此处无法方便获得 roleId 对应的 menu 缓存们
            @CacheEvict(value = RedisKeyConstants.USER_ROLE_ID_LIST,
                    allEntries = true) // allEntries 清空所有缓存，此处无法方便获得 roleId 对应的 user 缓存们
    })
    public void processRoleDeleted(Long roleId) {
        // 标记删除 UserRole
        userRoleMapper.deleteListByRoleId(roleId);
        // 标记删除 RoleMenu
        roleMenuMapper.deleteListByRoleId(roleId);
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.MENU_ROLE_ID_LIST, key = "#menuId")
    public void processMenuDeleted(Long menuId) {
        roleMenuMapper.deleteListByMenuId(menuId);
    }

    @Override
    public Set<Long> getRoleMenuListByRoleId(Collection<Long> roleIds) {
        if (CollUtil.isEmpty(roleIds)) {
            return Collections.emptySet();
        }

        // 如果是管理员的情况下，获取全部菜单编号
        if (roleService.hasAnySuperAdmin(roleIds)) {
            return convertSet(menuService.getMenuList(), MenuDO::getId);
        }
        // 如果是非管理员的情况下，获得拥有的菜单编号
        return convertSet(roleMenuMapper.selectListByRoleId(roleIds), RoleMenuDO::getMenuId);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.MENU_ROLE_ID_LIST, key = "#menuId")
    public Set<Long> getMenuRoleIdListByMenuIdFromCache(Long menuId) {
        return convertSet(roleMenuMapper.selectListByMenuId(menuId), RoleMenuDO::getRoleId);
    }

    // ========== 用户-角色的相关方法  ==========

    @Override
    @DSTransactional // 多数据源，使用 @DSTransactional 保证本地事务，以及数据源的切换
    @CacheEvict(value = RedisKeyConstants.USER_ROLE_ID_LIST, key = "#businessId + ':' + #userId" )
    public void incrAssignUserRole(Long userId, Set<Long> roleIds, Long businessId) {
        // 获得角色拥有角色编号
        Set<Long> dbRoleIds = convertSet(userRoleMapper.selectListByUserId(userId, businessId),
                UserRoleDO::getRoleId);
        // 计算新增和删除的角色编号
        Set<Long> roleIdList = CollUtil.emptyIfNull(roleIds);
        Collection<Long> createRoleIds = CollUtil.subtract(roleIdList, dbRoleIds);
        // 执行新增和删除。对于已经授权的角色，不用做任何处理
        if (!CollectionUtil.isEmpty(createRoleIds)) {
            userRoleMapper.insertBatch(CollectionUtils.convertList(createRoleIds, roleId -> {
                UserRoleDO entity = new UserRoleDO();
                entity.setUserId(userId);
                entity.setRoleId(roleId);
                entity.setBusinessId(businessId);
                return entity;
            }));
        }
    }



    @DSTransactional
    @CacheEvict(value = RedisKeyConstants.USER_ROLE_ID_LIST, key = "#businessId + ':' + #userId")
    @Override
    public void assignUserRole(Long userId, Set<Long> roleIds, Long businessId) {
        // 获得角色拥有角色编号
        Set<Long> dbRoleIds = convertSet(userRoleMapper.selectListByUserId(userId, businessId),
            UserRoleDO::getRoleId);
        // 计算新增和删除的角色编号
        Set<Long> roleIdList = CollUtil.emptyIfNull(roleIds);
        Collection<Long> createRoleIds = CollUtil.subtract(roleIdList, dbRoleIds);
        Collection<Long> deleteMenuIds = CollUtil.subtract(dbRoleIds, roleIdList);
        // 执行新增和删除。对于已经授权的角色，不用做任何处理
        if (!CollectionUtil.isEmpty(createRoleIds)) {
            userRoleMapper.insertBatch(CollectionUtils.convertList(createRoleIds, roleId -> {
                UserRoleDO entity = new UserRoleDO();
                entity.setUserId(userId);
                entity.setRoleId(roleId);
                entity.setBusinessId(businessId);
                return entity;
            }));
        }
        if (!CollectionUtil.isEmpty(deleteMenuIds)) {
            userRoleMapper.deleteListByUserIdAndRoleIdIds(userId, deleteMenuIds);
        }
    }



    @Override
    public Set<Long> getUserRoleIdListByUserId(Long userId, Long businessId) {
        return convertSet(userRoleMapper.selectListByUserId(userId, businessId), UserRoleDO::getRoleId);
    }

    @Override
    @Cacheable(
        value = RedisKeyConstants.USER_ROLE_ID_LIST,
        key = "#businessId + ':' + #userId" // 组合 businessId 和 userId
    )
    public Set<Long> getUserRoleIdListByUserIdFromCache(Long userId,Long businessId) {
        return getUserRoleIdListByUserId(userId, businessId);
    }

    @Override
    public Set<Long> getUserRoleIdListByRoleId(Collection<Long> roleIds) {
        return convertSet(userRoleMapper.selectListByRoleIds(roleIds), UserRoleDO::getUserId);
    }

    /**
     * 获得用户拥有的角色，并且这些角色是开启状态的
     *
     * @param userId 用户编号
     * @return 用户拥有的角色
     */
    @VisibleForTesting
    @Override
    @DataPermission(enable = false) // 关闭数据权限，不然就会出现递归获取数据权限的问题
    public List<RoleDO> getEnableUserRoleListByUserIdFromCache(Long userId, Long businessId) {
        // 获得用户拥有的角色编号
        Set<Long> roleIds = getSelf().getUserRoleIdListByUserIdFromCache(userId, businessId);
        // 获得角色数组，并移除被禁用的
        List<RoleDO> roles = roleService.getRoleListFromCache(roleIds);
        roles.removeIf(role -> !CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus()));
        return roles;
    }

    // ========== 用户-部门的相关方法  ==========

    @Override
    public void assignRoleDataScope(Long roleId, Integer dataScope, Set<Long> dataScopeDeptIds) {
        roleService.updateRoleDataScope(roleId, dataScope, dataScopeDeptIds);
    }

    @Override
    @DataPermission(enable = false) // 关闭数据权限，不然就会出现递归获取数据权限的问题
    public DeptDataPermissionRespDTO getDeptDataPermission(Long userId, Long businessId) {

        // 获得用户的角色
        List<RoleDO> roles = getEnableUserRoleListByUserIdFromCache(userId, businessId);

        // 如果角色为空，啥都看不了 抛出异常
        DeptDataPermissionRespDTO result = new DeptDataPermissionRespDTO();
        if (CollUtil.isEmpty(roles)) {
            log.error("用户{}在项目{}下没有分配角色，无法获得数据权限", userId, businessId);
            return result;
        }

        result.setBusinessId(BusinessContextHolder.getBusinessId());
        // 获得用户的组织编号的缓存，通过 Guava 的 Suppliers 惰性求值，即有且仅有第一次发起 DB 的查询
        Supplier<List<Long>> orgIds = Suppliers.memoize(() -> orgService.orgIdByUserId(userId));


        // 遍历每个角色，计算
        for (RoleDO role : roles) {
            // 为空时，跳过
            if (role.getDataScope() == null) {
                continue;
            }
            // 情况一，存在为ALL的角色  不需要查询关联的组织及门店 直接返回
            if (Objects.equals(role.getDataScope(), DataScopeEnum.ALL.getScope())) {
                result.setAll(true);
                return result;
            }
            // 情况二，DEPT_CUSTOM
            if (Objects.equals(role.getDataScope(), DataScopeEnum.ORG_CUSTOM.getScope())) {
                CollUtil.addAll(result.getOrgIds(), role.getDataScopeDeptIds());
                continue;
            }
            // 情况三，DEPT_ONLY
            if (Objects.equals(role.getDataScope(), DataScopeEnum.ORG_ONLY.getScope())) {
                CollUtil.addAll(result.getOrgIds(), orgIds.get());
                continue;
            }
            // 情况四，DEPT_DEPT_AND_CHILD
            if (Objects.equals(role.getDataScope(), DataScopeEnum.ORG_AND_CHILD.getScope())) {
                for (Long orgId : orgIds.get()) {
                    CollUtil.addAll(result.getOrgIds(), orgService.getChildIdList(orgId));
                    // 添加本身部门编号
                    CollectionUtils.addIfNotNull(result.getOrgIds(), orgId);
                }
                continue;
            }
            // 情况五，SELF
            if (Objects.equals(role.getDataScope(), DataScopeEnum.SELF.getScope())) {
                continue;
            }
            // 未知情况，error log 即可
            log.error("[getDeptDataPermission][LoginUser({}) role({}) 无法处理]", userId, toJsonString(result));
        }

        // storeIds
        List<Long> storeIds = systemStoreInfoMapper.selectStoreIdsByOrgIds(result.getOrgIds());
        List<Long> storeIdsByUserId = systemStoreUserMapper.getStoreIdsByUserId(userId);
        result.setStoreIds(new HashSet<>(storeIds));
        result.getStoreIds().addAll(storeIdsByUserId);
        return result;
    }

    @Override
    public void moveUserRole(PermissionMoveUserRoleReqVO updateReqVO) {

        List<Long> userIds = updateReqVO.getUserIds();

        // 获得角色拥有角色编号
        for (Long userId : userIds) {
            Set<Long> dbRoleIds = convertSet(userRoleMapper.selectListByUserId(userId), UserRoleDO::getRoleId);

            if (dbRoleIds.contains(updateReqVO.getCurrentRoleId())){
                throw exception(ROLE_USER_DUPLICATE);
            }
        }


        for (Long userId : userIds) {
            UserRoleDO entity = new UserRoleDO();
            entity.setUserId(userId);
            entity.setRoleId(updateReqVO.getCurrentRoleId());
            userRoleMapper.insert(entity);

            userRoleMapper.deleteListByUserIdAndRoleIdIds(userId, Collections.singleton(updateReqVO.getBeforeRoleId()));
        }

    }

    @Override
    @Transactional
    public void copyRole(PermissionCopyRoleReqVO reqVO) {
        // 获得角色拥有角色编号
        Set<Long> roleMenuListByRoleId = this.getRoleMenuListByRoleId(reqVO.getSourceRoleId());
//        if (CollectionUtils.isAnyEmpty(roleMenuListByRoleId)){
//            throw exception(ROLE_MENU_NOT_EXISTS);
//        }

        RoleDO role = roleService.getRole(reqVO.getSourceRoleId());
        if (role == null){
            throw exception(ROLE_NOT_EXISTS);
        }

        RoleSaveReqVO saveReqVO = new RoleSaveReqVO();
        saveReqVO.setName(reqVO.getName())
            .setRemark(reqVO.getRemark());

        Long roleId = roleService.createRole(saveReqVO, RoleTypeEnum.CUSTOM.getType());

        //设置角色权限
        if (!CollUtil.isEmpty(roleMenuListByRoleId)){
            this.assignRoleMenu(roleId, roleMenuListByRoleId);
        }
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.USER_ROLE_ID_LIST, allEntries = true)
    public void removeUserRole(PermissionRemoveUserRoleReqVO reqVO) {

        RoleDO role = roleService.getRole(reqVO.getRoleId());

        if (Objects.equals(role.getType(), RoleTypeEnum.SYSTEM.getType())){
            List<UserRoleDO> userRoleDOS = userRoleMapper.selectUserListByRoleId(reqVO.getRoleId());
            List<Long> userIds = userRoleDOS.stream().map(UserRoleDO::getUserId).toList();

            // 查询这些用户中有多少是启用的
            List<Long> enableUserIds = businessUserMapper.selectEnableCountUserIds(userIds);

            // 计算要移除的启用用户数量
            long removedEnableCount = reqVO.getUserIds().stream()
                .filter(enableUserIds::contains)
                .count();

            // 如果移除后没有启用的用户剩余，则抛出异常
            if (enableUserIds.size() - removedEnableCount <= 0) {
                throw exception(ROLE_SYSTEM_CONNOT_REMOVE_USER);
            }
        }

        userRoleMapper.deleteListByUserIdsAndRoleIdId(reqVO.getUserIds(), reqVO.getRoleId());
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private PermissionServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
