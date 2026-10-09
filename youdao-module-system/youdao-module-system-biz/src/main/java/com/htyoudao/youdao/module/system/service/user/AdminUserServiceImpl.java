package com.htyoudao.youdao.module.system.service.user;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.CollectionUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.common.util.validation.ValidationUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.datapermission.core.util.DataPermissionUtils;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.excel.core.service.listenner.GenericExcelListener;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.infra.api.config.ConfigApi;
import com.htyoudao.youdao.module.infra.api.file.FileApi;
import com.htyoudao.youdao.module.system.controller.admin.auth.vo.AuthRegisterReqVO;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.DeptUserOAPageRespVO;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.DeptUserPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.DeptUserPageRespVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgUserPageRespVO;
import com.htyoudao.youdao.module.system.controller.admin.permission.vo.role.RoleUserPageReqVO;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.profile.UserProfileUpdatePasswordReqVO;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import com.htyoudao.youdao.module.system.controller.admin.user.vo.user.*;
import com.htyoudao.youdao.module.system.controller.app.deptorg.vo.UserDeptInfoPageRespVO;
import com.htyoudao.youdao.module.system.controller.app.deptorg.vo.UserDeptInfoReqVO;
import com.htyoudao.youdao.module.system.controller.app.user.vo.SystemUserNavigationVO;
import com.htyoudao.youdao.module.system.controller.app.user.vo.SystemUserPermissionsVO;
import com.htyoudao.youdao.module.system.dal.dataobject.business.BusinessDO;
import com.htyoudao.youdao.module.system.dal.dataobject.businessuser.BusinessUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.DeptDO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.UserDeptDO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.dal.dataobject.orguser.OrgUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.MenuDO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.RoleDO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.RoleMenuDO;
import com.htyoudao.youdao.module.system.dal.dataobject.permission.UserRoleDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.storeuser.StoreUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.htyoudao.youdao.module.system.dal.mysql.business.BusinessMapper;
import com.htyoudao.youdao.module.system.dal.mysql.businessuser.BusinessUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.dept.DeptMapper;
import com.htyoudao.youdao.module.system.dal.mysql.dept.UserDeptMapper;
import com.htyoudao.youdao.module.system.dal.mysql.org.OrgMapper;
import com.htyoudao.youdao.module.system.dal.mysql.orguser.OrgUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.permission.RoleMapper;
import com.htyoudao.youdao.module.system.dal.mysql.permission.RoleMenuMapper;
import com.htyoudao.youdao.module.system.dal.mysql.permission.UserRoleMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.storeuser.StoreUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.user.AdminUserMapper;
import com.htyoudao.youdao.module.system.enums.dept.UserDeptConstants;
import com.htyoudao.youdao.module.system.enums.permission.RoleTypeEnum;
import com.htyoudao.youdao.module.system.service.business.BusinessUserService;
import com.htyoudao.youdao.module.system.service.org.OrgService;
import com.htyoudao.youdao.module.system.service.permission.MenuService;
import com.htyoudao.youdao.module.system.service.permission.PermissionService;
import com.htyoudao.youdao.module.system.service.permission.RoleService;
import com.htyoudao.youdao.module.system.util.user.ImportDataProcessor;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.EXCEL_IMPORT_FILE_FAILED;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertSet;
import static com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.system.enums.LogRecordConstants.*;

/**
 * 后台用户 Service 实现类
 *
 * @author 0090
 */
@Service("adminUserService")
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    static final String USER_INIT_PASSWORD_KEY = "system.user.init-password";

    @Resource
    private AdminUserMapper userMapper;

    //    @Resource
