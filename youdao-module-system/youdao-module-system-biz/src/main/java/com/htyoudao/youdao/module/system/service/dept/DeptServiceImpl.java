package com.htyoudao.youdao.module.system.service.dept;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.annotations.VisibleForTesting;
import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptDTO;
import com.htyoudao.youdao.module.system.controller.admin.business.vo.BusinessSimpleRespVO;
import com.htyoudao.youdao.module.system.controller.admin.dept.vo.dept.*;
import com.htyoudao.youdao.module.system.controller.admin.oauth2.vo.user.OAuth2UserInfoRespVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgStoreRespVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.UserRoleVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreResVO;
import com.htyoudao.youdao.module.system.controller.app.deptorg.vo.*;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.DeptDO;
import com.htyoudao.youdao.module.system.dal.dataobject.dept.UserDeptDO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.htyoudao.youdao.module.system.dal.mysql.dept.DeptMapper;
import com.htyoudao.youdao.module.system.dal.mysql.dept.UserDeptMapper;
import com.htyoudao.youdao.module.system.enums.dept.UserDeptConstants;
import com.htyoudao.youdao.module.system.service.business.BusinessService;
import com.htyoudao.youdao.module.system.service.org.OrgService;
import com.htyoudao.youdao.module.system.service.permission.RoleService;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import com.htyoudao.youdao.module.system.service.user.AdminUserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hutool.core.data.id.IdUtil;
import org.dromara.hutool.core.data.id.Snowflake;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertSet;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.*;

/**
 * 部门 Service 实现类
 *
 * @author 0090
 */
@Service
@Validated
@Slf4j
public class DeptServiceImpl implements DeptService {

    @Resource
    private DeptMapper deptMapper;

    @Resource
    private UserDeptMapper userDeptMapper;

    @Resource
    private RoleService roleService;

    @Resource
    private AdminUserService userService;

    @Resource
    private SystemStoreInfoService systemStoreInfoService;


    @Resource
    private OrgService orgService;

    @Resource
    private BusinessService businessService;


    @Override
    public List<DeptRespVO> getDeptList() {
        LambdaQueryWrapper<DeptDO> queryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<UserDeptDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        Long businessId = BusinessContextHolder.getBusinessId();
        // 获取部门列表 总部看全部 写死
        if(!Objects.equals(businessId, 1L)){
            queryWrapper.eq(DeptDO::getBusinessId, businessId);
            lambdaQueryWrapper.eq(UserDeptDO::getBusinessId, businessId);
        }
        List<DeptDO> list = deptMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        List<UserDeptDO> userDeptList = userDeptMapper.selectList(lambdaQueryWrapper);

        Map<Long, Long> deptCountMap = userDeptList.stream()
                .collect(Collectors.groupingBy(
                        UserDeptDO::getDeptId,
                        Collectors.counting()
                ));

        List<DeptRespVO> result = list.stream()
                .map(dept -> {
                    DeptRespVO vo = new DeptRespVO();
                    vo.setId(dept.getId());
                    vo.setName(dept.getName());
                    vo.setParentId(dept.getParentId());
                    vo.setSort(dept.getSort());
                    vo.setLevel(dept.getLevel());
                    vo.setBusinessId(dept.getBusinessId());

                    vo.setUserNum(deptCountMap.getOrDefault(dept.getId(), 0L));
                    if (Objects.equals(businessId, 1L)) {
                        vo.setUpdateFlag(
                                !Objects.equals(dept.getBusinessId(), businessId) ||
                                        Objects.equals(dept.getLevel(), 1) ? 1 : 0
                        );
                    } else {
                        vo.setUpdateFlag(Objects.equals(dept.getLevel(), 1) ? 1 : 0);
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        return result;
    }

    @Override
    public List<DeptRespVO> getAllDeptList() {
        LambdaQueryWrapper<DeptDO> queryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<UserDeptDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 获取全部部门列表
        List<DeptDO> list = deptMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        // 获取部门用户数量
//        List<UserDeptDO> userDeptList = userDeptMapper.selectList(lambdaQueryWrapper);
//        Map<Long, Long> deptCountMap = userDeptList.stream()
//                .collect(Collectors.groupingBy(
//                        UserDeptDO::getDeptId,
//                        Collectors.counting()
//                ));

        List<DeptRespVO> result = list.stream()
                .map(dept -> {
                    DeptRespVO vo = new DeptRespVO();
                    vo.setId(dept.getId());
                    vo.setName(dept.getName());
                    vo.setParentId(dept.getParentId());
                    //vo.setSort(dept.getSort());
                    vo.setLevel(dept.getLevel());
                    vo.setBusinessId(dept.getBusinessId());
                    // 部门标识
                    vo.setDeptFlag(0);
                    //vo.setUserNum(deptCountMap.getOrDefault(dept.getId(), 0L));
                    return vo;
                })
                .collect(Collectors.toList());

        return result;
    }

    @Override
    public List<DeptRespVO> getDeptListByLoginUser() {
        List<DeptDO> list = deptMapper.selectList();
        if (CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        List<UserDeptDO> userDeptList = userDeptMapper.selectList();

        Map<Long, Long> deptCountMap = userDeptList.stream()
                .collect(Collectors.groupingBy(
                        UserDeptDO::getDeptId,
                        Collectors.counting()
                ));

        List<DeptRespVO> result = list.stream()
                .map(dept -> {
                    DeptRespVO vo = new DeptRespVO();
                    vo.setId(dept.getId());
                    vo.setName(dept.getName());
                    vo.setParentId(dept.getParentId());
                    vo.setSort(dept.getSort());
                    vo.setLevel(dept.getLevel());
                    vo.setBusinessId(dept.getBusinessId());
                    vo.setUserNum(deptCountMap.getOrDefault(dept.getId(), 0L));
                    return vo;
                })
                .collect(Collectors.toList());

        return result;
    }

    @Override
    //@CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, allEntries = true)
    public Long createDept(DeptSaveReqVO createReqVO) {
        // 校验父部门
        DeptDO deptDO = deptMapper.selectById(createReqVO.getParentId());
        if (deptDO == null) {
            throw exception(DEPT_PARENT_NOT_EXITS);
        }
        Integer level = deptDO.getLevel();
        Long businessId = deptDO.getBusinessId();
        Long loginBusinessId = BusinessContextHolder.getBusinessId();
        // 校验项目
        if (!Objects.equals(businessId, loginBusinessId)){
            throw exception(DEPT_NOT_SAME_BUSINESS);
        }
        // 校验层级
        if(Objects.equals(level, 5)){
            throw exception(DEPT_OUT_OF_LEVEL);
        }
        // 校验部门名称
        LambdaQueryWrapper<DeptDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DeptDO::getName, createReqVO.getName());
        lambdaQueryWrapper.eq(DeptDO::getBusinessId, businessId);
        if (deptMapper.selectCount(lambdaQueryWrapper) > 0){
            throw exception(DEPT_NAME_EXISTS);
        }
        // 插入部门
        Snowflake snowflake = IdUtil.getSnowflake();
        Long next = snowflake.next();
        DeptDO dept = BeanUtils.toBean(createReqVO, DeptDO.class);
        dept.setLevel(level + 1);
        dept.setAncestors(deptDO.getAncestors() + "," + next);
        dept.setId(next);
        deptMapper.insert(dept);
        // 插入部门负责人
        if(ObjectUtil.isNotEmpty(createReqVO.getUserId())){
            LambdaQueryWrapper<UserDeptDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserDeptDO::getUserId, createReqVO.getUserId());
            queryWrapper.eq(UserDeptDO::getBusinessId, businessId);
            userDeptMapper.delete(queryWrapper);
            UserDeptDO userDept = new UserDeptDO();
            userDept.setDeptId(next);
            userDept.setBusinessId(businessId);
            userDept.setUserId(createReqVO.getUserId());
            userDept.setType(UserDeptConstants.USER_DEPT_TYPE_0);
            userDeptMapper.insert(userDept);
        }
        return next;
    }

    @Override
    //@CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, allEntries = true) // allEntries 清空所有缓存，因为操作一个部门，涉及到多个缓存
    public void updateDept(DeptSaveReqVO updateReqVO) {

        DeptDO deptDO = deptMapper.selectById(updateReqVO.getId());
        if (deptDO == null) {
            throw exception(DEPT_NOT_FOUND);
        }

        // 校验项目
        Long businessId = deptDO.getBusinessId();
        Long loginBusinessId = BusinessContextHolder.getBusinessId();
        if (!Objects.equals(businessId, loginBusinessId)){
            throw exception(DEPT_NOT_SAME_BUSINESS);
        }
        // 一级部门不允许修改
        Integer level = deptDO.getLevel();
        if(Objects.equals(level, 1)){
            throw exception(DEPT_CAN_NOT_UPDATE_BY_LEVEL);
        }

        // 校验部门名称
        LambdaQueryWrapper<DeptDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DeptDO::getName, updateReqVO.getName());
        lambdaQueryWrapper.eq(DeptDO::getBusinessId, businessId);
        lambdaQueryWrapper.ne(DeptDO::getId, updateReqVO.getId());
        if (deptMapper.selectCount(lambdaQueryWrapper) > 0){
            throw exception(DEPT_NAME_EXISTS);
        }

        // 更新部门
        LambdaUpdateWrapper<DeptDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DeptDO::getId, updateReqVO.getId());
        updateWrapper.eq(DeptDO::getBusinessId, businessId);
        updateWrapper.set(DeptDO::getName, updateReqVO.getName());
        deptMapper.update(updateWrapper);

        // 更新部门负责人(应该没用)
        if(ObjectUtil.isNotEmpty(updateReqVO.getUserId())){
            // 更新部门负责人
            LambdaUpdateWrapper<UserDeptDO> updateWrapper1 = new LambdaUpdateWrapper<>();
            updateWrapper1.eq(UserDeptDO::getDeptId, updateReqVO.getId());
            updateWrapper1.eq(UserDeptDO::getBusinessId, businessId);
            updateWrapper1.set(UserDeptDO::getUserId, updateReqVO.getUserId());
            updateWrapper1.eq(UserDeptDO::getType, UserDeptConstants.USER_DEPT_TYPE_1);
            userDeptMapper.update(updateWrapper1);

            UserDeptDO userDeptDO = new UserDeptDO();
            userDeptDO.setDeptId(updateReqVO.getId());
            userDeptDO.setBusinessId(businessId);
            userDeptDO.setUserId(updateReqVO.getUserId());
            userDeptDO.setType(UserDeptConstants.USER_DEPT_TYPE_0);
            userDeptMapper.insert(userDeptDO);
        }
    }

