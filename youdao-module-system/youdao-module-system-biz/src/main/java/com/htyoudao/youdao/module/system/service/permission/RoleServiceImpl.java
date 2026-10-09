package com.htyoudao.youdao.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.CollectionUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageRespVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.UserRoleVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RolePageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleRespVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleSaveReqVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleSortVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleUserPageReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.RoleDO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.UserRoleDO;
import com.htyoudao.youdao.module.system.dal.mysql.org.OrgMapper;
import com.htyoudao.youdao.module.system.dal.mysql.permission.RoleMapper;
import com.htyoudao.youdao.module.system.dal.mysql.permission.UserRoleMapper;
import com.htyoudao.youdao.module.system.dal.redis.RedisKeyConstants;
import com.htyoudao.youdao.module.system.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.system.enums.permission.DataScopeEnum;
import com.htyoudao.youdao.module.system.enums.permission.RoleCodeEnum;
import com.htyoudao.youdao.module.system.enums.permission.RoleTypeEnum;
import com.google.common.annotations.VisibleForTesting;
import com.htyoudao.youdao.module.system.service.user.AdminUserService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertMap;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.system.enums.LogRecordConstants.*;

/**
 * 角色 Service 实现类
 *
 * @author 0090
 */
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Resource
    private PermissionService permissionService;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private AdminUserService adminUserService;

    @Resource
    private OrgMapper orgMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_ROLE_TYPE, subType = SYSTEM_ROLE_CREATE_SUB_TYPE, bizNo = "{{#role.id}}",
            success = SYSTEM_ROLE_CREATE_SUCCESS)
    public Long createRole(RoleSaveReqVO createReqVO, Integer type) {
        // 1. 校验角色
        validateRoleDuplicate(createReqVO.getName(), null, null);

        // 2. 插入到数据库
        RoleDO role = BeanUtils.toBean(createReqVO, RoleDO.class)
                .setType(ObjectUtil.defaultIfNull(type, RoleTypeEnum.CUSTOM.getType()))
                .setStatus(ObjUtil.defaultIfNull(createReqVO.getStatus(), CommonStatusEnum.ENABLE.getStatus()))
                .setDataScope(DataScopeEnum.ORG_AND_CHILD.getScope()); // 默认可查看部门及以下数据权限

        role.setCode(UUID.randomUUID().toString());
        role.setSort(roleMapper.getMaxSort() + 1);
        roleMapper.insert(role);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("role", role);
        return role.getId();
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.ROLE, key = "#updateReqVO.id")
    @LogRecord(type = SYSTEM_ROLE_TYPE, subType = SYSTEM_ROLE_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = SYSTEM_ROLE_UPDATE_SUCCESS)
    public void updateRole(RoleSaveReqVO updateReqVO) {
        // 1.1 校验是否可以更新
        RoleDO role = validateRoleForUpdate(updateReqVO.getId());
        // 1.2 校验角色的唯一字段是否重复
        validateRoleDuplicate(updateReqVO.getName(), null, updateReqVO.getId());

        // 2. 更新到数据库
        RoleDO updateObj = BeanUtils.toBean(updateReqVO, RoleDO.class);
        roleMapper.updateById(updateObj);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(role, RoleSaveReqVO.class));
        LogRecordContext.putVariable("role", role);
    }

    @Override
    @CacheEvict(value = RedisKeyConstants.ROLE, key = "#id")
    public void updateRoleDataScope(Long id, Integer dataScope, Set<Long> dataScopeDeptIds) {
        // 校验是否可以更新
        validateRoleForUpdate(id);

        // 更新数据范围
        RoleDO updateObject = new RoleDO();
        updateObject.setId(id);
        updateObject.setDataScope(dataScope);
        updateObject.setDataScopeDeptIds(dataScopeDeptIds);
        roleMapper.updateById(updateObject);
    }



    @Override
    public PageResult<OrgUserPageRespVO> getUserListWithRole(RoleUserPageReqVO pageReqVO) {

        Long checkedOrgId = pageReqVO.getCheckedOrgId();
        // checkedOrgId == 0 查询无组织用户
        if (ObjectUtil.isNotEmpty(checkedOrgId) && ObjectUtil.equals(checkedOrgId, 0L)) {
            OrgUserPageReqVO orgUserPageReqVO = new OrgUserPageReqVO();
            BeanUtils.copyProperties(pageReqVO, orgUserPageReqVO);
            return adminUserService.getUserListNotInOrg(orgUserPageReqVO);
        }

        PageResult<OrgUserPageRespVO> page = adminUserService.getUserListWithRole(pageReqVO);
        List<OrgUserPageRespVO> list = page.getList();
        if (CollectionUtil.isNotEmpty(list)) {
            Set<Long> userIds = list.stream().map(OrgUserPageRespVO::getId).collect(Collectors.toSet());
            List<OrgUserVO> userOrgs = orgMapper.getOrgNameByUserIds(userIds);
            Map<Long, String> userOrgMap = userOrgs.stream()
                .collect(Collectors.toMap(OrgUserVO::getUserId, OrgUserVO::getOrgNames));
            for (OrgUserPageRespVO orgUserPageRespVO : list) {
                Long userId = orgUserPageRespVO.getId();
                orgUserPageRespVO.setOrgNames(userOrgMap.get(userId));
            }
        }
        return page;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = RedisKeyConstants.ROLE, key = "#id")
    @LogRecord(type = SYSTEM_ROLE_TYPE, subType = SYSTEM_ROLE_DELETE_SUB_TYPE, bizNo = "{{#id}}",
            success = SYSTEM_ROLE_DELETE_SUCCESS)
    public void deleteRole(Long id) {
        // 1. 校验是否可以更新
        RoleDO role = validateRoleForUpdate(id);

        List<UserRoleDO> userRoleDOS = userRoleMapper.selectUserListByRoleId(id);
        if (!org.springframework.util.CollectionUtils.isEmpty(userRoleDOS)){
            throw new ServiceException(ROLE_USER_NOT_EMPTY);
        }

        // 2.1 标记删除
        roleMapper.deleteById(id);
        // 2.2 删除相关数据
        permissionService.processRoleDeleted(id);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("role", role);
    }

    /**
     * 校验角色的唯一字段是否重复
     *
     * 1. 是否存在相同名字的角色
     * 2. 是否存在相同编码的角色
     *
     * @param name 角色名字
     * @param code 角色额编码
     * @param id 角色编号
     */
    @VisibleForTesting
    void validateRoleDuplicate(String name, String code, Long id) {
//        // 0. 超级管理员，不允许创建
//        if (RoleCodeEnum.isSuperAdmin(code)) {
//            throw exception(ROLE_ADMIN_CODE_ERROR, code);
//        }
        // 1. 该 name 名字被其它角色所使用
        RoleDO role = roleMapper.selectByName(name);
        if (role != null && !role.getId().equals(id)) {
            throw exception(ROLE_NAME_DUPLICATE, name);
        }
        // 2. 是否存在相同编码的角色
//        if (!StringUtils.hasText(code)) {
//            return;
//        }
        // 该 code 编码被其它角色所使用
//        role = roleMapper.selectByCode(code);
//        if (role != null && !role.getId().equals(id)) {
//            throw exception(ROLE_CODE_DUPLICATE, code);
//        }
    }

    /**
     * 校验角色是否可以被更新
     *
     * @param id 角色编号
     */
    @Override
    public RoleDO validateRoleForUpdate(Long id) {
        RoleDO role = roleMapper.selectById(id);
        if (role == null) {
            throw exception(ROLE_NOT_EXISTS);
        }
        // 内置角色，不允许删除
        if (RoleTypeEnum.SYSTEM.getType().equals(role.getType())) {
            throw exception(ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE);
        }

        return role;
    }

    @Override
    public RoleDO getRole(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    @Cacheable(value = RedisKeyConstants.ROLE, key = "#id",
            unless = "#result == null")
    @DataPermission(enable = false)
    public RoleDO getRoleFromCache(Long id) {
        return roleMapper.selectById(id);
    }


    @Override
    public List<RoleDO> getRoleListByStatus(Collection<Integer> statuses) {
        return roleMapper.selectListByStatus(statuses);
    }

    @Override
    public List<RoleDO> getRoleList() {
        return roleMapper.selectList();
    }

    @Override
    public List<RoleDO> getRoleList(Long businessId) {
        return roleMapper.selectListByBusiness(businessId);
    }

    @Override
    @DataPermission(enable = false)
    public List<RoleDO> getRoleList(Collection<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return roleMapper.selectBatchIds(ids);
    }

    @Override
    public List<RoleDO> getRoleListFromCache(Collection<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        // 这里采用 for 循环从缓存中获取，主要考虑 Spring CacheManager 无法批量操作的问题
        RoleServiceImpl self = getSelf();
        return CollectionUtils.convertList(ids, self::getRoleFromCache);
    }

    @Override
    public PageResult<RoleRespVO> getRolePage(RolePageReqVO reqVO) {
        PageResult<RoleDO> roleDOPageResult = roleMapper.selectPage(reqVO);
        PageResult<RoleRespVO> result = CollectionUtils.convertPage(roleDOPageResult,
            roleDO -> BeanUtils.toBean(roleDO, RoleRespVO.class));

        if (CollectionUtil.isEmpty(result.getList())){
            return result;
        }

        for (RoleRespVO roleRespVO : result.getList()) {
            //设置用户数量
            roleRespVO.setUserCount(userRoleMapper.selectUserCountByRoleId(roleRespVO.getId()));
        }

        return result;
    }

    @Override
    public boolean hasAnySuperAdmin(Collection<Long> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            return false;
        }
        RoleServiceImpl self = getSelf();
        return ids.stream().anyMatch(id -> {
            RoleDO role = self.getRoleFromCache(id);
            return role != null && RoleCodeEnum.isSuperAdmin(role.getCode());
        });
    }

    @Override
    public void validateRoleList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得角色信息
        List<RoleDO> roles = roleMapper.selectBatchIds(ids);
        Map<Long, RoleDO> roleMap = convertMap(roles, RoleDO::getId);
        // 校验
        ids.forEach(id -> {
            RoleDO role = roleMap.get(id);
            if (role == null) {
                throw exception(ROLE_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus())) {
                throw exception(ROLE_IS_DISABLE, role.getName());
            }
        });
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private RoleServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }


    @Override
    public List<UserRoleVO> getRoleNamesByUserIds(Set<Long> userIds) {
        return roleMapper.getRoleNamesByUserIds(userIds);
    }

    @Override
    public List<UserRoleVO> getRoleNamesByUserIdsV2(Set<Long> userIds) {
        Long loginBusinessId = BusinessContextHolder.getBusinessId();
        return roleMapper.getRoleNamesByUserIdsV2(userIds,loginBusinessId);
    }

    @Override
    public void sortRole(List<RoleSortVO> sortReqVOS) {
        for (RoleSortVO sortReqVO : sortReqVOS) {
            LambdaUpdateWrapper<RoleDO> updateWrapper = new LambdaUpdateWrapper();
            updateWrapper.set(RoleDO::getSort, sortReqVO.getSort());
            updateWrapper.eq(RoleDO::getId, sortReqVO.getRoleId());
            roleMapper.update(null, updateWrapper);
        }
    }
}
