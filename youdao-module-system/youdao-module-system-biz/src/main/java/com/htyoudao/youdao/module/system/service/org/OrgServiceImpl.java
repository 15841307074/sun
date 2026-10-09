package com.htyoudao.youdao.module.system.service.org;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.util.DataPermissionUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.system.api.org.dto.StoreOrgDTO;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.*;
import com.htyoudao.youdao.module.system.controller.admin.orguser.vo.OrgUserSaveReqVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.StoreSimpleResVO;
import com.htyoudao.youdao.module.system.controller.admin.storeuser.vo.StoreUserSaveReqVO;
import com.htyoudao.youdao.module.system.controller.app.deptorg.vo.AppOrgRespVO;
import com.htyoudao.youdao.module.system.dal.dataobject.business.BusinessDO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreUserDO;
import com.htyoudao.youdao.module.system.dal.dataobject.user.AdminUserDO;
import com.htyoudao.youdao.module.system.dal.mysql.business.BusinessMapper;
import com.htyoudao.youdao.module.system.dal.mysql.org.OrgMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreUserMapper;
import com.htyoudao.youdao.module.system.enums.org.OrgUserTypeConstants;
import com.htyoudao.youdao.module.system.service.orguser.OrgUserService;
import com.htyoudao.youdao.module.system.service.permission.RoleService;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import com.htyoudao.youdao.module.system.service.storeuser.StoreUserService;
import com.htyoudao.youdao.module.system.service.user.AdminUserService;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.executor.BatchResult;
import org.dromara.hutool.core.data.id.IdUtil;
import org.dromara.hutool.core.data.id.Snowflake;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.system.enums.LogRecordConstants.*;

/**
 * 组织机构 Service 实现类
 *
 * @author dht
 */
@Service
public class OrgServiceImpl implements OrgService {

    @Resource
    private OrgMapper orgMapper;

    @Resource
    private OrgUserService orgUserService;

    @Resource
    private StoreUserService storeUserService;

    @Resource
    private SystemStoreUserMapper systemStoreUserMapper;

    @Resource
    private SystemStoreInfoService systemStoreInfoService;

    @Resource
    private AdminUserService adminUserService;

    @Resource
    private RoleService roleService;
    @Resource
    private SystemStoreInfoMapper systemStoreInfoMapper;

    @Resource
    private BusinessMapper businessMapper;

