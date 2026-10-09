package com.htyoudao.youdao.module.system.service.business;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.api.business.dto.BusinessDTO;
import com.htyoudao.youdao.module.system.controller.admin.business.vo.*;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleSimpleRespVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreSimpleResVO;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.user.UserSaveReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.business.BusinessDO;
import com.htyoudao.youdao.module.system.dal.dataobject.businessuser.BusinessUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.DeptDO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.UserDeptDO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.dal.dataobject.orguser.OrgUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.RoleDO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.UserRoleDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreUserDO;
import com.htyoudao.youdao.module.system.dal.mysql.business.BusinessMapper;
import com.htyoudao.youdao.module.system.dal.mysql.businessuser.BusinessUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.dept.DeptMapper;
import com.htyoudao.youdao.module.system.dal.mysql.dept.UserDeptMapper;
import com.htyoudao.youdao.module.system.dal.mysql.org.OrgMapper;
import com.htyoudao.youdao.module.system.dal.mysql.orguser.OrgUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.permission.RoleMapper;
import com.htyoudao.youdao.module.system.dal.mysql.permission.UserRoleMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreUserMapper;
import com.htyoudao.youdao.module.system.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.system.enums.org.OrgUserTypeConstants;
import com.htyoudao.youdao.module.system.enums.permission.DataScopeEnum;
import com.htyoudao.youdao.module.system.enums.permission.RoleCodeEnum;
import com.htyoudao.youdao.module.system.enums.permission.RoleTypeEnum;
import com.htyoudao.youdao.module.system.service.permission.PermissionService;
import com.htyoudao.youdao.module.system.service.permission.RoleService;
import com.htyoudao.youdao.module.system.service.user.AdminUserService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.dromara.hutool.core.data.id.IdUtil;
import org.dromara.hutool.core.data.id.Snowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.system.enums.LogRecordConstants.*;

/**
 * 项目 Service 实现类
 *
 * @author 零零玖零
 */
@Slf4j
@Service
@Validated
public class BusinessServiceImpl implements BusinessService {

    @Resource
    private BusinessMapper businessMapper;

    @Resource
    private BusinessUserMapper businessUserMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    @Resource
    private RoleService roleService;

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private PermissionService permissionService;

    @Resource
    private OrgUserMapper orgUserMapper;

    @Resource
    private SystemStoreInfoMapper systemStoreInfoMapper;

    @Resource
    private OrgMapper orgMapper;

    @Resource
    private AdminUserService userService;

    @Resource
    private DeptMapper deptMapper;

    @Resource
    private UserDeptMapper userDeptMapper;