    @Override
    //@CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, allEntries = true) // allEntries 清空所有缓存，因为操作一个部门，涉及到多个缓存
    public void deleteDept(Long id) {
        DeptDO deptDO = deptMapper.selectById(id);
        if (deptDO == null) {
            throw exception(DEPT_NOT_FOUND);
        }

        // 校验项目
        Long businessId = deptDO.getBusinessId();
        Long loginBusinessId = BusinessContextHolder.getBusinessId();
        if (!Objects.equals(businessId, loginBusinessId)){
            throw exception(DEPT_NOT_SAME_BUSINESS);
        }
        // 校验部门下是否有用户
        LambdaQueryWrapper<UserDeptDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDeptDO::getDeptId, id);
        lambdaQueryWrapper.eq(UserDeptDO::getBusinessId, businessId);
        Long l = userDeptMapper.selectCount(lambdaQueryWrapper);
        if(!Objects.equals(l, 0L)){
            throw exception(DEPT_USER_EXISTS);
        }
        // 校验是否有子部门
        if (deptMapper.selectCountByParentId(id) > 0) {
            throw exception(DEPT_EXITS_CHILDREN);
        }
        // 删除部门
        deptMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deptSaveUser(DeptSaveUserReqVO createReqVO) {
        DeptDO deptDO = deptMapper.selectById(createReqVO.getDeptId());
        if (deptDO == null) {
            throw exception(DEPT_NOT_FOUND);
        }
        Long businessId = deptDO.getBusinessId();
        Long loginBusinessId = BusinessContextHolder.getBusinessId();
        if (!Objects.equals(businessId, loginBusinessId)){
            throw exception(DEPT_NOT_SAME_BUSINESS);
        }
        LambdaUpdateWrapper<UserDeptDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserDeptDO::getBusinessId, loginBusinessId);
        updateWrapper.in(UserDeptDO::getUserId, createReqVO.getUserIds());
        userDeptMapper.delete(updateWrapper);