    @Override
    public List<OrgTreeRespVO> getOrgListByUser() {
        if (BusinessContextHolder.getBusinessId().equals(1L)) {
            return getAllOrgListByUser();
        }
        Long userId = WebFrameworkUtils.getLoginUserId();
        //当前用户的机构ID
        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);
        if (CollectionUtil.isEmpty(orgIds)) {
            return List.of();
        }
        //当前用户的店铺ID
        List<Long> storeIds = storeUserService.selectUserStoreIds(userId);
        if (CollectionUtil.isEmpty(orgIds) && CollectionUtil.isEmpty(storeIds)) {
            return List.of();
        }
        // 门店的组织ids
        List<Long> storeOrgIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(storeIds)) {
            storeOrgIds = systemStoreInfoService.selectStoreOrgIds(storeIds);
        }
        // 本级 + 店铺本级
        Collection<Long> unionIds = CollectionUtil.union(orgIds, storeOrgIds);
        // 查询所有本级
        if (CollectionUtil.isEmpty(unionIds)) {
            return List.of();
        }
        List<OrgDO> orgList = orgMapper.selectByIds(unionIds);
        // 查询所有上级ids
        Set<Long> allOrgIds = new HashSet<>();
        for (OrgDO org : orgList) {
            if (StringUtils.isNotBlank(org.getAncestors())) {
                List<Long> parents = Arrays.stream(org.getAncestors().split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Long::parseLong)
                        .filter(id -> ObjectUtil.notEqual(id, org.getId()))
                        .toList();
                allOrgIds.addAll(parents);
            }
        }
        // 查询所有上级
        List<OrgDO> upOrgList = orgMapper.selectByIds(allOrgIds);
        // 查询所有下级
        Set<OrgDO> allOrgList = new HashSet<>();
        for (Long orgId : orgIds) {
            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
            allOrgList.addAll(childOrgList);
        }
        allOrgList.addAll(orgList);
        // 查询所有本级、下级节点的ID
        Set<Long> collect = allOrgList.stream().map(OrgDO::getId).collect(Collectors.toSet());
        // 只查询本级和下级的节点人数 店数

        // 组织节点人数map
        Map<Long, Integer> userNumMap = new ConcurrentHashMap<>(8);
        //组织节点店数map
        Map<Long, Integer> storeNumMap = new ConcurrentHashMap<>(8);

        // 组织节点人数
        List<OrgUserNum> userNumList = orgUserService.getNodeUserNum(collect);
        if (CollectionUtil.isNotEmpty(userNumList)) {
            userNumMap = userNumList.parallelStream()
                    .collect(Collectors.toMap(
                            OrgUserNum::getOrgId,
                            OrgUserNum::getUserNum,
                            (existing, replacement) -> existing,
                            ConcurrentHashMap::new
                    ));
        }
        // 组织节点店数
        List<OrgStoreNum> storeNumList = systemStoreInfoService.getNodeStoreNum(collect);
        if (CollectionUtil.isNotEmpty(storeNumList)) {
            storeNumMap = storeNumList.parallelStream()
                    .collect(Collectors.toMap(
                            OrgStoreNum::getOrgId,
                            OrgStoreNum::getStoreNum,
                            (existing, replacement) -> existing,
                            ConcurrentHashMap::new
                    ));
        }

        // 加入上级节点
       allOrgList.addAll(upOrgList);

        List<OrgTreeRespVO> voList = allOrgList.stream()
                .map(createConverter(userNumMap, storeNumMap))
                .collect(Collectors.toList());
        voList.forEach(vo -> {
            if (collect.contains(vo.getId())) {
                vo.setIsMyOrg(1);
                vo.setIsFlag(false);
            }
        });
        return voList;
    }

    @Override
    public List<AppOrgRespVO> getAllOrg(Long businessId) {
        LambdaQueryWrapper<OrgDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjectUtil.isNotEmpty(businessId),OrgDO::getBusinessId, businessId);
        return BeanUtils.toBean(orgMapper.selectList(queryWrapper), AppOrgRespVO.class);
    }

    @Override
    public List<OrgTreeRespVO> getOrgListByUserV2() {
        // 查询所有组织
        List<OrgDO> orgDOS = orgMapper.selectList();
        Set<Long> collect = orgDOS.stream().map(OrgDO::getId).collect(Collectors.toSet());

        // 组织节点人数map
        Map<Long, Integer> userNumMap = new ConcurrentHashMap<>(8);
        //组织节点店数map
        Map<Long, Integer> storeNumMap = new ConcurrentHashMap<>(8);

        // 组织节点人数
        List<OrgUserNum> userNumList = orgUserService.getNodeUserNum(collect);
        if (CollectionUtil.isNotEmpty(userNumList)) {
            userNumMap = userNumList.parallelStream()
                    .collect(Collectors.toMap(
                            OrgUserNum::getOrgId,
                            OrgUserNum::getUserNum,
                            (existing, replacement) -> existing,
                            ConcurrentHashMap::new
                    ));
        }
        // 组织节点店数
        List<OrgStoreNum> storeNumList = systemStoreInfoService.getNodeStoreNum(collect);
        if (CollectionUtil.isNotEmpty(storeNumList)) {
            storeNumMap = storeNumList.parallelStream()
                    .collect(Collectors.toMap(
                            OrgStoreNum::getOrgId,
                            OrgStoreNum::getStoreNum,
                            (existing, replacement) -> existing,
                            ConcurrentHashMap::new
                    ));
        }
        // 组织节点转换
        List<OrgTreeRespVO> voList = orgDOS.stream()
                .map(createConverter(userNumMap, storeNumMap))
                .collect(Collectors.toList());
        return voList;
    }

    /**
     * 创建转换器
     *
     * @param userNumMap  userNumMap
     * @param storeNumMap storeNumMap
     * @return
     */
    private Function<OrgDO, OrgTreeRespVO> createConverter(Map<Long, Integer> userNumMap, Map<Long, Integer> storeNumMap) {
        return orgDo -> convertOrgDOToOrgTreeRespVO(orgDo, userNumMap, storeNumMap);
    }

    /**
     * OrgDO转换为OrgTreeRespVO
     *
     * @param orgDo
     * @return
     */
    private OrgTreeRespVO convertOrgDOToOrgTreeRespVO(OrgDO orgDo, Map<Long, Integer> userNumMap, Map<Long, Integer> storeNumMap) {
        OrgTreeRespVO vo = new OrgTreeRespVO();
        vo.setId(orgDo.getId());
        vo.setName(orgDo.getName());
        vo.setParentId(orgDo.getParentId());
        vo.setLevel(orgDo.getLevel());
        vo.setAncestors(orgDo.getAncestors());
        vo.setChildren(new ArrayList<>());
        vo.setSort(orgDo.getSort());
        vo.setBusinessId(orgDo.getBusinessId());
        vo.setStoreNum(ObjectUtil.isNull(storeNumMap.get(orgDo.getId())) ? 0 : storeNumMap.get(orgDo.getId()));
        vo.setUserNum(ObjectUtil.isNull(userNumMap.get(orgDo.getId())) ? 0 : userNumMap.get(orgDo.getId()));
        // 设置其他属性...
        return vo;
    }

    @Override
    public Set<Long> getChildIdList(Long orgId) {
        return orgMapper.getChildIdList(orgId);
    }

    @Override
    public List<Long> orgIdByUserId(Long userId) {
        return orgMapper.orgByUserId(userId);
    }

    @Override
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_CREATE_SUB_TYPE, bizNo = "{{#createReqVO.id}}", success = SYSTEM_ORG_CREATE_SUCCESS)
    public Long createOrg(OrgSaveReqVO createReqVO) {
        // 插入
        OrgDO org = BeanUtils.toBean(createReqVO, OrgDO.class);
        Long upLevelId = createReqVO.getParentId();
        OrgDO orgDO = orgMapper.selectById(upLevelId);
        Snowflake snowflake = IdUtil.getSnowflake();
        Long next = snowflake.next();
        if (orgDO != null) {
            org.setAncestors(orgDO.getAncestors() + "," + next);
        }
        Integer level = orgDO.getLevel();
        if (ObjectUtil.isNotEmpty(level) && level != 9) {
            org.setLevel(level + 1);
        }
        org.setId(next);

        //新组织最大的sort
        QueryWrapper<OrgDO> queryWrapper = new QueryWrapper<OrgDO>();
        queryWrapper.select("MAX(sort) as maxSort");
        queryWrapper.lambda().eq(OrgDO::getParentId, upLevelId);
        List<Map<String, Object>> maps = orgMapper.selectMaps(queryWrapper);
        Integer maxSort = 0;
        if(CollectionUtil.isNotEmpty(maps)){
            Map<String, Object> stringObjectMap = maps.get(0);
            maxSort = stringObjectMap == null ? 0 : (Integer) stringObjectMap.get("maxSort");
        }
        ++maxSort;

        org.setSort(maxSort);
        orgMapper.insert(org);

        if (ObjectUtil.isNotEmpty(createReqVO.getUserId())) {
            OrgUserSaveReqVO orgUser = new OrgUserSaveReqVO();
            orgUser.setUserId(createReqVO.getUserId());
            orgUser.setOrgId(next);
            orgUser.setType(OrgUserTypeConstants.ORG_USER_TYPE_PRINCIPAL);
            orgUser.setVisible(OrgUserTypeConstants.ORG_USER_VISIBLE);
            orgUserService.createOrgUser(orgUser);
        }
        LogRecordContext.putVariable("createReqVO", org);
        // 返回
        return org.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}", success = SYSTEM_ORG_UPDATE_SUCCESS)
    public void updateOrg(OrgSaveReqVO updateReqVO) {
        // 校验存在
        validateOrgExists(updateReqVO.getId());
        // 更新
        OrgDO updateObj = BeanUtils.toBean(updateReqVO, OrgDO.class);
        orgMapper.updateById(updateObj);
        if (ObjectUtil.isNotEmpty(updateReqVO.getUserId())) {
            orgUserService.setChargedByUpdate(updateReqVO.getId(), updateReqVO.getUserId());
        }
        LogRecordContext.putVariable("updateReqVO", updateReqVO);
    }

    @Override
    public PageResult<OrgUserPageRespVO> getUserListWithOrg(OrgUserPageReqVO pageReqVO) {
        // 查询用户列表 选中组织和当前组织一样 返回空数据
        Long checkedOrgId = pageReqVO.getCheckedOrgId();
        Long orgId = pageReqVO.getOrgId();

        // 选中组织和当前组织一样 返回空数据
        if (ObjectUtil.equals(orgId, checkedOrgId)) {
            return PageResult.empty();
        }

        // checkedOrgId == 0 查询无组织用户
        if (ObjectUtil.isNotEmpty(checkedOrgId) && ObjectUtil.equals(checkedOrgId, 0L)) {
            PageResult<OrgUserPageRespVO> page = adminUserService.getUserListNotInOrg(pageReqVO);
            return page;
        }
        PageResult<OrgUserPageRespVO> page = adminUserService.getUserListWithOrg(pageReqVO);
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
    public PageResult<OrgUserPageRespVO> getUserListWithStore(OrgUserPageReqVO pageReqVO) {
        PageResult<OrgUserPageRespVO> page = adminUserService.getUserListWithStore(pageReqVO);
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
    public PageResult<OrgStorePageRespVO> getStoreListWithOrg(OrgStorePageReqVO pageReqVO) {
        return systemStoreInfoService.selectPage(pageReqVO);
    }

    @Override
    public List<OrgStoreRespVO> getStoreListByOrgId(Long orgId, String storeName, Integer status) {
        // 判断是否是可见节点
//        Boolean b = canDetermineWhetherTheNodeIsVisible(orgId);
//        if (b) {
//            List<SystemStoreInfoDO> storeInfoDOS = systemStoreInfoService.getStoreListByOrgId(orgId, storeName);
//            return BeanUtils.toBean(storeInfoDOS, OrgStoreRespVO.class);
//        }
//        // 通过用户id获取门店id
//        Long userId = WebFrameworkUtils.getLoginUserId();
//        List<Long> storeIds = storeUserService.selectUserStoreIds(userId);
//        if (CollectionUtil.isEmpty(storeIds)) {
//            return List.of();
//        }
        List<SystemStoreInfoDO> storeInfoDOS = systemStoreInfoService.getStoreListByOrgIdAndStoreId(orgId, storeName, status);
        List<OrgStoreRespVO> bean = BeanUtils.toBean(storeInfoDOS, OrgStoreRespVO.class);
        return bean;
    }

    @Override
    public List<OrgStoreRespVO> getOpenStoreListByOrgId(Long orgId, String storeName) {
        List<SystemStoreInfoDO> storeInfoDOS = systemStoreInfoService.getOpenStoreListByOrgId(orgId, storeName);
        return BeanUtils.toBean(storeInfoDOS, OrgStoreRespVO.class);
    }

    @Override
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_ADD_STORE_TYPE, bizNo = "{{#orgStoreSaveReqVO.orgId}}", success = SYSTEM_ORG_ADD_STORE_SUCCESS)
    public int addStoreForOrg(OrgStoreSaveReqVO orgStoreSaveReqVO) {
        LogRecordContext.putVariable("orgStoreSaveReqVO", orgStoreSaveReqVO);
        return systemStoreInfoService.addStoreForOrg(orgStoreSaveReqVO);
    }

    @Override
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_ADD_USER_TYPE, bizNo = "{{#orgUserSaveReqVO.orgId}}", success = SYSTEM_ORG_ADD_USER_SUCCESS)
    public Boolean addUserForOrg(OrgUserSaveReqVO orgUserSaveReqVO) {
        LogRecordContext.putVariable("orgUserSaveReqVO", orgUserSaveReqVO);
        return orgUserService.saveBatch(orgUserSaveReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_MOVE_USER_TYPE, bizNo = "{{#orgMoveReqVO.oldOrgId}}", success = SYSTEM_ORG_MOVE_USER_SUCCESS)
    public Boolean moveOrgUser(OrgUserMoveReqVO orgMoveReqVO) {
        // 清除原有关系
        orgUserService.deleteBatchOrgUser(orgMoveReqVO);

        // 校验用户是否存在 在就清除
        List<Long> newOrgIds = orgMoveReqVO.getNewOrgIds();
        List<Long> userIds = orgMoveReqVO.getUserIds();
        List<OrgUserSaveReqVO> orgUserSaveReqVOS = new ArrayList<>();
        OrgUserSaveReqVO orgUserSaveReqVO = new OrgUserSaveReqVO();
        for (Long newOrgId : newOrgIds) {
            orgMoveReqVO.setOldOrgId(newOrgId);
            orgUserService.deleteBatchOrgUser(orgMoveReqVO);
            for (Long userId : userIds) {
                orgUserSaveReqVO = new OrgUserSaveReqVO();
                orgUserSaveReqVO.setOrgId(newOrgId);
                orgUserSaveReqVO.setType(OrgUserTypeConstants.ORG_USER_TYPE_NORMAL);
                orgUserSaveReqVO.setUserId(userId);
                orgUserSaveReqVOS.add(orgUserSaveReqVO);
            }
        }
        LogRecordContext.putVariable("orgMoveReqVO", orgMoveReqVO);
        return orgUserService.insertBatch(orgUserSaveReqVOS);
    }

    @Override
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_REMOVE_USER_TYPE, bizNo = "{{#orgMoveReqVO.oldOrgId}}", success = SYSTEM_ORG_REMOVE_USER_SUCCESS)
    public int removeOrgUser(OrgUserMoveReqVO orgMoveReqVO) {
        LogRecordContext.putVariable("orgMoveReqVO", orgMoveReqVO);
        return orgUserService.deleteBatchOrgUser(orgMoveReqVO);
    }

    @Override
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_SET_CHARGE_TYPE, bizNo = "{{#orgUserChargedReqVO.orgUserId}}", success = SYSTEM_ORG_SET_CHARGE_SUCCESS)
    public int setCharged(OrgUserChargedReqVO orgUserChargedReqVO) {
        LogRecordContext.putVariable("orgUserChargedReqVO", orgUserChargedReqVO);
        return orgUserService.setCharged(orgUserChargedReqVO);
    }

    @Override
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_UN_CHARGE_TYPE, bizNo = "{{#orgUserChargedReqVO.orgUserId}}", success = SYSTEM_ORG_UN_CHARGE_SUCCESS)
    public int unCharged(OrgUserChargedReqVO orgUserChargedReqVO) {
        LogRecordContext.putVariable("orgUserChargedReqVO", orgUserChargedReqVO);
        return orgUserService.unCharged(orgUserChargedReqVO);
    }

    @Override
    public PageResult<OrgUserPageRespVO> getUserListByOrgId(OrgUserPageReqVO pageReqVO) {
//        Boolean b = canDetermineWhetherTheNodeIsVisible(pageReqVO.getOrgId());
//        if (!b) {
//            return PageResult.empty();
//        }
        // 查询用户列表
        Page<OrgUserPageRespVO> page = orgUserService.getUserListByOrgId(pageReqVO);
        if (CollectionUtil.isEmpty(page.getRecords())) {
            return new PageResult<>(page.getRecords(), page.getTotal());
        }
        List<OrgUserPageRespVO> list = page.getRecords();
        Set<Long> userIds = list.stream().map(OrgUserPageRespVO::getId).collect(Collectors.toSet());
        // 查询用户角色
        List<UserRoleVO> roles = roleService.getRoleNamesByUserIds(userIds);
        // 查询用户组织
        List<OrgUserVO> orgUserVOS = orgMapper.getOrgNameByUserIds(userIds);
        Map<Long, String> userRoleMap = roles.stream()
                .filter(userRole -> ObjectUtil.isNotEmpty(userRole.getRoleNames()))
                .collect(Collectors.toMap(UserRoleVO::getUserId, UserRoleVO::getRoleNames));
        Map<Long, String> useroOrgMap = orgUserVOS.stream()
                .filter(orgUserVO -> ObjectUtil.isNotEmpty(orgUserVO.getOrgNames()))
                .collect(Collectors.toMap(OrgUserVO::getUserId, OrgUserVO::getOrgNames));
        // 设置用户角色和用户组织
        for (OrgUserPageRespVO orgUserPageRespVO : list) {
            Long userId = orgUserPageRespVO.getId();
            orgUserPageRespVO.setRoleNames(userRoleMap.get(userId));
            orgUserPageRespVO.setOrgNames(useroOrgMap.get(userId));
        }
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public PageResult<OrgUserPageRespVO> getUserListByStoreId(StoreUserPageReqVO pageReqVO) {
        // 查询用户列表
        Page<OrgUserPageRespVO> page = storeUserService.getUserListByStoreId(pageReqVO);
        if (CollectionUtil.isEmpty(page.getRecords())) {
            return new PageResult<>(page.getRecords(), page.getTotal());
        }
        List<OrgUserPageRespVO> list = page.getRecords();
        Set<Long> userIds = list.stream().map(OrgUserPageRespVO::getId).collect(Collectors.toSet());
        // 查询用户角色
        List<UserRoleVO> roles = roleService.getRoleNamesByUserIds(userIds);
        // 查询用户组织
        List<OrgUserVO> orgUserVOS = orgMapper.getOrgNameByUserIds(userIds);
        Map<Long, String> userRoleMap = roles.stream()
                .filter(userRole -> ObjectUtil.isNotEmpty(userRole.getRoleNames()))
                .collect(Collectors.toMap(UserRoleVO::getUserId, UserRoleVO::getRoleNames));
        Map<Long, String> useroOrgMap = orgUserVOS.stream()
                .filter(orgUserVO -> ObjectUtil.isNotEmpty(orgUserVO.getOrgNames()))
                .collect(Collectors.toMap(OrgUserVO::getUserId, OrgUserVO::getOrgNames));
        // 设置用户角色和用户组织
        for (OrgUserPageRespVO orgUserPageRespVO : list) {
            Long userId = orgUserPageRespVO.getId();
            orgUserPageRespVO.setRoleNames(userRoleMap.get(userId));
            orgUserPageRespVO.setOrgNames(useroOrgMap.get(userId));
        }
        return new PageResult<>(page.getRecords(), page.getTotal());
    }

    @Override
    public List<OrgUserRespVO> getUserListByUpdate(Long orgId) {
        return orgUserService.getUserListByUpdate(orgId);
    }

    @Override
    public Boolean addUserForStore(StoreUserSaveBatchReqVO storeUserSaveBatchReqVO) {
        Long storeId = storeUserSaveBatchReqVO.getStoreId();
        List<Long> userIds = storeUserSaveBatchReqVO.getUserIds();
        List<StoreUserSaveReqVO> list = new ArrayList<>();
        for (Long userId : userIds) {
            StoreUserSaveReqVO storeUserSaveReqVO = new StoreUserSaveReqVO();
            storeUserSaveReqVO.setStoreId(storeId);
            storeUserSaveReqVO.setUserId(userId);
            list.add(storeUserSaveReqVO);
        }
        return storeUserService.saveBatch(list,storeId);
    }


    /**
     * 判断节点是否可见
     */
    private Boolean canDetermineWhetherTheNodeIsVisible(Long orgId) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        //当前用户的机构ID
        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);
        if (CollectionUtil.isEmpty(orgIds)) {
            return false;
        }
        // 查询所有下级
        Set<Long> allOrgList = new HashSet<>();
        allOrgList.addAll(orgIds);
        for (Long item : orgIds) {
            Set<Long> childOrgIdList = orgMapper.getChildOrgIdList(item);
            allOrgList.addAll(childOrgIdList);
        }
        // 判断是否包含当前机构
        if (allOrgList.contains(orgId)) {
            return true;
        }
        return false;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_MOVE_TYPE, bizNo = "{{#orgMoveReqVO.newOrgId}}", success = SYSTEM_ORG_MOVE_SUCCESS)
    public int moveOrg(OrgMoveReqVO orgMoveReqVO) {

        Long newOrgId = orgMoveReqVO.getNewOrgId();
        Long id = orgMoveReqVO.getOldOrgId();
        OrgDO oldOrg = orgMapper.selectById(id);
        OrgDO newOrg = orgMapper.selectById(newOrgId);
        // 校验层级 同级别移动
        if (oldOrg.getLevel() - 1 != newOrg.getLevel()) {
            throw exception(ORG_SAME_LEVEL_MOVE);
        }
        Set<OrgDO> childOrgList = orgMapper.getChildOrgList(id);
        Set<Long> allOrgIds = childOrgList.stream().map(OrgDO::getId).distinct().collect(Collectors.toSet());

        //新组织最大的sort
        QueryWrapper<OrgDO> queryWrapper = new QueryWrapper<OrgDO>();
        queryWrapper.select("MAX(sort) as maxSort");
        queryWrapper.lambda().eq(OrgDO::getParentId, newOrgId);
        List<Object> objects  = orgMapper.selectObjs(queryWrapper);
        Integer maxSort = 0;
        if(CollectionUtil.isNotEmpty(objects)){
            maxSort = objects.get(0) == null ? 0 : (Integer) objects.get(0);
        }
        ++maxSort;
        // 2. 获取新旧ancestors映射关系
        String oldAncestorPrefix = newOrg.getAncestors() + (newOrg.getAncestors().isEmpty() ? "" : ",") + id;

        //017
        String newParentAncestors = getAncestors(newOrgId);
        //0178
        String newAncestorPrefix = newParentAncestors + (newParentAncestors.isEmpty() ? "" : ",") + id;
        if (childOrgList.size() != 1) {
            // 批量更新下级组织
            String oldAncestor = oldOrg.getAncestors();
            orgMapper.batchUpdateChildrenAncestors(allOrgIds, oldAncestor, newAncestorPrefix);
        }
        LogRecordContext.putVariable("orgMoveReqVO", orgMoveReqVO);

        // 更新当前组织parentId、ancestors
        return orgMapper.updateParentAndAncestors(id, newOrgId, newAncestorPrefix, maxSort);
    }

    /**
     * 获取指定组织的完整ancestors路径
     *
     * @param orgId 组织ID
     * @return 父组织ID路径（如"1,2,3"），如果是根组织返回空字符串
     */
    private String getAncestors(Long orgId) {
        if (orgId == null || orgId == 0L) {
            return "";
        }

        OrgDO org = orgMapper.selectById(orgId);
        return org != null ? org.getAncestors() : "";
    }

    @Override
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_DELETE_TYPE, bizNo = "{{#id}}", success = SYSTEM_ORG_DELETE_SUCCESS)
    public void deleteOrg(Long id) {
        // 校验存在
        String ancestors = validateOrgExists(id);

        // 校验有没有下级组织
        hasChildOrg(id);

        // 校验有没有门店
        hasSore(id);

        // 校验有没有用户
        hasUser(id);
        LogRecordContext.putVariable("id", id);
        // 删除
        orgMapper.deleteById(id);
    }

    /**
     * 判断当前节点是否有用户
     *
     * @param id
     */
    private void hasUser(Long id) {
        Boolean b = orgUserService.hasOrgUser(id);
        if (b) {
            throw exception(ORG_HAS_USER_ERROR);
        }
    }

    /**
     * 判断当前节点是否有门店
     *
     * @param id orgId
     */
    private void hasSore(Long id) {
        Boolean b = systemStoreInfoService.hasOrgStore(id);
        if (b) {
            throw exception(ORG_HAS_STORE_ERROR);
        }
    }

    /**
     * 判断是否有下级组织
     *
     * @param id orgId
     */
    private void hasChildOrg(Long id) {
        Set<Long> childIdList = orgMapper.hasChildOrg(id);
        int i = CollectionUtil.isEmpty(childIdList) ? 0 : childIdList.size();
        if (i > 0) {
            throw exception(ORG_HAS_CHILD_ERROR);
        }
    }

    /**
     * 校验org是否存在
     *
     * @param id
     */
    private String validateOrgExists(Long id) {
        OrgDO orgDO = orgMapper.selectById(id);
        if (ObjectUtil.isEmpty(orgDO)) {
            throw exception(ORG_NOT_EXISTS);
        }
        return orgDO.getAncestors();
    }

    @Override

    public OrgDO getOrg(Integer id) {
        return orgMapper.selectById(id);
    }

    @Override
    public PageResult<OrgDO> getOrgPage(OrgPageReqVO pageReqVO) {
        return orgMapper.selectPage(pageReqVO);
    }


    @Override
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_MOVE_STORE_TYPE, bizNo = "{{#orgStoreMoveReqVO.newOrgId}}", success = SYSTEM_ORG_MOVE_STORE_SUCCESS)
    public Integer moveStore(OrgStoreMoveReqVO orgStoreMoveReqVO) {
        LogRecordContext.putVariable("orgStoreMoveReqVO", orgStoreMoveReqVO);
        return systemStoreInfoService.moveStore(orgStoreMoveReqVO);
    }

    @Override
    public Integer removeStore(OrgStoreMoveReqVO orgStoreMoveReqVO) {
        return systemStoreInfoService.removeStore(orgStoreMoveReqVO);
    }

    @Override
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_SET_STORE_MANAGER_TYPE, bizNo = "{{#storeManagerReqVO.userId}}", success = SYSTEM_ORG_SET_STORE_MANAGER_SUCCESS)
    public int setStoreManager(StoreManagerReqVO storeManagerReqVO) {
        LogRecordContext.putVariable("storeManagerReqVO", storeManagerReqVO);
        return storeUserService.setStoreManager(storeManagerReqVO);
    }

    @Override
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_UN_STORE_MANAGER_TYPE, bizNo = "{{#storeManagerReqVO.userId}}", success = SYSTEM_ORG_UN_STORE_MANAGER_SUCCESS)
    public int unStoreManager(StoreManagerReqVO storeManagerReqVO) {
        LogRecordContext.putVariable("storeManagerReqVO", storeManagerReqVO);
        return storeUserService.unStoreManager(storeManagerReqVO);
    }

    @Override
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_REMOVE_STORE_USER_TYPE, bizNo = "{{#storeUserRemoveReqVO.storeId}}", success = SYSTEM_ORG_REMOVE_STORE_USER_SUCCESS)
    public int removeStoreUser(StoreUserRemoveReqVO storeUserRemoveReqVO) {
        LogRecordContext.putVariable("storeUserRemoveReqVO", storeUserRemoveReqVO);
        return storeUserService.removeStoreUser(storeUserRemoveReqVO);
    }

    @Override
    public List<OrgTreeRespVO> getOrgListByUserAndBusID(Long businessId) {
        Long userId = WebFrameworkUtils.getLoginUserId();

        // 当前用户的机构ID
        List<Long> orgIds = orgUserService.selectUserOrgIdsByBusinessId(userId, businessId);

        // 当前用户的店铺ID
        List<Long> storeIds = storeUserService.selectUserStoreIds(userId);

        if (CollectionUtil.isEmpty(orgIds) && CollectionUtil.isEmpty(storeIds)) {
            return List.of();
        }

        // 门店组织
        List<Long> storeOrgIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(storeIds)) {
            storeOrgIds = systemStoreInfoService.selectStoreOrgIds(storeIds);
        }

        // 合并所有ids
        Collection<Long> unionIds = CollectionUtil.union(orgIds, storeOrgIds);

        // 查询所有本级
        List<OrgDO> orgList = orgMapper.selectByIds(unionIds);

        // 查询所有上级ids
        Set<Long> allOrgIds = new HashSet<>();
        for (OrgDO org : orgList) {
            if (StringUtils.isNotBlank(org.getAncestors())) {
                List<Long> parents = Arrays.stream(org.getAncestors().split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Long::parseLong)
                        .filter(id -> !Objects.equals(id, org.getId()))
                        .toList();
                allOrgIds.addAll(parents);
            }
        }

        // 查询所有上级
        List<OrgDO> upOrgList = orgMapper.selectByIds(allOrgIds);

        // 查询所有下级
        Set<OrgDO> allOrgSet = new HashSet<>();
        for (Long orgId : orgIds) {
            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
            allOrgSet.addAll(childOrgList);
        }
        allOrgSet.addAll(upOrgList);
        allOrgSet.addAll(orgList);

        // 获取所有组织的 orgId
        List<Long> allOrgIdsList = new ArrayList<>(allOrgSet.stream().map(OrgDO::getId).collect(Collectors.toList()));

        // 查询这些 orgId 关联的门店数
        Map<Long, Integer> storeCountMap = countStoresByOrgIds(allOrgIdsList);

        // 转换为 OrgTreeRespVO
        List<OrgTreeRespVO> voList = allOrgSet.stream()
                .map(orgDO -> {
                    OrgTreeRespVO vo = convertOrgDOToOrgTreeRespVO(orgDO, new HashMap<>(), new HashMap<>());
                    vo.setStoreNum(storeCountMap.getOrDefault(orgDO.getId(), 0));
                    return vo;
                })
                .collect(Collectors.toList());

        // 构建组织树
        Map<Long, OrgTreeRespVO> orgMap = voList.stream()
                .collect(Collectors.toMap(OrgTreeRespVO::getId, vo -> vo));

        List<OrgTreeRespVO> result = new ArrayList<>();
        for (OrgTreeRespVO vo : voList) {
            if (vo.getParentId() == null || !orgMap.containsKey(vo.getParentId())) {
                result.add(vo);
            }
        }

        return result;
    }

    /**
     * 根据当前登入人以及项目 获取本级组织以及下级组织id
     */
    @Override
    public Set<Long> getStoreIdsByUser() {
        Long userId = WebFrameworkUtils.getLoginUserId();
        //TODO 跨模块 项目id 是null
        //当前用户的机构ID
        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);
        if (CollectionUtil.isEmpty(orgIds)) {
            return Set.of();
        }
        // 查询所有下级
        Set<OrgDO> allOrgList = new HashSet<>();
        for (Long orgId : orgIds) {
            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
            allOrgList.addAll(childOrgList);
        }
        // 查询所有本级、下级节点的ID
        Set<Long> collect = allOrgList.stream().map(OrgDO::getId).collect(Collectors.toSet());
        collect.addAll(orgIds);
        //根据组织查询门店
        Set<Long> storeOrgIds = systemStoreInfoService.getStoreIdsByOrgIds(collect);
        return storeOrgIds;
    }

    @Override
    public List<OrgStoreTreeRespVO> getOrgStoreListByUserAndBusID() {
        Long userId = WebFrameworkUtils.getLoginUserId();

        // 当前用户的机构ID
        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);
        // 当前用户的店铺ID
        List<Long> storeIds = storeUserService.selectUserStoreIds(userId);

        if (CollectionUtil.isEmpty(orgIds) && CollectionUtil.isEmpty(storeIds)) {
            return List.of();
        }

        // 门店的组织ids
        List<Long> storeOrgIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(storeIds)) {
            storeOrgIds = systemStoreInfoService.selectStoreOrgIds(storeIds);
        }

        // 本级 + 店铺本级
        Collection<Long> unionIds = CollectionUtil.union(orgIds, storeOrgIds);

        // 查询所有本级
        if (CollectionUtil.isEmpty(unionIds)){
            return List.of();
        }
        List<OrgDO> orgList = orgMapper.selectByIds(unionIds);

        // 查询所有上级ids
        Set<Long> allOrgIds = new HashSet<>();
        for (OrgDO org : orgList) {
            if (StringUtils.isNotBlank(org.getAncestors())) {
                List<Long> parents = Arrays.stream(org.getAncestors().split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Long::parseLong)
                        .filter(id -> !Objects.equals(id, org.getId()))
                        .toList();
                allOrgIds.addAll(parents);
            }
        }

        // 查询所有上级
        List<OrgDO> upOrgList = orgMapper.selectByIds(allOrgIds);

        // 查询所有下级
        Set<OrgDO> allOrgSet = new HashSet<>();
        for (Long orgId : orgIds) {
            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
            allOrgSet.addAll(childOrgList);
        }
        allOrgSet.addAll(orgList);

        // 查询所有本级、下级节点的ID
        Set<Long> collect = allOrgSet.stream().map(OrgDO::getId).collect(Collectors.toSet());
        allOrgSet.addAll(upOrgList);
        // 组织节点店数 map
        Map<Long, List<SystemStoreInfoDO>> storeMap = systemStoreInfoService.selectStoreByOrgIds(collect).stream()
                .collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));

        // 加入上级节点
        allOrgSet.addAll(upOrgList);
        List<OrgStoreTreeRespVO> voList = allOrgSet.stream()
                .flatMap(org -> {
                    // 生成不包含门店信息的 OrgStoreTreeRespVO 对象
                    OrgStoreTreeRespVO voWithoutStore = createOrgStoreTreeRespVOWithoutStore(org);
                    if(!upOrgList.contains(org)){
                        voWithoutStore.setIsMyOrg(1);
                    }
                    // 获取与当前 OrgDO 对应的 SystemStoreInfoDO 对象列表
                    List<SystemStoreInfoDO> stores = storeMap.get(org.getId());
                    if (CollectionUtil.isEmpty(stores)) {
                        return Stream.of(voWithoutStore);
                    }
                    // 生成包含门店信息的 OrgStoreTreeRespVO 对象
                    List<OrgStoreTreeRespVO> voWithStores = stores.stream()
                            .map(store -> createOrgStoreTreeRespVOWithStore(org, store))
                            .collect(Collectors.toList());
                    // 合并不包含门店信息的对象和包含门店信息的对象
                    return Stream.concat(Stream.of(voWithoutStore), voWithStores.stream());
                })
                .collect(Collectors.toList());
        return voList;
    }

    @Override
    public Set<Long> getStoreIdListByOrgID(Long orgId) {
        // 查询所有下级
        Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
        if (CollectionUtil.isEmpty(childOrgList)) {
            return new HashSet<>();
        }
        // 查询所有本级、下级节点的ID
        Set<Long> collect = childOrgList.stream().map(OrgDO::getId).collect(Collectors.toSet());
        collect.add(orgId);
        //根据组织查询门店
        Set<Long> storeOrgIds = systemStoreInfoService.getStoreIdsByOrgIds(collect);
        return storeOrgIds;
    }

    @Override
    public Set<Long> getStoreIdListByOrgIDRpc(Long orgId, Long businessId) {
        // 查询所有下级
        Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
        BusinessContextHolder.setBusinessId(businessId);
        if (CollectionUtil.isEmpty(childOrgList)) {
            return new HashSet<>();
        }
        // 查询所有本级、下级节点的ID
        Set<Long> collect = childOrgList.stream().map(OrgDO::getId).collect(Collectors.toSet());
        collect.add(orgId);
        //根据组织查询门店
        Set<Long> storeOrgIds = systemStoreInfoService.getStoreIdsByOrgIds(collect);
        return storeOrgIds;
    }

    @Override
    public Set<OrgDO> getChildOrgList(Long orgId) {
        return orgMapper.getChildOrgList(orgId);
    }

    public Map<Long, Integer> countStoresByOrgIds(List<Long> orgIds) {
        if (CollectionUtil.isEmpty(orgIds)) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<SystemStoreInfoDO> queryWrapper = Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                .in(SystemStoreInfoDO::getOrgId, orgIds);
        List<SystemStoreInfoDO> storeList = systemStoreInfoMapper.selectList(queryWrapper);

        Map<Long, Integer> storeCountMap = new HashMap<>();
        for (SystemStoreInfoDO store : storeList) {
            storeCountMap.put(store.getOrgId(), storeCountMap.getOrDefault(store.getOrgId(), 0) + 1);
        }

        return storeCountMap;
    }

    @Override
    public Set<Long> getStoreIdsByUserRpc(Long businessId) {
        Long userId = WebFrameworkUtils.getLoginUserId();
        BusinessContextHolder.setBusinessId(businessId);
        //TODO 跨模块 项目id 是null
        //当前用户的机构ID
        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);
        if (CollectionUtil.isEmpty(orgIds)) {
            return new HashSet<>();
        }
        // 查询所有下级
        Set<OrgDO> allOrgList = new HashSet<>();
        for (Long orgId : orgIds) {
            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
            allOrgList.addAll(childOrgList);
        }
        // 查询所有本级、下级节点的ID
        Set<Long> collect = allOrgList.stream().map(OrgDO::getId).collect(Collectors.toSet());
        collect.addAll(orgIds);
        //根据组织查询门店
        Set<Long> storeOrgIds = systemStoreInfoService.getStoreIdsByOrgIds(collect);
        return storeOrgIds;
    }

    public List<OrgTreeRespVO> getAllOrgListByUser() {
        // 查询总部项目
        List<BusinessDO> businessList = businessMapper.selectList(new LambdaQueryWrapper<BusinessDO>().eq(BusinessDO::getDeleted, 0));
        // 查询所有组织
        Set<Long> businessIds = businessList.stream().map(BusinessDO::getId).collect(Collectors.toSet());
        List<OrgDO> orgDOList = orgMapper.selectList(businessIds);
        businessList.removeIf(businessDO -> businessDO.getId().equals(1L));
        List<OrgTreeRespVO> busList = businessList.stream()
                .map(businessDO -> {
                    OrgTreeRespVO vo = new OrgTreeRespVO();
                    vo.setId(businessDO.getId());
                    vo.setName(businessDO.getName());
                    vo.setBusinessId(businessDO.getId());
                    if (businessDO.getId().equals(1L)) {
                        vo.setLevel(1);
                        vo.setParentId(0L);
                    } else {
                        vo.setParentId(1L);
                        vo.setLevel(2);
                        vo.setAncestors("1");
                    }
                    vo.setChildren(new ArrayList<>());
                    return vo;
                })
                .collect(Collectors.toList());

        // 转换为 OrgTreeRespVO
        List<OrgTreeRespVO> voList = orgDOList.stream()
                .map(orgDO -> {
                    OrgTreeRespVO vo = new OrgTreeRespVO();
                    vo.setId(orgDO.getId());
                    vo.setName(orgDO.getName());
                    vo.setParentId(orgDO.getParentId());
                    vo.setLevel(orgDO.getLevel());
                    vo.setAncestors(orgDO.getAncestors());
                    vo.setBusinessId(orgDO.getId());
                    vo.setChildren(new ArrayList<>());
                    return vo;
                })
                .collect(Collectors.toList());

        // 构建组织树
        busList.addAll(voList);
        return voList;
    }

    /**
     * OrgDO转换为OrgStoreTreeRespVO
     *
     * @return
     */

