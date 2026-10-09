package com.htyoudao.youdao.module.system.service.applet.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.system.api.orgstore.OrgStoreApi;
import com.htyoudao.youdao.module.system.controller.admin.applet.vo.*;
import com.htyoudao.youdao.module.system.dal.dataobject.applet.AppletPageManagementDO;
import com.htyoudao.youdao.module.system.dal.dataobject.applet.AppletPageManagementStoreDO;
import com.htyoudao.youdao.module.system.dal.dataobject.applet.AppletPageManagementTagDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreTagDO;
import com.htyoudao.youdao.module.system.dal.dataobject.tagvalue.TagValueDO;
import com.htyoudao.youdao.module.system.dal.mysql.applet.AppletPageManagementMapper;
import com.htyoudao.youdao.module.system.dal.mysql.applet.AppletPageManagementStoreMapper;
import com.htyoudao.youdao.module.system.dal.mysql.applet.AppletPageManagementTagMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreTagMapper;
import com.htyoudao.youdao.module.system.dal.mysql.tagvalue.TagValueMapper;
import com.htyoudao.youdao.module.system.dal.redis.applet.AppletRedisDAO;
import com.htyoudao.youdao.module.system.enums.AppletPageLocationEnum;
import com.htyoudao.youdao.module.system.enums.applet.AppScopeEnum;
import com.htyoudao.youdao.module.system.enums.applet.StoreScopeEnum;
import com.htyoudao.youdao.module.system.service.applet.AppletPageManagementService;
import com.htyoudao.youdao.module.system.util.page.PageUtils;
import com.htyoudao.youdao.module.system.util.string.StringUtils;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.module.system.dal.redis.RedisKeyConstants.APPLET_PAGE_ALL_MANAGEMENT;
import static com.htyoudao.youdao.module.system.dal.redis.RedisKeyConstants.APPLET_PAGE_MANAGEMENT;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.APPLET_PAGE_ONLINE_UPDATE_ERROR;
import static com.htyoudao.youdao.module.system.enums.LogRecordConstants.*;

@Service
@Slf4j
public class AppletPageManagementServiceImpl implements AppletPageManagementService {

    @Resource
    private AppletPageManagementMapper appletPageManagementMapper;

    @Resource
    private AppletPageManagementStoreMapper appletPageManagementStoreMapper;

    @Resource
    private AppletPageManagementTagMapper appletPageManagementTagMapper;
    @Resource
    private SystemStoreTagMapper systemStoreTagMapper;

    @Resource
    private AppletRedisDAO appletRedisDAO;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private TagValueMapper tagValueMapper;

    @Resource
    private SystemStoreInfoMapper systemStoreInfoMapper;
    @Resource
    private AppletPageConfig appletPageConfig;

    @DubboReference
    private OrgStoreApi orgStoreApi;

    @Override
    public PageResult<AppletPageManagementFullStoreRespVO> getPage(AppletPageManagementReqVO appletPageManagement) {
        // ===================== 1. 组织/门店权限过滤 =====================
        Long orgReqId = appletPageManagement.getOrgId();
        Long storeReqId = appletPageManagement.getStoreId();
        List<Long> storeIds = new ArrayList<>();
        List<Long> appletPageIds = new ArrayList<>();

        // 1.1 获取门店集合
        if (ObjectUtil.isNotEmpty(orgReqId)) {
            CommonResult<List<Long>> result = orgStoreApi.selectByOrgStoreList(orgReqId);
            if (result != null) storeIds = result.getCheckedData();
        } else if (ObjectUtil.isNotEmpty(storeReqId)) {
            storeIds.add(storeReqId);
        }

        // 1.2 仅当有门店权限时，才查关联页面（大幅优化）
        if (CollectionUtil.isNotEmpty(storeIds)) {
            // ① 部分门店关联页面（只查ID，不查整行）
            List<Long> directPageIds = appletPageManagementStoreMapper.selectPageIdsByStoreIds(storeIds);

            // ② 标签关联页面（只查ID，不查整行）
            List<Long> indirectPageIds = appletPageManagementTagMapper.selectPageIdsByStoreIds(storeIds);

            // ③ 全部门店页面（只查ID）
            List<Long> allStorePageIds = appletPageManagementMapper.selectAllStorePageIds();

            // 合并
            appletPageIds.addAll(directPageIds);
            appletPageIds.addAll(indirectPageIds);
            appletPageIds.addAll(allStorePageIds);
            appletPageIds = appletPageIds.stream().distinct().toList();
        }

        // ===================== 2. 页面主表查询 =====================
        LambdaQueryWrapper<AppletPageManagementDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppletPageManagementDO::getDeleted, 0);

        if (CollectionUtil.isNotEmpty(appletPageIds)) {
            queryWrapper.in(AppletPageManagementDO::getAppletPageId, appletPageIds);
        }

        // 条件
        if (ObjectUtil.isNotEmpty(appletPageManagement.getAppletPageLocation())) {
            queryWrapper.eq(AppletPageManagementDO::getAppletPageLocation, appletPageManagement.getAppletPageLocation());
        }
        if (ObjectUtil.isNotEmpty(appletPageManagement.getAppletPageName())) {
            queryWrapper.like(AppletPageManagementDO::getAppletPageName, appletPageManagement.getAppletPageName());
        }

        // 排序
        queryWrapper.orderByDesc(AppletPageManagementDO::getUpdateTime);
        queryWrapper.orderByAsc(AppletPageManagementDO::getAppletPageStatus);

        // 分页
        Page<AppletPageManagementDO> page = appletPageManagementMapper.selectPage(PageUtils.getPageInfo(), queryWrapper);
        List<AppletPageManagementDO> pageList = page.getRecords();

        if (CollectionUtil.isEmpty(pageList)) {
            return new PageResult<>();
        }

        // ===================== 3. 批量预查询（性能核心！） =====================
        List<Long> pageIds = pageList.stream().map(AppletPageManagementDO::getAppletPageId).toList();

        // 一次性批量查所有页面-门店
        Map<Long, List<AppletPageManagementStoreDO>> pageStoreMap = new HashMap<>();
        if (pageIds.size() > 0) {
            List<AppletPageManagementStoreDO> pageStoreList = appletPageManagementStoreMapper.selectList(
                    Wrappers.lambdaQuery(AppletPageManagementStoreDO.class)
                            .in(AppletPageManagementStoreDO::getAppletPageId, pageIds)
                            .eq(AppletPageManagementStoreDO::getDeleted, 0)
            );
            pageStoreMap = pageStoreList.stream().collect(Collectors.groupingBy(AppletPageManagementStoreDO::getAppletPageId));
        }