        List<Long> userIds = createReqVO.getUserIds();
        Set<UserDeptDO> collect = userIds.stream().map(userId -> {
            UserDeptDO userDeptDO = new UserDeptDO();
            userDeptDO.setDeptId(createReqVO.getDeptId());
            userDeptDO.setUserId(userId);
            userDeptDO.setType(UserDeptConstants.USER_DEPT_TYPE_1);
            userDeptDO.setBusinessId(loginBusinessId);
            return userDeptDO;
        }).collect(Collectors.toSet());
        return userDeptMapper.insertBatch(collect);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer moveDeptUser(MoveDeptUserReqVO moveReqVO) {
        DeptDO deptDO = deptMapper.selectById(moveReqVO.getOldDeptId());
        Long loginBusinessId = BusinessContextHolder.getBusinessId();
        if (deptDO == null) {
            throw exception(DEPT_NOT_FOUND);
        }else {
            Long businessId = deptDO.getBusinessId();
            if (!Objects.equals(businessId, loginBusinessId)){
                throw exception(DEPT_NOT_SAME_BUSINESS);
            }
        }

        DeptDO deptDO1 = deptMapper.selectById(moveReqVO.getNewDeptId());
        if (deptDO1 == null) {
            throw exception(DEPT_NOT_FOUND);
        }else {
            Long businessId = deptDO1.getBusinessId();
            if (!Objects.equals(businessId, loginBusinessId)){
                throw exception(DEPT_NOT_SAME_BUSINESS);
            }
            Integer level = deptDO1.getLevel();
            if (level < 2){
                throw exception(DEPT_CAN_NOT_ADD_USER);
            }
        }
        LambdaUpdateWrapper<UserDeptDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserDeptDO::getDeptId, moveReqVO.getOldDeptId());
        updateWrapper.eq(UserDeptDO::getBusinessId, loginBusinessId);
        updateWrapper.in(UserDeptDO::getUserDeptId, moveReqVO.getUserDeptIds());
        updateWrapper.set(UserDeptDO::getType, UserDeptConstants.USER_DEPT_TYPE_1);
        updateWrapper.set(UserDeptDO::getDeptId, moveReqVO.getNewDeptId());
        return userDeptMapper.update(updateWrapper);
    }

    @Override
    public Integer removeDeptUser(RemoveDeptUserReqVO moveReqVO) {
        DeptDO deptDO = deptMapper.selectById(moveReqVO.getOldDeptId());
        Long loginBusinessId = BusinessContextHolder.getBusinessId();
        if (deptDO == null) {
            throw exception(DEPT_NOT_FOUND);
        }else {
            Long businessId = deptDO.getBusinessId();
            if (!Objects.equals(businessId, loginBusinessId)){
                throw exception(DEPT_NOT_SAME_BUSINESS);
            }
        }
//        LambdaQueryWrapper<DeptDO> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(DeptDO::getLevel, 1);
//        queryWrapper.eq(DeptDO::getBusinessId, loginBusinessId);
//        DeptDO dept = deptMapper.selectOne(queryWrapper);

        List<Long> userDeptIds = moveReqVO.getUserDeptIds();
//        for (Long userDeptId : userDeptIds) {
//
//        }
        LambdaUpdateWrapper<UserDeptDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserDeptDO::getDeptId, moveReqVO.getOldDeptId());
        updateWrapper.eq(UserDeptDO::getBusinessId, loginBusinessId);
        updateWrapper.in(UserDeptDO::getUserDeptId, userDeptIds);
        //updateWrapper.set(UserDeptDO::getDeptId, dept.getId());
        //updateWrapper.set(UserDeptDO::getType, UserDeptConstants.USER_DEPT_TYPE_1);
        return userDeptMapper.delete(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deptUserSetCharge(DeptUserSetChargeReqVO chargeReqVO) {
        DeptDO deptDO = deptMapper.selectById(chargeReqVO.getDeptId());
        Long loginBusinessId = BusinessContextHolder.getBusinessId();
        if (deptDO == null) {
            throw exception(DEPT_NOT_FOUND);
        }else {
            Long businessId = deptDO.getBusinessId();
            if (!Objects.equals(businessId, loginBusinessId)){
                throw exception(DEPT_NOT_SAME_BUSINESS);
            }
        }
        LambdaUpdateWrapper<UserDeptDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserDeptDO::getDeptId, chargeReqVO.getDeptId());
        updateWrapper.eq(UserDeptDO::getBusinessId, loginBusinessId);
        updateWrapper.set(UserDeptDO::getType, UserDeptConstants.USER_DEPT_TYPE_1);
        userDeptMapper.update(updateWrapper);

        LambdaUpdateWrapper<UserDeptDO> updateWrapper1 = new LambdaUpdateWrapper<>();
        updateWrapper1.eq(UserDeptDO::getDeptId, chargeReqVO.getDeptId());
        updateWrapper1.eq(UserDeptDO::getBusinessId, loginBusinessId);
        updateWrapper1.set(UserDeptDO::getType, UserDeptConstants.USER_DEPT_TYPE_0);
        updateWrapper1.eq(UserDeptDO::getUserDeptId, chargeReqVO.getUserDeptId());
        return userDeptMapper.update(updateWrapper1);
    }

    @Override
    public Integer deptUserUnCharge(DeptUserSetChargeReqVO chargeReqVO) {
        DeptDO deptDO = deptMapper.selectById(chargeReqVO.getDeptId());
        Long loginBusinessId = BusinessContextHolder.getBusinessId();
        if (deptDO == null) {
            throw exception(DEPT_NOT_FOUND);
        }else {
            Long businessId = deptDO.getBusinessId();
            if (!Objects.equals(businessId, loginBusinessId)){
                throw exception(DEPT_NOT_SAME_BUSINESS);
            }
        }
        LambdaUpdateWrapper<UserDeptDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserDeptDO::getDeptId, chargeReqVO.getDeptId());
        updateWrapper.eq(UserDeptDO::getBusinessId, loginBusinessId);
        updateWrapper.set(UserDeptDO::getType, UserDeptConstants.USER_DEPT_TYPE_1);
        updateWrapper.eq(UserDeptDO::getUserDeptId, chargeReqVO.getUserDeptId());
        return userDeptMapper.update(updateWrapper);
    }

    @Override
    public PageResult<DeptUserPageRespVO> getUserListWithDept(DeptUserPageReqVO reqVO) {
        DeptDO deptDO = deptMapper.selectById(reqVO.getDeptId());
        Long loginBusinessId = BusinessContextHolder.getBusinessId();
        if (deptDO == null) {
            throw exception(DEPT_NOT_FOUND);
        }else {
            Long businessId = deptDO.getBusinessId();
            if (!Objects.equals(businessId, loginBusinessId)){
                throw exception(DEPT_NOT_SAME_BUSINESS);
            }
        }
        LambdaQueryWrapper<UserDeptDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserDeptDO::getDeptId, reqVO.getDeptId());
        queryWrapper.eq(UserDeptDO::getBusinessId, loginBusinessId);
        List<UserDeptDO> userDeptDOS = userDeptMapper.selectList(queryWrapper);
        List<Long> deptUserIds = userDeptDOS.stream().map(UserDeptDO::getUserId).toList();

        PageResult<DeptUserPageRespVO> page = userService.getUserListWithDept(reqVO,deptUserIds);
        List<DeptUserPageRespVO> list = page.getList();
        if (CollectionUtils.isEmpty(list)){
            return PageResult.empty();
        }
        Set<Long> userIds = list.stream().map(DeptUserPageRespVO::getId).collect(Collectors.toSet());
        List<UserRoleVO> roleNames = roleService.getRoleNamesByUserIdsV2(userIds);
        Map<Long, String> userRoleMap = roleNames.stream()
                .filter(userRole -> ObjectUtil.isNotEmpty(userRole.getRoleNames()))
                .collect(Collectors.toMap(UserRoleVO::getUserId, UserRoleVO::getRoleNames));
        for (DeptUserPageRespVO item : list) {
            Long id = item.getId();
            item.setRoleNames(userRoleMap.get(id));
        }
        return page;
    }

    @VisibleForTesting
    void validateDeptExists(Long id) {
        if (id == null) {
            return;
        }
        DeptDO dept = deptMapper.selectById(id);
        if (dept == null) {
            throw exception(DEPT_NOT_FOUND);
        }
    }

    @VisibleForTesting
    void validateParentDept(Long id, Long parentId) {
        if (parentId == null || DeptDO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父部门
        if (Objects.equals(id, parentId)) {
            throw exception(DEPT_PARENT_ERROR);
        }
        // 2. 父部门不存在
        DeptDO parentDept = deptMapper.selectById(parentId);
        if (parentDept == null) {
            throw exception(DEPT_PARENT_NOT_EXITS);
        }
        // 3. 递归校验父部门，如果父部门是自己的子部门，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentDept.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(DEPT_PARENT_IS_CHILD);
            }
            // 3.2 继续递归下一级父部门
            if (parentId == null || DeptDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentDept = deptMapper.selectById(parentId);
            if (parentDept == null) {
                break;
            }
        }
    }

    @VisibleForTesting
    void validateDeptNameUnique(Long id, Long parentId, String name) {
        DeptDO dept = deptMapper.selectByParentIdAndName(parentId, name);
        if (dept == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的部门
        if (id == null) {
            throw exception(DEPT_NAME_DUPLICATE);
        }
        if (ObjectUtil.notEqual(dept.getId(), id)) {
            throw exception(DEPT_NAME_DUPLICATE);
        }
    }

    @Override
    public DeptDO getDept(Long id) {
        return deptMapper.selectById(id);
    }

    @Override
    public List<DeptDO> getDeptList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return deptMapper.selectBatchIds(ids);
    }

    @Override
    public List<DeptDO> getChildDeptList(Collection<Long> ids) {
        List<DeptDO> children = new LinkedList<>();
        // 遍历每一层
        Collection<Long> parentIds = ids;
        for (int i = 0; i < Short.MAX_VALUE; i++) { // 使用 Short.MAX_VALUE 避免 bug 场景下，存在死循环
            // 查询当前层，所有的子部门
            List<DeptDO> depts = deptMapper.selectListByParentId(parentIds);
            // 1. 如果没有子部门，则结束遍历
            if (CollUtil.isEmpty(depts)) {
                break;
            }
            // 2. 如果有子部门，继续遍历
            children.addAll(depts);
            parentIds = convertSet(depts, DeptDO::getId);
        }
        return children;
    }

    @Override
    @DataPermission(enable = false)
    public UserDeptInfoRespVO getUserDeptInfo(Long userId) {
        UserDeptInfoRespVO userDeptInfoRespVO = new UserDeptInfoRespVO();
        AdminUserDO user = userService.getUser(userId);
        if(ObjectUtil.isEmpty(user)){
            return userDeptInfoRespVO;
        }
        userDeptInfoRespVO.setId(user.getId());
        userDeptInfoRespVO.setNickname(user.getNickname());
        userDeptInfoRespVO.setMobile(user.getMobile());
        userDeptInfoRespVO.setUsername(user.getUsername());

        // 门店
        List<StoreResVO> storeResVOS = systemStoreInfoService.getStoreListByUserId(userId);
        if(CollectionUtils.isNotEmpty(storeResVOS)){
            List<StoreResVO> list = storeResVOS.stream().map(storeResVO -> {
                StoreResVO storeRes = new StoreResVO();
                storeRes.setStoreId(storeResVO.getStoreId());
                storeRes.setStoreName(storeResVO.getStoreName());
                return storeRes;
            }).toList();
            userDeptInfoRespVO.setStores(list);
        }else {
            userDeptInfoRespVO.setStores(new ArrayList<>());
        }
        // 部门
        LambdaQueryWrapper<UserDeptDO> queryWrapper = new LambdaQueryWrapper<UserDeptDO>();
        queryWrapper.select(UserDeptDO::getDeptId);
        queryWrapper.eq(UserDeptDO::getUserId, userId);
        List<UserDeptDO> userDeptDOList = userDeptMapper.selectList(queryWrapper);
        if(CollectionUtils.isNotEmpty(userDeptDOList)){
            List<Long> deptIds = userDeptDOList.stream().map(UserDeptDO::getDeptId).toList();
            List<DeptDO> deptDOList = deptMapper.selectByIds(deptIds);
            List<DeptRespVO> list = deptDOList.stream().map(dept -> {
                DeptRespVO deptRespVO = new DeptRespVO();
                deptRespVO.setId(dept.getId());
                deptRespVO.setName(dept.getName());
                deptRespVO.setBusinessId(dept.getBusinessId());
                deptRespVO.setLevel(dept.getLevel());
                return deptRespVO;
            }).toList();
            userDeptInfoRespVO.setDepts(list);
        }else {
            userDeptInfoRespVO.setDepts(new ArrayList<>());
        }
        List<BusinessSimpleRespVO> list = businessService.getBusinessListRoleByUserId(userId);
        userDeptInfoRespVO.setBusinessList(list);
        return userDeptInfoRespVO;
    }

    @Override
    public PageResult<UserDeptInfoPageRespVO> getUserDeptInfoPage(UserDeptInfoReqVO userDeptInfoReqVO) {
        PageResult<UserDeptInfoPageRespVO> userDeptInfoPage = userService.getUserDeptInfoPage(userDeptInfoReqVO);
        List<UserDeptInfoPageRespVO> list = userDeptInfoPage.getList();
        if(CollectionUtils.isEmpty(list)){
            return userDeptInfoPage;
        }
        List<Long> userIds = list.stream().map(UserDeptInfoPageRespVO::getId).toList();

        // 部门
        LambdaQueryWrapper<UserDeptDO> queryWrapper = new LambdaQueryWrapper<UserDeptDO>();
        queryWrapper.select(UserDeptDO::getDeptId, UserDeptDO::getUserId);
        queryWrapper.in(UserDeptDO::getUserId, userIds);
        List<UserDeptDO> userDeptDOList = userDeptMapper.selectList(queryWrapper);
        if(CollectionUtils.isEmpty(userDeptDOList)){
            for (UserDeptInfoPageRespVO user : list) {
                user.setDeptNames("");
            }
            return userDeptInfoPage;
        }
        List<Long> deptIds = userDeptDOList.stream().map(UserDeptDO::getDeptId).toList();
        List<DeptDO> depts = deptMapper.selectByIds(deptIds);

        // 构建部门ID到部门名称的映射
        Map<Long, String> deptMap = depts.stream()
                .collect(Collectors.toMap(DeptDO::getId, DeptDO::getName));

        // 构建用户ID到部门ID列表的映射
        Map<Long, List<Long>> userDeptMap = userDeptDOList.stream()
                .collect(Collectors.groupingBy(
                        UserDeptDO::getUserId,
                        Collectors.mapping(UserDeptDO::getDeptId, Collectors.toList())
                ));

        // 为每个用户设置部门名称（用分号拼接）
        for (UserDeptInfoPageRespVO user : list) {
            List<Long> userDeptIds = userDeptMap.get(user.getId());
            if (CollectionUtils.isNotEmpty(userDeptIds)) {
                String deptNames = userDeptIds.stream()
                        .map(deptMap::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(";"));
                user.setDeptNames(deptNames);
            } else {
                user.setDeptNames("");
            }
        }
        return userDeptInfoPage;
    }

    @Override
    public List<DeptUserRespVO> getUserListByDeptId(Long deptId, String nameOrMobile) {
        List<AdminUserDO> users = userService.getUserListByDeptId(deptId,nameOrMobile);
        DeptDO deptDO = deptMapper.selectById(deptId);
        if(CollectionUtils.isNotEmpty(users)){
            List<DeptUserRespVO> list = users.stream().map(user -> {
                DeptUserRespVO deptUserRespVO = new DeptUserRespVO();
                deptUserRespVO.setId(user.getId());
                deptUserRespVO.setName(user.getNickname());
                deptUserRespVO.setMobile(user.getMobile());
                deptUserRespVO.setDeptFlag(1);
                deptUserRespVO.setParentId(deptDO.getId());
                deptUserRespVO.setDeptName(deptDO.getName());
                return deptUserRespVO;
            }).toList();
            return list;
        }
        return Collections.emptyList();
    }

    @Override
    public List<AppOrgStoreRespVO> getStoreListByOrgId(Long orgId, String storeName) {
        List<OrgStoreRespVO> list = orgService.getStoreListByOrgId(orgId, storeName, CommonStatusEnum.ENABLE.getStatus());
        if(CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        List<AppOrgStoreRespVO> result = list.stream().map(item -> {
            AppOrgStoreRespVO appOrgStoreRespVO = new AppOrgStoreRespVO();
            appOrgStoreRespVO.setId(item.getStoreId());
            appOrgStoreRespVO.setName(item.getStoreName());
            appOrgStoreRespVO.setParentId(orgId);
            appOrgStoreRespVO.setOrgFlag(1);
            appOrgStoreRespVO.setUserId(item.getUserId());
            appOrgStoreRespVO.setStoreLeader(item.getStoreLeader());
            appOrgStoreRespVO.setStorePhone(item.getStorePhone());
            return appOrgStoreRespVO;
        }).toList();
        return result;
    }

    @Override
    public List<AppOrgRespVO> getAllOrg(Long businessId) {
        List<AppOrgRespVO> allOrg = orgService.getAllOrg(businessId);
        if(CollectionUtils.isEmpty(allOrg)){
            return Collections.emptyList();
        }


        AppOrgRespVO appOrgStoreRespVO = new AppOrgRespVO();
        List<OrgStoreRespVO> list = orgService.getStoreListByOrgId(null, null, CommonStatusEnum.ENABLE.getStatus());
        Map<Long, List<OrgStoreRespVO>> groupedMap = list.stream()
                .filter(item -> item.getOrgId() != null)
                .collect(Collectors.groupingBy(OrgStoreRespVO::getOrgId));
        List<AppOrgRespVO> copyOfAllOrg = new ArrayList<>(allOrg);
        List<AppOrgRespVO> storeAddList = new ArrayList<>();
        for (AppOrgRespVO appOrgRespVO : copyOfAllOrg) {
            appOrgRespVO.setOrgFlag(0);
            if(groupedMap.containsKey(appOrgRespVO.getId())){
                List<OrgStoreRespVO> list1 = groupedMap.get(appOrgRespVO.getId());

                for (OrgStoreRespVO item : list1) {
                    log.info("deptId---{},storeId---{}",appOrgRespVO.getId(),item.getStoreId());
                    appOrgStoreRespVO = new AppOrgRespVO();
                    appOrgStoreRespVO.setId(item.getStoreId());
                    appOrgStoreRespVO.setName(item.getStoreName());
                    appOrgStoreRespVO.setParentId(appOrgRespVO.getId());
                    appOrgStoreRespVO.setOrgFlag(1);
                    //appOrgStoreRespVO.setSort(0);
                    appOrgStoreRespVO.setUserId(item.getUserId());
                    appOrgStoreRespVO.setStoreLeader(item.getStoreLeader());
                    appOrgStoreRespVO.setStorePhone(item.getStorePhone());
                    storeAddList.add(appOrgStoreRespVO);
                }
            }
        }
        copyOfAllOrg.addAll(storeAddList);
        return copyOfAllOrg;
    }

    @Override
    public PageResult<DeptUserOAPageRespVO> getUserAndDeptPageWithOA(DeptUserOAReqVO deptUserOAReqVO) {
        Page<DeptUserOAPageRespVO> page = new Page<>(deptUserOAReqVO.getPageNo(), deptUserOAReqVO.getPageSize());
        Page<DeptUserOAPageRespVO> userDeptInfoPage = deptMapper.getUserAndDeptWithOAPage(page,deptUserOAReqVO);
        List<DeptUserOAPageRespVO> records = userDeptInfoPage.getRecords();
        if(CollectionUtils.isEmpty(records)){
            return PageResult.empty();
        }
        List<Long> userIds = records.stream().map(DeptUserOAPageRespVO::getId).toList();
        LambdaQueryWrapper<UserDeptDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(UserDeptDO::getDeptId, UserDeptDO::getUserId, UserDeptDO::getType, UserDeptDO::getUserDeptId);
        lambdaQueryWrapper.in(UserDeptDO::getUserId, userIds);
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(deptUserOAReqVO.getType()),UserDeptDO::getType, deptUserOAReqVO.getType());
        List<UserDeptDO> userDeptDOS = userDeptMapper.selectList(lambdaQueryWrapper);

        if(CollectionUtils.isEmpty(userDeptDOS)){
            return PageResult.empty();
        }

        Map<Long, Integer> userDeptTypeMap = userDeptDOS.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserDeptDO::getUserDeptId, UserDeptDO::getType));


        Map<Long, Long> userDeptIdMap = userDeptDOS.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserDeptDO::getUserDeptId, UserDeptDO::getDeptId));

        Map<Long, List<Long>> userDeptMap = userDeptDOS.stream()
                .collect(Collectors.groupingBy(
                        UserDeptDO::getUserId,
                        Collectors.mapping(UserDeptDO::getUserDeptId, Collectors.toList())
                ));

        List<DeptDO> depts = deptMapper.selectList(new LambdaQueryWrapper<DeptDO>()
                .select(DeptDO::getId, DeptDO::getName)
                .in(DeptDO::getId, userDeptDOS.stream().map(UserDeptDO::getDeptId).toList()));

        if(CollectionUtils.isEmpty(depts)){
            return PageResult.empty();
        }
        List<DeptRespVO> bean = BeanUtils.toBean(depts, DeptRespVO.class);


        // 构建部门ID到部门对象的映射
        Map<Long, DeptRespVO> deptMap = bean.stream()
                .collect(Collectors.toMap(DeptRespVO::getId, Function.identity()));



        // 将部门列表设置到records中
        for (DeptUserOAPageRespVO record : records) {
            Long id = record.getId();
            List<Long> longs = userDeptMap.get(id);
            if (longs != null && !longs.isEmpty()) {
                List<DeptRespVO> deptList = new ArrayList<>();
                for (Long userDeptId : longs){
                    DeptRespVO dept = deptMap.get(userDeptIdMap.get(userDeptId));
                    dept.setType(userDeptTypeMap.get(userDeptId));
                    deptList.add(dept);
                }
                record.setDepts(deptList);
            } else {
                record.setDepts(new ArrayList<>()); // 没有部门时设为空列表
            }
        }
        PageResult<DeptUserOAPageRespVO> result = new PageResult<>();
        result.setList(records);
        result.setTotal(userDeptInfoPage.getTotal());
        return result;
    }

    @Override
    public List<DeptUserOAPageRespVO> getAllUserAndDeptPageWithOA(DeptUserOAReqVO deptUserOAReqVO) {
        List<DeptUserOAPageRespVO> userDeptOAList = deptMapper.getAllUserAndDeptPageWithOA(deptUserOAReqVO);
        if(CollectionUtils.isEmpty(userDeptOAList)){
            return new ArrayList<>();
        }
        List<Long> userIds = userDeptOAList.stream().map(DeptUserOAPageRespVO::getId).toList();
        LambdaQueryWrapper<UserDeptDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.select(UserDeptDO::getDeptId, UserDeptDO::getUserId, UserDeptDO::getType, UserDeptDO::getUserDeptId);
        lambdaQueryWrapper.in(UserDeptDO::getUserId, userIds);
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(deptUserOAReqVO.getType()),UserDeptDO::getType, deptUserOAReqVO.getType());
        List<UserDeptDO> userDeptDOS = userDeptMapper.selectList(lambdaQueryWrapper);
        if(CollectionUtils.isEmpty(userDeptDOS)){
            return new ArrayList<>();
        }

        Map<Long, Integer> userDeptTypeMap = userDeptDOS.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserDeptDO::getUserDeptId, UserDeptDO::getType));


        Map<Long, Long> userDeptIdMap = userDeptDOS.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserDeptDO::getUserDeptId, UserDeptDO::getDeptId));

        Map<Long, List<Long>> userDeptMap = userDeptDOS.stream()
                .collect(Collectors.groupingBy(
                        UserDeptDO::getUserId,
                        Collectors.mapping(UserDeptDO::getUserDeptId, Collectors.toList())
                ));

        List<DeptDO> depts = deptMapper.selectList(new LambdaQueryWrapper<DeptDO>()
                .select(DeptDO::getId, DeptDO::getName)
                .in(DeptDO::getId, userDeptDOS.stream().map(UserDeptDO::getDeptId).toList()));

        if(CollectionUtils.isEmpty(depts)){
            return new ArrayList<>();
        }
        List<DeptRespVO> bean = BeanUtils.toBean(depts, DeptRespVO.class);

        // 构建部门ID到部门对象的映射
        Map<Long, DeptRespVO> deptMap = bean.stream()
                .collect(Collectors.toMap(DeptRespVO::getId, Function.identity()));

        // 将部门列表设置到records中
        for (DeptUserOAPageRespVO record : userDeptOAList) {
            Long id = record.getId();
            List<Long> longs = userDeptMap.get(id);
            if (longs != null && !longs.isEmpty()) {
                List<DeptRespVO> deptList = new ArrayList<>();
                for (Long userDeptId : longs){
                    DeptRespVO dept = deptMap.get(userDeptIdMap.get(userDeptId));
                    dept.setType(userDeptTypeMap.get(userDeptId));
                    deptList.add(dept);
                }
                record.setDepts(deptList);
            } else {
                record.setDepts(new ArrayList<>()); // 没有部门时设为空列表
            }
        }
        return userDeptOAList;
    }

    @Override
    @DataPermission(enable = false)
    public List<Long> getUserIdsByDept() {
        // WebFrameworkUtils.getLoginUserId();
        Long userId = SecurityFrameworkUtils.getLoginUserId();

        List<Long> userIds = new ArrayList<>();
        //Long userId = 1316301207872757760L;

        // 判断是否是总经办 看全部部门
        Set<Long> businessIds = isZJB(userId);
        if(CollectionUtils.isNotEmpty(businessIds)) {
            List<UserDeptDO> userDepts = userDeptMapper.selectList(new LambdaQueryWrapper<UserDeptDO>()
                    .in(UserDeptDO::getBusinessId, businessIds));
            if(CollectionUtils.isNotEmpty(userDepts)){
                List<Long> deptIds = userDepts.stream().map(UserDeptDO::getDeptId).toList();
                List<UserDeptDO> result = userDeptMapper.selectList(new LambdaQueryWrapper<UserDeptDO>()
                        .select(UserDeptDO::getUserId)
                        .in(UserDeptDO::getDeptId, deptIds));
                List<Long> list = result.stream().map(UserDeptDO::getUserId).toList();
                userIds.addAll(list);
            }
            List<UserDeptDO> userDepts1 = userDeptMapper.selectList(new LambdaQueryWrapper<UserDeptDO>()
                    .select(UserDeptDO::getDeptId)
                    .eq(UserDeptDO::getUserId, userId)
                    .eq(UserDeptDO::getType,UserDeptConstants.USER_DEPT_TYPE_0)
                    .notIn(UserDeptDO::getBusinessId, businessIds));
            if(CollectionUtils.isEmpty(userDepts1)){
                return userIds;
            }

            // 获取当前用户负责的部门id
            List<Long> currDeptIds = userDepts1.stream().map(UserDeptDO::getDeptId).toList();
            List<DeptDO> depts = deptMapper.selectList(new LambdaQueryWrapper<DeptDO>()
                    .select(DeptDO::getAncestors)
                    .in(DeptDO::getId, currDeptIds));

            List<Long> deptIds = new ArrayList<>();
            // 获取当前用户负责的部门id的子部门id
            for (DeptDO dept : depts){
                LambdaQueryWrapper<DeptDO> queryWrapper = new LambdaQueryWrapper<DeptDO>()
                        .select(DeptDO::getId)
                        .like(DeptDO::getAncestors, dept.getAncestors());
                deptIds.addAll(deptMapper.selectList(queryWrapper).stream().map(DeptDO::getId).toList());
            }
            // 获取当前用户负责的部门id的子部门的人id
            List<UserDeptDO> result = userDeptMapper.selectList(new LambdaQueryWrapper<UserDeptDO>()
                    .select(UserDeptDO::getUserId)
                    .in(UserDeptDO::getDeptId, deptIds));
            List<Long> list = result.stream().map(UserDeptDO::getUserId).toList();
            userIds.addAll(list);
            return userIds;
        }

        // 获取当前用户负责的部门id
        List<UserDeptDO> userDepts = userDeptMapper.selectList(new LambdaQueryWrapper<UserDeptDO>()
                .select(UserDeptDO::getDeptId)
                .eq(UserDeptDO::getUserId, userId)
                .eq(UserDeptDO::getType,UserDeptConstants.USER_DEPT_TYPE_0));
        if(CollectionUtils.isEmpty(userDepts)){
            return List.of(userId);
        }
        // 获取当前用户负责的部门id
        List<Long> currDeptIds = userDepts.stream().map(UserDeptDO::getDeptId).toList();
        List<DeptDO> depts = deptMapper.selectList(new LambdaQueryWrapper<DeptDO>()
                .select(DeptDO::getAncestors)
                .in(DeptDO::getId, currDeptIds));

        List<Long> deptIds = new ArrayList<>();
        // 获取当前用户负责的部门id的子部门id
        for (DeptDO dept : depts){
            LambdaQueryWrapper<DeptDO> queryWrapper = new LambdaQueryWrapper<DeptDO>()
                    .select(DeptDO::getId)
                    .like(DeptDO::getAncestors, dept.getAncestors());
            deptIds.addAll(deptMapper.selectList(queryWrapper).stream().map(DeptDO::getId).toList());
        }
        // 获取当前用户负责的部门id的子部门的人id
        List<UserDeptDO> result = userDeptMapper.selectList(new LambdaQueryWrapper<UserDeptDO>()
                .select(UserDeptDO::getUserId)
                .in(UserDeptDO::getDeptId, deptIds));

        return result.stream().map(UserDeptDO::getUserId).toList();
    }

    @Override
    public Map<Long,DeptDO> getDeptListByUserIds(Collection<Long> ids) {
        if (org.springframework.util.CollectionUtils.isEmpty(ids)){
            return Collections.emptyMap();
        }

        List<UserDeptDO> userDepts = userDeptMapper.selectList(new LambdaQueryWrapper<UserDeptDO>()
                .select(UserDeptDO::getDeptId,UserDeptDO::getUserId)
                .in(UserDeptDO::getUserId, ids));
        if (userDepts.isEmpty()) {
            return new HashMap<>();
        }
        List<Long> deptIds = userDepts.stream()
                .map(UserDeptDO::getDeptId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (deptIds.isEmpty()) {
            return new HashMap<>();
        }

        Map<Long, DeptDO> deptMap = deptMapper.selectByIds(deptIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(DeptDO::getId, Function.identity()));


        // 构建最终结果，对于重复的userId只取第一个
        Map<Long, DeptDO> result = new HashMap<>();
        for (UserDeptDO userDept : userDepts) {
            Long userId = userDept.getUserId();
            // 如果该userId已经存在，跳过（只取第一个）
            if (result.containsKey(userId)) {
                continue;
            }
            DeptDO dept = deptMap.get(userDept.getDeptId());
            if (dept != null) {
                result.put(userId, dept);
            }
        }
        return result;
    }

    @Override
    public List<AppDeptUserRespVO> getAllDeptAndUserList() {
        // 所有部门
        List<DeptDO> deptList = deptMapper.selectList();
        if(CollectionUtils.isEmpty(deptList)){
            return List.of();
        }
        // 所有部门用户关系
        List<AppDeptUserRespVO> deptUserList = userDeptMapper.getAllDeptAndUserList();
        List<AppDeptUserRespVO> result = new ArrayList<>();
        AppDeptUserRespVO vo = new AppDeptUserRespVO();
        for (DeptDO dept : deptList) {
            vo = new AppDeptUserRespVO();
            vo.setId(dept.getId());
            vo.setName(dept.getName());
            vo.setParentId(dept.getParentId());
            vo.setLevel(dept.getLevel());
            vo.setBusinessId(dept.getBusinessId());
            // 部门标识
            vo.setDeptFlag(0);
            result.add(vo);
        }
        result.addAll(deptUserList);
        return result;
    }

    @Override
    @DataPermission(enable = false)
    public DeptUserRespVO getDeptManager(Long deptId) {
        LambdaQueryWrapper<UserDeptDO> queryWrapper = new LambdaQueryWrapper<UserDeptDO>()
                .select(UserDeptDO::getUserId)
                .eq(UserDeptDO::getDeptId, deptId)
                .eq(UserDeptDO::getType,UserDeptConstants.USER_DEPT_TYPE_0);
        UserDeptDO userDeptDO = userDeptMapper.selectOne(queryWrapper);
        if(ObjectUtil.isEmpty(userDeptDO)){
            return new DeptUserRespVO();
        }
        LambdaQueryWrapper<AdminUserDO> queryWrapper1 = new LambdaQueryWrapper<AdminUserDO>();
        queryWrapper1.eq(AdminUserDO::getId, userDeptDO.getUserId());
        AdminUserDO adminUserDO = userService.getUser(userDeptDO.getUserId());
        return BeanUtils.toBean(adminUserDO, DeptUserRespVO.class);
    }

    /**
     * 判断当前用户是否是总经办
     * @param userId
     * @return
     */
    private Set<Long> isZJB(Long userId) {
        List<UserDeptDO> userDepts = userDeptMapper.selectList(new LambdaQueryWrapper<UserDeptDO>()
                .select(UserDeptDO::getDeptId,UserDeptDO::getBusinessId)
                .eq(UserDeptDO::getUserId, userId));
        if(CollectionUtils.isEmpty(userDepts)){
            return Set.of();
        }
        List<Long> deptIds = userDepts.stream().map(UserDeptDO::getDeptId).toList();
        List<DeptDO> deptDOS = deptMapper.selectByIds(deptIds);
        Set<Long> businessIds = new HashSet<>();
        for (DeptDO deptDO : deptDOS) {
            String name = deptDO.getName();
            if(name.contains("总经办")){
                businessIds.add(deptDO.getBusinessId());
            }
        }
        return businessIds;
    }

    @Override
    public Map<Long, DeptDTO> getParentDeptList(List<Long> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return Collections.emptyMap();
        }
        // 所有当前部门
        Map<Long, DeptDO> deptMap = deptMapper.selectList(
                new LambdaQueryWrapper<DeptDO>()
                        .in(DeptDO::getId, ids)
        ).stream().collect(Collectors.toMap(DeptDO::getId, dept -> dept));

        if(ObjectUtil.isEmpty(deptMap)){
            return Collections.emptyMap();
        }

        // 所有上级部门
        Set<Long> ancestorIds = deptMap.values().stream()
                .filter(dept -> ObjectUtil.isNotEmpty(dept.getAncestors()))
                .flatMap(dept -> Arrays.stream(dept.getAncestors().split(","))
                        .map(Long::valueOf))
                .collect(Collectors.toSet());

        // 所有当前部门的二级部门
        Map<Long, DeptDO> level2DeptMap = deptMapper.selectList(
                new LambdaQueryWrapper<DeptDO>()
                        .in(DeptDO::getId, ancestorIds)
                        .eq(DeptDO::getLevel, 2)
        ).stream().collect(Collectors.toMap(DeptDO::getId, dept -> dept));

        Map<Long, DeptDTO> result = deptMap.values().stream()
                .filter(dept -> ObjectUtil.isNotEmpty(dept.getAncestors()))
                .collect(Collectors.toMap(
                        DeptDO::getId,
                        dept -> {
                            // 匹配
                            String[] ancestors = dept.getAncestors().split(",");
                            DeptDTO deptDTO = new DeptDTO();
                            for (String ancestorId : ancestors) {
                                DeptDO parent = level2DeptMap.get(Long.valueOf(ancestorId));
                                if (ObjectUtil.isNotEmpty(parent)) {
                                    deptDTO = new DeptDTO();
                                    deptDTO.setId(parent.getId());
                                    deptDTO.setName(parent.getName());
                                    return deptDTO;
                                }
                            }
                            DeptDO deptDO = deptMap.get(dept.getId());
                            deptDTO.setId(deptDO.getId());
                            deptDTO.setName(deptDO.getName());
                            return deptDTO;
                        }
                ))
                .entrySet().stream()
                .filter(entry -> ObjectUtil.isNotEmpty(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return result;
    }

    @Override
    public List<Long> getSonDeptList(Long id) {
        DeptDO deptDO = deptMapper.selectById(id);
        if(ObjectUtil.isEmpty(deptDO)){
            return List.of(id);
        }
        String ancestors = deptDO.getAncestors();
        LambdaQueryWrapper<DeptDO> queryWrapper = new LambdaQueryWrapper<DeptDO>()
                .select(DeptDO::getId)
                .likeRight(DeptDO::getAncestors, ancestors);
        List<DeptDO> deptDOS = deptMapper.selectList(queryWrapper);
        List<Long> ids = deptDOS.stream().map(DeptDO::getId).toList();
        return ids;
    }

    //    @Override
//    public List<DeptDO> getDeptListByLeaderUserId(Long id) {
//        return deptMapper.selectListByLeaderUserId(id);
//    }

//    @Override
//    @DataPermission(enable = false) // 禁用数据权限，避免建立不正确的缓存
//    @Cacheable(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, key = "#id")
//    public Set<Long> getChildDeptIdListFromCache(Long id) {
//        List<DeptDO> children = getChildDeptList(id);
//        return convertSet(children, DeptDO::getId);
//    }

//    @Override
//    public void validateDeptList(Collection<Long> ids) {
//        if (CollUtil.isEmpty(ids)) {
//            return;
//        }
//        // 获得科室信息
//        Map<Long, DeptDO> deptMap = getDeptMap(ids);
//        // 校验
//        ids.forEach(id -> {
//            DeptDO dept = deptMap.get(id);
//            if (dept == null) {
//                throw exception(DEPT_NOT_FOUND);
//            }
//            if (!CommonStatusEnum.ENABLE.getStatus().equals(dept.getStatus())) {
//                throw exception(DEPT_NOT_ENABLE, dept.getName());
//            }
//        });
//    }

}