// 新增方法：生成不包含门店信息的 OrgStoreTreeRespVO 对象
    private OrgStoreTreeRespVO createOrgStoreTreeRespVOWithoutStore(OrgDO org) {
        OrgStoreTreeRespVO vo = new OrgStoreTreeRespVO();
        vo.setId(org.getId());
        vo.setName(org.getName());
        vo.setParentId(org.getParentId());
        vo.setLevel(org.getLevel());
        vo.setAncestors(org.getAncestors());
        vo.setChildren(new ArrayList<>());
        vo.setIsStore(0);
        vo.setSort(org.getSort());
        return vo;
    }

    // 新增方法：生成包含门店信息的 OrgStoreTreeRespVO 对象
    private OrgStoreTreeRespVO createOrgStoreTreeRespVOWithStore(OrgDO org, SystemStoreInfoDO store) {
        OrgStoreTreeRespVO vo = createOrgStoreTreeRespVOWithoutStore(org);
        vo.setParentId(org.getId());
        vo.setLevel(org.getLevel() + 1);
        vo.setId(store.getStoreId());
        vo.setName(store.getStoreName());
        vo.setStoreName(store.getStoreName());
        vo.setStoreCityName(store.getStoreCityName());
        vo.setStoreId(store.getStoreId());
        vo.setIsStore(1);
        return vo;
    }



    @Override
    @LogRecord(type = SYSTEM_ORG_TYPE, subType = SYSTEM_ORG_STORE_USER_UNVISIBLE_TYPE, bizNo = "{{#storeManagerReqVO.storeId}}", success = SYSTEM_ORG_STORE_USER_UNVISIBLE_TYPE_SUCCESS)
    public int storeSetUserIsVisible(StoreManagerReqVO storeManagerReqVO) {
        LogRecordContext.putVariable("storeManagerReqVO", storeManagerReqVO);
        return storeUserService.storeSetUserIsVisible(storeManagerReqVO);
    }

    @Override
    public int storeSetUserUnVisible(StoreManagerReqVO storeManagerReqVO) {
        return storeUserService.storeSetUserUnVisible(storeManagerReqVO);
    }

    @Override
    public int orgSetUserIsVisible(OrgUserChargedReqVO orgUserChargedReqVO) {
        return orgUserService.orgSetUserIsVisible(orgUserChargedReqVO);
    }

    @Override
    public int orgSetUserUnVisible(OrgUserChargedReqVO orgUserChargedReqVO) {
        return orgUserService.orgSetUserUnVisible(orgUserChargedReqVO);
    }

    @Override
    public List<OrgTreeRespVO> getOrgListByUsers() {
        Long userId = WebFrameworkUtils.getLoginUserId();

        Long businessId = BusinessContextHolder.getBusinessId();

        // 当前用户的机构ID
        List<Long> orgIds = orgUserService.selectUserOrgIdsByBusinessId(userId, businessId);

        // 当前用户的店铺ID
        List<Long> storeIds = storeUserService.selectUserStoreIds(userId);

        if (CollectionUtil.isEmpty(orgIds) && CollectionUtil.isEmpty(storeIds)) {
            return List.of();
        }

        // 门店组织
        List<Long> storeOrgIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(storeIds)) {
            storeOrgIds = systemStoreInfoService.selectStoreOrgIds(storeIds);
        }

        // 合并所有ids
        Collection<Long> unionIds = CollectionUtil.union(orgIds, storeOrgIds);

        // 不一定能看见全量的店铺 并且看不见下级的orgId
        Collection<Long> difference = CollectionUtils.subtract(storeOrgIds, orgIds);

        // 查询所有本级
        List<OrgDO> orgList = orgMapper.selectByIds(unionIds);

        // 查询所有上级ids
        Set<Long> allOrgIds = new HashSet<>();
        for (OrgDO org : orgList) {
            if (StringUtils.isNotBlank(org.getAncestors())) {
                List<Long> parents = Arrays.stream(org.getAncestors().split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Long::parseLong)
                        .filter(id -> !Objects.equals(id, org.getId()))
                        .toList();
                allOrgIds.addAll(parents);
            }
        }

        // 查询所有上级
        List<OrgDO> upOrgList = orgMapper.selectByIds(allOrgIds);

        // 查询所有下级
        Set<OrgDO> allOrgSet = new HashSet<>();
        for (Long orgId : orgIds) {
            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
            allOrgSet.addAll(childOrgList);
        }
        allOrgSet.addAll(upOrgList);
        allOrgSet.addAll(orgList);

        // 获取所有组织的 orgId
        List<Long> allOrgIdsList = new ArrayList<>(allOrgSet.stream().map(OrgDO::getId).collect(Collectors.toList()));

        // 查询这些 orgId 关联的门店数
        Map<Long, Integer> storeCountMap = countStoresByOrgIds(allOrgIdsList);

        // 转换为 OrgTreeRespVO
        List<OrgTreeRespVO> voList = allOrgSet.stream()
                .map(orgDO -> {
                    OrgTreeRespVO vo = convertOrgDOToOrgTreeRespVO(orgDO, new HashMap<>(), new HashMap<>());
                    vo.setStoreNum(storeCountMap.getOrDefault(orgDO.getId(), 0));
                    return vo;
                })
                .collect(Collectors.toList());

        // 构建组织树
        Map<Long, OrgTreeRespVO> orgMap = voList.stream()
                .collect(Collectors.toMap(OrgTreeRespVO::getId, vo -> vo));

        List<OrgTreeRespVO> result = new ArrayList<>();
        for (OrgTreeRespVO vo : voList) {
            if (vo.getParentId() == null || !orgMap.containsKey(vo.getParentId())) {
                result.add(vo);
            }
        }

        return result;
    }

    @Override
    public List<OrgStoreTreeRespVO> getOrgStoreListByUserData() {


        //组织节点店数map
        Map<Long, Integer> storeNumMap = new ConcurrentHashMap<>(8);

        Long userId = WebFrameworkUtils.getLoginUserId();

        // 当前用户的组织ID
        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);
        // 当前用户的店铺ID
        List<Long> storeIds = storeUserService.selectUserStoreIdsTwo(userId);

        if (CollectionUtil.isEmpty(orgIds) && CollectionUtil.isEmpty(storeIds)) {
            return List.of();

        }

        // 门店的组织ids
        List<Long> storeOrgIds = new ArrayList<>();
        List<SystemStoreInfoDO> storeInfoDOS = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(storeIds)) {
            // 店铺所在组织    上级置灰   下级置灰     本级绑定什么门店能看什么门店
            storeInfoDOS = systemStoreInfoService.selectStoreOrgInfos(storeIds);
            if(!org.springframework.util.StringUtils.isEmpty(storeInfoDOS)){
                storeOrgIds = storeInfoDOS.stream().map(mm->mm.getOrgId()).collect(Collectors.toList());
            }
        }

        // 本级 + 店铺本级
        Collection<Long> unionIds = CollectionUtil.union(orgIds, storeOrgIds);

        // 查询所有本级
        if (CollectionUtil.isEmpty(unionIds)){
            return List.of();
        }
        List<OrgDO> orgList = orgMapper.selectByIds(unionIds);
        List<OrgDO> orgListTwo = new ArrayList<>();
        if(ObjectUtil.isNotEmpty(orgIds)){
            //本级组织
            orgListTwo = orgMapper.selectByIds(orgIds);
        }



        // 查询所有上级ids
        Set<Long> allOrgIds = new HashSet<>();
        for (OrgDO org : orgList) {
            if (StringUtils.isNotBlank(org.getAncestors())) {
                List<Long> parents = Arrays.stream(org.getAncestors().split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Long::parseLong)
                        .filter(id -> !Objects.equals(id, org.getId()))
                        .toList();
                allOrgIds.addAll(parents);
            }
        }

        // 查询所有上级
        List<OrgDO> upOrgList = orgMapper.selectByIds(allOrgIds);

        Set<OrgDO> allOrgSetTwo = new HashSet<>();
        // 查询所有下级
        Set<OrgDO> allOrgSet = new HashSet<>();
        for (Long orgId : orgIds) {
            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
            allOrgSet.addAll(childOrgList);
            allOrgSetTwo.addAll(childOrgList);
        }