        // 一次性批量查所有页面-标签
        Map<Long, List<AppletPageManagementTagDO>> pageTagMap = new HashMap<>();
        if (pageIds.size() > 0) {
            List<AppletPageManagementTagDO> pageTagList = appletPageManagementTagMapper.selectList(
                    Wrappers.lambdaQuery(AppletPageManagementTagDO.class)
                            .in(AppletPageManagementTagDO::getAppletPageId, pageIds)
                            .eq(AppletPageManagementTagDO::getDeleted, 0)
            );
            pageTagMap = pageTagList.stream().collect(Collectors.groupingBy(AppletPageManagementTagDO::getAppletPageId));
        }

        // ===================== 4. 组装VO =====================
        List<AppletPageManagementFullStoreRespVO> respList = new ArrayList<>();
        for (AppletPageManagementDO pageDO : pageList) {
            AppletPageManagementFullStoreRespVO respVO = new AppletPageManagementFullStoreRespVO();
            BeanUtils.copyProperties(pageDO, respVO);

            List<SystemStoreInfoDO> storeList = new ArrayList<>();
            int totalStore = 0;

            if (pageDO.getAppScope() == 1) {
                if (pageDO.getStoreScope() == 2) {
                    List<AppletPageManagementStoreDO> storeDOList = pageStoreMap.getOrDefault(pageDO.getAppletPageId(), Collections.emptyList());
                    totalStore = storeDOList.size();
                    storeList = storeDOList.stream().limit(3).map(e -> {
                        SystemStoreInfoDO vo = new SystemStoreInfoDO();
                        vo.setStoreId(e.getStoreId());
                        vo.setStoreName(e.getStoreName());
                        return vo;
                    }).toList();
                }
            }
            // 按标签
            else if (pageDO.getAppScope() == 2) {
                List<Long> tagIds = pageTagMap.getOrDefault(pageDO.getAppletPageId(), Collections.emptyList())
                        .stream()
                        .map(AppletPageManagementTagDO::getTagId)
                        .toList();

                if (CollectionUtil.isNotEmpty(tagIds)) {
                    List<Long> storeIdList = systemStoreTagMapper.selectStoreIdsByTagIds(tagIds);
                    totalStore = storeIdList.size();

                    if (CollectionUtil.isNotEmpty(storeIdList)) {
                        storeList = systemStoreInfoMapper.selectList(
                                        Wrappers.lambdaQuery(SystemStoreInfoDO.class)
                                                .in(SystemStoreInfoDO::getStoreId, storeIdList)
                                                .eq(SystemStoreInfoDO::getDeleted, 0)
                                ).stream()
                                .limit(3)
                                .toList();
                    }
                }
            }

            respVO.setStoreList(BeanUtils.toBean(storeList, FullStoreRespVO.class));
            respVO.setTotalStore(totalStore);
            respList.add(respVO);
        }