    // 总部项目默认 id
    public static final Long DEFAULT_BUSINESS_ID = 1L;
    @Autowired
    private SystemStoreUserMapper systemStoreUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_BUSINESS_TYPE, subType = SYSTEM_BUSINESS_CREATE_SUB_TYPE, bizNo = "{{#business.id}}", success = SYSTEM_BUSINESS_CREATE_SUCCESS)
    public Long createBusiness(BusinessSaveReqVO createReqVO) {

        // 自营项目，有效期不设置
        if (Objects.equals(createReqVO.getManageType(), 0)) {
            createReqVO.setValidityEndTime(null);
            createReqVO.setValidityStartTime(null);
        }

        if (createReqVO.getOldId() != null) {
            BusinessDO businessDO = businessMapper.selectById(createReqVO.getOldId());
            if (businessDO != null) {
                throw exception(BUSINESS_ID_IS_EXISTS);
            }
        }

        // 1. 校验角色
        validateBusinessDuplicate(createReqVO.getName(), null);

        // 项目启用时，必须处于有效期内
        validateStatus(createReqVO.getStatus(), createReqVO.getValidityStartTime(), createReqVO.getValidityEndTime());

        // 插入
        BusinessDO business = BeanUtils.toBean(createReqVO, BusinessDO.class);
        business.setId(createReqVO.getOldId());
        business.setCode(UUID.randomUUID().toString());
        business.setMenuIds(createReqVO.getMenuIds().stream().map(String::valueOf).collect(Collectors.joining(",")));
        businessMapper.insert(business);

        // 上下文重新设置为新创建的 businessId
        BusinessContextHolder.setBusinessId(business.getId());

        // 创建项目 admin 角色
        Long roleId = createRole(createReqVO.getMenuIds(), business.getName());
        log.info("创建项目角色：{}", roleId);
        
        // 创建项目 默认创建部门  不需要负责人
        Long deptId = createDept(business.getName());
        log.info("创建项目默认创建部门：{}", deptId);
        createReqVO.setDeptId(deptId);
        // 创建项目负责人
        Long userId = createUser(createReqVO, roleId);
        log.info("创建项目用户:{}", userId);

        // 创建项目 默认父级组织
        Long orgId = createOrg(business.getName(), userId);
        log.info("创建项目默认父级别组织：{}", orgId);

        // 项目重新设置用户 id
        business.setUserId(userId);
        businessMapper.updateById(business);

        LogRecordContext.putVariable("business", business);
        // 返回
        return business.getId();
    }

    private void validateBusinessDuplicate(String name, Long id) {
        // 0. 总部项目，不允许修改
        if (Objects.equals(id, DEFAULT_BUSINESS_ID)) {
            throw exception(BUSINESS_CAN_NOT_UPDATE);
        }
        // 1. 该 name 名字被其它角色所使用
        BusinessDO businessDO = businessMapper.selectByName(name);
        if (businessDO != null && !businessDO.getId().equals(id)) {
            throw exception(BUSINESS_NAME_DUPLICATE, name);
        }
    }

    private Long createOrg(String orgName, Long userId) {
        OrgDO orgDO = new OrgDO();
        orgDO.setName(orgName);
        orgDO.setParentId(0L);
        orgDO.setLevel(1);
        Snowflake snowflake = IdUtil.getSnowflake();
        Long next = snowflake.next();
        orgDO.setAncestors("0," + next);
        orgDO.setId(next);
        orgMapper.insert(orgDO);

        OrgUserDO orgUserDO = new OrgUserDO();
        orgUserDO.setUserId(userId);
        orgUserDO.setOrgId(orgDO.getId());
        orgUserDO.setType(OrgUserTypeConstants.ORG_USER_TYPE_PRINCIPAL);
        orgUserMapper.insert(orgUserDO);
        return next;
    }

    private Long createDept(String deptName) {
        DeptDO deptDO = new DeptDO();
        deptDO.setName(deptName);
        deptDO.setParentId(1L);
        deptDO.setLevel(1);
        Snowflake snowflake = IdUtil.getSnowflake();
        Long next = snowflake.next();
        deptDO.setAncestors("1," + next);
        deptDO.setId(next);
        deptMapper.insert(deptDO);

        return next;
    }

    private Long createUser(BusinessSaveReqVO createReqVO, Long roleId) {
        UserSaveReqVO userSaveReqVO = new UserSaveReqVO();
        userSaveReqVO.setUsername(createReqVO.getUsername());
        userSaveReqVO.setMobile(createReqVO.getMobile());
        userSaveReqVO.setPassword(createReqVO.getPassword());
        userSaveReqVO.setNickname(createReqVO.getNickname());
        userSaveReqVO.setIsProject(0);
        userSaveReqVO.setRoleList(Collections.singletonList(roleId));
        userSaveReqVO.setDeptId(createReqVO.getDeptId());
        return userService.createUser(userSaveReqVO);
    }

    private Long createRole(Set<Long> menuIds, String name) {
        // 创建角色
        RoleDO role = new RoleDO();
        role.setName(RoleCodeEnum.BUSINESS_ADMIN.getName());
        role.setType(RoleTypeEnum.SYSTEM.getType());
        role.setStatus(CommonStatusEnum.ENABLE.getStatus());
        role.setDataScope(DataScopeEnum.ALL.getScope());
        role.setCode(UUID.randomUUID().toString());
        role.setSort(0);
        role.setRemark("系统自动生成");
        roleMapper.insert(role);

        // 分配权限
        permissionService.assignRoleMenu(role.getId(), menuIds);
        return role.getId();
    }

    @Override
    @LogRecord(type = SYSTEM_BUSINESS_TYPE, subType = SYSTEM_BUSINESS_UPDATE_SUB_TYPE, bizNo = "{{#business.id}}", success = SYSTEM_BUSINESS_UPDATE_SUCCESS)
    public void updateBusiness(BusinessSaveReqVO updateReqVO) {
        // 校验存在
        BusinessDO businessDO = businessMapper.selectById(updateReqVO.getId());
        if (businessDO == null) {
            throw exception(BUSINESS_NOT_EXISTS);
        }

        // 自营项目，有效期不设置
        if (Objects.equals(updateReqVO.getManageType(), 0)) {
            updateReqVO.setValidityEndTime(null);
            updateReqVO.setValidityStartTime(null);
        }

        validateBusinessDuplicate(updateReqVO.getName(), updateReqVO.getId());

        // 项目启用时，必须处于有效期内
        validateStatus(updateReqVO.getStatus(), updateReqVO.getValidityStartTime(), updateReqVO.getValidityEndTime());

        String menuIds = updateReqVO.getMenuIds().stream().map(String::valueOf).collect(Collectors.joining(","));

        // 更新
        BusinessDO updateObj = BeanUtils.toBean(updateReqVO, BusinessDO.class);
        updateObj.setMenuIds(menuIds);
        businessMapper.updateById(updateObj);

        // 如果项目权限 有更新
        if (!Objects.equals(menuIds, businessDO.getMenuIds())) {
            List<RoleDO> roleList = roleService.getRoleList(updateReqVO.getId());
            // 重新更新默认管理员角色权限
            roleList.stream().filter(r -> r.getType().equals(RoleTypeEnum.SYSTEM.getType())).findFirst()
                    .ifPresent(r -> {
                        permissionService.assignRoleMenu(r.getId(), updateReqVO.getMenuIds());
                    });

            List<Long> oldList = Arrays.stream(businessDO.getMenuIds().split(",")).map(Long::valueOf).toList();
            // 如果项目权限 减少了 则重新设置项目自定义角色权限 取项目新集合 和 之前自定义权限的 交集
            if (updateReqVO.getMenuIds().size() < oldList.size()) {
                List<RoleDO> list = roleList.stream()
                        .filter(r -> r.getType().equals(RoleTypeEnum.CUSTOM.getType()))
                        .toList();
                for (RoleDO roleDO : list) {
                    Set<Long> oldCustomMenuIds = permissionService.getRoleMenuListByRoleId(roleDO.getId());
                    Collection<Long> newMenuIds = CollectionUtils.intersection(updateReqVO.getMenuIds(), oldCustomMenuIds);
                    permissionService.assignRoleMenu(roleDO.getId(), new HashSet<>(newMenuIds));
                }
            }
        }
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(businessDO, BusinessSaveReqVO.class));
        LogRecordContext.putVariable("business", updateObj);
    }

    private void validateStatus(Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        if (Objects.equals(status, CommonStatusEnum.ENABLE.getStatus())) {
            LocalDateTime now = LocalDateTime.now();

            if (startTime == null || endTime == null) {
                return;
            }

            if (now.isBefore(startTime) || now.isAfter(endTime)) {
                throw exception(BUSINESS_NOT_VALIDITY);
            }
        }
    }

    @Override
    @LogRecord(type = SYSTEM_BUSINESS_TYPE, subType = SYSTEM_BUSINESS_DELETE_SUB_TYPE, bizNo = "{{#business.id}}", success = SYSTEM_BUSINESS_DELETE_SUCCESS)
    public void deleteBusiness(Long id) {

        BusinessDO businessDO = businessMapper.selectById(id);
        if (businessDO == null) {
            throw exception(BUSINESS_NOT_EXISTS);
        }
        // 删除
        businessMapper.deleteById(id);
        LogRecordContext.putVariable("business", businessDO);
    }


    @Override
    public BusinessDO getBusiness(Long id) {
        return businessMapper.selectById(id);
    }

    @Override
    public String getBusinessName(Long id) {
        List<Object> nameList = businessMapper.selectObjs(
                Wrappers.<BusinessDO>lambdaQuery().select(BusinessDO::getName).eq(BusinessDO::getId, id));
        if (CollectionUtils.isNotEmpty(nameList)) {
            return nameList.get(0).toString();
        }
        return "";
    }

    @Override
    public PageResult<BusinessPageRespVO> getBusinessPage(BusinessPageReqVO pageReqVO) {
        PageResult<BusinessDO> businessDOPageResult = businessMapper.selectPage(pageReqVO);
        PageResult<BusinessPageRespVO> bean = BeanUtils.toBean(businessDOPageResult, BusinessPageRespVO.class);
        for (BusinessPageRespVO businessPageRespVO : bean.getList()) {
            businessPageRespVO.setStoreCount(systemStoreInfoMapper.selectCountByBusinessId(businessPageRespVO.getId()));
            businessPageRespVO.setUserCount(userRoleMapper.selectUserCountByBusinessId(businessPageRespVO.getId()));
        }

        return bean;
    }

    @Override
    @LogRecord(type = SYSTEM_BUSINESS_TYPE, subType = SYSTEM_BUSINESS_STATUS_SUB_TYPE, bizNo = "{{#business.id}}", success = SYSTEM_BUSINESS_STATUS_SUCCESS)
    public void updateBusinessStatus(Long id, Integer status) {
        // 0. 总部项目，不允许修改
        if (Objects.equals(id, DEFAULT_BUSINESS_ID)) {
            throw exception(BUSINESS_CAN_NOT_UPDATE);
        }

        // 校验项目存在
        BusinessDO businessDO = businessMapper.selectById(id);

        if (businessDO == null) {
            throw exception(BUSINESS_NOT_EXISTS);
        }

        // 项目启用时，必须处于有效期内
        validateStatus(status, businessDO.getValidityStartTime(), businessDO.getValidityEndTime());

        // 更新状态
        businessDO.setStatus(status);
        businessMapper.updateById(businessDO);
        LogRecordContext.putVariable("business", businessDO);
    }

    /**
     * 根据 用户查询所属项目 及 角色
     */
    @Override
    public BusinessSimpleRespVO getBusinessRoleByUserId(Long userId) {
        // 根据项目查询项目所属角色信息
        BusinessDO businessDO = businessMapper.selectById(BusinessContextHolder.getBusinessId());
        BusinessSimpleRespVO businessSimpleRespVO = new BusinessSimpleRespVO();
        List<String> storeList = new ArrayList<>();
        if (businessDO != null) {
            businessSimpleRespVO = BeanUtils.toBean(businessDO, BusinessSimpleRespVO.class);
        }
// 1. 查询用户关联的组织ID及其所有子组织ID
        List<Long> orgIds = orgUserMapper.selectList(
                        new LambdaQueryWrapperX<OrgUserDO>()
                                .eq(OrgUserDO::getDeleted, 0)
                                .eq(OrgUserDO::getUserId, userId)
                ).stream()
                .map(OrgUserDO::getOrgId)
                .distinct()
                .collect(Collectors.toList());

        Set<Long> orgIdList = new HashSet<>();
        if (CollectionUtils.isNotEmpty(orgIds)) {
            orgIds.forEach(orgId -> {
                orgIdList.addAll(orgMapper.getChildIdList(orgId));
            });
            orgIdList.addAll(orgIds);
        }
        // 拼接组织名称
        if (orgIdList != null && !orgIdList.isEmpty()) {
            List<OrgDO> orgList = orgMapper.selectByIds(orgIdList);
            businessSimpleRespVO.setOrgName(orgList.stream().map(OrgDO::getName).collect(Collectors.joining(",")));
        }
        // 提取公共的查询用户门店ID逻辑
        List<Long> storeIds = systemStoreUserMapper.selectList(new LambdaQueryWrapperX<SystemStoreUserDO>()
                        .eq(SystemStoreUserDO::getUserId, userId)
                        .eq(SystemStoreUserDO::getDeleted, 0)
                        .eq(SystemStoreUserDO::getBusinessId, BusinessContextHolder.getBusinessId()))
                .stream()
                .map(SystemStoreUserDO::getStoreId)
                .distinct()
                .collect(Collectors.toList());
// 2. 拼接组织名称
        if (CollectionUtils.isNotEmpty(orgIdList)) {
            List<OrgDO> orgList = orgMapper.selectByIds(orgIdList);
            businessSimpleRespVO.setOrgName(
                    orgList.stream()
                            .map(OrgDO::getName)
                            .collect(Collectors.joining(","))
            );
        }
// 3. 优化门店数量查询 - 使用子查询合并条件
        Long storeCount = 0L;
        if (CollectionUtils.isNotEmpty(orgIdList) || CollectionUtils.isNotEmpty(storeIds)) {
            LambdaQueryWrapperX<SystemStoreInfoDO> wrapperX = new LambdaQueryWrapperX<>();

            // 基础查询条件
            wrapperX.eq(SystemStoreInfoDO::getDeleted, 0)
                    .eq(SystemStoreInfoDO::getStoreSource, 0)
                    .eq(SystemStoreInfoDO::getStoreStatus, 0);

            // 使用 OR 连接两个条件分支
            wrapperX.and(wq -> {
                // 条件1: 组织及其子组织下的门店
                if (CollectionUtils.isNotEmpty(orgIdList)) {
                    wq.in(SystemStoreInfoDO::getOrgId, orgIdList);
                } else {
                }

                // 条件2: 用户直接关联的门店
                if (CollectionUtils.isNotEmpty(storeIds)) {
                    wq.or().in(SystemStoreInfoDO::getStoreId, storeIds);
                }
            });

            storeCount = systemStoreInfoMapper.selectCount(wrapperX);
            storeList = systemStoreInfoMapper.selectList(wrapperX).stream().map(SystemStoreInfoDO::getStoreName).toList();
        }
        businessSimpleRespVO.setStoreList(storeList);
        businessSimpleRespVO.setStoreCount(storeCount);
        List<Long> roleIds = userRoleMapper.selectList(
                        new LambdaQueryWrapperX<UserRoleDO>().eq(UserRoleDO::getDeleted, 0).eq(UserRoleDO::getUserId, userId)
                                .eq(UserRoleDO::getBusinessId, BusinessContextHolder.getBusinessId()))
                .stream().map(UserRoleDO::getRoleId) // 假设 UserRoleDO 中有 getRoleId 方法
                .distinct().toList();
        if (roleIds != null && !roleIds.isEmpty()) {
            List<RoleDO> roleList = roleService.getRoleList(roleIds);
            if (roleList != null && !roleList.isEmpty()) {
                businessSimpleRespVO.setRoleList(BeanUtils.toBean(roleList, RoleSimpleRespVO.class));
            }
        }
        List<UserDeptDO> userDeptDOs = userDeptMapper.selectList(new LambdaQueryWrapperX<UserDeptDO>()
                .eq(UserDeptDO::getUserId, userId).eq(UserDeptDO::getDeleted, 0)
                .eq(UserDeptDO::getBusinessId, BusinessContextHolder.getBusinessId()));
        if (CollectionUtils.isNotEmpty(userDeptDOs)) {
            UserDeptDO userDeptDO = userDeptDOs.get(0);
            businessSimpleRespVO.setDeptName(deptMapper.selectById(userDeptDO.getDeptId()).getName());
            businessSimpleRespVO.setDeptId(userDeptDO.getDeptId());
            businessSimpleRespVO.setDeptUserType(userDeptDO.getType());
            businessSimpleRespVO.setUserDeptId(userDeptDO.getUserDeptId());
        }
        return businessSimpleRespVO;
    }

    @Override
    public List<BusinessUserRespVO> getUserList(Long loginUserId) {
        List<Long> businessIds = businessUserMapper.enableBusinessIdByUserId(loginUserId);
        // 5.只有一个项目的时候 停用该用户 提示 暂无项目权限，请联系管理员分配角色后再登录
        if (CollUtil.isEmpty(businessIds)) {
            throw exception(ErrorCodeConstants.BUSINESS_USER_NOT_VALIDITY);
        }

        List<BusinessDO> businessDOS = businessMapper.selectByIds(businessIds);

        // 过滤掉禁用项目
        businessDOS.removeIf(b -> Objects.equals(b.getStatus(), CommonStatusEnum.DISABLE.getStatus()));
        // 6.只有一个项目的时候 停用该项目 提示 项目已停用，无法操作
        if (CollUtil.isEmpty(businessDOS)) {
            throw exception(ErrorCodeConstants.BUSINESS_IS_STOP);
        }

        // 过滤掉过期项目
        businessDOS.removeIf(b -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = b.getValidityStartTime();
            LocalDateTime end = b.getValidityEndTime();

            if (start == null || end == null) {
                return false; // 如果时间为空，视为有效
            }

            // 当前时间在[start, end]区间内
            return now.isBefore(start) || now.isAfter(end);
        });
        // 10.只有一个项目的时候 且项目己到期 提示 项目已到期，无法操作
        if (CollUtil.isEmpty(businessDOS)) {
            throw exception(ErrorCodeConstants.BUSINESS_IS_VALIDITY);
        }

        // set role name
        List<BusinessUserRespVO> respVOS = BeanUtils.toBean(businessDOS, BusinessUserRespVO.class);
        for (BusinessUserRespVO respVO : respVOS) {
            Long businessId = respVO.getId();
            List<RoleDO> roleDOS = permissionService.getEnableUserRoleListByUserIdFromCache(
                    loginUserId, businessId);
            if (CollUtil.isEmpty(roleDOS)) {
                respVO.setRoleNames(Collections.emptyList());
            } else {
                respVO.setRoleNames(roleDOS.stream().map(RoleDO::getName).toList());
            }
        }

        // 8.只有一个项目的时候 且在该项目下 没有角色 提示 暂未分配角色，请联系项目管理员
        if (respVOS.size() == 1 && CollUtil.isEmpty(respVOS.get(0).getRoleNames())) {
            throw exception(ErrorCodeConstants.BUSINESS_USER_ROLE_NOT_VALIDITY);
        }

        return respVOS;
    }
    @Override
    public List<BusinessUserRespVO> getUserByBossList(Long loginUserId) {
        List<Long> businessIds = businessUserMapper.enableBusinessIdByUserId(loginUserId);
        // 5.只有一个项目的时候 停用该用户 提示 暂无项目权限，请联系管理员分配角色后再登录
        if (CollUtil.isEmpty(businessIds)) {
            throw exception(ErrorCodeConstants.BUSINESS_USER_NOT_VALIDITY);
        }

        List<BusinessDO> businessDOS = businessMapper.selectByIds(businessIds);

        // 过滤掉禁用项目
        businessDOS.removeIf(b -> Objects.equals(b.getStatus(), CommonStatusEnum.DISABLE.getStatus()));
        // 6.只有一个项目的时候 停用该项目 提示 项目已停用，无法操作
        if (CollUtil.isEmpty(businessDOS)) {
            throw exception(ErrorCodeConstants.BUSINESS_IS_STOP);
        }

        // 过滤掉过期项目
        businessDOS.removeIf(b -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = b.getValidityStartTime();
            LocalDateTime end = b.getValidityEndTime();

            if (start == null || end == null) {
                return false; // 如果时间为空，视为有效
            }

            // 当前时间在[start, end]区间内
            return now.isBefore(start) || now.isAfter(end);
        });
        // 10.只有一个项目的时候 且项目己到期 提示 项目已到期，无法操作
        if (CollUtil.isEmpty(businessDOS)) {
            throw exception(ErrorCodeConstants.BUSINESS_IS_VALIDITY);
        }

        // set role name
        List<BusinessUserRespVO> respVOS = BeanUtils.toBean(businessDOS, BusinessUserRespVO.class);
        for (BusinessUserRespVO respVO : respVOS) {
            Long businessId = respVO.getId();
            List<RoleDO> roleDOS = permissionService.getEnableUserRoleListByUserIdFromCache(
                    loginUserId, businessId);
            if (CollUtil.isEmpty(roleDOS)) {
                respVO.setRoleNames(Collections.emptyList());
            } else {
                respVO.setRoleNames(roleDOS.stream().map(RoleDO::getName).toList());
            }
        }

        // 8.只有一个项目的时候 且在该项目下 没有角色 提示 暂未分配角色，请联系项目管理员
        if (respVOS.size() == 1 && CollUtil.isEmpty(respVOS.get(0).getRoleNames())) {
            throw exception(ErrorCodeConstants.BUSINESS_USER_ROLE_NOT_VALIDITY);
        }

        return respVOS;
    }
    @Override
    public List<BusinessDO> getBusinessList() {
        return businessMapper.selectList(new LambdaQueryWrapperX<BusinessDO>().eq(BusinessDO::getDeleted, 0));
    }

    @Override
    public List<BusinessDO> getBusinessList(Collection<Long> businessIds) {
        return businessMapper.selectList(Wrappers.<BusinessDO>lambdaQuery().in(BusinessDO::getId, businessIds));
    }

    /**
     * 根据 用户查询所属项目 及 角色 List
     */
    @Override
    public List<BusinessSimpleRespVO> getBusinessListRoleByUserId(Long userId) {
        List<BusinessSimpleRespVO> list = new ArrayList<>();
        List<BusinessUserDO> businessUserDOList = businessUserMapper.selectList(userId);
        // 根据项目查询项目所属角色信息
        businessUserDOList.forEach(businessUserDO -> {
            BusinessSimpleRespVO businessSimpleRespVO = new BusinessSimpleRespVO();
            BusinessDO businessDO = businessMapper.selectById(businessUserDO.getBusinessId());
            if (businessDO == null) {
                return;
            }
            businessSimpleRespVO = BeanUtils.toBean(businessDO, BusinessSimpleRespVO.class);
            businessSimpleRespVO.setUserCreateTime(businessUserDO.getCreateTime());
            List<Long> orgIds = orgUserMapper.selectList(
                            new LambdaQueryWrapperX<OrgUserDO>().eq(OrgUserDO::getDeleted, 0)
                                    .eq(OrgUserDO::getBusinessId, businessDO.getId()).eq(OrgUserDO::getUserId, userId)).stream()
                    .map(OrgUserDO::getOrgId) // 假设 UserRoleDO 中有 getRoleId 方法
                    .distinct().toList();
            Set<Long> orgIdList = new HashSet<>();
            if (orgIds != null) {
                orgIds.forEach(orgId -> {
                    orgIdList.addAll(orgMapper.getChildIdList(orgId));
                });
                orgIdList.addAll(orgIds);
            }
            // 拼接组织名称
            if (orgIdList != null && !orgIdList.isEmpty()) {
                List<OrgDO> orgList = orgMapper.selectByIds(orgIdList);
                businessSimpleRespVO.setOrgName(orgList.stream().map(OrgDO::getName).collect(Collectors.joining(",")));
            }
            if (orgIds != null && !orgIds.isEmpty()) {
                if (orgIds != null && !orgIds.isEmpty()) {
                    Long storeCount = systemStoreInfoMapper.selectCount(
                            new LambdaQueryWrapperX<SystemStoreInfoDO>().in(SystemStoreInfoDO::getOrgId, orgIdList)
                                    .eq(SystemStoreInfoDO::getDeleted, 0).eq(SystemStoreInfoDO::getBusinessId, businessDO.getId()).eq(SystemStoreInfoDO::getStoreStatus, 0));
                    businessSimpleRespVO.setStoreCount(storeCount);
                }
            }
            List<Long> roleIds = userRoleMapper.selectList(
                            new LambdaQueryWrapperX<UserRoleDO>().eq(UserRoleDO::getBusinessId, businessDO.getId()).eq(UserRoleDO::getDeleted, 0).eq(UserRoleDO::getUserId, userId))
                    .stream().map(UserRoleDO::getRoleId) // 假设 UserRoleDO 中有 getRoleId 方法
                    .distinct().toList();
            if (roleIds != null && !roleIds.isEmpty()) {
                List<RoleDO> roleList = roleService.getRoleList(roleIds);
                if (roleList != null && !roleList.isEmpty()) {
                    businessSimpleRespVO.setRoleList(BeanUtils.toBean(roleList, RoleSimpleRespVO.class));
                }
            }
            list.add(businessSimpleRespVO);
        });
        return list;
    }

    @Override
    public List<BusinessDTO> listAll() {
        List<BusinessDO> businessDOS = businessMapper.selectList(new LambdaQueryWrapperX<BusinessDO>().eq(BusinessDO::getDeleted, 0));
        List<BusinessDTO> list = BeanCopyUtils.copyBeanList(businessDOS, BusinessDTO.class);
        return list;
    }
    @Override
    public List<BusinessStoreRespVO> listAllStore() {
        // 一次性查询所有未删除的门店
        List<SystemStoreInfoDO> allStores = systemStoreInfoMapper.selectList(
                new LambdaQueryWrapperX<SystemStoreInfoDO>().eq(SystemStoreInfoDO::getDeleted, 0)
                        .eq(SystemStoreInfoDO::getStoreSource, 0)
        );
        List<BusinessStoreRespVO> storeSimpleResVOS =  allStores.stream()
                .filter(store -> !ObjectUtils.isEmpty(store.getBusinessId()) )
                .collect(Collectors.groupingBy(SystemStoreInfoDO::getBusinessId))
                .entrySet().stream()
                .map(entry -> {
                    BusinessStoreRespVO respVO = new BusinessStoreRespVO();
                    respVO.setId(entry.getKey());
                    respVO.setStoreInfoVOList(BeanCopyUtils.copyBeanList(entry.getValue(), StoreSimpleResVO.class));
                    return respVO;
                })
                .collect(Collectors.toList());
        storeSimpleResVOS.forEach(respVO -> {
            BusinessDO business = businessMapper.selectById(respVO.getId());
            if (business != null) {
                respVO.setBusinessName(business.getName());
            }
        });
        return storeSimpleResVOS;
    }

}