//        allOrgSet.addAll(upOrgList);
        allOrgSet.addAll(orgList);
        allOrgSetTwo.addAll(orgListTwo);
        // 查询所有本级、下级节点的ID
        Set<Long> collect = allOrgSet.stream().map(OrgDO::getId).collect(Collectors.toSet());
        Set<Long> collectTwo = new HashSet<>();
        if(ObjectUtil.isNotEmpty(allOrgSetTwo)){
            // 查询所有本级
            collectTwo = allOrgSetTwo.stream().map(OrgDO::getId).collect(Collectors.toSet());
        }



        // 组织节点店数
//        List<OrgStoreNum> storeNumList = systemStoreInfoService.getNodeStoreNum(collect);
//        if (CollectionUtil.isNotEmpty(storeNumList)) {
//            storeNumMap = storeNumList.parallelStream()
//                    .collect(Collectors.toMap(
//                            OrgStoreNum::getOrgId,
//                            OrgStoreNum::getStoreNum,
//                            (existing, replacement) -> existing,
//                            ConcurrentHashMap::new
//                    ));
//        }


        // 组织节点店数 map
        Map<Long, List<SystemStoreInfoDO>> storeMap = systemStoreInfoService.selectStoreByOrgIds(collect).stream()
                .collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));


        if(ObjectUtil.isNotEmpty(storeIds)){
            Set<Long> longSet = new HashSet<>();
            for (Long storeId : storeIds) {
                LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreId,storeId);
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreSource,0);
                SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectOne(lambdaQueryWrapper);
                if(ObjectUtil.isNotEmpty(systemStoreInfoDO)){
                    Long orgId = systemStoreInfoDO.getOrgId();
                    if(ObjectUtil.isNotEmpty(orgId) && ObjectUtil.isNotEmpty(allOrgSetTwo)){
                        List<Long> collectList = allOrgSetTwo.stream().map(mm -> mm.getId()).collect(Collectors.toList());
                        if (!collectList.contains(orgId)) {
                            longSet.add(storeId);
                        }
                    }else if(ObjectUtil.isNotEmpty(orgId)&& ObjectUtil.isEmpty(allOrgSetTwo)){
                        longSet.add(storeId);
                    }
                }

            }

            if(ObjectUtil.isNotEmpty(longSet)){
                List<SystemStoreInfoDO> list = new ArrayList<>();
                LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.in(SystemStoreInfoDO::getStoreId,storeIds);
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreSource,0);
                List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(lambdaQueryWrapper);
                if(ObjectUtil.isNotEmpty(systemStoreInfoDOS)){
                    list.addAll(systemStoreInfoDOS);
                }
                if(ObjectUtil.isNotEmpty(list)){
                    Map<Long, List<SystemStoreInfoDO>> listMap = list.stream().collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));
                    for (Long aLong : listMap.keySet()) {
                        if (!orgIds.contains(aLong)) {
                            storeMap.put(aLong, listMap.get(aLong));
                        }
                    }
                }
            }

        }

        // 加入上级节点
        allOrgSet.addAll(upOrgList);
        Map<Long, Integer> finalStoreNumMap = storeNumMap;
        Set<Long> finalCollectTwo = collectTwo;
        List<OrgStoreTreeRespVO> voList = allOrgSet.parallelStream()
                .flatMap(org -> {
                    // 生成不包含门店信息的 OrgStoreTreeRespVO 对象
                    OrgStoreTreeRespVO voWithoutStore = createOrgStoreTreeRespVOWithoutStoreTwo(org, finalCollectTwo);
                    // 获取与当前 OrgDO 对应的 SystemStoreInfoDO 对象列表
                    List<SystemStoreInfoDO> stores = storeMap.get(org.getId());
                    if (CollectionUtil.isEmpty(stores)) {
                        return Stream.of(voWithoutStore);
                    }
                    // 生成包含门店信息的 OrgStoreTreeRespVO 对象
                    List<OrgStoreTreeRespVO> voWithStores = stores.stream()
                            .map(store -> createOrgStoreTreeRespWithStore(org, store, finalStoreNumMap))
                            .collect(Collectors.toList());
                    // 合并不包含门店信息的对象和包含门店信息的对象
                    return Stream.concat(Stream.of(voWithoutStore), voWithStores.stream());
                })
                .collect(Collectors.toList());

        return voList;
    }

    private OrgStoreTreeRespVO createOrgStoreTreeRespVOWithoutStoreTwo(OrgDO org, Set<Long> collect) {
        OrgStoreTreeRespVO vo = new OrgStoreTreeRespVO();
        vo.setId(org.getId());
        vo.setName(org.getName());
        vo.setParentId(org.getParentId());
        vo.setLevel(org.getLevel());
        vo.setAncestors(org.getAncestors());
        vo.setChildren(new ArrayList<>());
        vo.setSort(org.getSort());
        vo.setIsStore(0);
        if(collect.contains(org.getId())){
            vo.setIsFlag(false);
        }
        return vo;
    }

    @Override
    public List<OrgStoreCityTreeRespVO> getOrgStoreByCity() {
        //组织节点店数map
        Map<Long, Integer> storeNumMap = new ConcurrentHashMap<>(8);
        List<OrgStoreCityTreeRespVO> voListTwo = new ArrayList<>();
        Long userId = WebFrameworkUtils.getLoginUserId();

        // 当前用户的机构ID
        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);
        // 当前用户的店铺ID
        List<Long> storeIds = storeUserService.selectUserStoreIdsTwo(userId);

        if (CollectionUtil.isEmpty(orgIds) && CollectionUtil.isEmpty(storeIds)) {
            return List.of();
        }
        Map<Long, Integer> finalStoreNumMap = storeNumMap;
        // 门店的组织ids
        List<Long> storeOrgIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(storeIds)) {
            storeOrgIds = systemStoreInfoService.selectStoreOrgIds(storeIds);
        }

        // 本级 + 店铺本级
        Collection<Long> unionIds = CollectionUtil.union(orgIds, storeOrgIds);

        // 查询所有本级
        if (CollectionUtil.isEmpty(unionIds)){
            return List.of();
        }
        List<OrgDO> orgList = orgMapper.selectByIds(unionIds);

        // 查询所有上级ids
        Set<Long> allOrgIds = new HashSet<>();
        for (OrgDO org : orgList) {
            if (StringUtils.isNotBlank(org.getAncestors())) {
                List<Long> parents = Arrays.stream(org.getAncestors().split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Long::parseLong)
                        .filter(id -> !Objects.equals(id, org.getId()))
                        .toList();
                allOrgIds.addAll(parents);
            }
        }

        // 查询所有上级
        List<OrgDO> upOrgList = orgMapper.selectByIds(allOrgIds);
        List<OrgDO> orgListTwo = new ArrayList<>();
        if(ObjectUtil.isNotEmpty(orgIds)){
            //本级组织
            orgListTwo = orgMapper.selectByIds(orgIds);
        }

        Set<OrgDO> allOrgSetTwo = new HashSet<>();
        // 查询所有下级
        Set<OrgDO> allOrgSet = new HashSet<>();
        for (Long orgId : orgIds) {
            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
            allOrgSet.addAll(childOrgList);
            allOrgSetTwo.addAll(childOrgList);
        }
 
        allOrgSet.addAll(orgList);
        allOrgSetTwo.addAll(orgListTwo);
        // 查询所有本级、下级节点的ID
        Set<Long> collect = allOrgSet.stream().map(OrgDO::getId).collect(Collectors.toSet());

        // 查询所有本级