        PageResult<AppletPageManagementFullStoreRespVO> result = new PageResult<>();
        result.setList(respList);
        result.setTotal(page.getTotal());
        return result;
    }

    @Override
    @LogRecord(type = SYSTEM_APPLET_PAGE_MANAGEMENT_TYPE, subType = SYSTEM_APPLET_PAGE_MANAGEMENT_ADD_TYPE, bizNo = "{{#appletPageManagement.appletPageLocation}}", success = SYSTEM_APPLET_PAGE_MANAGEMENT_ADD_SUCCESS)
    public void createAppletPage(AppletPageManagementReqVO appletPageManagement) {
        // 1. 插入主表
        appletPageManagement.setAppletPageStatus(2);

        LambdaQueryWrapper<AppletPageManagementDO> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(AppletPageManagementDO::getAppletPageLocation, appletPageManagement.getAppletPageLocation());
        List<AppletPageManagementDO> appletPageManagementDOS = appletPageManagementMapper.selectList(nameWrapper);

        if (CollectionUtil.isNotEmpty(appletPageManagementDOS) && appletPageManagementDOS.size() >= appletPageConfig.getAppletPageCount()) {
            throw new ServiceException(APPLET_PAGE_COUNT_MAX_ERROR);
        }


        if (checkName(appletPageManagement, appletPageManagementDOS)){
            throw new ServiceException(APPLET_PAGE_NAME_UNIQUE_ERROR);
        }

        AppletPageManagementDO managementDO = BeanUtils.toBean(appletPageManagement, AppletPageManagementDO.class);
        appletPageManagementMapper.insert(managementDO);

        // 拿到刚插入的主键
        Long appletPageId = managementDO.getAppletPageId();

        // 2. 应用范围：全部门店 / 部分门店
        Integer appScope = appletPageManagement.getAppScope();

        // 部分门店
        Integer storeScope = appletPageManagement.getStoreScope();
        if (ObjectUtil.equals(StoreScopeEnum.PART.getType(), storeScope)) {
            List<AppletPageManagementStoreReqVO> storeList = appletPageManagement.getStoreList();
            if (CollectionUtil.isNotEmpty(storeList)) {
                List<AppletPageManagementStoreDO> storeDOList = storeList.stream().map(store -> {
                    AppletPageManagementStoreDO storeDO = new AppletPageManagementStoreDO();
                    storeDO.setAppletPageId(appletPageId);
                    storeDO.setStoreId(store.getStoreId());
                    storeDO.setStoreName(store.getStoreName());
                    storeDO.setAppletPageLocation(appletPageManagement.getAppletPageLocation());
                    return storeDO;
                }).toList();
                appletPageManagementStoreMapper.insertBatch(storeDOList);
            }
        }

        // 标签门店
        if (ObjectUtil.equals(AppScopeEnum.TAG.getType(), appScope)) {
            List<Long> tagList = appletPageManagement.getTagList();
            if (CollectionUtil.isNotEmpty(tagList)) {
                List<AppletPageManagementTagDO> tagDOList = tagList.stream().map(tagId -> {
                    AppletPageManagementTagDO tagDO = new AppletPageManagementTagDO();
                    tagDO.setAppletPageId(appletPageId);
                    tagDO.setTagId(tagId);
                    tagDO.setAppletPageLocation(appletPageManagement.getAppletPageLocation());
                    return tagDO;
                }).toList();
                appletPageManagementTagMapper.insertBatch(tagDOList);
            }
        }


        // 操作日志
        LogRecordContext.putVariable("appletPageManagement", appletPageManagement);
    }

    private boolean checkName(AppletPageManagementReqVO appletPageManagement, List<AppletPageManagementDO> appletPageManagementDOS) {
        // 空集合直接返回不重复
        if (CollectionUtil.isEmpty(appletPageManagementDOS)) {
            return false;
        }

        // 检查名称是否重复
        return appletPageManagementDOS.stream()
                .anyMatch(ele -> ObjectUtil.equals(appletPageManagement.getAppletPageName(), ele.getAppletPageName()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_APPLET_PAGE_MANAGEMENT_TYPE, subType = SYSTEM_APPLET_PAGE_MANAGEMENT_STATUS_UPDATE_TYPE, bizNo = "{{#appletPageManagement.appletPageId}}", success = SYSTEM_APPLET_PAGE_MANAGEMENT_STATUS_UPDATE_SUCCESS)
    public void updateAppletPageStatus(AppletPageManagementReqVO appletPageManagement) {
        Long businessId = BusinessContextHolder.getBusinessId();

        LambdaQueryWrapper<AppletPageManagementDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppletPageManagementDO::getAppletPageId, appletPageManagement.getAppletPageId());
        AppletPageManagementDO appletPageManagementDO = appletPageManagementMapper.selectOne(wrapper);

        // 兜底下架拦截
        checkGuaranteeStatus(appletPageManagement, appletPageManagementDO);

        if (ObjectUtil.equals(1, appletPageManagement.getAppletPageStatus())) {
            //是要启用
            LambdaUpdateWrapper<AppletPageManagementDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AppletPageManagementDO::getAppletPageId, appletPageManagement.getAppletPageId());
            updateWrapper.set(AppletPageManagementDO::getAppletPageStatus, 1);
            updateWrapper.set(AppletPageManagementDO::getBusinessId, businessId);
            // 记录发布时间
            updateWrapper.set(AppletPageManagementDO::getReleaseTime, LocalDateTime.now());
            appletPageManagementMapper.update(updateWrapper);

            // 构建发布顺序缓存
            String redisKey = "APPLET_SX_"+businessId +"_"+appletPageManagement.getAppletPageLocation();

            appletRedisDAO.zAdd(redisKey, String.valueOf(appletPageManagement.getAppletPageId()));
            // 启用缓存
            addRedisAppletPage(appletPageManagement.getAppletPageId(), appletPageManagement.getAppletPageLocation(), appletPageManagementDO, businessId);

        } else {

            //是要禁用
            LambdaUpdateWrapper<AppletPageManagementDO> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(AppletPageManagementDO::getAppletPageId, appletPageManagement.getAppletPageId());
            updateWrapper.set(AppletPageManagementDO::getAppletPageStatus, 2);
            updateWrapper.set(AppletPageManagementDO::getBusinessId, businessId);
            appletPageManagementMapper.update(updateWrapper);

            // 构建发布顺序缓存
            String redisKey = "APPLET_SX_"+businessId +"_"+appletPageManagement.getAppletPageLocation();

            appletRedisDAO.zRemove(redisKey, String.valueOf(appletPageManagement.getAppletPageId()));

            deleteRedisAppletPage(appletPageManagement.getAppletPageId(), appletPageManagement.getAppletPageLocation() ,businessId);

        }
        // 记录操作日志上下文
        LogRecordContext.putVariable("appletPageManagement", appletPageManagement);
    }

    private void checkGuaranteeStatus(AppletPageManagementReqVO appletPageManagement, AppletPageManagementDO appletPageManagementDO) {
        if (!ObjectUtil.equals(1, appletPageManagement.getAppletPageStatus())) {

            if (ObjectUtil.equals(1,appletPageManagementDO.getGuaranteeFlag())){
                // 兜底不可以下架
                throw new ServiceException(APPLET_PAGE_STATUS_GUARANTEE_DELETE);
            }
        }
    }

    private void addRedisAppletPage(Long appletPageId, Integer appletPageLocation, AppletPageManagementDO appletPageManagementDO, Long businessId) {

        // 兜底发布
        if (ObjectUtil.equals(1, appletPageManagementDO.getGuaranteeFlag())){
            appletRedisDAO.addByAppletPageLocation(appletPageId, appletPageLocation, appletPageManagementDO.getAppletPageInfo(), businessId);
            return;
        }

        // 全部门店
        if (ObjectUtil.equals(AppScopeEnum.STORE.getType(), appletPageManagementDO.getAppScope()) &&
                ObjectUtil.equals(StoreScopeEnum.ALL.getType(), appletPageManagementDO.getStoreScope())){

            addRedisAppletPageByAllStores(appletPageId, appletPageLocation, appletPageManagementDO.getAppletPageInfo(), businessId);
            return;
        }
        // 部分门店
        if (ObjectUtil.equals(AppScopeEnum.STORE.getType(), appletPageManagementDO.getAppScope()) &&
                ObjectUtil.equals(StoreScopeEnum.PART.getType(), appletPageManagementDO.getStoreScope())){
            LambdaQueryWrapper<AppletPageManagementStoreDO> storeWrapper = new LambdaQueryWrapper<>();
            storeWrapper.eq(AppletPageManagementStoreDO::getAppletPageId, appletPageId);
            List<AppletPageManagementStoreDO> appletPageManagementStoreDOS = appletPageManagementStoreMapper.selectList(storeWrapper);

            addRedisAppletPageByStores(appletPageManagementStoreDOS, appletPageLocation, appletPageManagementDO.getAppletPageInfo(), businessId);
            return;
        }
        if (ObjectUtil.equals(AppScopeEnum.TAG.getType(), appletPageManagementDO.getAppScope())){
            LambdaQueryWrapper<AppletPageManagementTagDO> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(AppletPageManagementTagDO::getAppletPageId, appletPageId);
            List<AppletPageManagementTagDO> appletPageManagementTagDOS = appletPageManagementTagMapper.selectList(tagWrapper);

            addRedisAppletPageByTags(appletPageManagementTagDOS, appletPageLocation, appletPageId, appletPageManagementDO.getAppletPageInfo(), businessId);
        }
    }

    private void deleteRedisAppletPage(Long appletPageId, Integer appletPageLocation,Long businessId) {
        LambdaQueryWrapper<AppletPageManagementDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppletPageManagementDO::getAppletPageId, appletPageId);

        AppletPageManagementDO appletPageManagementDO = appletPageManagementMapper.selectOne(wrapper);

        if (ObjectUtil.equals(1, appletPageManagementDO.getGuaranteeFlag())){
            return;
        }

        if (ObjectUtil.equals(AppScopeEnum.STORE.getType(), appletPageManagementDO.getAppScope())&&
                ObjectUtil.equals(StoreScopeEnum.ALL.getType(), appletPageManagementDO.getStoreScope())){

            deleteRedisAppletPageByAllStores(appletPageId, appletPageLocation,businessId);
            return;
        }
        if (ObjectUtil.equals(AppScopeEnum.STORE.getType(), appletPageManagementDO.getAppScope())&&
                ObjectUtil.equals(StoreScopeEnum.PART.getType(), appletPageManagementDO.getStoreScope())){
            LambdaQueryWrapper<AppletPageManagementStoreDO> storeWrapper = new LambdaQueryWrapper<>();
            storeWrapper.eq(AppletPageManagementStoreDO::getAppletPageId, appletPageId);
            List<AppletPageManagementStoreDO> appletPageManagementStoreDOS = appletPageManagementStoreMapper.selectList(storeWrapper);

            deleteRedisAppletPageByStores(appletPageManagementStoreDOS, appletPageLocation, businessId);
            return;
        }
        if (ObjectUtil.equals(AppScopeEnum.TAG.getType(), appletPageManagementDO.getAppScope())){
            LambdaQueryWrapper<AppletPageManagementTagDO> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(AppletPageManagementTagDO::getAppletPageId, appletPageId);
            List<AppletPageManagementTagDO> appletPageManagementTagDOS = appletPageManagementTagMapper.selectList(tagWrapper);

            deleteRedisAppletPageByTags(appletPageManagementTagDOS, appletPageLocation, appletPageId, businessId);
        }
    }

    @Override
    public AppletPageManagementRespVO getInfo(Long appletPageId) {
        AppletPageManagementDO appletPageManagementDO = appletPageManagementMapper.selectById(appletPageId);

        AppletPageManagementRespVO respVO = BeanUtils.toBean(appletPageManagementDO, AppletPageManagementRespVO.class);

        if (ObjectUtil.equals(AppScopeEnum.STORE.getType(), respVO.getAppScope()) &&
                ObjectUtil.equals(StoreScopeEnum.PART.getType(), respVO.getStoreScope())){
            // 部分门店
            LambdaQueryWrapper<AppletPageManagementStoreDO> storeWrapper = new LambdaQueryWrapper<>();
            storeWrapper.eq(AppletPageManagementStoreDO::getAppletPageId, appletPageManagementDO.getAppletPageId());
            List<AppletPageManagementStoreDO> storeList = appletPageManagementStoreMapper.selectList(storeWrapper);
            List<AppletPageManagementStoreRespVO> storeResp = BeanUtils.toBean(storeList, AppletPageManagementStoreRespVO.class);

            respVO.setStoreList(storeResp);

            return respVO;
        }

        if (ObjectUtil.equals(AppScopeEnum.TAG.getType(), respVO.getAppScope())){
            // 标签范围
            LambdaQueryWrapper<AppletPageManagementTagDO> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(AppletPageManagementTagDO::getAppletPageId, appletPageManagementDO.getAppletPageId());
            List<AppletPageManagementTagDO> tagList = appletPageManagementTagMapper.selectList(tagWrapper);
            List<Long> tagResp = tagList.stream()
                    .map(AppletPageManagementTagDO::getTagId)
                    .filter(Objects::nonNull)  // 过滤null，防止空指针
                    .toList();

            respVO.setStoreList(Collections.emptyList());
            respVO.setTagList(tagResp);

            return respVO;

        }

        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = SYSTEM_APPLET_PAGE_MANAGEMENT_TYPE, subType = SYSTEM_APPLET_PAGE_MANAGEMENT_DELETE_TYPE, bizNo = "{{#appletPageManagement}}", success = SYSTEM_APPLET_PAGE_MANAGEMENT_DELETE_SUCCESS)
    public void deleteAppletPage(Long appletPageId) {
        Long businessId = BusinessContextHolder.getBusinessId();
        AppletPageManagementDO one = appletPageManagementMapper.selectById(appletPageId);
        if (ObjectUtil.equals(1, one.getAppletPageStatus())) {
            throw new ServiceException(APPLET_PAGE_STATUS_DELETE);
        }
        if (ObjectUtil.equals(1, one.getGuaranteeFlag())) {
            throw new ServiceException(APPLET_PAGE_GUARANTEE_DELETE);
        }

        LambdaQueryWrapper<AppletPageManagementDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AppletPageManagementDO::getAppletPageLocation, one.getAppletPageLocation());
        lambdaQueryWrapper.eq(AppletPageManagementDO::getBusinessId, businessId);
        List<AppletPageManagementDO> list = this.appletPageManagementMapper.selectList(lambdaQueryWrapper);
        if (list.size() <= 1) {
            throw new ServiceException(APPLET_PAGE_DELETE);
        }

        LambdaUpdateWrapper<AppletPageManagementDO> UpdateWrapper = new LambdaUpdateWrapper<>();
        UpdateWrapper.eq(AppletPageManagementDO::getAppletPageId, appletPageId);
        UpdateWrapper.set(AppletPageManagementDO::getDeleted, 1);
        appletPageManagementMapper.update(UpdateWrapper);
        // 记录操作日志上下文
        LogRecordContext.putVariable("appletPageManagement", appletPageId);
    }

    @Override
    @LogRecord(type = SYSTEM_APPLET_PAGE_MANAGEMENT_TYPE, subType = SYSTEM_APPLET_PAGE_MANAGEMENT_UPDATE_TYPE, bizNo = "{{#appletPageManagement.appletPageId}}", success = SYSTEM_APPLET_PAGE_MANAGEMENT_UPDATE_SUCCESS)
    public void updateAppletPage(AppletPageManagementReqVO appletPageManagement) {
        Long businessId = BusinessContextHolder.getBusinessId();
        LambdaQueryWrapper<AppletPageManagementDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AppletPageManagementDO::getAppletPageId, appletPageManagement.getAppletPageId());