//    private DeptService deptService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @DubboReference
    private ConfigApi configApi;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Resource
    private OrgMapper orgMapper;
    @Resource
    private OrgUserMapper orgUserMapper;
    @Resource
    private ExcelActionService<UserRespVO> userRespVOExcelActionService;
    @Resource
    private RoleService roleService;
    @Resource
    private MenuService menuService;
    @Resource
    private ExcelActionService<UserDeptImportExcel> userDeptImportExcelService;

    @Resource
    private ExcelActionService<UserImportExcelVO> userImportExcelVOExcelActionService;
    @Resource
    private BusinessMapper businessMapper;
    @DubboReference
    private FileApi fileApi;
    @Autowired
    private RoleMapper roleMapper;
    @Resource
    private StoreUserMapper storeUserMapper;
    @Autowired
    private BusinessUserMapper businessUserMapper;
    @Resource
    private SystemStoreInfoMapper systemStoreInfoMapper;
    @Resource
    @Lazy
    private OrgService orgService;
    @Resource
    private SystemStoreUserMapper systemStoreUserMapper;
    @Resource
    private DeptMapper deptMapper;
    @Resource
    private UserDeptMapper userDeptMapper;
    @Resource
    private BusinessUserService businessUserService;

    @Resource
    private Validator validator;

    @Resource
    private IdentifierGenerator identifierGenerator;
    // 外部客户页
    public static final Integer PAGE_TYPE_EXTERNAL = 0;
    // 单0090门店页
    public static final Integer PAGE_TYPE_SINGLE_0090 = 1;
    // 多门店页
    public static final Integer PAGE_TYPE_MULTIPLE = 2;
    // 外部
    public static final Integer PAGE_TYPE_STORE_EXTERNAL = 3;
    //供应链员工 不在组织
    public static final Integer PAGESTORE_EXTERNAL = 4;
    //只有Oa
    //啥都不是
    public static final Integer GO_AWAY = 9;
    // 门店状态：开启
    public static final Integer STORE_OPEN_STATUS = 0;
    // 门店状态：使用中
    public static final Integer STORE_USE_STATUS = 1;
    // 逻辑删除：未删除
    public static final Integer DELETED_NO = 0;
    public static final Long BUSINESS_ID_SUPPLY_CHAIN = 11L;
    public static final Long BUSINESS_ID_OTHER = 10L;

    public static final Integer STORE_TYPE_EXTERNAL = 0;
    // 1=单门店（特指0090门店）
    public static final Integer STORE_TYPE_SINGLE_0090 = 1;
    // 2=多门店
    public static final Integer STORE_TYPE_MULTIPLE = 2;
    // 供应链客户角色ID
    public static final Long ROLE_ID_SUPPLY_CHAIN_CUSTOMER = 1968516364897636354L;
    // 供应链员工角色ID
    public static final Long ROLE_ID_SUPPLY_CHAIN_EMPLOYEE = 1968516419863990274L;
    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_USER_TYPE, subType = SYSTEM_USER_CREATE_SUB_TYPE, bizNo = "{{#user.id}}", success = SYSTEM_USER_CREATE_SUCCESS)
    public Long createUser(UserSaveReqVO createReqVO) {
        // 1.2 校验正确性
        AdminUserDO oldUser = validateUserBusinessExists(createReqVO.getMobile());
        if (createReqVO.getDeptId() == null) {
            DeptDO deptDO = deptMapper.selectOne(new LambdaQueryWrapper<DeptDO>().eq(DeptDO::getLevel, 1).eq(DeptDO::getBusinessId, BusinessContextHolder.getBusinessId()));
            if (deptDO != null) {
                createReqVO.setDeptId(deptDO.getId());
            }
        }
        if (oldUser == null) {
            validateUserForCreateOrUpdate(null, createReqVO.getUsername().trim(), createReqVO.getMobile());
            oldUser = BeanUtils.toBean(createReqVO, AdminUserDO.class);
            oldUser.setStatus(CommonStatusEnum.ENABLE.getStatus()); // 默认开启
            oldUser.setPassword(encodePassword(createReqVO.getPassword())); // 加密密码
            if (createReqVO.getSupplyName() != null) {
                oldUser.setNickname(createReqVO.getSupplyName());
            }
            userMapper.insert(oldUser);
        }
        if (createReqVO.getSupplyName() != null) {
            oldUser.setNickname(createReqVO.getSupplyName());
        }
        if (CollectionUtil.isNotEmpty(createReqVO.getRoleList())) {
            Set<Long> roleIds = new HashSet<>(createReqVO.getRoleList());
            permissionService.assignUserRole(oldUser.getId(), roleIds, BusinessContextHolder.getBusinessId());
        }
        if (StringUtils.isNotBlank(createReqVO.getPassword())) {
            oldUser.setPassword(encodePassword(createReqVO.getPassword()));
            userMapper.updateById(oldUser);
        }
        userMapper.updateById(oldUser);

        // 添加项目关联关系
        List<Long> businessIds = businessUserMapper.selectBusinessList(oldUser.getId());
        if (CollectionUtil.isEmpty(businessIds) || !businessIds.contains(BusinessContextHolder.getBusinessId())) {
            BusinessUserDO businessUserDO = new BusinessUserDO();
            businessUserDO.setBusinessId(BusinessContextHolder.getBusinessId());
            businessUserDO.setUserId(oldUser.getId());
            businessUserDO.setNickname(createReqVO.getNickname());
            businessUserDO.setStatus(createReqVO.getStatus());
            businessUserDO.setBusinessId(BusinessContextHolder.getBusinessId());
            businessUserMapper.insert(businessUserDO);
        }
//        //添加部门关联关系
//        UserDeptDO userDeptDO = userDeptMapper.selectOne(new LambdaQueryWrapper<UserDeptDO>().eq(UserDeptDO::getUserId, oldUser.getId()));
//       if (userDeptDO != null){
//           userDeptDO.setDeptId(createReqVO.getDeptId());
//           userDeptDO.setType(1);
//           userDeptMapper.updateById(userDeptDO);
//       }else {
//           UserDeptDO userDeptDO1 = new UserDeptDO();
//           userDeptDO1.setUserId(oldUser.getId());
//           userDeptDO1.setDeptId(createReqVO.getDeptId());
//           userDeptDO1.setType(1);
//           userDeptMapper.insert(userDeptDO1);
//       }
        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("user", oldUser);
        return oldUser.getId();
    }

    @Override
    public Long registerUser(AuthRegisterReqVO registerReqVO) {
        // 1.2 校验正确性
        validateUserForCreateOrUpdate(null, registerReqVO.getUsername(), null);

        // 2. 插入用户
        AdminUserDO user = BeanUtils.toBean(registerReqVO, AdminUserDO.class);
        user.setStatus(CommonStatusEnum.ENABLE.getStatus()); // 默认开启
        user.setPassword(encodePassword(registerReqVO.getPassword())); // 加密密码
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_USER_TYPE, subType = SYSTEM_USER_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}", success = SYSTEM_USER_UPDATE_SUCCESS)
    public void updateUser(UserSaveReqVO updateReqVO) {
        // 1. 校验正确性
        validateUserForCreateOrUpdate(updateReqVO.getId(), updateReqVO.getUsername().trim(), updateReqVO.getMobile());
        // 2.1 更新用户
        AdminUserDO oldUser = BeanUtils.toBean(updateReqVO, AdminUserDO.class);
        if (updateReqVO.getPassword() != null) {
            oldUser.setPassword(encodePassword(updateReqVO.getPassword()));
        }
        if (updateReqVO.getStatus().equals(CommonStatusEnum.DISABLE.getStatus())) {
            List<UserRoleDO> roleUserDOList = userRoleMapper.selectListByUserId(updateReqVO.getId());
            Set<Long> roleId = roleUserDOList.stream().map(UserRoleDO::getRoleId).collect(Collectors.toSet());
            if (roleId.size() > 0) {
                RoleDO roleDO = roleMapper.selectOne(new LambdaQueryWrapperX<RoleDO>().in(RoleDO::getId, roleId).eq(RoleDO::getType, RoleTypeEnum.SYSTEM.getType()));
                if (roleDO != null) {
                    Long roleCount = userRoleMapper.selectCount(new LambdaQueryWrapperX<UserRoleDO>()
                            .eq(UserRoleDO::getBusinessId, BusinessContextHolder.getBusinessId()).eq(UserRoleDO::getRoleId, roleDO.getId()));
                    if (roleCount <= 1) {
                        throw exception(USER_SYSTEM_DEACTIVATED);
                    }
                }
            }
        }
        //添加部门关联关系
        if (updateReqVO.getDeptId() == null) {
            DeptDO deptDO = deptMapper.selectOne(new LambdaQueryWrapper<DeptDO>().eq(DeptDO::getLevel, 1).eq(DeptDO::getBusinessId, BusinessContextHolder.getBusinessId()));
            if (deptDO != null) {
                updateReqVO.setDeptId(deptDO.getId());
            }
        }
        if (updateReqVO.getSupplyName() != null) {
            oldUser.setNickname(updateReqVO.getSupplyName());
        }
        userMapper.updateById(oldUser);
        if (!updateReqVO.getRoleList().isEmpty()) {
            Set<Long> roleIds = new HashSet<>(updateReqVO.getRoleList());
            permissionService.assignUserRole(oldUser.getId(), roleIds, BusinessContextHolder.getBusinessId());

        }
        if (updateReqVO.getStatus() != null) {
            //同步供应链用户状态
            Long businessId = BusinessContextHolder.getBusinessId();
            if (businessId == 10) {
                userMapper.update(new LambdaUpdateWrapper<AdminUserDO>().set(AdminUserDO::getStatus, updateReqVO.getStatus()).eq(AdminUserDO::getId, updateReqVO.getId()));
            }
            businessUserMapper.update(new LambdaUpdateWrapper<BusinessUserDO>().set(BusinessUserDO::getStatus, updateReqVO.getStatus()).set(updateReqVO.getNickname() != null, BusinessUserDO::getNickname, updateReqVO.getNickname()).eq(BusinessUserDO::getBusinessId, BusinessContextHolder.getBusinessId()).eq(BusinessUserDO::getUserId, updateReqVO.getId()));
        }
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(updateReqVO, UserSaveReqVO.class));
        LogRecordContext.putVariable("user", updateReqVO);
    }


    @Override
    public void updateUserLogin(Long id, String loginIp) {
        userMapper.updateById(new AdminUserDO().setId(id).setLoginIp(loginIp).setLoginDate(LocalDateTime.now()));
    }

    @Override
    public void updateUserProfile(Long id, UserProfileUpdateReqVO reqVO) {
        // 校验正确性
        validateUserExists(id);
        validateEmailUnique(id, reqVO.getEmail());
        validateMobileUnique(id, reqVO.getMobile());
        validateUsernameUnique(id, reqVO.getUsername());

        // 执行更新
        if (reqVO.getNickname() != null) {
            List<BusinessUserDO> businessUserDOList = businessUserMapper.selectList(new LambdaQueryWrapper<BusinessUserDO>().eq(BusinessUserDO::getUserId, id));
            businessUserDOList.forEach(businessUserDO -> {
                businessUserDO.setNickname(reqVO.getNickname());
                businessUserMapper.updateById(businessUserDO);
            });
        }
        userMapper.updateById(BeanUtils.toBean(reqVO, AdminUserDO.class).setId(id));
    }

    @Override
    public void updateUserPassword(Long id, UserProfileUpdatePasswordReqVO reqVO) {
        // 校验旧密码密码
        validateOldPassword(id, reqVO.getOldPassword());
        // 执行更新
        AdminUserDO updateObj = new AdminUserDO().setId(id);
        updateObj.setPassword(encodePassword(reqVO.getNewPassword())); // 加密密码
        userMapper.updateById(updateObj);
    }

    @Override
    public String updateUserAvatar(Long id, InputStream avatarFile) {
        validateUserExists(id);
        // 存储文件
        String avatar = fileApi.createFile(IoUtil.readBytes(avatarFile));
        // 更新路径
        AdminUserDO sysUserDO = new AdminUserDO();
        sysUserDO.setId(id);
        sysUserDO.setAvatar(avatar);
        userMapper.updateById(sysUserDO);
        return avatar;
    }

    @Override
    @LogRecord(type = SYSTEM_USER_TYPE, subType = SYSTEM_USER_UPDATE_PASSWORD_SUB_TYPE, bizNo = "{{#id}}", success = SYSTEM_USER_UPDATE_PASSWORD_SUCCESS)
    public void updateUserPassword(Long id, String password) {
        // 1. 校验用户存在
        AdminUserDO user = validateUserExists(id);

        // 2. 更新密码
        AdminUserDO updateObj = new AdminUserDO();
        updateObj.setId(id);
        updateObj.setPassword(encodePassword(password));
        userMapper.updateById(updateObj);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("user", user);
        LogRecordContext.putVariable("newPassword", updateObj.getPassword());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void registerFixedUserAccount(Long id, String mobile, String password) {
        // 1. 校验用户存在
        validateUserExists(id);
        // 2. 校验手机号是否已注册（包括固定用户自己已绑定该手机号的场景）
        AdminUserDO mobileUser = userMapper.selectByMobile(mobile);
        if (mobileUser != null) {
            throw exception(USER_MOBILE_EXISTS);
        }
        // 3. 校验账号是否已存在（username 也更新为手机号）
        AdminUserDO usernameUser = userMapper.selectByUsername(mobile);
        if (usernameUser != null) {
            throw exception(USER_USERNAME_EXISTS);
        }
        // 4. 执行更新
        AdminUserDO updateObj = new AdminUserDO();
        updateObj.setId(id);
        updateObj.setUsername(mobile);
        updateObj.setMobile(mobile);
        updateObj.setPassword(encodePassword(password));
        userMapper.updateById(updateObj);
    }

    @Override
    public void updateUserStatus(Long id, Integer status) {
        // 校验用户存在
        validateUserExists(id);
        // 更新状态
        List<UserRoleDO> roleUserDOList = userRoleMapper.selectListByUserId(id);
        if (CollectionUtil.isNotEmpty(roleUserDOList)) {
            Set<Long> roleId = roleUserDOList.stream().map(UserRoleDO::getRoleId).collect(Collectors.toSet());
            if (roleId.size() > 0) {
                List<RoleDO> roleDO = roleMapper.selectList(new LambdaQueryWrapperX<RoleDO>().in(RoleDO::getId, roleId).eq(RoleDO::getType, RoleTypeEnum.SYSTEM.getType()));
                if (CollectionUtil.isNotEmpty(roleDO)) {//组织删除
                    Long roleCount = userRoleMapper.selectCount(new LambdaQueryWrapperX<UserRoleDO>()
                            .eq(UserRoleDO::getBusinessId, BusinessContextHolder.getBusinessId()).in(UserRoleDO::getRoleId, roleDO.stream().map(RoleDO::getId).toList()));
                    if (roleCount <= 1) {
                        throw exception(USER_SYSTEM_DEACTIVATED);
                    }
                }
            }
        }
        Long businessId = BusinessContextHolder.getBusinessId();
        //同步供应链用户状态
        if (businessId == 10) {
            List<BusinessUserDO> businessUserDOS = businessUserService.getBusinessUserDOS(businessId, id);
            //当有其他项目存在该用户时，不能禁用，仍然可以登录APP使用OA
            if ((CollectionUtil.isEmpty(businessUserDOS) && Objects.equals(CommonStatusEnum.DISABLE.getStatus(), status))
                    || Objects.equals(CommonStatusEnum.ENABLE.getStatus(), status)) {
                userMapper.update(new LambdaUpdateWrapper<AdminUserDO>().set(AdminUserDO::getStatus, status).eq(AdminUserDO::getId, id));
            }
        }
        businessUserMapper.update(new LambdaUpdateWrapper<BusinessUserDO>().set(BusinessUserDO::getStatus, status).eq(BusinessUserDO::getUserId, id).eq(BusinessUserDO::getBusinessId, BusinessContextHolder.getBusinessId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_USER_TYPE, subType = SYSTEM_USER_DELETE_SUB_TYPE, bizNo = "{{#id}}", success = SYSTEM_USER_DELETE_SUCCESS)
    public void deleteUser(Long id) {
        // 1. 校验用户存在
        AdminUserDO user = validateUserExists(id);
        //删除用户
        delUser(id);
        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("user", user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_USER_TYPE, subType = SYSTEM_USER_DELETE_SUB_TYPE, bizNo = "{{#id}}", success = SYSTEM_USER_DELETE_SUCCESS)
    public void deleteUsers(List<Long> ids) {
        // 2.1 删除用户
        ids.forEach(id -> {
            delUser(id);
        });
        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("users", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_USER_TYPE, subType = SYSTEM_USER_DELETE_SUB_TYPE, bizNo = "停用", success = SYSTEM_USER_DELETE_SUB_TYPE)
    public void deleteDeactivated() {
        // 2.1 删除用户
        List<Long> ids = businessUserMapper.selectList(new LambdaQueryWrapperX<BusinessUserDO>().eq(BusinessUserDO::getStatus, CommonStatusEnum.DISABLE.getStatus()).eq(BusinessUserDO::getBusinessId, BusinessContextHolder.getBusinessId())).stream().map(BusinessUserDO::getUserId).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(ids)) {
            throw exception(USER_NOT_DEACTIVATED);
        }
        // 2.1 删除用户
        ids.forEach(id -> {
            delUser(id);
        });
        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("users", ids);
    }


    @Override
    public AdminUserDO getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public AdminUserDO getUserByMobile(String mobile) {
        return userMapper.selectByMobile(mobile);
    }

    @Override
    public PageResult<UserRespVO> getUserPage(UserPageReqVO reqVO) {
        //查询deptIDs
        Set<Long> orgIds = new HashSet<>();
        if (CollectionUtil.isNotEmpty(reqVO.getOrgList())) {
            orgIds = orgMapper.selectChildByIds(reqVO.getOrgList()).stream().map(OrgDO::getId).collect(Collectors.toSet());
            orgIds.addAll(reqVO.getOrgList());
        }
        Page<UserRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        // 分页查询
        IPage<UserRespVO> pageList = userMapper.selectPageList(page, reqVO, orgIds, BusinessContextHolder.getBusinessId());
        return new PageResult<>(pageList.getRecords(), pageList.getTotal());
    }

    @Override
    public AdminUserDO getUser(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public String getUsername(Long id) {
        List<Object> usernameList = userMapper.selectObjs(
                Wrappers.<AdminUserDO>lambdaQuery().select(AdminUserDO::getUsername).eq(AdminUserDO::getId, id));
        if (CollectionUtil.isNotEmpty(usernameList)) {
            return usernameList.get(0).toString();
        }
        return "";
    }

    @Override
    public List<AdminUserDO> getUserListByDeptIds(Collection<Long> deptIds) {
        if (CollUtil.isEmpty(deptIds)) {
            return Collections.emptyList();
        }
        return userMapper.selectListByDeptIds(deptIds);
    }


    @Override
    public List<AdminUserDO> getUserList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return userMapper.selectBatchIds(ids);
    }

    @Override
    public void exportUserList(UserPageReqVO reqVO, HttpServletRequest request, HttpServletResponse response) throws IOException {

        Set<Long> orgIds = new HashSet<>();
        if (CollectionUtil.isNotEmpty(reqVO.getOrgList())) {
            orgIds = orgMapper.selectChildByIds(reqVO.getOrgList()).stream().map(OrgDO::getId).collect(Collectors.toSet());
            orgIds.addAll(reqVO.getOrgList());
        }
        Page<UserRespVO> page = new Page<>(reqVO.getPageNo(), reqVO.getPageSize());
        //异步分页导出
        //查询所有角色/门店数/组织

        //根据项目查询项目所属角色信息
        Set<Long> orgIdList = orgUserMapper.selectList(
                        new LambdaQueryWrapperX<OrgUserDO>().eq(OrgUserDO::getDeleted, 0)).stream()
                .map(OrgUserDO::getOrgId) // 假设 UserRoleDO 中有 getRoleId 方法
                .collect(Collectors.toSet());
        if (orgIds != null) {
            orgIds.forEach(orgId -> {
                orgIdList.addAll(orgMapper.getChildIdList(orgId));
            });
            orgIdList.addAll(orgIds);
        }
        //拼接组织名称

        Map<Long, String> userRoleNameMap = new HashMap<>();
        Map<Long, String> userOrgNameMap = new HashMap<>();
        List<BusinessUserDO> businessUserDOList = businessUserMapper.selectList(new LambdaQueryWrapperX<BusinessUserDO>().eq(BusinessUserDO::getDeleted, 0)
                .eq(BusinessUserDO::getBusinessId, BusinessContextHolder.getBusinessId()));
        Set<Long> userIds = businessUserDOList.stream().map(BusinessUserDO::getUserId).collect(Collectors.toSet());

        userRoleNameMap = mapUserIdToRoleNames(userIds);
        userOrgNameMap = mapUserIdToOrgNames(userIds);
        Map<Long, Long> storeCountMap = mapUserIdToStoreNum(userIds);
        Set<Long> finalOrgIds = orgIds;
        Map<Long, String> finalUserRoleNameMap = userRoleNameMap;
        Map<Long, String> finalUserOrgNameMap = userOrgNameMap;
        Map<Long, Long> finalStoreCountMap = storeCountMap;
        userRespVOExcelActionService.exportAsyncExcel(UserRespVO.class, page, param -> this.getUserExportList(param, reqVO, finalOrgIds, finalStoreCountMap, finalUserRoleNameMap, finalUserOrgNameMap), "用户列表");

        //同步导出
//        page.setCurrent(-1);
//        List<UserRespVO> userExportList = this.getUserExportList(page, reqVO, orgIds);
//        userRespVOExcelActionService.exportExcel(response, request, "用户列表", userExportList, UserRespVO.class);
    }

    private List<UserRespVO> getUserExportList(Page<UserRespVO> page, UserPageReqVO reqVO, Set<Long> orgIds, Map<Long, Long> storeCountMap, Map<Long, String> userRoleNameMap, Map<Long, String> userOrgNameMap) {
        // 分页查询
        List<UserRespVO> list = userMapper.selectPageList(page, reqVO, orgIds, BusinessContextHolder.getBusinessId()).getRecords();
        list = list.stream()
                .filter(distinctByKey(UserRespVO::getUsername))
                .peek(item -> {
                    String roleName = Optional.ofNullable(userRoleNameMap.get(item.getId())).orElse("");
                    String orgName = Optional.ofNullable(userOrgNameMap.get(item.getId())).orElse("");
                    Long count = storeCountMap.getOrDefault(item.getId(), 0L);
                    item.setRoleName(roleName);
                    item.setOrgName(orgName);
                    item.setStoreCount(count);
                    item.setUserStatusStr(CommonStatusEnum.valueOf(item.getUserStatus()).getName());
                })
                .collect(Collectors.toList());


        return list;
    }

    // 辅助方法，用于根据指定的键进行去重
    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @Override
    public void importUserList(MultipartFile file) throws IOException {
        List<UserImportExcelVO> userImportExcelVOS = userImportExcelVOExcelActionService.importExcel(file.getInputStream(), UserImportExcelVO.class);
        log.info("导入用户数据：{}", JSON.toJSON(userImportExcelVOS));
        List<AdminUserDO> adminUserDOList = BeanUtils.toBean(userImportExcelVOS, AdminUserDO.class);
        adminUserDOList = adminUserDOList.stream()
                .map(importVO -> {
                    AdminUserDO adminUserDO = BeanUtils.toBean(importVO, AdminUserDO.class);
                    adminUserDO.setPassword(encodePassword("abc" + adminUserDO.getMobile())); // 设置默认密码
                    return adminUserDO;
                })
                .collect(Collectors.toList());
        userMapper.insertBatch(adminUserDOList);
        // 添加项目关联关系
        adminUserDOList.forEach(adminUserDO -> {
            BusinessUserDO businessUserDO = new BusinessUserDO();
            businessUserDO.setBusinessId(BusinessContextHolder.getBusinessId());
            businessUserDO.setUserId(adminUserDO.getId());
            businessUserDO.setNickname(adminUserDO.getNickname());
            businessUserDO.setStatus(adminUserDO.getStatus());
            businessUserDO.setBusinessId(BusinessContextHolder.getBusinessId());
            businessUserMapper.insert(businessUserDO);
        });
        //添加用户角色关系
        adminUserDOList.forEach(adminUserDO -> {
            UserRoleDO entity = new UserRoleDO();
            entity.setUserId(adminUserDO.getId());
            entity.setRoleId(1922566714688880642L);
            entity.setBusinessId(BusinessContextHolder.getBusinessId());
            userRoleMapper.insert(entity);
        });
    }

    @Override
    public void validateUserList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得岗位信息
        List<AdminUserDO> users = userMapper.selectBatchIds(ids);
        Map<Long, AdminUserDO> userMap = CollectionUtils.convertMap(users, AdminUserDO::getId);
        // 校验
        ids.forEach(id -> {
            AdminUserDO user = userMap.get(id);
            if (user == null) {
                throw exception(USER_NOT_EXISTS);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(user.getStatus())) {
                throw exception(USER_IS_DISABLE, user.getNickname());
            }
        });
    }

    @Override
    public List<AdminUserDO> getUserListByNickname(String nickname) {
        return userMapper.selectListByNickname(nickname);
    }

    /**
     * 获得部门条件：查询指定部门的子部门编号们，包括自身
     *
     * @param deptId 部门编号
     * @return 部门编号集合
     */
    private Set<Long> getDeptCondition(Long deptId) {
        if (deptId == null) {
            return Collections.emptySet();
        }
        Set<Long> deptIds = new HashSet<>();
        //Set<Long> deptIds = convertSet(deptService.getChildDeptList(deptId), DeptDO::getId);
        deptIds.add(deptId); // 包括自身
        return deptIds;
    }

    private AdminUserDO validateUserForCreateOrUpdate(Long id, String username, String mobile) {
        // 关闭数据权限，避免因为没有数据权限，查询不到数据，进而导致唯一校验不正确
        return DataPermissionUtils.executeIgnore(() -> {
            // 校验用户存在
            AdminUserDO user = validateUserExists(id);
            // 校验用户名唯一
            validateUsernameUnique(id, username);
            // 校验手机号唯一
            validateMobileUnique(id, mobile);
            return user;
        });
    }

    AdminUserDO validateUserExists(Long id) {
        if (id == null) {
            return null;
        }
        AdminUserDO user = userMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        return user;
    }

    @VisibleForTesting
    void validateUsernameUnique(Long id, String username) {
        if (StrUtil.isBlank(username)) {
            return;
        }
        AdminUserDO user = userMapper.selectByUsername(username);
        if (user == null) {
            return;
        }
        if (!user.getId().equals(id)) {
            throw exception(USER_USERNAME_EXISTS);
        }
    }

    @VisibleForTesting
    void validateEmailUnique(Long id, String email) {
        if (StrUtil.isBlank(email)) {
            return;
        }
        AdminUserDO user = userMapper.selectByEmail(email);
        if (user == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的用户
        if (id == null) {
            throw exception(USER_EMAIL_EXISTS);
        }
        if (!user.getId().equals(id)) {
            throw exception(USER_EMAIL_EXISTS);
        }
    }

    @VisibleForTesting
    void validateMobileUnique(Long id, String mobile) {
        if (StrUtil.isBlank(mobile)) {
            return;
        }
        AdminUserDO user = userMapper.selectByMobile(mobile);
        if (user == null) {
            return;
        }
        if (!user.getId().equals(id)) {
            throw exception(USER_MOBILE_EXISTS);
        }
    }

    @Override
    public PageResult<OrgUserPageRespVO> getUserListWithStore(OrgUserPageReqVO pageReqVO) {
        //查询用户列表
        QueryWrapper<AdminUserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bu.deleted", CommonStatusEnum.ENABLE.getStatus());
        queryWrapper.eq("su.deleted", CommonStatusEnum.ENABLE.getStatus());
//        queryWrapper.eq("su.user_type", "00");
        queryWrapper.eq("bu.status", CommonStatusEnum.ENABLE.getStatus());
        if (ObjectUtil.isNotEmpty(pageReqVO.getNickname())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("su.nickname", pageReqVO.getNickname())
                    .or()
                    .like("bu.nickname", pageReqVO.getNickname())
                    .or()
                    .like("su.mobile", pageReqVO.getNickname())
            );
        }
        queryWrapper.orderByDesc("su.create_time");
        //当前门店下的用户
        List<Long> userIds = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(pageReqVO.getStoreId())) {
            QueryWrapper<StoreUserDO> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("store_id", pageReqVO.getStoreId());
            List<StoreUserDO> users = storeUserMapper.selectList(userQueryWrapper);
            if (CollectionUtil.isNotEmpty(users)) {
                userIds = users.stream().map(StoreUserDO::getUserId).toList();
            }
        }

        //选中的组织下的用户
        Long checkedOrgId = pageReqVO.getCheckedOrgId();
        //查询选中组织下用户ids
        List<Long> checkedUserIds = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(checkedOrgId)) {
            Set<Long> childIdList = orgService.getChildIdList(checkedOrgId);
            QueryWrapper<OrgUserDO> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.in("org_id", childIdList);
            List<OrgUserDO> users = orgUserMapper.selectList(userQueryWrapper);
            if (CollectionUtil.isNotEmpty(users)) {
                //选中组织下的用户
                checkedUserIds = users.stream().map(OrgUserDO::getUserId).distinct().toList();
            }
        }

        //拼接查询条件、排除用户
        if (CollectionUtil.isEmpty(checkedUserIds) && ObjectUtil.isNotEmpty(checkedOrgId)) {
            //查询选中的组织下用户为空的情况
            return PageResult.empty();
        } else if (CollectionUtil.isEmpty(checkedUserIds) && ObjectUtil.isEmpty(checkedOrgId)) {
            //没选择组织的情况
            if (CollectionUtil.isNotEmpty(userIds)) {
                queryWrapper.notIn("su.id", userIds);
            }
        } else {
            if (CollectionUtil.isNotEmpty(userIds)) {
                //选中的组织排除当前组织下的用户
                Collection<Long> subtract = CollectionUtil.subtract(checkedUserIds, userIds);
                queryWrapper.in(CollectionUtil.isNotEmpty(subtract), "su.id", subtract);
            } else {
                //当前门店下用户位空
                queryWrapper.in("su.id", checkedUserIds);
            }
        }

        Page<AdminUserDO> page = userMapper.getUserListWithOrgOrStore(new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), queryWrapper);
        PageResult<OrgUserPageRespVO> result = new PageResult<>();
        List<AdminUserDO> records = page.getRecords();
        List<OrgUserPageRespVO> orgUserPageRespVOList = BeanUtils.toBean(records, OrgUserPageRespVO.class);
        result.setList(orgUserPageRespVOList);
        result.setTotal(page.getTotal());
        return result;
    }


    @Override
    public PageResult<OrgUserPageRespVO> getUserListWithRole(RoleUserPageReqVO pageReqVO) {
        //查询用户列表
        QueryWrapper<AdminUserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bu.deleted", CommonStatusEnum.ENABLE.getStatus());
        queryWrapper.eq("su.deleted", CommonStatusEnum.ENABLE.getStatus());
//        queryWrapper.eq("su.user_type", "00");
        queryWrapper.eq("bu.status", CommonStatusEnum.ENABLE.getStatus());
        if (ObjectUtil.isNotEmpty(pageReqVO.getNickname())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("su.nickname", pageReqVO.getNickname())
                    .or()
                    .like("su.username", pageReqVO.getNickname())
            );
        }
        if (StringUtils.isNotEmpty(pageReqVO.getMobile())) {
            queryWrapper.like("su.mobile", pageReqVO.getMobile());
        }
        queryWrapper.orderByDesc("su.create_time");
        //当前角色下的用户
        List<Long> userIds = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(pageReqVO.getRoleId())) {
            //先不过滤当前角色下用户
//            List<UserRoleDO> users = userRoleMapper.selectUserListByRoleId(pageReqVO.getRoleId());
//            if (CollectionUtil.isNotEmpty(users)) {
//                userIds = users.stream().map(UserRoleDO::getUserId).toList();
//            }
        }

        //选中的组织下的用户
        Long checkedOrgId = pageReqVO.getCheckedOrgId();
        //查询选中组织下用户ids
        List<Long> checkedUserIds = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(checkedOrgId)) {
            Set<Long> childIdList = orgService.getChildIdList(checkedOrgId);
            QueryWrapper<OrgUserDO> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.in("org_id", childIdList);
            List<OrgUserDO> users = orgUserMapper.selectList(userQueryWrapper);
            if (CollectionUtil.isNotEmpty(users)) {
                //选中组织下的用户
                checkedUserIds = users.stream().map(OrgUserDO::getUserId).distinct().toList();
            }
        }

        //拼接查询条件、排除用户
        if (CollectionUtil.isEmpty(checkedUserIds) && ObjectUtil.isNotEmpty(checkedOrgId)) {
            //查询选中的组织下用户为空的情况
            return PageResult.empty();
        } else if (CollectionUtil.isEmpty(checkedUserIds) && ObjectUtil.isEmpty(checkedOrgId)) {
            //没选择组织的情况
            if (CollectionUtil.isNotEmpty(userIds)) {
                queryWrapper.notIn("su.id", userIds);
            }
        } else {
            if (CollectionUtil.isNotEmpty(userIds)) {
                //选中的组织排除当前组织下的用户
                Collection<Long> subtract = CollectionUtil.subtract(checkedUserIds, userIds);
                queryWrapper.in(CollectionUtil.isNotEmpty(subtract), "su.id", subtract);
            } else {
                //当前门店下用户位空
                queryWrapper.in("su.id", checkedUserIds);
            }
        }

        Page<AdminUserDO> page = userMapper.getUserListWithOrgOrStore(new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), queryWrapper);
        PageResult<OrgUserPageRespVO> result = new PageResult<>();
        List<AdminUserDO> records = page.getRecords();
        List<OrgUserPageRespVO> orgUserPageRespVOList = BeanUtils.toBean(records, OrgUserPageRespVO.class);
        result.setList(orgUserPageRespVOList);
        result.setTotal(page.getTotal());
        return result;
    }

    /**
     * 校验旧密码
     *
     * @param id          用户 id
     * @param oldPassword 旧密码
     */
    @VisibleForTesting
    void validateOldPassword(Long id, String oldPassword) {
        AdminUserDO user = userMapper.selectById(id);
        if (user == null) {
            throw exception(USER_NOT_EXISTS);
        }
        if (!isPasswordMatch(oldPassword, user.getPassword())) {
            throw exception(USER_OLD_PASSWORD_FAILED);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务，异常则回滚所有导入
    public UserImportRespVO importUserList(List<UserImportExcelVO> importUsers, boolean isUpdateSupport) {
        // 1.1 参数校验
        if (CollUtil.isEmpty(importUsers)) {
            throw exception(USER_IMPORT_LIST_IS_EMPTY);
        }
        // 1.2 初始化密码不能为空
        String initPassword = configApi.getConfigValueByKey(USER_INIT_PASSWORD_KEY).getCheckedData();
        if (StrUtil.isEmpty(initPassword)) {
            throw exception(USER_IMPORT_INIT_PASSWORD);
        }

        // 2. 遍历，逐个创建 or 更新
        UserImportRespVO respVO = UserImportRespVO.builder().createUsernames(new ArrayList<>()).updateUsernames(new ArrayList<>()).failureUsernames(new LinkedHashMap<>()).build();
        importUsers.forEach(importUser -> {
            // 2.1.1 校验字段是否符合要求
            try {
                ValidationUtils.validate(BeanUtils.toBean(importUser, UserSaveReqVO.class).setPassword(initPassword));
            } catch (ConstraintViolationException ex) {
                respVO.getFailureUsernames().put(importUser.getUsername(), ex.getMessage());
                return;
            }
            // 2.1.2 校验，判断是否有不符合的原因
            try {
                validateUserForCreateOrUpdate(null, null, importUser.getMobile());
            } catch (ServiceException ex) {
                respVO.getFailureUsernames().put(importUser.getUsername(), ex.getMessage());
                return;
            }

            // 2.2.1 判断如果不存在，在进行插入
            AdminUserDO existUser = userMapper.selectByUsername(importUser.getUsername());
            if (existUser == null) {
                userMapper.insert(BeanUtils.toBean(importUser, AdminUserDO.class).setPassword(encodePassword(initPassword)).setPostIds(new HashSet<>())); // 设置默认密码及空岗位编号数组
                respVO.getCreateUsernames().add(importUser.getUsername());
                return;
            }
            // 2.2.2 如果存在，判断是否允许更新
            if (!isUpdateSupport) {
                respVO.getFailureUsernames().put(importUser.getUsername(), USER_USERNAME_EXISTS.getMsg());
                return;
            }
            AdminUserDO updateUser = BeanUtils.toBean(importUser, AdminUserDO.class);
            updateUser.setId(existUser.getId());
            userMapper.updateById(updateUser);
            respVO.getUpdateUsernames().add(importUser.getUsername());
        });
        return respVO;
    }

    @Override
    public List<UserSimpleVO> getUserListByStatus(Integer status, String userName) {

        return userMapper.selectUserList(BusinessContextHolder.getBusinessId(), userName);
    }

    @Override
    public boolean isPasswordMatch(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public PageResult<OrgUserPageRespVO> getUserListWithOrg(OrgUserPageReqVO pageReqVO) {
        //查询用户列表
        QueryWrapper<AdminUserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bu.deleted", CommonStatusEnum.ENABLE.getStatus());
        queryWrapper.eq("su.deleted", CommonStatusEnum.ENABLE.getStatus());
//        queryWrapper.eq("su.user_type", "00");
        queryWrapper.eq("bu.status", CommonStatusEnum.ENABLE.getStatus());
        if (ObjectUtil.isNotEmpty(pageReqVO.getNickname())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("su.nickname", pageReqVO.getNickname())
                    .or()
                    .like("bu.nickname", pageReqVO.getNickname())
                    .or()
                    .eq("su.mobile", pageReqVO.getNickname())
            );
        }
        queryWrapper.orderByDesc("su.create_time");
        //选中的组织
        Long checkedOrgId = pageReqVO.getCheckedOrgId();
        //查询选中组织下用户ids
        List<Long> checkedUserIds = new ArrayList<>();
        //查询当前组织下的用户ids
        List<Long> currUserIds = new ArrayList<>();

        //查询选中组织下的用户
        if (ObjectUtil.isNotEmpty(checkedOrgId)) {
            Set<Long> childIdList = orgService.getChildIdList(checkedOrgId);
            QueryWrapper<OrgUserDO> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.in("org_id", childIdList);
            List<OrgUserDO> users = orgUserMapper.selectList(userQueryWrapper);
            if (CollectionUtil.isNotEmpty(users)) {
                //选中组织下的用户
                checkedUserIds = users.stream().map(OrgUserDO::getUserId).distinct().toList();
            }
        }

        //当前组织下的用户
        if (ObjectUtil.isNotEmpty(pageReqVO.getOrgId())) {
            QueryWrapper<OrgUserDO> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("org_id", pageReqVO.getOrgId());
            List<OrgUserDO> users = orgUserMapper.selectList(userQueryWrapper);
            if (CollectionUtil.isNotEmpty(users)) {
                //当前组织下的用户
                currUserIds = users.stream().map(OrgUserDO::getUserId).toList();
            }
        }

        //拼接查询条件、排除用户
        if (CollectionUtil.isEmpty(checkedUserIds) && ObjectUtil.isNotEmpty(checkedOrgId)) {
            //查询选中的组织下用户为空的情况
            return PageResult.empty();
        } else if (CollectionUtil.isEmpty(checkedUserIds) && ObjectUtil.isEmpty(checkedOrgId)) {
            //没选择组织的情况
            if (CollectionUtil.isNotEmpty(currUserIds)) {
                queryWrapper.notIn("su.id", currUserIds);
            }
        } else {
            if (CollectionUtil.isNotEmpty(currUserIds)) {
                //选中的组织排除当前组织下的用户
                Collection<Long> subtract = CollectionUtil.subtract(checkedUserIds, currUserIds);
                queryWrapper.in(CollectionUtil.isNotEmpty(subtract), "su.id", subtract);
            } else {
                //选中的组织下用户为空的情况
                queryWrapper.in("su.id", checkedUserIds);
            }
        }

        //查询用户列表,查完转换成响应对象
        PageResult<OrgUserPageRespVO> result = new PageResult<>();
        Page<AdminUserDO> page = userMapper.getUserListWithOrgOrStore(new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), queryWrapper);
        //Page<AdminUserDO> page = userMapper.selectPage(new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), queryWrapper);
        List<AdminUserDO> records = page.getRecords();
        List<OrgUserPageRespVO> orgUserPageRespVOList = BeanUtils.toBean(records, OrgUserPageRespVO.class);
        result.setList(orgUserPageRespVOList);
        result.setTotal(page.getTotal());
        return result;
    }


    @Override
    public PageResult<DeptUserPageRespVO> getUserListWithDept(DeptUserPageReqVO reqVO, List<Long> deptUserIds) {
        Long loginBusinessId = BusinessContextHolder.getBusinessId();
        QueryWrapper<AdminUserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bu.deleted", CommonStatusEnum.ENABLE.getStatus());
        queryWrapper.eq("su.deleted", CommonStatusEnum.ENABLE.getStatus());
        queryWrapper.eq("bu.business_id", loginBusinessId);
        queryWrapper.notIn(CollectionUtil.isNotEmpty(deptUserIds), "su.id", deptUserIds);
        if (ObjectUtil.isNotEmpty(reqVO.getNickname())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("su.nickname", reqVO.getNickname())
                    .or()
                    .like("bu.nickname", reqVO.getNickname())
                    .or()
                    .eq("su.mobile", reqVO.getNickname())
            );
        }
        queryWrapper.eq(ObjectUtil.isNotEmpty(reqVO.getMobile()), "su.mobile", reqVO.getMobile());
        queryWrapper.orderByDesc("su.create_time");
        Page<AdminUserDO> page = userMapper.getUserListWithOrgOrStore(new Page<>(reqVO.getPageNo(), reqVO.getPageSize()), queryWrapper);

        PageResult<DeptUserPageRespVO> result = new PageResult<>();
        List<AdminUserDO> records = page.getRecords();
        List<DeptUserPageRespVO> bean = BeanUtils.toBean(records, DeptUserPageRespVO.class);
        result.setList(bean);
        result.setTotal(page.getTotal());
        return result;
    }

    @Override
    public PageResult<UserDeptInfoPageRespVO> getUserDeptInfoPage(UserDeptInfoReqVO userDeptInfoReqVO) {
        LambdaQueryWrapper<AdminUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdminUserDO::getStatus, CommonStatusEnum.ENABLE.getStatus());
        if (com.htyoudao.youdao.framework.common.util.string.StringUtils.isNotEmpty(userDeptInfoReqVO.getNameOrMobile())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(AdminUserDO::getNickname, userDeptInfoReqVO.getNameOrMobile())
                    .or()
                    .like(AdminUserDO::getMobile, userDeptInfoReqVO.getNameOrMobile())
            );
        }
        Page<AdminUserDO> page = userMapper.selectPage(new Page<>(userDeptInfoReqVO.getPageNo(), userDeptInfoReqVO.getPageSize()), queryWrapper);
        List<AdminUserDO> records = page.getRecords();
        List<UserDeptInfoPageRespVO> bean = BeanUtils.toBean(records, UserDeptInfoPageRespVO.class);
        PageResult<UserDeptInfoPageRespVO> result = new PageResult<>();
        result.setList(bean);
        result.setTotal(page.getTotal());
        return result;
    }

    @Override
    public List<AdminUserDO> getUserListByDeptId(Long deptId, String nameOrMobile) {
        return userMapper.getUserListByDeptId(deptId, nameOrMobile);
    }

    @Override
    public UserRespVO checkMobile(String mobile) {
        UserRespVO userRespVO = new UserRespVO();
        validateUserBusinessExists(mobile);
        AdminUserDO user = userMapper.selectByMobile(mobile);
        if (user != null) {
            userRespVO = BeanUtils.toBean(user, UserRespVO.class);
            List<BusinessUserDO> businessUserDOs = businessUserMapper.selectList(new LambdaQueryWrapperX<BusinessUserDO>().eq(BusinessUserDO::getUserId, user.getId()));
            if (businessUserDOs != null && CollectionUtil.isNotEmpty(businessUserDOs)) {
                List<BusinessDO> businessDOs = businessMapper.selectBatchIds(businessUserDOs.stream().map(BusinessUserDO::getBusinessId).toList());
                userRespVO.setBusinessNames(businessDOs.stream()
                        .map(BusinessDO::getName)
                        .collect(Collectors.joining(",")));
            }
            return userRespVO;
        }
        return null;
    }

    /**
     * 根据手机号回参
     */
    @Override
    public UserRespVO getCheckPhoneByBusiness(String mobile) {
        UserRespVO userRespVO;
        AdminUserDO user = userMapper.selectByMobile(mobile);
        if (user != null) {
            userRespVO = BeanUtils.toBean(user, UserRespVO.class);
            List<BusinessUserDO> businessUserDOs = businessUserMapper.selectList(user.getId());
            if (businessUserDOs != null && CollectionUtil.isNotEmpty(businessUserDOs)) {
                List<Long> businessIds = businessUserDOs.stream().map(BusinessUserDO::getBusinessId).toList();
                List<BusinessDO> businessDOs = businessMapper.selectList(new LambdaQueryWrapperX<BusinessDO>()
                        .in(BusinessDO::getId, businessIds)
                        .eq(BusinessDO::getDeleted, CommonStatusEnum.ENABLE.getStatus())); // 只查询未删除的数据
                userRespVO.setBusinessNames(businessDOs.stream()
                        .map(BusinessDO::getName)
                        .collect(Collectors.joining(",")));
            }
            return userRespVO;
        }
        return null;
    }

    @Override
    public UserRespVO getUserDetail(Long id) {
        AdminUserDO user = userMapper.selectById(id);
        UserRespVO userRespVO = BeanUtils.toBean(user, UserRespVO.class);
        userRespVO.setSupplyName(user.getNickname());
        BusinessUserDO businessUserDO = businessUserMapper.selectOne(new LambdaQueryWrapperX<BusinessUserDO>().eq(BusinessUserDO::getBusinessId, BusinessContextHolder.getBusinessId()).eq(BusinessUserDO::getUserId, id));
        if (businessUserDO != null) {
            userRespVO.setUserStatus(businessUserDO.getStatus());
            if (businessUserDO.getBusinessId() != null) {
                userRespVO.setUserNickname(businessUserDO.getNickname());
            }
        }
        UserDeptDO userDeptDO = userDeptMapper.selectOne(new LambdaQueryWrapperX<UserDeptDO>().eq(UserDeptDO::getBusinessId, BusinessContextHolder.getBusinessId()).eq(UserDeptDO::getUserId, id));
        if (userDeptDO != null) {
            DeptDO deptDO = deptMapper.selectByDeptId(userDeptDO.getDeptId());
            userRespVO.setDeptId(userDeptDO.getDeptId());
            userRespVO.setDeptName(deptDO.getName());
        }
        return userRespVO;
    }

    /**
     * 对密码进行加密
     *
     * @param password 密码
     * @return 加密后的密码
     */
    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 删除用户通用
     */
    public void delUser(Long id) {
        //获取用户所用项目角色关系
        List<BusinessUserDO> businessUserDOList = businessUserMapper.selectList(id);
        BusinessDO businessDO = businessMapper.selectOne(new LambdaQueryWrapperX<BusinessDO>().eq(BusinessDO::getId, BusinessContextHolder.getBusinessId()).eq(BusinessDO::getUserId, id));
        if (businessDO != null) {
            throw exception(USER_SYSTEM_DELSYSTEM);
        }
        if (businessUserDOList.size() <= 1) {
            // 2.1 删除用户
            userMapper.deleteById(id);
        }
        // 2.2 删除用户关联数据
        businessUserMapper.delete(new LambdaUpdateWrapper<BusinessUserDO>().eq(BusinessUserDO::getUserId, id).eq(BusinessUserDO::getBusinessId, BusinessContextHolder.getBusinessId()));
        userRoleMapper.delete(new LambdaUpdateWrapper<UserRoleDO>().eq(UserRoleDO::getUserId, id).eq(UserRoleDO::getBusinessId, BusinessContextHolder.getBusinessId()));
        orgUserMapper.delete(new LambdaUpdateWrapper<OrgUserDO>().eq(OrgUserDO::getUserId, id).eq(OrgUserDO::getBusinessId, BusinessContextHolder.getBusinessId()));
        storeUserMapper.delete(new LambdaUpdateWrapper<StoreUserDO>().eq(StoreUserDO::getUserId, id).eq(StoreUserDO::getBusinessId, BusinessContextHolder.getBusinessId()));
        List<SystemStoreInfoDO> list = systemStoreInfoMapper.selectList(new LambdaQueryWrapperX<SystemStoreInfoDO>()
                .eq(SystemStoreInfoDO::getUserId, id).eq(SystemStoreInfoDO::getBusinessId, BusinessContextHolder.getBusinessId()));
        if (CollectionUtil.isNotEmpty(list)) {
            list.forEach(item -> {
                LambdaUpdateWrapper<SystemStoreInfoDO> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(SystemStoreInfoDO::getStoreId, item.getStoreId())
                        .set(SystemStoreInfoDO::getStoreLeaderPhone, null)
                        .set(SystemStoreInfoDO::getStoreLeader, null)
                        .set(SystemStoreInfoDO::getUserId, null);
                systemStoreInfoMapper.update(null, updateWrapper);
            });
        }
    }

    /**
     * 校验用户是否绑定该项目关系
     *
     * @return
     */
    AdminUserDO validateUserBusinessExists(String mobile) {
        AdminUserDO user = userMapper.selectByMobile(mobile);
        if (user == null) {
            return null;
        }

        BusinessUserDO businessUserDO = businessUserMapper.selectOne(new LambdaQueryWrapperX<BusinessUserDO>().eq(BusinessUserDO::getBusinessId, BusinessContextHolder.getBusinessId()).eq(BusinessUserDO::getUserId, user.getId()));
        if (businessUserDO != null) {
            throw exception(USER_BUSINESS_USER_EXISTS);
        }
        return user;
    }

    @Override
    public PageResult<OrgUserPageRespVO> getUserListNotInOrg(OrgUserPageReqVO pageReqVO) {
        Page<AdminUserDO> page = userMapper.getUserListNotInOrg(new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize()), pageReqVO);
        // 封装结果
        PageResult<OrgUserPageRespVO> result = new PageResult<>();
        List<AdminUserDO> records = page.getRecords();
        List<OrgUserPageRespVO> orgUserPageRespVOList = BeanUtils.toBean(records, OrgUserPageRespVO.class);
        result.setList(orgUserPageRespVOList);
        result.setTotal(page.getTotal());
        return result;
    }

    @Override
    public List<AdminUserDO> getRegionalManagerUserList() {


        LambdaQueryWrapper<RoleDO> roleDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roleDOLambdaQueryWrapper.eq(RoleDO::getName, "区域经理");

        List<RoleDO> roleDOS = roleMapper.selectList(roleDOLambdaQueryWrapper);
        if (roleDOS.size() > 0) {
            List<Long> collect = roleDOS.stream().map(rr -> rr.getId()).collect(Collectors.toList());
            LambdaQueryWrapper<UserRoleDO> userRoleDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userRoleDOLambdaQueryWrapper.in(UserRoleDO::getRoleId, collect);
            List<UserRoleDO> userRoleDOS = userRoleMapper.selectList(userRoleDOLambdaQueryWrapper);
            if (userRoleDOS.size() > 0) {
                List<Long> longList = userRoleDOS.stream().map(uu -> uu.getUserId()).collect(Collectors.toList());
                if (longList.size() > 0) {
                    LambdaQueryWrapper<AdminUserDO> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.in(AdminUserDO::getId, longList);
                    List<AdminUserDO> adminUserDOS = userMapper.selectList(queryWrapper);
                    return adminUserDOS;
                }
            }
        }
        return new ArrayList<>();
    }

    public Map<Long, String> mapUserIdToOrgNames(Collection<Long> userIds) {
        Map<Long, String> userOrgNameMap = new HashMap<>();

        if (CollectionUtil.isEmpty(userIds)) {
            return userOrgNameMap;
        }

        // 1. 查询用户与组织的关联关系
        List<OrgUserDO> orgUserList = orgUserMapper.selectList(
                new LambdaQueryWrapperX<OrgUserDO>().in(OrgUserDO::getUserId, userIds)
        );

        if (orgUserList.isEmpty()) {
            return userOrgNameMap;
        }

        // 2. 提取所有相关的组织ID
        Set<Long> orgIds = orgUserList.stream()
                .map(OrgUserDO::getOrgId)
                .collect(Collectors.toSet());

        // 3. 查询组织信息
        List<OrgDO> orgList = orgMapper.selectByIds(orgIds);

        if (orgList.isEmpty()) {
            return userOrgNameMap;
        }

        // 4. 构建组织ID到组织名称的映射
        Map<Long, String> orgIdNameMap = orgList.stream()
                .collect(Collectors.toMap(OrgDO::getId, OrgDO::getName));

        // 5. 按用户ID分组，拼接组织名称
        Map<Long, String> userOrgNames = orgUserList.stream()
                .filter(uo -> orgIdNameMap.containsKey(uo.getOrgId())) // 确保组织存在
                .collect(
                        Collectors.groupingBy(
                                OrgUserDO::getUserId,
                                Collectors.mapping(uo -> orgIdNameMap.get(uo.getOrgId()), Collectors.joining("、"))
                        )
                );

        // 6. 赋值到结果Map（确保所有传入userId都有默认值）
        for (Long userId : userIds) {
            userOrgNameMap.put(userId, userOrgNames.getOrDefault(userId, ""));
        }

        return userOrgNameMap;

    }

    public Map<Long, String> mapUserIdToRoleNames(Collection<Long> userIds) {
        Map<Long, String> userRoleNameMap = new HashMap<>();

        if (userIds == null || userIds.isEmpty()) {
            return userRoleNameMap;
        }

        // 查询用户与角色的关联关系
        List<UserRoleDO> userRoleList = userRoleMapper.selectList(
                new LambdaQueryWrapperX<UserRoleDO>().in(UserRoleDO::getUserId, userIds)
        );

        if (userRoleList.isEmpty()) {
            return userRoleNameMap;
        }

        // 提取所有相关的角色ID
        Set<Long> roleIds = userRoleList.stream()
                .map(UserRoleDO::getRoleId)
                .collect(Collectors.toSet());

        // 查询角色信息
        List<RoleDO> roleList = roleMapper.selectList(
                new LambdaQueryWrapperX<RoleDO>().in(RoleDO::getId, roleIds)
        );

        // 构建角色ID到角色名称的映射
        Map<Long, String> roleIdNameMap = roleList.stream()
                .collect(Collectors.toMap(RoleDO::getId, RoleDO::getName));

        // 按用户ID分组，拼接角色名称
        Map<Long, String> userRoles = userRoleList.stream()
                .filter(ur -> roleIdNameMap.containsKey(ur.getRoleId())) // 确保角色存在
                .collect(
                        Collectors.groupingBy(
                                UserRoleDO::getUserId,
                                Collectors.mapping(ur -> roleIdNameMap.get(ur.getRoleId()), Collectors.joining(", "))
                        )
                );

        // 赋值到结果Map
        for (Long userId : userIds) {
            userRoleNameMap.put(userId, userRoles.getOrDefault(userId, ""));
        }

        return userRoleNameMap;
    }

    public Map<Long, Long> mapUserIdToStoreNum(Collection<Long> userIds) {
        Map<Long, Long> userStoreNumMap = new HashMap<>();


        if (userIds == null || userIds.isEmpty()) {
            return userStoreNumMap;
        }


        // 确保所有传入 userId 都有默认值（0）
        for (Long userId : userIds) {
            Long storeCount = 0L;
            List<Long> orgIds = orgUserMapper.selectList(
                            new LambdaQueryWrapperX<OrgUserDO>().eq(OrgUserDO::getDeleted, 0).eq(OrgUserDO::getUserId, userId)).stream()
                    .map(OrgUserDO::getOrgId) // 假设 UserRoleDO 中有 getRoleId 方法
                    .distinct().toList();
            Set<Long> orgIdList = new HashSet<>();
            if (orgIds != null) {
                orgIds.forEach(orgId -> {
                    orgIdList.addAll(orgMapper.getChildIdList(orgId));
                });
                orgIdList.addAll(orgIds);
            }
            if (orgIds != null && !orgIds.isEmpty()) {
                if (orgIds != null && !orgIds.isEmpty()) {
                    List<Long> storeIds = systemStoreUserMapper.selectList(new LambdaQueryWrapperX<SystemStoreUserDO>()
                            .eq(SystemStoreUserDO::getUserId, userId).eq(SystemStoreUserDO::getDeleted, 0).eq(SystemStoreUserDO::getBusinessId, BusinessContextHolder.getBusinessId())).stream().map(SystemStoreUserDO::getStoreId).distinct().toList();
                    LambdaQueryWrapperX<SystemStoreInfoDO> wrapperX = new LambdaQueryWrapperX<SystemStoreInfoDO>();
                    wrapperX.in(SystemStoreInfoDO::getOrgId, orgIdList);
                    if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(storeIds) && storeIds.size() > 0) {
                        wrapperX.or()
                                .in(SystemStoreInfoDO::getStoreId, storeIds);
                    }
                    wrapperX.eq(SystemStoreInfoDO::getDeleted, 0);
                    wrapperX.eq(SystemStoreInfoDO::getStoreSource, 0);
                    wrapperX.eq(SystemStoreInfoDO::getStoreStatus, 0);
                    storeCount = systemStoreInfoMapper.selectCount(
                            wrapperX
                    );
                }
            } else {
                List<Long> storeIds = systemStoreUserMapper.selectList(new LambdaQueryWrapperX<SystemStoreUserDO>()
                        .eq(SystemStoreUserDO::getUserId, userId).eq(SystemStoreUserDO::getDeleted, 0).eq(SystemStoreUserDO::getBusinessId, BusinessContextHolder.getBusinessId())).stream().map(SystemStoreUserDO::getStoreId).distinct().toList();
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(storeIds) && storeIds.size() > 0) {
                    LambdaQueryWrapperX<SystemStoreInfoDO> wrapperX = new LambdaQueryWrapperX<SystemStoreInfoDO>();
                    wrapperX.or()
                            .in(SystemStoreInfoDO::getStoreId, storeIds);
                    wrapperX.eq(SystemStoreInfoDO::getDeleted, 0);
                    wrapperX.eq(SystemStoreInfoDO::getStoreSource, 0);
                    wrapperX.eq(SystemStoreInfoDO::getStoreStatus, 0);
                    storeCount = systemStoreInfoMapper.selectCount(
                            wrapperX
                    );
                }
            }
            userStoreNumMap.put(userId, storeCount);
        }

        return userStoreNumMap;
    }

    @Override
    public CommonResult<Boolean> isStoreLeader(Long userId) {
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = new LambdaQueryWrapper<SystemStoreInfoDO>();
        queryWrapper.eq(SystemStoreInfoDO::getUserId, userId);
        queryWrapper.eq(SystemStoreInfoDO::getStoreStatus, 0);
        return CommonResult.success(systemStoreInfoMapper.selectCount(queryWrapper) > 0);
    }