//        Set<Long> collectTwo = orgList.stream().map(OrgDO::getId).collect(Collectors.toSet());
        // 组织节点店数 map
        Map<Long, List<SystemStoreInfoDO>> storeMap = systemStoreInfoService.selectStoreByOrgIds(collect).stream()
                .collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));
//        if(ObjectUtil.isNotEmpty(storeIds)){
//            LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//            lambdaQueryWrapper.in(SystemStoreInfoDO::getStoreId,storeIds);
//            lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreSource,0);
//            List<SystemStoreInfoDO> list =systemStoreInfoMapper.selectList(lambdaQueryWrapper);
//            if(ObjectUtil.isNotEmpty(list)){
//                Map<Long, List<SystemStoreInfoDO>> listMap = list.stream().collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));
//                for (Long aLong : listMap.keySet()) {
//                    if (!orgIds.contains(aLong)) {
//                        storeMap.put(aLong, listMap.get(aLong));
//                    }
//                }
//            }
//        }

        if(ObjectUtil.isNotEmpty(storeIds)){
            Set<Long> longSet = new HashSet<>();
            for (Long storeId : storeIds) {
                LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreId,storeId);
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreSource,0);
                SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectOne(lambdaQueryWrapper);
                if(ObjectUtil.isNotEmpty(systemStoreInfoDO)){
                    Long orgId = systemStoreInfoDO.getOrgId();
                    if(ObjectUtil.isNotEmpty(orgId) && ObjectUtil.isNotEmpty(allOrgSetTwo)){
                        List<Long> collectList = allOrgSetTwo.stream().map(mm -> mm.getId()).collect(Collectors.toList());
                        if (!collectList.contains(orgId)) {
                            longSet.add(storeId);
                        }
                    }else if(ObjectUtil.isNotEmpty(orgId)&& ObjectUtil.isEmpty(allOrgSetTwo)){
                        longSet.add(storeId);
                    }
                }

            }

            if(ObjectUtil.isNotEmpty(longSet)){
                List<SystemStoreInfoDO> list = new ArrayList<>();
                LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.in(SystemStoreInfoDO::getStoreId,storeIds);
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreSource,0);
                List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(lambdaQueryWrapper);
                if(ObjectUtil.isNotEmpty(systemStoreInfoDOS)){
                    list.addAll(systemStoreInfoDOS);
                }
                if(ObjectUtil.isNotEmpty(list)){
                    Map<Long, List<SystemStoreInfoDO>> listMap = list.stream().collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));
                    for (Long aLong : listMap.keySet()) {
                        if (!orgIds.contains(aLong)) {
                            storeMap.put(aLong, listMap.get(aLong));
                        }
                    }
                }
            }

        }



        allOrgSet.addAll(upOrgList);

        List<OrgStoreTreeRespVO> voList = allOrgSet.parallelStream()
                .flatMap(org -> {
                    // 生成不包含门店信息的 OrgStoreTreeRespVO 对象
                    OrgStoreTreeRespVO voWithoutStore = createOrgStoreTreeRespVOWithoutStore(org);
                    // 获取与当前 OrgDO 对应的 SystemStoreInfoDO 对象列表
                    List<SystemStoreInfoDO> stores = storeMap.get(org.getId());
                    if (CollectionUtil.isEmpty(stores)) {
                        return Stream.of(voWithoutStore);
                    }
                    // 生成包含门店信息的 OrgStoreTreeRespVO 对象
                    List<OrgStoreTreeRespVO> voWithStores = stores.stream()
                            .map(store -> createOrgStoreTreeRespWithStore(org, store, finalStoreNumMap))
                            .collect(Collectors.toList());
                    // 合并不包含门店信息的对象和包含门店信息的对象
                    return Stream.concat(Stream.of(voWithoutStore), voWithStores.stream());
                })
                .collect(Collectors.toList());