//        lambdaQueryWrapper.eq(AppletPageManagementDO::getBusinessId, businessId);
        AppletPageManagementDO one = appletPageManagementMapper.selectOne(lambdaQueryWrapper);
        // 发布状态 非兜底模板 更新
        if (ObjectUtil.equals(1, one.getAppletPageStatus()) && ObjectUtil.notEqual(1, one.getGuaranteeFlag())) {
            throw new ServiceException(APPLET_PAGE_ONLINE_UPDATE_ERROR);
        }

        if (ObjectUtil.equals(1, one.getGuaranteeFlag()) &&
                (ObjectUtil.equals(AppScopeEnum.TAG.getType(), appletPageManagement.getAppScope()) ||
                        ObjectUtil.equals(StoreScopeEnum.PART.getType(), appletPageManagement.getStoreScope()))
        ){
            throw new ServiceException(APPLET_PAGE_GUARANTEE_UPDATE_ERROR);
        }

        Long appletPageId = appletPageManagement.getAppletPageId();
        // 2. 应用范围：全部门店 / 部分门店
        Integer appScope = appletPageManagement.getAppScope();

        // 删除门店与标签
        // 部分门店
        if (ObjectUtil.equals(AppScopeEnum.STORE.getType(), one.getAppScope()) &&
                ObjectUtil.equals(StoreScopeEnum.PART.getType(), one.getStoreScope())) {
            LambdaQueryWrapper<AppletPageManagementStoreDO> storeWrapper = new LambdaQueryWrapper<>();
            storeWrapper.eq(AppletPageManagementStoreDO::getAppletPageId, appletPageId);

            if (ObjectUtil.equals(one.getAppletPageStatus() ,1)) {
                List<AppletPageManagementStoreDO> appletPageManagementStoreDOS = appletPageManagementStoreMapper.selectList(storeWrapper);

                // 发布状态 删除缓存
                deleteRedisAppletPageByStores(appletPageManagementStoreDOS,appletPageManagement.getAppletPageLocation(),businessId);
            }
            appletPageManagementStoreMapper.delete(storeWrapper);
        }
        // 全部门店
        if (ObjectUtil.equals(AppScopeEnum.STORE.getType(), one.getAppScope()) &&
                ObjectUtil.equals(StoreScopeEnum.ALL.getType(), one.getStoreScope())) {

            if (ObjectUtil.equals(one.getAppletPageStatus() ,1)) {
                // 发布状态 删除缓存
                deleteRedisAppletPageByAllStores(one.getAppletPageId(), one.getAppletPageLocation(), businessId);
            }
        }

        // 标签
        if (ObjectUtil.equals(AppScopeEnum.TAG.getType(), one.getAppScope())) {
            LambdaQueryWrapper<AppletPageManagementTagDO> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.eq(AppletPageManagementTagDO::getAppletPageId, appletPageId);


            if (ObjectUtil.equals(one.getAppletPageStatus() ,1)) {
                List<AppletPageManagementTagDO> appletPageManagementTagDOS = appletPageManagementTagMapper.selectList(tagWrapper);
                // 发布状态 删除缓存
                deleteRedisAppletPageByTags(appletPageManagementTagDOS ,appletPageManagement.getAppletPageLocation() ,appletPageId ,businessId);
            }
            appletPageManagementTagMapper.delete(tagWrapper);
        }

        // 部分门店 更新插入
        if (ObjectUtil.equals(AppScopeEnum.STORE.getType(), appScope) &&
                ObjectUtil.equals(StoreScopeEnum.PART.getType(), appletPageManagement.getStoreScope())) {
            List<AppletPageManagementStoreReqVO> storeList = appletPageManagement.getStoreList();
            if (CollectionUtil.isNotEmpty(storeList)) {
                List<AppletPageManagementStoreDO> storeDOList = storeList.stream().map(store -> {
                    AppletPageManagementStoreDO storeDO = new AppletPageManagementStoreDO();
                    storeDO.setAppletPageId(appletPageId);
                    storeDO.setStoreId(store.getStoreId());
                    storeDO.setStoreName(store.getStoreName());
                    storeDO.setAppletPageLocation(appletPageManagement.getAppletPageLocation());
                    return storeDO;
                }).toList();
                appletPageManagementStoreMapper.insertBatch(storeDOList);
            }
        }

        // 标签门店 更新插入
        if (ObjectUtil.equals(AppScopeEnum.TAG.getType(), appScope)) {
            List<Long> tagList = appletPageManagement.getTagList();
            if (CollectionUtil.isNotEmpty(tagList)) {
                List<AppletPageManagementTagDO> tagDOList = tagList.stream().map(tagId -> {
                    AppletPageManagementTagDO tagDO = new AppletPageManagementTagDO();
                    tagDO.setAppletPageId(appletPageId);
                    tagDO.setTagId(tagId);
                    tagDO.setAppletPageLocation(appletPageManagement.getAppletPageLocation());
                    return tagDO;
                }).toList();
                appletPageManagementTagMapper.insertBatch(tagDOList);
            }
        }

        // 更新模板信息
        AppletPageManagementDO bean = BeanUtils.toBean(appletPageManagement, AppletPageManagementDO.class);
        appletPageManagementMapper.updateById(bean);

        // 兜底需要刷新缓存
        if (ObjectUtil.equal(1, one.getGuaranteeFlag())){
            addRedisAppletPageByAllStores(bean.getAppletPageId(), bean.getAppletPageLocation(), bean.getAppletPageInfo(),businessId);
        }


        // 记录操作日志上下文
        LogRecordContext.putVariable("appletPageManagement", appletPageManagement);
    }

    private void deleteRedisAppletPageByAllStores(Long appletPageId, Integer appletPageLocation, Long businessId) {
        appletRedisDAO.delByAppletPageLocation(appletPageId, appletPageLocation, businessId);
    }

    private void addRedisAppletPageByAllStores(Long appletPageId, Integer appletPageLocation, String appletPageInfo,Long businessId) {
        appletRedisDAO.addByAppletPageLocation(appletPageId, appletPageLocation, appletPageInfo, businessId);
    }

    private void deleteRedisAppletPageByStores(List<AppletPageManagementStoreDO> appletPageManagementStoreDOS ,Integer appletPageLocation ,Long businessId) {
        if (CollectionUtil.isEmpty(appletPageManagementStoreDOS)) {
            return;
        }

        // 一次性生成所有需要删除的 key（_1_ 和 _2_ 全部放一起）
        List<String> allKeys = new ArrayList<>(appletPageManagementStoreDOS.size() * 2);
        for (AppletPageManagementStoreDO bean : appletPageManagementStoreDOS) {
            allKeys.add("APPLET_STORE_" + businessId + ":" +appletPageLocation +"_" + bean.getAppletPageId() + "_" + bean.getStoreId());
        }

        // 【一次 pipeline 删完所有】性能提升一倍！
        redisTemplate.executePipelined((RedisCallback<Void>) connection -> {
            for (String key : allKeys) {
                connection.keyCommands().unlink(key.getBytes());
            }
            return null;
        });
    }

    private void addRedisAppletPageByStores(List<AppletPageManagementStoreDO> appletPageManagementStoreDOS ,Integer appletPageLocation ,String appletPageInfo, Long businessId) {
        if (CollectionUtil.isEmpty(appletPageManagementStoreDOS)) {
            return;
        }
        redisTemplate.executePipelined((RedisCallback<Void>) connection -> {
            for (AppletPageManagementStoreDO bean : appletPageManagementStoreDOS) {
                String key = "APPLET_STORE_" + businessId + ":" + appletPageLocation + "_"
                        + bean.getAppletPageId() + "_" + bean.getStoreId();

                if (appletPageInfo == null) {
                    continue;
                }

                redisTemplate.opsForValue().set(key, appletPageInfo);
            }
            return null;
        });
    }

    private void deleteRedisAppletPageByTags(List<AppletPageManagementTagDO> appletPageManagementTagDOS ,Integer appletPageLocation ,Long appletPageId, Long businessId) {
        if (CollectionUtil.isEmpty(appletPageManagementTagDOS)) {
            return;
        }
        List<Long> tagList = appletPageManagementTagDOS.stream()
                .map(AppletPageManagementTagDO::getTagId)
                .toList();

        // 通过标签查询门店
        List<Long> storeIdsByTagIds = getStoreIdsByTagIds(tagList);

        // 一次性生成所有需要删除的 key（_1_ 和 _2_ 全部放一起）
        List<String> allKeys = new ArrayList<>();
        for (Long storeId : storeIdsByTagIds) {

            allKeys.add("APPLET_STORE_" + businessId + ":" + appletPageLocation + "_" + appletPageId + "_" + storeId);

        }

        // 【一次 pipeline 删完所有】性能提升一倍！
        redisTemplate.executePipelined((RedisCallback<Void>) connection -> {
            for (String key : allKeys) {
                connection.keyCommands().unlink(key.getBytes());
            }
            return null;
        });
    }

    private void addRedisAppletPageByTags(List<AppletPageManagementTagDO> appletPageManagementTagDOS ,Integer appletPageLocation ,Long appletPageId, String appletPageInfo, Long businessId) {
        if (CollectionUtil.isEmpty(appletPageManagementTagDOS)) {
            return;
        }

        List<Long> tagList = appletPageManagementTagDOS.stream()
                .map(AppletPageManagementTagDO::getTagId)
                .filter(Objects::nonNull)
                .toList();

        List<Long> storeIdsByTagIds = getStoreIdsByTagIds(tagList);
        if (CollectionUtil.isEmpty(storeIdsByTagIds)) {
            return;
        }

        redisTemplate.executePipelined((RedisCallback<Void>) connection -> {
            for (Long storeId : storeIdsByTagIds) {
                String key = "APPLET_STORE_" + businessId + ":" + appletPageLocation + "_" + appletPageId + "_" + storeId;

                if (appletPageInfo == null) {
                    continue;
                }

                redisTemplate.opsForValue().set(key, appletPageInfo);
            }
            return null;
        });
    }

    public List<Long> getStoreIdsByTagIds(List<Long> tagIds) {
        LambdaQueryWrapper<SystemStoreTagDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SystemStoreTagDO::getTagId, tagIds);
        wrapper.eq(SystemStoreTagDO::getDeleted, false);

        List<Long> storeIdList = new ArrayList<>();
        systemStoreTagMapper.selectStoreIdByTag(wrapper, context -> {
            Long pageId = context.getResultObject();
            if (pageId != null) {
                storeIdList.add(pageId);
            }
        });
        return storeIdList;
    }

    public List<Long> getTagIdsByTagGroupId(Long tagGroupId) {
        LambdaQueryWrapper<TagValueDO> tagValueWrapper = new LambdaQueryWrapper<>();

        tagValueWrapper.eq(TagValueDO::getTagGroupId, tagGroupId);
        tagValueWrapper.eq(TagValueDO::getDeleted, false);

        List<Long> tagIdList = new ArrayList<>();
        tagValueMapper.selectTagIdList(tagValueWrapper, context -> {
            Long pageId = context.getResultObject();
            if (pageId != null) {
                tagIdList.add(pageId);
            }
        });
        return tagIdList;
    }

    @Override
    public Object getAppletPageManagementForApplet(AppletPageManagementReqVO appletPageManagement) {
        Long businessId = BusinessContextHolder.getBusinessId();
        Integer appletPageLocation = appletPageManagement.getAppletPageLocation();
        if (ObjectUtil.isEmpty(appletPageLocation)) {
            throw new ServiceException(APPLET_PAGE_PARAM);
        }
        String key = APPLET_PAGE_ALL_MANAGEMENT + "_" + businessId;
        String field = APPLET_PAGE_MANAGEMENT + ":" + appletPageLocation;
        String appletPageInfo = "";
        try {
            long startTime = System.currentTimeMillis();
            Object o = redisTemplate.opsForHash().get(key,field);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            log.info("AppletPageManagementForApplet执行耗时: {} 毫秒", duration);
            if (ObjectUtil.isEmpty(o)) {
                LambdaQueryWrapper<AppletPageManagementDO> queryWrapper = new LambdaQueryWrapper<>();
//                queryWrapper.eq(AppletPageManagementDO::getIsDelete, 0);
                queryWrapper.eq(AppletPageManagementDO::getAppletPageLocation, appletPageLocation);
                queryWrapper.eq(AppletPageManagementDO::getAppletPageStatus, 1);
//                queryWrapper.eq(AppletPageManagementDO::getBusinessId, businessId);
                AppletPageManagementDO one = appletPageManagementMapper.selectOne(queryWrapper);
                if (ObjectUtil.isNotEmpty(one)) {
                    appletPageInfo = one.getAppletPageInfo();
                    redisTemplate.opsForHash().put(key,field, appletPageInfo);
                }
            } else {
                appletPageInfo = o.toString();
            }
        } catch (Exception e) {
            log.error("AppletPageManagementForApplet error:",e);
            LambdaQueryWrapper<AppletPageManagementDO> queryWrapper = new LambdaQueryWrapper<>();
//            queryWrapper.eq(AppletPageManagementDO::getIsDelete, 0);
            queryWrapper.eq(AppletPageManagementDO::getAppletPageLocation, appletPageManagement.getAppletPageLocation());
            queryWrapper.eq(AppletPageManagementDO::getAppletPageStatus, 1);
            queryWrapper.eq(AppletPageManagementDO::getBusinessId, businessId);
            queryWrapper.orderBy(true,false,AppletPageManagementDO::getCreateTime,AppletPageManagementDO::getAppletPageId);
            List<AppletPageManagementDO> list = appletPageManagementMapper.selectList(queryWrapper);
            if(CollectionUtil.isNotEmpty(list) && ObjectUtil.isNotEmpty(list.get(0))){
                appletPageInfo = list.get(0).getAppletPageInfo();
            }
        }
        return appletPageInfo;
    }

    @Override
    @DataPermission(enable = false)
    public void flushAppletPage() {
        LambdaQueryWrapper<AppletPageManagementDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AppletPageManagementDO::getAppletPageStatus, 1);
        List<AppletPageManagementDO> list = appletPageManagementMapper.selectList(queryWrapper);
        Map<Long, List<AppletPageManagementDO>> map = list.stream().collect( Collectors.groupingBy(AppletPageManagementDO::getBusinessId));
        sethashRedis(map);
    }

    private void sethashRedis(Map<Long, List<AppletPageManagementDO>> map) {
        map.forEach((key, value) -> {
            String redisKey = APPLET_PAGE_ALL_MANAGEMENT + "_" + key;
            // 处理键值对
            value.forEach(vo ->{
                String redisField = APPLET_PAGE_MANAGEMENT  + ":" + vo.getAppletPageLocation();
                redisTemplate.opsForHash().put(redisKey, redisField, vo.getAppletPageInfo());
            });
        });
    }

    @Override
    public String getAppletPageManagementForAppletNew(AppAppletPageManagementReqVO appletPageManagement) {
        // 1. 参数获取与基础校验
        Long businessId = BusinessContextHolder.getBusinessId();
        Integer pageLocation = appletPageManagement.getAppletPageLocation();
        Long storeId = appletPageManagement.getStoreId();

        if (ObjectUtil.isEmpty(pageLocation)) {
            throw new ServiceException(APPLET_PAGE_PARAM);
        }
/*        if (ObjectUtil.equals(businessId, 10) && ObjectUtil.isEmpty(storeId)) {
            throw new ServiceException(APPLET_PAGE_PARAM);
        }*/

        // 2. 构建Redis Key
        String pageSortKey = "APPLET_SX_" + businessId + "_" + pageLocation;

        try {
            // 3. 获取排序后的页面ID列表
            Set<String> pageIdSet = appletRedisDAO.zGetAllReverse(pageSortKey);
            if (CollectionUtil.isNotEmpty(pageIdSet)) {
                for (String pageId : pageIdSet) {

                    // 先查全局缓存（全局装修配置）
                    String globalKey = APPLET_PAGE_ALL_MANAGEMENT + "_" + businessId + ":" + pageLocation + "_" + pageId;
                    String globalInfo = appletRedisDAO.getStr(globalKey);
                    if (StringUtils.isNotBlank(globalInfo)) {
                        String visibleGlobalInfo = filterByShowWeek(globalInfo);
                        if (visibleGlobalInfo != null) {
                            return visibleGlobalInfo;
                        }
                        // 当前页面不在展示星期内，继续遍历排序队列找下一个可见页面
                        continue;
                    }

                    // 全局无，再查门店（互斥，门店装修配置）
                    if (ObjectUtil.isNotNull(storeId)) {
                        String storeKey = "APPLET_STORE_" + businessId + ":" + pageLocation + "_" + pageId + "_" + storeId;
                        String storeInfo = appletRedisDAO.getStr(storeKey);
                        if (StringUtils.isNotBlank(storeInfo)) {
                            String visibleStoreInfo = filterByShowWeek(storeInfo);
                            if (visibleStoreInfo != null) {
                                return visibleStoreInfo;
                            }
                            // 当前页面不在展示星期内，继续遍历排序队列找下一个可见页面
                            continue;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Redis 异常只打印日志，不抛出，直接走兜底
            log.error("【小程序页面配置】Redis查询异常，直接降级走数据库 pageLocation={} businessId={}", pageLocation, businessId, e);
        }

        // 5. 所有缓存都未命中 或 Redis异常 → 查询数据库兜底；若兜底页也不在展示星期内，返回 null（前端收到空）
        return filterByShowWeek(getAndCacheGuaranteePage(pageLocation, businessId, pageSortKey));
    }

    private String getAndCacheGuaranteePage(Integer pageLocation, Long businessId, String pageSortKey) {
        // 查询兜底配置
        LambdaQueryWrapper<AppletPageManagementDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppletPageManagementDO::getAppletPageLocation, pageLocation)
                .eq(AppletPageManagementDO::getGuaranteeFlag, 1)
                .eq(AppletPageManagementDO::getAppletPageStatus, 1);
        AppletPageManagementDO pageDO = appletPageManagementMapper.selectOne(wrapper);

        if (pageDO == null) {
            return null;
        }

        // 缓存兜底数据
        appletRedisDAO.zAdd(pageSortKey, String.valueOf(pageDO.getAppletPageId()));
        appletRedisDAO.addByAppletPageLocation(
                pageDO.getAppletPageId(),
                pageLocation,
                pageDO.getAppletPageInfo(),
                businessId
        );

        return pageDO.getAppletPageInfo();
    }

    // ===================== C端展示星期过滤（showWeek，根级判断） =====================

    /**
     * C端读侧展示星期过滤：对整段装修配置 JSON 做根级判断。
     * showWeek 位于 JSON 根节点（与 home、commodity 等键平级），
     * 星期一到星期日对应 1-7（与 {@link java.time.DayOfWeek#getValue()} 一致，周一=1、周日=7）。
     * <p>
     * 过滤规则：
     * 1. 根节点不含 showWeek，或为空/无法解析出任何有效星期值：视为无展示限制，原样返回
     * 2. showWeek 非空：当前系统时间的星期值在 showWeek 集合中 → 原样返回；否则该页面不可见，返回 null
     * <p>
     * 缓存无 TTL，故必须在读侧返回前实时判断；任何异常均吞掉并返回原文，绝不影响接口可用性
     *
     * @param appletPageInfo 装修配置 JSON 原文
     * @return 可见时原样返回原文；当前星期不在 showWeek 集合内时返回 null
     */
    private String filterByShowWeek(String appletPageInfo) {
        if (StringUtils.isBlank(appletPageInfo)) {
            return appletPageInfo;
        }
        try {
            JsonNode root = JsonUtils.parseTree(appletPageInfo);
            if (!root.isObject()) {
                return appletPageInfo;
            }
            Set<Integer> showWeeks = parseShowWeekSet(root.get("showWeek"));
            if (showWeeks.isEmpty()) {
                // 字段缺失/为空/无法解析出任何有效星期值：视为无展示限制，原样返回
                return appletPageInfo;
            }
            int todayWeek = LocalDateTime.now().getDayOfWeek().getValue();
            return showWeeks.contains(todayWeek) ? appletPageInfo : null;
        } catch (Exception e) {
            log.error("【小程序页面配置】showWeek展示星期过滤异常，返回原文", e);
            return appletPageInfo;
        }
    }

    /**
     * 解析 showWeek 字段为星期值集合（1-7）。
     * 兼容形态：JSON 数组（如 [1,2,7]，元素可为数字或数字字符串）、单个数字、逗号分隔字符串（如 "1,2,7"）；
     * 集合中任一元素非法则跳过该元素；解析不出任何有效星期值时返回空集合
     */
    private Set<Integer> parseShowWeekSet(JsonNode node) {
        Set<Integer> weekSet = new HashSet<>();
        if (node == null || node.isNull()) {
            return weekSet;
        }
        if (node.isArray()) {
            for (JsonNode element : node) {
                addValidWeek(element.asText(), weekSet);
            }
        } else {
            // 单个数字节点或逗号分隔字符串，统一按逗号拆分后逐个解析
            for (String part : node.asText().split(",")) {
                addValidWeek(part, weekSet);
            }
        }
        return weekSet;
    }

    /**
     * 将单个星期值文本校验后加入集合：仅接受 1-7 的整数，非法值跳过
     */
    private void addValidWeek(String text, Set<Integer> weekSet) {
        if (StringUtils.isBlank(text)) {
            return;
        }
        try {
            int week = Integer.parseInt(text.trim());
            if (week >= 1 && week <= 7) {
                weekSet.add(week);
            }
        } catch (NumberFormatException ignore) {
            // 非法元素跳过
        }
    }

    @Override
    public void addRedisByStoreTag(Long storeId, Long tagId, Long businessId) {

        // 1. 流式查询：获取标签关联的页面ID（安全不OOM）
        LambdaQueryWrapper<AppletPageManagementTagDO> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.select(AppletPageManagementTagDO::getAppletPageId)
                .eq(AppletPageManagementTagDO::getTagId, tagId)
                .eq(AppletPageManagementTagDO::getDeleted, false);

        Set<Long> pageIdSet = new HashSet<>();
        appletPageManagementTagMapper.selectAppletPageIdStream(tagWrapper, context -> {
            Long pageId = context.getResultObject();
            if (pageId != null) {
                pageIdSet.add(pageId);
            }
        });

        // 空值直接返回，避免无效查询
        if (CollectionUtil.isEmpty(pageIdSet)) {
            return;
        }

        // 2. 批量查询页面信息（一次SQL，性能最高）
        LambdaQueryWrapper<AppletPageManagementDO> pageWrapper = new LambdaQueryWrapper<>();
        pageWrapper.eq(AppletPageManagementDO::getAppletPageStatus, 1)
                .in(AppletPageManagementDO::getAppletPageId, pageIdSet);

        List<AppletPageManagementDO> pageList = appletPageManagementMapper.selectList(pageWrapper);

        // 3. 批量写入Redis（比循环单条写更快）
        Map<String, String> redisDataMap = new HashMap<>(pageList.size());
        for (AppletPageManagementDO page : pageList) {
            String key = String.format("APPLET_STORE_%s:%s_%s_%s",
                    businessId, page.getAppletPageLocation(), page.getAppletPageId(), storeId);
            redisDataMap.put(key, page.getAppletPageInfo());
        }

        // 批量写入Redis，大幅减少网络IO
        if (CollectionUtil.isNotEmpty(redisDataMap)) {
            appletRedisDAO.multiSet(redisDataMap);
        }
    }

    @Override
    public void addRedisByStoreTags(Long storeId, List<Long> tagIdList, Long businessId) {
        // 1. 空集合直接返回
        if (CollectionUtil.isEmpty(tagIdList)) {
            return;
        }

        // 2. 流式查询：批量获取多个标签关联的所有页面ID（去重、高性能、防OOM）
        LambdaQueryWrapper<AppletPageManagementTagDO> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.select(AppletPageManagementTagDO::getAppletPageId)
                .in(AppletPageManagementTagDO::getTagId, tagIdList) // 批量标签查询
                .eq(AppletPageManagementTagDO::getDeleted, false);

        Set<Long> pageIdSet = new HashSet<>();
        appletPageManagementTagMapper.selectAppletPageIdStream(tagWrapper, context -> {
            Long pageId = context.getResultObject();
            if (pageId != null) {
                pageIdSet.add(pageId);
            }
        });

        // 3. 无关联页面直接返回
        if (CollectionUtil.isEmpty(pageIdSet)) {
            return;
        }

        // 4. 批量查询页面信息（一次SQL）
        LambdaQueryWrapper<AppletPageManagementDO> pageWrapper = new LambdaQueryWrapper<>();
        pageWrapper.eq(AppletPageManagementDO::getAppletPageStatus, 1)
                .in(AppletPageManagementDO::getAppletPageId, pageIdSet);

        List<AppletPageManagementDO> pageList = appletPageManagementMapper.selectList(pageWrapper);

        // 5. 组装Redis数据并批量写入
        Map<String, String> redisDataMap = new HashMap<>(pageList.size());
        for (AppletPageManagementDO page : pageList) {
            String key = String.format("APPLET_STORE_%s:%s_%s_%s",
                    businessId, page.getAppletPageLocation(), page.getAppletPageId(), storeId);
            redisDataMap.put(key, page.getAppletPageInfo());
        }

        // 6. 批量写入Redis
        if (CollectionUtil.isNotEmpty(redisDataMap)) {
            appletRedisDAO.multiSet(redisDataMap);
        }
    }

    @Override
    public void delRedisByStoreTag(Long storeId, Long tagId, Long businessId) {

        LambdaQueryWrapper<AppletPageManagementTagDO> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.select(AppletPageManagementTagDO::getAppletPageId,
                        AppletPageManagementTagDO::getAppletPageLocation)
                .eq(AppletPageManagementTagDO::getTagId, tagId) // 批量标签查询
                .eq(AppletPageManagementTagDO::getDeleted, false);

        List<AppletPageManagementTagDO> pageIdSet = appletPageManagementTagMapper.selectList(tagWrapper);

        if (CollectionUtil.isEmpty(pageIdSet)) {
            return;
        }

        // 构建所有需要删除的 KEY（和单标签规则完全一致）
        List<String> deleteKeys = new ArrayList<>(AppletPageLocationEnum.ARRAYS.length * pageIdSet.size());
        for (AppletPageManagementTagDO page : pageIdSet) {
            String key = String.format("APPLET_STORE_%s:%s_%s_%s",
                    businessId, page.getAppletPageLocation(), page.getAppletPageId(), storeId);
            deleteKeys.add(key);
        }


        // 管道批量删除（高性能）
        redisTemplate.executePipelined((RedisCallback<Void>) connection -> {
            for (String key : deleteKeys) {
                connection.keyCommands().unlink(key.getBytes());
            }
            return null;
        });

    }

    @Override
    public void delRedisByStoreTags(Long storeId, List<Long> tagIdList, Long businessId) {
        // 1. 空集合直接返回
        if (CollectionUtil.isEmpty(tagIdList)) {
            return;
        }

        // 2. 流式查询：批量获取多个标签关联的所有页面ID（去重、防OOM）
        LambdaQueryWrapper<AppletPageManagementTagDO> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.select(AppletPageManagementTagDO::getAppletPageId, AppletPageManagementTagDO::getAppletPageLocation)
                .in(AppletPageManagementTagDO::getTagId, tagIdList) // 批量标签查询
                .eq(AppletPageManagementTagDO::getDeleted, false);

        List<AppletPageManagementTagDO> pageIdSet = appletPageManagementTagMapper.selectList(tagWrapper);

        // 3. 无关联页面直接返回
        if (CollectionUtil.isEmpty(pageIdSet)) {
            return;
        }

        // 4. 构建所有需要删除的 KEY（和单标签规则完全一致）
        List<String> deleteKeys = new ArrayList<>(AppletPageLocationEnum.ARRAYS.length * pageIdSet.size());
        for (AppletPageManagementTagDO page : pageIdSet) {
            String key = String.format("APPLET_STORE_%s:%s_%s_%s",
                    businessId, page.getAppletPageLocation(), page.getAppletPageId(), storeId);
            deleteKeys.add(key);
        }

        // 5. 管道批量删除（高性能）
        redisTemplate.executePipelined((RedisCallback<Void>) connection -> {
            for (String key : deleteKeys) {
                connection.keyCommands().unlink(key.getBytes());
            }
            return null;
        });
    }

    @Override
    public void delAppletPageManagementTags(List<Long> tagIdList) {
        LambdaQueryWrapper<AppletPageManagementTagDO> delWrapper = new LambdaQueryWrapper<>();
        delWrapper.in(AppletPageManagementTagDO::getTagId, tagIdList);
        appletPageManagementTagMapper.delete(delWrapper);
    }

    @Override
    public boolean appletPageExistByStoreTagGroup(long tagGroupId, long businessId) {

        // 标签组下所有标签
        List<Long> tagIdList = getTagIdsByTagGroupId(tagGroupId);

        // 获取需要重新设置缓存的页面ID
        LambdaQueryWrapper<AppletPageManagementTagDO> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.select(AppletPageManagementTagDO::getAppletPageId)
                .in(AppletPageManagementTagDO::getTagId, tagIdList);

        List<AppletPageManagementTagDO> appletPageManagementTagDOS = appletPageManagementTagMapper.selectList(tagWrapper);

        return CollectionUtil.isNotEmpty(appletPageManagementTagDOS);

    }
}