//    @Override
//    public Boolean importUserDept(MultipartFile file) {
//        LambdaQueryWrapper<AdminUserDO> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.select(AdminUserDO::getMobile, AdminUserDO::getId);
//        List<AdminUserDO> userList = userMapper.selectList(queryWrapper);
//        Map<String, Long> mobileIdMap = userList.stream()
//                .collect(Collectors.toMap(
//                        AdminUserDO::getMobile,
//                        AdminUserDO::getId
//                ));
//        Set<String> mobileSet = userList.stream().map(AdminUserDO::getMobile).collect(Collectors.toSet());
//        List<UserDeptDO> userDeptList = new ArrayList<>();
//        UserDeptDO userDeptDO = new UserDeptDO();
//        String mobile = null;
//        List<AdminUserDO> insertUsers = new ArrayList<>();
//        AdminUserDO adminUserDO = new AdminUserDO();
//
//        List<BusinessUserDO> insertBusinessUsers = new ArrayList<>();
//        BusinessUserDO businessUserDO = new BusinessUserDO();
//
//        LambdaQueryWrapper<DeptDO> queryWrapper1 = new LambdaQueryWrapper<>();
//        queryWrapper1.select(DeptDO::getId, DeptDO::getName);
//        queryWrapper1.ne(DeptDO::getLevel, 1);
//        List<DeptDO> deptList = deptMapper.selectList(queryWrapper1);
//        Map<String, Long> deptIdMap = deptList.stream()
//                .collect(Collectors.toMap(
//                        DeptDO::getName,
//                        DeptDO::getId
//                ));
//
//        List<UserDeptImportExcel> userDeptImportExcels = readExcelNew(file, UserDeptImportExcel.class);
//        for (UserDeptImportExcel userDeptImportExcel : userDeptImportExcels) {
//            userDeptDO = new UserDeptDO();
//            mobile = userDeptImportExcel.getMobile();
//            String deptName = userDeptImportExcel.getDeptName();
//            String newDeptName = ImportDataProcessor.processDepartmentField(deptName);
//            if(mobileSet.contains(mobile)){
//                Long userId = mobileIdMap.get(mobile);
//                userDeptDO.setUserId(userId);
//                userDeptDO.setDeptId(deptIdMap.get(newDeptName));
//                userDeptDO.setType(UserDeptConstants.USER_DEPT_TYPE_1);
//                userDeptList.add(userDeptDO);
//            }else {
//                adminUserDO = new AdminUserDO();
//                long userId = identifierGenerator.nextId(null).longValue();
//                adminUserDO.setMobile(mobile);
//                adminUserDO.setNickname(userDeptImportExcel.getName());
//                adminUserDO.setUsername(mobile);
//                adminUserDO.setId(userId);
//                adminUserDO.setPassword(passwordEncoder.encode(mobile));
//
//                businessUserDO = new BusinessUserDO();
//                businessUserDO.setUserId(userId);
//                businessUserDO.setBusinessId(BusinessContextHolder.getBusinessId());
//                businessUserDO.setNickname(userDeptImportExcel.getName());
//                insertBusinessUsers.add(businessUserDO);
//
//                userDeptDO.setUserId(userId);
//                userDeptDO.setDeptId(deptIdMap.get(newDeptName));
//                userDeptDO.setType(UserDeptConstants.USER_DEPT_TYPE_1);
//                userDeptList.add(userDeptDO);
//            }
//        }
//
//        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(userDeptList)){
//            userDeptMapper.insertBatch(userDeptList);
//        }
//        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(insertBusinessUsers)){
//            businessUserMapper.insertBatch(insertBusinessUsers);
//        }
//        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(insertUsers)){
//            userMapper.insertBatch(insertUsers);
//        }
//        return Boolean.TRUE;
//    }


    @Override
    public Boolean importUserDeptV2(MultipartFile file) {
        // 1. 准备数据映射：获取所有现有用户的手机号与ID映射
        Map<String, Long> mobileIdMap = prepareUserMobileIdMap();
        Set<String> existingMobiles = mobileIdMap.keySet();

        // 2. 准备部门映射：获取所有部门名称与ID映射（排除一级部门）
        Map<String, Long> deptIdMap = prepareDeptNameIdMap();

        List<UserDeptImportExcel> importDataList = new ArrayList<>();
        try {
            // 3. 读取Excel文件数据
            importDataList =
                    userDeptImportExcelService.importExcel(file.getInputStream(), UserDeptImportExcel.class);
        } catch (Exception e) {

        }


        // 4. 处理导入数据
        ImportProcessResult processResult = processImportData(
                importDataList, existingMobiles, mobileIdMap, deptIdMap);

        // 5. 批量插入数据
        batchInsertData(processResult);

        return Boolean.TRUE;
    }

    @Override
    @DataPermission(enable = false)
    public SystemUserPermissionsVO getUserPermissions(Long userId) {
        SystemUserPermissionsVO permissionsVO = new SystemUserPermissionsVO();
        permissionsVO.setUserId(userId);

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
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(orgIds)) {
            orgIds.forEach(orgId -> {
                orgIdList.addAll(orgMapper.getChildIdList(orgId));
            });
            orgIdList.addAll(orgIds);
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
        List<Long> storeGylIds = systemStoreInfoMapper.selectList(new LambdaQueryWrapperX<SystemStoreInfoDO>()
                        .eq(SystemStoreInfoDO::getUserId, userId)
                        .eq(SystemStoreInfoDO::getUseStatus, 1)
                        .eq(SystemStoreInfoDO::getDeleted, 0)
                        // 将所有businessId相关的OR条件包裹在同一个lambda中，保证逻辑正确性
                        .and(wrapper -> wrapper
                                .eq(SystemStoreInfoDO::getBusinessId, 11L)
                                .or().isNull(SystemStoreInfoDO::getBusinessId)
                        )).stream()
                .map(SystemStoreInfoDO::getStoreId)
                .distinct()
                .collect(Collectors.toList());
        Long storeCount = 0L;
        storeIds.addAll(storeGylIds);
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(orgIdList) || org.apache.commons.collections4.CollectionUtils.isNotEmpty(storeIds)) {
            LambdaQueryWrapperX<SystemStoreInfoDO> wrapperX = new LambdaQueryWrapperX<>();

            // 基础查询条件
            wrapperX.eq(SystemStoreInfoDO::getDeleted, 0)
                    .eq(SystemStoreInfoDO::getStoreStatus, 0);

            // 使用 OR 连接两个条件分支
            wrapperX.and(wq -> {
                // 条件1: 组织及其子组织下的门店
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(orgIdList)) {
                    wq.in(SystemStoreInfoDO::getOrgId, orgIdList);
                } else {
                }

                // 条件2: 用户直接关联的门店
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(storeIds)) {
                    wq.or().in(SystemStoreInfoDO::getStoreId, storeIds);
                }
            });

            storeCount = systemStoreInfoMapper.selectCount(wrapperX);
        }
        //多门店场景
        if (storeCount > 1) {
            permissionsVO.setStoreType(STORE_TYPE_MULTIPLE);
            // 判断是否是门店外部客户
            if (!isStoreExternalCustomer(userId) && isStoreGylCustomer(userId)) {
                permissionsVO.setType(PAGE_TYPE_STORE_EXTERNAL);
            } else {
                permissionsVO.setType(PAGE_TYPE_MULTIPLE);
            }
            return permissionsVO;
        }
        // 是否属于部门
        boolean isIn0090Dept = userDeptMapper.exists(new LambdaQueryWrapper<UserDeptDO>()
                .eq(UserDeptDO::getUserId, userId)
                .eq(UserDeptDO::getDeleted, DELETED_NO));

        // 是否关联组织
        boolean isInOrg = orgUserMapper.exists(new LambdaQueryWrapper<OrgUserDO>()
                .eq(OrgUserDO::getUserId, userId)
                .eq(OrgUserDO::getDeleted, DELETED_NO));

        // 供应链角色判断
        //是否是供应链客户
        boolean isSupplyChainCustomer = userRoleMapper.exists(new LambdaQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO::getUserId, userId)
                .eq(UserRoleDO::getRoleId, ROLE_ID_SUPPLY_CHAIN_CUSTOMER)
                .eq(UserRoleDO::getDeleted, DELETED_NO));

        // 是否是供应链员工
        boolean isSupplyChainEmployee = userRoleMapper.exists(new LambdaQueryWrapper<UserRoleDO>()
                .eq(UserRoleDO::getUserId, userId)
                .eq(UserRoleDO::getRoleId, ROLE_ID_SUPPLY_CHAIN_EMPLOYEE)
                .eq(UserRoleDO::getDeleted, DELETED_NO));
        //查询是否存在oa权限
        List<Long> roleId = userRoleMapper.selectList(
                        new LambdaQueryWrapper<UserRoleDO>()
                                .eq(UserRoleDO::getUserId, userId)
                                .eq(UserRoleDO::getDeleted, DELETED_NO)
                )
                .stream()
                .map(UserRoleDO::getRoleId)
                .collect(Collectors.toList());
        boolean isOa = false;
        if (CollectionUtil.isNotEmpty(roleId) && roleId.size() > 0) {
            isOa = roleMenuMapper.exists(new LambdaQueryWrapperX<RoleMenuDO>()
                    .eq(RoleMenuDO::getMenuId, 1993489870492037121L)
                    .in(RoleMenuDO::getRoleId, roleId));
        }
        // 单0090门店页场景判断
        boolean isSingle0090Store = false;
        if (isIn0090Dept) {
            // 在0090部门（无论角色/组织）
            isSingle0090Store = true;
        } else if (isSupplyChainEmployee) {

            isSingle0090Store = true;
        } else if (isSupplyChainCustomer && isInOrg) {
            // 供应链客户 + 在组织
            isSingle0090Store = true;
        } else if (isInOrg && !isSupplyChainCustomer) {
            //非供应链客户 + 在组织
            isSingle0090Store = true;
        }
        if (storeCount == 0 && (isOa || isIn0090Dept)) {
            permissionsVO.setStoreType(STORE_TYPE_SINGLE_0090);
            permissionsVO.setType(PAGESTORE_EXTERNAL);
            return permissionsVO;
        }
        if (storeCount == 0 && !isOa && !isIn0090Dept) {
            permissionsVO.setStoreType(STORE_TYPE_SINGLE_0090);
            permissionsVO.setType(GO_AWAY);
            return permissionsVO;
        }
        if (storeCount != 0 && !isStoreExternalCustomer(userId) && !isInOrg && !isIn0090Dept) {
            permissionsVO.setStoreType(STORE_TYPE_SINGLE_0090);
            permissionsVO.setType(PAGE_TYPE_EXTERNAL);
            return permissionsVO;
        }
        if (storeCount != 0 && isStoreExternalCustomer(userId) && !isInOrg && isIn0090Dept) {
            permissionsVO.setStoreType(STORE_TYPE_SINGLE_0090);
            permissionsVO.setType(PAGE_TYPE_SINGLE_0090);
             return permissionsVO;
        }
        if (storeCount == 0 && !isStoreExternalCustomer(userId) && isSupplyChainEmployee && !isInOrg) {
            permissionsVO.setStoreType(STORE_TYPE_SINGLE_0090);
            permissionsVO.setType(PAGESTORE_EXTERNAL);
            return permissionsVO;
        }
        if (storeCount == 0 && isStoreExternalCustomer(userId) && isSupplyChainEmployee && !isInOrg) {
            permissionsVO.setStoreType(STORE_TYPE_SINGLE_0090);
            permissionsVO.setType(PAGESTORE_EXTERNAL);
            return permissionsVO;
        }

        if (isSingle0090Store) {
            permissionsVO.setStoreType(STORE_TYPE_SINGLE_0090);
            permissionsVO.setType(PAGE_TYPE_SINGLE_0090);
        } else {
            // 外部场景
            permissionsVO.setStoreType(STORE_TYPE_EXTERNAL);
            if (!isStoreExternalCustomer(userId) && isStoreGylCustomer(userId)) {
                permissionsVO.setType(PAGE_TYPE_STORE_EXTERNAL);
            } else {
                permissionsVO.setType(STORE_TYPE_SINGLE_0090);
            }
        }

        return permissionsVO;
    }

    @Override
    @DataPermission(enable = false)
    public SystemUserNavigationVO getNavigationByRole(Long userId) {
        SystemUserNavigationVO permissionsVO = new SystemUserNavigationVO();

        // 1.2 获得角色列表
        Set<Long> roleIds = permissionService.getUserRoleIdListByUserId(getLoginUserId(), BusinessContextHolder.getBusinessId());
        if (CollUtil.isEmpty(roleIds)) {
            throw new ServiceException(BUSINESS_BOSS_USER_ROLE_NOT_FOUND);
        }
        List<RoleDO> roles = roleService.getRoleList(roleIds);
        roles.removeIf(role -> !CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus())); // 移除禁用的角色

        // 1.3 获得菜单列表
        Set<Long> menuIds = permissionService.getRoleMenuListByRoleId(convertSet(roles, RoleDO::getId));
        List<MenuDO> menuList = menuService.getMenuList(menuIds);
        menuList = menuService.filterDisableMenus(menuList);
        List<Long> menuIdList = Objects.isNull(menuList)
                ? Collections.emptyList()
                : menuList.stream()
                .filter(Objects::nonNull)
                .map(MenuDO::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Long> parentMenuIdList = Objects.isNull(menuList)
                ? Collections.emptyList()
                : menuList.stream()
                .filter(Objects::nonNull)
                .map(MenuDO::getParentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!CollUtil.isEmpty(menuIdList)
                && !menuIdList.contains(1967835995479326722L)
                && !CollUtil.isEmpty(parentMenuIdList)
                && !parentMenuIdList.contains(1967835995479326722L)) {
            throw new ServiceException(BUSINESS_BOSS_USER_ROLE_NOT_FOUND);
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
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(orgIds)) {
            orgIds.forEach(orgId -> {
                orgIdList.addAll(orgMapper.getChildIdList(orgId));
            });
            orgIdList.addAll(orgIds);
        }
        List<Long> storeIds = systemStoreUserMapper.selectList(new LambdaQueryWrapperX<SystemStoreUserDO>()
                        .eq(SystemStoreUserDO::getUserId, userId)
                        .eq(SystemStoreUserDO::getDeleted, 0)
                        .eq(SystemStoreUserDO::getBusinessId, BusinessContextHolder.getBusinessId()))
                .stream()
                .map(SystemStoreUserDO::getStoreId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> storeGylIds = systemStoreInfoMapper.selectList(new LambdaQueryWrapperX<SystemStoreInfoDO>()
                        .eq(SystemStoreInfoDO::getUserId, userId)
                        .eq(SystemStoreInfoDO::getUseStatus, 1)
                        .eq(SystemStoreInfoDO::getDeleted, 0)
                        // 将所有businessId相关的OR条件包裹在同一个lambda中，保证逻辑正确性
                        .and(wrapper -> wrapper
                                .eq(SystemStoreInfoDO::getBusinessId, 11L)
                                .or().isNull(SystemStoreInfoDO::getBusinessId)
                        )).stream()
                .map(SystemStoreInfoDO::getStoreId)
                .distinct()
                .collect(Collectors.toList());
        Long storeCount = 0L;
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(orgIdList) || org.apache.commons.collections4.CollectionUtils.isNotEmpty(storeIds)) {
            LambdaQueryWrapperX<SystemStoreInfoDO> wrapperX = new LambdaQueryWrapperX<>();

            // 基础查询条件
            wrapperX.eq(SystemStoreInfoDO::getDeleted, 0)
                    .eq(SystemStoreInfoDO::getStoreStatus, 0);

            // 使用 OR 连接两个条件分支
            wrapperX.and(wq -> {
                // 条件1: 组织及其子组织下的门店
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(orgIdList)) {
                    wq.in(SystemStoreInfoDO::getOrgId, orgIdList);
                } else {
                }

                // 条件2: 用户直接关联的门店
                if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(storeIds)) {
                    wq.or().in(SystemStoreInfoDO::getStoreId, storeIds);
                }
            });

            storeCount = systemStoreInfoMapper.selectCount(wrapperX);
        }
        //多门店场景
        if (storeCount > 1) {
            permissionsVO.setIsManyStore(true);

        } else if (storeCount == 1) {
            permissionsVO.setIsManyStore(false);
        }

        if (!CollUtil.isEmpty(menuIdList) && menuIdList.contains(1987818051948843010L)) {
            permissionsVO.setIsOrder(true);
        }
        if (!CollUtil.isEmpty(menuIdList) && menuIdList.contains(1983350243378651138L)) {
            permissionsVO.setIsOA(true);
        }
        if (!CollUtil.isEmpty(menuIdList) && menuIdList.contains(2012408515641311234L)) {
            permissionsVO.setIsData(true);
        }
        if (!CollUtil.isEmpty(menuIdList) && menuIdList.contains(2011271232221437953L)) {
            permissionsVO.setIsManagement(true);
        }
        if(storeCount == 0){
            permissionsVO.setIsManagement(false);
            permissionsVO.setIsData(false);
        }
        if(!CollUtil.isEmpty(menuIdList) && menuIdList.contains(1967836030178803714L))  {
            permissionsVO.setIsPurchase(true);
        }
        return permissionsVO;
    }

    /**
     * 准备用户手机号与ID的映射关系
     */
    private Map<String, Long> prepareUserMobileIdMap() {
        LambdaQueryWrapper<AdminUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(AdminUserDO::getMobile, AdminUserDO::getId);
        List<AdminUserDO> userList = userMapper.selectList(queryWrapper);

        return userList.stream()
                .collect(Collectors.toMap(
                        AdminUserDO::getMobile,
                        AdminUserDO::getId,
                        (existing, replacement) -> existing, // 处理重复键，保留现有值
                        LinkedHashMap::new // 保持顺序
                ));
    }

    /**
     * 准备部门名称与ID的映射关系（排除一级部门）
     */
    private Map<String, Long> prepareDeptNameIdMap() {
        LambdaQueryWrapper<DeptDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(DeptDO::getId, DeptDO::getName)
                .ne(DeptDO::getLevel, 1); // 排除一级部门
        List<DeptDO> deptList = deptMapper.selectList(queryWrapper);

        return deptList.stream()
                .collect(Collectors.toMap(
                        DeptDO::getName,
                        DeptDO::getId,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    /**
     * 处理导入数据，分离现有用户和新用户
     */
    private ImportProcessResult processImportData(List<UserDeptImportExcel> importDataList,
                                                  Set<String> existingMobiles,
                                                  Map<String, Long> mobileIdMap,
                                                  Map<String, Long> deptIdMap) {
        ImportProcessResult result = new ImportProcessResult();
        Set<Long> userIds = new HashSet<>();

        for (UserDeptImportExcel excelData : importDataList) {
            String mobile = excelData.getMobile();
            String rawDeptName = excelData.getDeptName();
            String processedDeptName = ImportDataProcessor.processDepartmentField(rawDeptName);
            Long deptId = deptIdMap.get(processedDeptName);

            // 验证部门是否存在
            if (deptId == null) {
                log.warn("部门不存在: {}, 原始部门名称: {}", processedDeptName, rawDeptName);
                continue; // 跳过无效部门数据
            }

            // 处理用户数据
            if (existingMobiles.contains(mobile)) {
                Long userId = mobileIdMap.get(mobile);
                userIds.add(userId);
                // 现有用户：直接建立部门关系
                processExistingUser(result.getUserDeptList(), mobileIdMap, mobile, deptId);
            } else {
                // 新用户：创建用户并建立部门关系
                Long userId = processNewUser(result, excelData, deptId, mobile);
                userIds.add(userId);
            }
        }
        result.setUserIds(userIds);
        return result;
    }

    /**
     * 处理现有用户的部门关系
     */
    private void processExistingUser(List<UserDeptDO> userDeptList,
                                     Map<String, Long> mobileIdMap,
                                     String mobile, Long deptId) {
        Long userId = mobileIdMap.get(mobile);
        if (userId == null) {
            log.warn("手机号 {} 在映射表中不存在", mobile);
            return;
        }

        UserDeptDO userDept = new UserDeptDO();
        userDept.setUserId(userId);
        userDept.setDeptId(deptId);
        userDept.setType(UserDeptConstants.USER_DEPT_TYPE_1);
        userDept.setCreateTime(LocalDateTime.now());
        userDept.setBusinessId(10L);
        userDeptList.add(userDept);
    }

    /**
     * 处理新用户：创建用户信息和部门关系
     */
    private Long processNewUser(ImportProcessResult result,
                                UserDeptImportExcel excelData,
                                Long deptId, String mobile) {
        Long userId = identifierGenerator.nextId(null).longValue();

        // 创建管理员用户
        AdminUserDO adminUser = createAdminUser(excelData, mobile, userId);
        result.getInsertUsers().add(adminUser);

        // 创建业务用户
        BusinessUserDO businessUser = createBusinessUser(excelData, userId);
        result.getInsertBusinessUsers().add(businessUser);

        // 创建部门关系
        UserDeptDO userDept = createUserDept(userId, deptId);
        result.getUserDeptList().add(userDept);

        // 更新手机号映射，避免重复创建
        result.getNewMobileIdMap().put(mobile, userId);

        return userId;
    }

    /**
     * 创建管理员用户对象
     */
    private AdminUserDO createAdminUser(UserDeptImportExcel excelData, String mobile, Long userId) {
        AdminUserDO adminUser = new AdminUserDO();
        adminUser.setId(userId);
        adminUser.setMobile(mobile);
        adminUser.setNickname(excelData.getName());
        adminUser.setUsername(mobile);
        // 密码默认为手机号
        adminUser.setPassword(passwordEncoder.encode(mobile));
        // 可以设置其他默认字段，如状态等
        return adminUser;
    }

    /**
     * 创建业务用户对象
     */
    private BusinessUserDO createBusinessUser(UserDeptImportExcel excelData, Long userId) {
        BusinessUserDO businessUser = new BusinessUserDO();
        businessUser.setUserId(userId);
        businessUser.setBusinessId(10L);
        businessUser.setNickname(excelData.getName());
        businessUser.setCreateTime(LocalDateTime.now());
        return businessUser;
    }

    /**
     * 创建用户部门关系对象
     */
    private UserDeptDO createUserDept(Long userId, Long deptId) {
        UserDeptDO userDept = new UserDeptDO();
        userDept.setUserId(userId);
        userDept.setDeptId(deptId);
        userDept.setType(UserDeptConstants.USER_DEPT_TYPE_1);
        userDept.setBusinessId(10L);
        return userDept;
    }

    /**
     * 批量插入数据到数据库
     */
    private void batchInsertData(ImportProcessResult result) {
        // 批量插入用户部门关系
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(result.getUserDeptList())) {
            userDeptMapper.insertBatch(result.getUserDeptList());
            log.info("成功插入 {} 条用户部门关系", result.getUserDeptList().size());
        }

        // 批量插入业务用户
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(result.getInsertBusinessUsers())) {
            businessUserMapper.insertBatch(result.getInsertBusinessUsers());
            log.info("成功插入 {} 条业务用户记录", result.getInsertBusinessUsers().size());
        }

        // 批量插入管理员用户
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(result.getInsertUsers())) {
            userMapper.insertBatch(result.getInsertUsers());
            log.info("成功插入 {} 条管理员用户记录", result.getInsertUsers().size());
        }

        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(result.getUserIds())) {
            Set<Long> userIds = result.getUserIds();
            List<UserRoleDO> list = userIds.stream().map(userId -> {
                UserRoleDO userRole = new UserRoleDO();
                userRole.setUserId(userId);
                userRole.setRoleId(1985889851291271170L);
                userRole.setBusinessId(10L);
                userRole.setCreateTime(LocalDateTime.now());
                return userRole;
            }).toList();
            userRoleMapper.insertBatch(list);
            log.info("成功插入 {} 条管角色记录", result.getInsertUsers().size());
        }
    }

    /**
     * 读取excel 下面的不太好使
     *
     * @param file
     * @param clazz
     * @return
     */
    private List<UserDeptImportExcel> readExcel(MultipartFile file, Class<UserDeptImportExcel> clazz) {
        GenericExcelListener<UserDeptImportExcel> listener = new GenericExcelListener<>(validator);
        try {
            EasyExcel.read(file.getInputStream(), clazz, listener).sheet().doRead();
        } catch (Exception e) {
            throw new RuntimeException("Excel解析失败", e);
        }

        //强校验 必填项是否填写
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(listener.getErrors())) {
            String errorMsg = Joiner.on("\n\r").join(listener.getErrors());
            throw new ServiceException(EXCEL_IMPORT_FILE_FAILED.getCode(), errorMsg);
        }
        //导入内容
        return listener.getSuccessList();
    }


    /**
     * 读取excel 下面的不太好使
     *
     * @param file
     * @param
     * @return
     */
    public List<UserDeptImportExcel> readExcel(MultipartFile file) {
        List<UserDeptImportExcel> memberMobiles = new ArrayList<>();
        try {
            // 读取Excel文件
            EasyExcel.read(file.getInputStream(), UserDeptImportExcel.class, new AnalysisEventListener<UserDeptImportExcel>() {
                @Override
                public void invoke(UserDeptImportExcel data, AnalysisContext context) {
                    log.info("解析到一条数据:{}", data);
                    memberMobiles.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {

                }

                @Override
                public void onException(Exception exception, AnalysisContext context) {
                    log.error("有异常");
                    // 如果是某一个单元格的转换异常 能获取到具体行号
                    // 如果要获取头的信息 配合invokeHeadMap使用
                    if (exception instanceof ExcelDataConvertException excelDataConvertException) {
                        log.warn("第{}行，第{}列解析异常，数据为:{}");
                        Integer columnIndex = excelDataConvertException.getColumnIndex();
                        ++columnIndex;
                        Integer rowIndex = excelDataConvertException.getRowIndex();
                        throw new RuntimeException("第" + rowIndex + "行" +
                                "，第" + columnIndex + "列读取错误");
                    }
                }
            }).excelType(ExcelTypeEnum.XLSX).sheet().doRead();
            return memberMobiles;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isStoreExternalCustomer(Long userId) {
        return businessUserMapper.exists(new LambdaQueryWrapper<BusinessUserDO>()
                .eq(BusinessUserDO::getUserId, userId)
                .eq(BusinessUserDO::getBusinessId, BUSINESS_ID_OTHER)
                .eq(BusinessUserDO::getDeleted, DELETED_NO));
    }

    private boolean isStoreGylCustomer(Long userId) {
        return businessUserMapper.exists(new LambdaQueryWrapper<BusinessUserDO>()
                .eq(BusinessUserDO::getUserId, userId)
                .eq(BusinessUserDO::getBusinessId, BUSINESS_ID_SUPPLY_CHAIN)
                .eq(BusinessUserDO::getDeleted, DELETED_NO));
    }
}