//        LambdaQueryWrapper<SystemStoreInfoDO> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(SystemStoreInfoDO::getOrgId,collect);
//        List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(wrapper);
        if(ObjectUtil.isNotEmpty(voList)){
            List<OrgStoreTreeRespVO> voLists = new ArrayList<>();
            for (OrgStoreTreeRespVO vo : voList) {
                if(!org.springframework.util.StringUtils.isEmpty(vo.getStoreCity())){
                    voLists.add(vo);
                }

            }
//            Map<String, List<OrgStoreTreeRespVO>> listMap = voList.stream().collect(
//                    Collectors.groupingBy(OrgStoreTreeRespVO::getStoreCity));
            Map<String, List<OrgStoreTreeRespVO>> listMap = voLists.stream()
                    .collect(Collectors.groupingBy(
                            OrgStoreTreeRespVO::getStoreCity,
                            Collectors.filtering(x -> true, Collectors.toList())
                    ))
                    .entrySet().stream()
                    .filter(entry -> !entry.getValue().isEmpty())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

//            for (Map.Entry<String, List<OrgStoreTreeRespVO>> nmap : listMap.entrySet()) {
//                nmap.getValue().sort(Comparator.comparing(OrgStoreTreeRespVO::getCreateTime));
//            }
            for (String s : listMap.keySet()) {
                Random random = new Random();
                long nextLong = random.nextLong();
                List<OrgStoreTreeRespVO> systemStoreInfoDOS1 = listMap.get(s);

                OrgStoreCityTreeRespVO orgStoreCityTreeRespVO = new OrgStoreCityTreeRespVO();

                orgStoreCityTreeRespVO.setId(nextLong);
                orgStoreCityTreeRespVO.setStoreNum(systemStoreInfoDOS1.size());
                orgStoreCityTreeRespVO.setName(s);
                orgStoreCityTreeRespVO.setIsStore(0);
                orgStoreCityTreeRespVO.setParentId(0L);
                voListTwo.add(orgStoreCityTreeRespVO);
                if(systemStoreInfoDOS1.size()>0){

                    for (OrgStoreTreeRespVO systemStoreInfoDO : systemStoreInfoDOS1) {
                        OrgStoreCityTreeRespVO respVO = new OrgStoreCityTreeRespVO();
                        respVO.setName(systemStoreInfoDO.getStoreName());
                        respVO.setIsStore(1);
                        respVO.setParentId(nextLong);
                        respVO.setStoreId(systemStoreInfoDO.getStoreId());
                        respVO.setId(systemStoreInfoDO.getStoreId());
                        voListTwo.add(respVO);

                    }


                }
            }

        }
        return voListTwo;


    }

    @Override
    public Set<StoreOrgDTO> getOrgListByStoreId(List<Long> storeIds) {
        List<OrgDO> orgList = orgMapper.selectList();
        List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectByIds(storeIds);
        return convertToStoreOrgList(orgList, systemStoreInfoDOS);
    }

    @Override
    public Set<StoreOrgDTO> getOrgListByStoreIdV2(List<Long> storeIds) {
        List<OrgDO> orgList = orgMapper.selectList();
        List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectByIds(storeIds);
        return convertToStoreOrgListV2(orgList, systemStoreInfoDOS);
    }

    @Override
    public Set<StoreOrgDTO> getAllOrgListByStoreIdV2() {
        List<OrgDO> orgList = orgMapper.selectList();
        List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList();
        return convertToStoreOrgListV2(orgList, systemStoreInfoDOS);
    }

    @Override
    public Boolean sortOrg(List<SortOrgReqVO> sortOrgList) {
        if (ObjectUtil.isEmpty(sortOrgList)) {
            return Boolean.TRUE;
        }
        List<OrgDO> updateEntityList = sortOrgList.stream().map(sortOrgReqVO -> {
            OrgDO orgDO = new OrgDO();
            orgDO.setId(sortOrgReqVO.getOrgId());
            orgDO.setSort(sortOrgReqVO.getSort());
            return orgDO;
        }).toList();
        return orgMapper.updateBatch(updateEntityList);
    }

    @Override
    public List<OrgStoreTreeRespVO> getAllOrgStoreList(Boolean filterClosedStore) {


        LambdaQueryWrapper<OrgDO> orgDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orgDOLambdaQueryWrapper.eq(OrgDO::getStatus,1);
        List<OrgDO> orgDOList = orgMapper.selectList(orgDOLambdaQueryWrapper);


        if(ObjectUtil.isNotEmpty(orgDOList)){
            Set<Long> collect = orgDOList.stream().map(mm -> mm.getId()).collect(Collectors.toSet());

            // 组织节点店数 map
            Map<Long, List<SystemStoreInfoDO>> storeMap = systemStoreInfoService.selectStoreByOrgIds(collect).stream()
                    .filter(store -> !Boolean.TRUE.equals(filterClosedStore) || !Objects.equals(store.getStoreStatus(), 1))
                    .collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));
            List<OrgStoreTreeRespVO> voList = orgDOList.stream()
                    .flatMap(org -> {
                        // 生成不包含门店信息的 OrgStoreTreeRespVO 对象
                        OrgStoreTreeRespVO voWithoutStore = createOrgStoreTreeRespVOWithoutStore(org);
                        // 获取与当前 OrgDO 对应的 SystemStoreInfoDO 对象列表
                        List<SystemStoreInfoDO> stores = storeMap.get(org.getId());
                        if (CollectionUtil.isEmpty(stores)) {
                            return Stream.of(voWithoutStore);
                        }
                        // 生成包含门店信息的 OrgStoreTreeRespVO 对象
                        List<OrgStoreTreeRespVO> voWithStores = stores.stream()
                                .map(store -> createOrgStoreTreeRespWithStoreTwo(org, store))
                                .collect(Collectors.toList());
                        // 合并不包含门店信息的对象和包含门店信息的对象
                        return Stream.concat(Stream.of(voWithoutStore), voWithStores.stream());
                    })
                    .collect(Collectors.toList());

            return voList;
        }
        return new ArrayList<>();
    }

    @Override
    public List<OrgTreeRespVO> getAllOrgList() {

//        Long userId = WebFrameworkUtils.getLoginUserId();
//        //当前用户的机构ID
//        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);
//        if (CollectionUtil.isEmpty(orgIds)) {
//            return List.of();
//        }
//        //当前用户的店铺ID
//        List<Long> storeIds = storeUserService.selectUserStoreIds(userId);
//        if (CollectionUtil.isEmpty(orgIds) && CollectionUtil.isEmpty(storeIds)) {
//            return List.of();
//        }
        // 门店的组织ids
//        List<Long> storeOrgIds = new ArrayList<>();
//        if (CollectionUtil.isNotEmpty(storeIds)) {
//            storeOrgIds = systemStoreInfoService.selectStoreOrgIds(storeIds);
//        }
//        // 本级 + 店铺本级
//        Collection<Long> unionIds = CollectionUtil.union(orgIds, storeOrgIds);
//        // 查询所有本级
//        if (CollectionUtil.isEmpty(unionIds)) {
//            return List.of();
//        }
        LambdaQueryWrapper<OrgDO> orgDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orgDOLambdaQueryWrapper.eq(OrgDO::getStatus,1);
        List<OrgDO> orgDOList = orgMapper.selectList(orgDOLambdaQueryWrapper);
//        List<OrgDO> orgList = orgMapper.selectByIds(unionIds);
        // 查询所有上级ids
//        Set<Long> allOrgIds = new HashSet<>();
//        for (OrgDO org : orgList) {
//            if (StringUtils.isNotBlank(org.getAncestors())) {
//                List<Long> parents = Arrays.stream(org.getAncestors().split(","))
//                        .filter(StringUtils::isNotBlank)
//                        .map(String::trim)
//                        .map(Long::parseLong)
//                        .filter(id -> ObjectUtil.notEqual(id, org.getId()))
//                        .toList();
//                allOrgIds.addAll(parents);
//            }
//        }
//        // 查询所有上级
//        List<OrgDO> upOrgList = orgMapper.selectByIds(allOrgIds);
        // 查询所有下级
//        Set<OrgDO> allOrgList = new HashSet<>();
//        for (Long orgId : orgIds) {
//            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
//            allOrgList.addAll(childOrgList);
//        }
//        allOrgList.addAll(orgList);
        // 查询所有本级、下级节点的ID
//        Set<Long> collect = allOrgList.stream().map(OrgDO::getId).collect(Collectors.toSet());
        // 只查询本级和下级的节点人数 店数

        // 组织节点人数map
//        Map<Long, Integer> userNumMap = new ConcurrentHashMap<>(8);
//        //组织节点店数map
//        Map<Long, Integer> storeNumMap = new ConcurrentHashMap<>(8);
//
//        // 组织节点人数
//        List<OrgUserNum> userNumList = orgUserService.getNodeUserNum(collect);
//        if (CollectionUtil.isNotEmpty(userNumList)) {
//            userNumMap = userNumList.parallelStream()
//                    .collect(Collectors.toMap(
//                            OrgUserNum::getOrgId,
//                            OrgUserNum::getUserNum,
//                            (existing, replacement) -> existing,
//                            ConcurrentHashMap::new
//                    ));
//        }
//        // 组织节点店数
//        List<OrgStoreNum> storeNumList = systemStoreInfoService.getNodeStoreNum(collect);
//        if (CollectionUtil.isNotEmpty(storeNumList)) {
//            storeNumMap = storeNumList.parallelStream()
//                    .collect(Collectors.toMap(
//                            OrgStoreNum::getOrgId,
//                            OrgStoreNum::getStoreNum,
//                            (existing, replacement) -> existing,
//                            ConcurrentHashMap::new
//                    ));
//        }
//
//        // 加入上级节点
//        allOrgList.addAll(upOrgList);
//
//        List<OrgTreeRespVO> voList = allOrgList.stream()
//                .map(createConverter(userNumMap, storeNumMap))
//                .collect(Collectors.toList());
//        voList.forEach(vo -> {
//            if (collect.contains(vo.getId())) {
//                vo.setIsMyOrg(1);
//            }
//        });
        List<OrgTreeRespVO> voList = BeanCopyUtils.copyBeanList(orgDOList, OrgTreeRespVO.class);
        return voList;
    }

    private OrgStoreTreeRespVO createOrgStoreTreeRespWithStoreTwo(OrgDO org, SystemStoreInfoDO store) {

        OrgStoreTreeRespVO vo = createOrgStoreTreeRespVOWithoutStore(org);
        vo.setParentId(org.getId());
        vo.setLevel(org.getLevel() + 1);
        vo.setId(store.getStoreId());
        vo.setName(store.getStoreName());
        vo.setStoreCity(store.getStoreCity());
        vo.setStoreName(store.getStoreName());
        vo.setStoreId(store.getStoreId());
        vo.setIdentificationTemplate(store.getIdentificationTemplate());
//        vo.setStoreNum(ObjectUtil.isNull(storeNumMap.get(org.getId())) ? 0 : storeNumMap.get(org.getId()));
        vo.setIsStore(1);
        return vo;
    }

    @Override
    public List<OrgTreeRespVO> getOrganizationsByLoginUser() {
        return List.of();
    }

    @Override
    public List<OrgTreeRespVO> getOrgAndStoreByLoginUser() {
        Long userId = WebFrameworkUtils.getLoginUserId();

        //当前用户的机构ID
        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);


        //当前用户的店铺ID
        List<Long> storeIds = storeUserService.selectUserStoreIds(userId);
        if (CollectionUtil.isEmpty(orgIds) && CollectionUtil.isEmpty(storeIds)) {
            return List.of();
        }

        // 门店的组织ids
        List<Long> storeOrgIds = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(storeIds)) {
            storeOrgIds = systemStoreInfoService.selectStoreOrgIds(storeIds);
        }

        // 本级 + 店铺本级
        Collection<Long> unionIds = CollectionUtil.union(orgIds, storeOrgIds);
        // 查询所有本级
        if (CollectionUtil.isEmpty(unionIds)) {
            return List.of();
        }
        List<OrgDO> orgList = orgMapper.selectByIds(unionIds);
        // 查询所有上级ids
        Set<Long> allOrgIds = new HashSet<>();
        for (OrgDO org : orgList) {
            if (StringUtils.isNotBlank(org.getAncestors())) {
                List<Long> parents = Arrays.stream(org.getAncestors().split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Long::parseLong)
                        .filter(id -> ObjectUtil.notEqual(id, org.getId()))
                        .toList();
                allOrgIds.addAll(parents);
            }
        }

        // 查询所有上级
        List<OrgDO> upOrgList = orgMapper.selectByIds(allOrgIds);
        // 查询所有下级
        Set<OrgDO> allOrgList = new HashSet<>();
        for (Long orgId : orgIds) {
            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
            allOrgList.addAll(childOrgList);
        }
        Set<Long> downOrg = allOrgList.stream().map(OrgDO::getId).collect(Collectors.toSet());

        Set<Long> thisAndDownnOrg = new HashSet<>();
        thisAndDownnOrg.addAll(orgIds);
        thisAndDownnOrg.addAll(downOrg);
        List<SystemStoreInfoDO> list = systemStoreInfoService.selectStoreByOrgIds(thisAndDownnOrg);
        List<StoreInfoDTO> storesByStoreIds = systemStoreInfoService.getStoresByStoreIds(storeIds);


        return List.of();
    }

    @Override
    public List<OrgStoreTreeRespVO> getOperationStoreList() {
        LambdaQueryWrapper<OrgDO> orgDOLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orgDOLambdaQueryWrapper.eq(OrgDO::getStatus,1);
        List<OrgDO> orgDOList = orgMapper.selectList(orgDOLambdaQueryWrapper);


        if(ObjectUtil.isNotEmpty(orgDOList)){
            Set<Long> collect = orgDOList.stream().map(mm -> mm.getId()).collect(Collectors.toSet());

            // 组织节点店数 map
            Map<Long, List<SystemStoreInfoDO>> storeMap = systemStoreInfoService.selectOperationStoreByOrgIds(collect).stream()
                    .collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));
            List<OrgStoreTreeRespVO> voList = orgDOList.stream()
                    .flatMap(org -> {
                        // 生成不包含门店信息的 OrgStoreTreeRespVO 对象
                        OrgStoreTreeRespVO voWithoutStore = createOrgStoreTreeRespVOWithoutStore(org);
                        // 获取与当前 OrgDO 对应的 SystemStoreInfoDO 对象列表
                        List<SystemStoreInfoDO> stores = storeMap.get(org.getId());
                        if (CollectionUtil.isEmpty(stores)) {
                            return Stream.of(voWithoutStore);
                        }
                        // 生成包含门店信息的 OrgStoreTreeRespVO 对象
                        List<OrgStoreTreeRespVO> voWithStores = stores.stream()
                                .map(store -> createOrgStoreTreeRespWithStoreTwo(org, store))
                                .collect(Collectors.toList());
                        // 合并不包含门店信息的对象和包含门店信息的对象
                        return Stream.concat(Stream.of(voWithoutStore), voWithStores.stream());
                    })
                    .collect(Collectors.toList());

            return voList;
        }
        return new ArrayList<>();
    }

    @Override
    public List<OrgStoreTreeRespVO> getOperationStoreListByUserData() {

        Long businessId = BusinessContextHolder.getBusinessId();
        if(businessId==11L){
            List<StoreSimpleResVO> list = DataPermissionUtils.executeIgnore(() -> systemStoreInfoService.storeGylListByUser());

            Long aa = 0L;
            if(ObjectUtil.isNotEmpty(list)){
                List<OrgStoreTreeRespVO> orgStoreTreeRespVOS = BeanCopyUtils.copyBeanList(list, OrgStoreTreeRespVO.class);
                for (OrgStoreTreeRespVO orgStoreTreeRespVO : orgStoreTreeRespVOS) {
                    orgStoreTreeRespVO.setLevel(2);
                    orgStoreTreeRespVO.setIsStore(1);
                    orgStoreTreeRespVO.setId(aa);
                    orgStoreTreeRespVO.setIsFlag(false);
                    orgStoreTreeRespVO.setParentId(0L);
                    orgStoreTreeRespVO.setName(orgStoreTreeRespVO.getStoreName());
                    aa++;
                }
                return orgStoreTreeRespVOS;
            }else{
                return new ArrayList<>();
            }


        }

        //组织节点店数map
        Map<Long, Integer> storeNumMap = new ConcurrentHashMap<>(8);

        Long userId = WebFrameworkUtils.getLoginUserId();

        // 当前用户的组织ID
        List<Long> orgIds = orgUserService.selectUserOrgIds(userId);
        // 当前用户的店铺ID
        List<Long> storeIds = storeUserService.selectUserStoreIdsTwo(userId);

        if (CollectionUtil.isEmpty(orgIds) && CollectionUtil.isEmpty(storeIds)) {
            return List.of();

        }

        // 门店的组织ids
        List<Long> storeOrgIds = new ArrayList<>();
        List<SystemStoreInfoDO> storeInfoDOS = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(storeIds)) {
            // 店铺所在组织    上级置灰   下级置灰     本级绑定什么门店能看什么门店
            storeInfoDOS = systemStoreInfoService.selectStoreOrgInfos(storeIds);
            if(!org.springframework.util.StringUtils.isEmpty(storeInfoDOS)){
                storeOrgIds = storeInfoDOS.stream().map(mm->mm.getOrgId()).collect(Collectors.toList());
            }
        }

        // 本级 + 店铺本级
        Collection<Long> unionIds = CollectionUtil.union(orgIds, storeOrgIds);

        // 查询所有本级
        if (CollectionUtil.isEmpty(unionIds)){
            return List.of();
        }
        List<OrgDO> orgList = orgMapper.selectByIds(unionIds);
        List<OrgDO> orgListTwo = new ArrayList<>();
        if(ObjectUtil.isNotEmpty(orgIds)){
            //本级组织
            orgListTwo = orgMapper.selectByIds(orgIds);
        }



        // 查询所有上级ids
        Set<Long> allOrgIds = new HashSet<>();
        for (OrgDO org : orgList) {
            if (StringUtils.isNotBlank(org.getAncestors())) {
                List<Long> parents = Arrays.stream(org.getAncestors().split(","))
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .map(Long::parseLong)
                        .filter(id -> !Objects.equals(id, org.getId()))
                        .toList();
                allOrgIds.addAll(parents);
            }
        }

        // 查询所有上级
        List<OrgDO> upOrgList = orgMapper.selectByIds(allOrgIds);

        Set<OrgDO> allOrgSetTwo = new HashSet<>();
        // 查询所有下级
        Set<OrgDO> allOrgSet = new HashSet<>();
        for (Long orgId : orgIds) {
            Set<OrgDO> childOrgList = orgMapper.getChildOrgList(orgId);
            allOrgSet.addAll(childOrgList);
            allOrgSetTwo.addAll(childOrgList);
        }
//        allOrgSet.addAll(upOrgList);
        allOrgSet.addAll(orgList);
        allOrgSetTwo.addAll(orgListTwo);
        // 查询所有本级、下级节点的ID
        Set<Long> collect = allOrgSet.stream().map(OrgDO::getId).collect(Collectors.toSet());
        Set<Long> collectTwo = new HashSet<>();
        if(ObjectUtil.isNotEmpty(allOrgSetTwo)){
            // 查询所有本级
            collectTwo = allOrgSetTwo.stream().map(OrgDO::getId).collect(Collectors.toSet());
        }


        // 组织节点店数 map
        Map<Long, List<SystemStoreInfoDO>> storeMap = systemStoreInfoService.selectOperationStoreByOrgIds(collect).stream()
                .collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));


        if(ObjectUtil.isNotEmpty(storeIds)){
            Set<Long> longSet = new HashSet<>();
            for (Long storeId : storeIds) {
                LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreId,storeId);
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreSource,0);
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreStatus,0);
                SystemStoreInfoDO systemStoreInfoDO = systemStoreInfoMapper.selectOne(lambdaQueryWrapper);
                if(ObjectUtil.isNotEmpty(systemStoreInfoDO)){
                    Long orgId = systemStoreInfoDO.getOrgId();
                    if(ObjectUtil.isNotEmpty(orgId) && ObjectUtil.isNotEmpty(allOrgSetTwo)){
                        List<Long> collectList = allOrgSetTwo.stream().map(mm -> mm.getId()).collect(Collectors.toList());
                        if (!collectList.contains(orgId)) {
                            longSet.add(storeId);
                        }
                    }else if(ObjectUtil.isNotEmpty(orgId)&& ObjectUtil.isEmpty(allOrgSetTwo)){
                        longSet.add(storeId);
                    }
                }

            }

            if(ObjectUtil.isNotEmpty(longSet)){
                List<SystemStoreInfoDO> list = new ArrayList<>();
                LambdaQueryWrapper<SystemStoreInfoDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
                lambdaQueryWrapper.in(SystemStoreInfoDO::getStoreId,storeIds);
                lambdaQueryWrapper.eq(SystemStoreInfoDO::getStoreSource,0);
                List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(lambdaQueryWrapper);
                if(ObjectUtil.isNotEmpty(systemStoreInfoDOS)){
                    list.addAll(systemStoreInfoDOS);
                }
                if(ObjectUtil.isNotEmpty(list)){
                    Map<Long, List<SystemStoreInfoDO>> listMap = list.stream().collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));
                    for (Long aLong : listMap.keySet()) {
                        if (!orgIds.contains(aLong)) {
                            storeMap.put(aLong, listMap.get(aLong));
                        }
                    }
                }
            }

        }

        // 加入上级节点
        allOrgSet.addAll(upOrgList);
        Map<Long, Integer> finalStoreNumMap = storeNumMap;
        Set<Long> finalCollectTwo = collectTwo;
        List<OrgStoreTreeRespVO> voList = allOrgSet.parallelStream()
                .flatMap(org -> {
                    // 生成不包含门店信息的 OrgStoreTreeRespVO 对象
                    OrgStoreTreeRespVO voWithoutStore = createOrgStoreTreeRespVOWithoutStoreTwo(org, finalCollectTwo);
                    // 获取与当前 OrgDO 对应的 SystemStoreInfoDO 对象列表
                    List<SystemStoreInfoDO> stores = storeMap.get(org.getId());
                    if (CollectionUtil.isEmpty(stores)) {
                        return Stream.of(voWithoutStore);
                    }
                    // 生成包含门店信息的 OrgStoreTreeRespVO 对象
                    List<OrgStoreTreeRespVO> voWithStores = stores.stream()
                            .map(store -> createOrgStoreTreeRespWithStore(org, store, finalStoreNumMap))
                            .collect(Collectors.toList());
                    // 合并不包含门店信息的对象和包含门店信息的对象
                    return Stream.concat(Stream.of(voWithoutStore), voWithStores.stream());
                })
                .collect(Collectors.toList());

        return voList;
    }

    private Set<StoreOrgDTO> convertToStoreOrgList(List<OrgDO> orgList, List<SystemStoreInfoDO> orgStores) {
        // 先将orgs转为Map便于查找
        Map<Long, OrgDO> orgMap = orgList.stream()
                .collect(Collectors.toMap(OrgDO::getId, org -> org));

        return orgStores.stream()
                .map(orgStore -> {
                    StoreOrgDTO dto = new StoreOrgDTO();
                    BeanUtils.copyProperties(orgStore, dto);

                    // 获取关联的org信息
                    OrgDO org = orgMap.get(orgStore.getOrgId());
                    if (org != null) {
                        // 拼接祖级orgName（根据业务需求调整）
                        dto.setOrgName(buildFullOrgName(org, orgMap));
                    }
                    return dto;
                })
                .collect(Collectors.toSet());
    }

    private Set<StoreOrgDTO> convertToStoreOrgListV2(List<OrgDO> orgList, List<SystemStoreInfoDO> orgStores) {
        // 先将orgs转为Map便于查找
        Map<Long, OrgDO> orgMap = orgList.stream()
                .collect(Collectors.toMap(OrgDO::getId, org -> org));

        return orgStores.stream()
                .map(orgStore -> {
                    StoreOrgDTO dto = new StoreOrgDTO();
                    BeanUtils.copyProperties(orgStore, dto);

                    // 获取关联的org信息
                    OrgDO org = orgMap.get(orgStore.getOrgId());
                    if (org != null) {
                        // 拼接祖级orgName（根据业务需求调整）
                        dto.setOrgName(buildFullOrgNameV2(org, orgMap));
                    }
                    return dto;
                })
                .collect(Collectors.toSet());
    }

    /**
     * 递归构建完整的祖级组织名称
     */
    private String buildFullOrgName(OrgDO org, Map<Long, OrgDO> orgMap) {
        if (org.getAncestors() == null || org.getAncestors().isEmpty()) {
            return org.getName();
        }

        // 示例：假设ancestors格式为 "1,2,3"
        List<String> ancestorNames = Arrays.stream(org.getAncestors().split(","))
                .map(Long::parseLong)
                .map(orgId -> orgMap.containsKey(orgId) ? orgMap.get(orgId).getName() : "")
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());

        //ancestorNames.add(org.getName());
        return String.join(";", ancestorNames);
    }


    /**
     * 递归构建完整的祖级组织名称
     */
    private String buildFullOrgNameV2(OrgDO org, Map<Long, OrgDO> orgMap) {
        if (org.getAncestors() == null || org.getAncestors().isEmpty()) {
            return org.getName();
        }

        // 示例：假设ancestors格式为 "1,2,3"
        List<String> ancestorNames = Arrays.stream(org.getAncestors().split(","))
                .map(Long::parseLong)
                .map(orgId -> orgMap.containsKey(orgId) ? orgMap.get(orgId).getName() : "")
                .filter(name -> !name.isEmpty())
                .collect(Collectors.toList());

        //ancestorNames.add(org.getName());
        return String.join("/", ancestorNames);
    }

    @Override
    public AdminUserDO getOrgLeaderByOrgId(Long orgId) {
        orgUserService.getOrgLeaderIdByOrgId(orgId);
        return null;
    }

    private OrgStoreTreeRespVO createOrgStoreTreeRespWithStore(OrgDO org, SystemStoreInfoDO store, Map<Long, Integer> storeNumMap) {
        OrgStoreTreeRespVO vo = createOrgStoreTreeRespVOWithoutStore(org);
        vo.setParentId(org.getId());
        vo.setLevel(org.getLevel() + 1);
        vo.setId(store.getStoreId());
        vo.setName(store.getStoreName());
        vo.setStoreCity(store.getStoreCity());
        vo.setStoreName(store.getStoreName());
        vo.setStoreId(store.getStoreId());
        vo.setIsAllProduct(store.getIsAllProduct());
        vo.setIsAllCommdity(store.getIsAllCommdity());
        vo.setIsSameLine(store.getIsSameLine());
        vo.setWarehouseId(store.getWarehouseId());
        vo.setWarehouseName(store.getWarehouseName());
        vo.setProjectId(store.getProjectId());
        vo.setProjectCode(store.getProjectCode());
        vo.setProjectName(store.getProjectName());
        vo.setDeliveryLineId(store.getDeliveryLineId());
        vo.setDeliveryLineName(store.getDeliveryLineName());
//        vo.setStoreNum(ObjectUtil.isNull(storeNumMap.get(org.getId())) ? 0 : storeNumMap.get(org.getId()));
        vo.setIsStore(1);
        vo.setIsFlag(false);
        return vo;
    }
}
