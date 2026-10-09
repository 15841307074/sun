package com.htyoudao.youdao.module.system.service.storebackground;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.background.StoreBackgroundRespVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.background.StoreBackgroundSaveReqVO;
import com.htyoudao.youdao.module.system.controller.admin.store.vo.background.StoreBackgroundStatusReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.storebackground.StoreBackgroundStoreDO;
import com.htyoudao.youdao.module.system.dal.dataobject.storebackground.StoreBackgroundTagDO;
import com.htyoudao.youdao.module.system.dal.dataobject.storebackground.StoreBackgroundTemplateDO;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.storebackground.StoreBackgroundStoreMapper;
import com.htyoudao.youdao.module.system.dal.mysql.storebackground.StoreBackgroundTagMapper;
import com.htyoudao.youdao.module.system.dal.mysql.storebackground.StoreBackgroundTemplateMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.*;

/**
 * 门店背景模板服务实现。
 */
@Service
@Slf4j
@DataPermission(enable = false)
public class StoreBackgroundServiceImpl implements StoreBackgroundService {

    private static final int APP_SCOPE_STORE = 1;
    private static final int APP_SCOPE_TAG = 2;
    private static final int STORE_SCOPE_ALL = 1;
    private static final int STORE_SCOPE_PART = 2;
    private static final int STATUS_CLOSED = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int DEFAULT_FLAG_CUSTOM = 0;
    private static final int DEFAULT_FLAG_SYSTEM = 1;
    private static final String DEFAULT_TEMPLATE_NAME = "系统默认模板";

    @Resource
    private StoreBackgroundTemplateMapper templateMapper;
    @Resource
    private StoreBackgroundStoreMapper backgroundStoreMapper;
    @Resource
    private StoreBackgroundTagMapper backgroundTagMapper;
    @Resource
    private SystemStoreInfoMapper storeInfoMapper;
    @Resource
    private StoreBackgroundCacheService cacheService;

    /**
     * 查询全部模板，并补充模板关联的门店、标签及实际覆盖门店信息。
     */
    @Override
    public List<StoreBackgroundRespVO> getList() {
        List<StoreBackgroundTemplateDO> templates = templateMapper.selectList(
                new LambdaQueryWrapperX<StoreBackgroundTemplateDO>()
                        .orderByDesc(StoreBackgroundTemplateDO::getDefaultFlag)
                        .orderByDesc(StoreBackgroundTemplateDO::getReleaseTime)
                        .orderByDesc(StoreBackgroundTemplateDO::getBackgroundId));
        return templates.stream().map(this::buildResponse).toList();
    }

    /**
     * 查询指定模板详情。
     *
     * @param id 模板编号
     * @return 模板详情
     */
    @Override
    public StoreBackgroundRespVO get(Long id) {
        return buildResponse(validateExists(id));
    }

    /**
     * 创建自定义模板及其适用门店或标签关系。
     *
     * @param reqVO 模板保存参数
     * @return 模板编号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(StoreBackgroundSaveReqVO reqVO) {
        validateNameUnique(null, reqVO.getTemplateName());
        validateScope(reqVO);
        StoreBackgroundTemplateDO template = BeanUtils.toBean(reqVO, StoreBackgroundTemplateDO.class);
        template.setTemplateName(reqVO.getTemplateName().trim());
        template.setPublishStatus(STATUS_CLOSED);
        template.setDefaultFlag(DEFAULT_FLAG_CUSTOM);
        template.setReleaseTime(null);
        templateMapper.insert(template);
        saveRelations(template.getBackgroundId(), reqVO);
        return template.getBackgroundId();
    }

    /**
     * 修改模板。系统默认模板仅更新图片，自定义模板仅允许在关闭状态修改。
     *
     * @param reqVO 模板保存参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(StoreBackgroundSaveReqVO reqVO) {
        StoreBackgroundTemplateDO existing = validateExists(reqVO.getBackgroundId());
        if (Objects.equals(existing.getDefaultFlag(), DEFAULT_FLAG_SYSTEM)) {
            StoreBackgroundTemplateDO update = new StoreBackgroundTemplateDO();
            update.setBackgroundId(existing.getBackgroundId());
            update.setTemplateName(DEFAULT_TEMPLATE_NAME);
            update.setBackgroundImage(reqVO.getBackgroundImage());
            templateMapper.updateById(update);
            existing.setTemplateName(DEFAULT_TEMPLATE_NAME);
            existing.setBackgroundImage(reqVO.getBackgroundImage());
            cacheService.updateDefaultAfterCommit(existing);
            return;
        }
        if (!Objects.equals(existing.getPublishStatus(), STATUS_CLOSED)) {
            throw exception(STORE_BACKGROUND_PUBLISHED_NOT_EDITABLE);
        }
        validateNameUnique(existing.getBackgroundId(), reqVO.getTemplateName());
        validateScope(reqVO);
        StoreBackgroundTemplateDO update = BeanUtils.toBean(reqVO, StoreBackgroundTemplateDO.class);
        update.setTemplateName(reqVO.getTemplateName().trim());
        update.setPublishStatus(null);
        update.setDefaultFlag(null);
        update.setReleaseTime(null);
        templateMapper.updateById(update);
        deleteRelations(existing.getBackgroundId());
        saveRelations(existing.getBackgroundId(), reqVO);
    }

    /**
     * 修改模板发布状态，同时更新发布时间，并在事务提交后重建受影响门店的缓存。
     *
     * @param reqVO 模板状态参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(StoreBackgroundStatusReqVO reqVO) {
        StoreBackgroundTemplateDO template = validateExists(reqVO.getBackgroundId());
        if (!Objects.equals(reqVO.getPublishStatus(), STATUS_CLOSED)
                && !Objects.equals(reqVO.getPublishStatus(), STATUS_PUBLISHED)) {
            throw exception(STORE_BACKGROUND_SCOPE_INVALID);
        }
        if (Objects.equals(template.getDefaultFlag(), DEFAULT_FLAG_SYSTEM)) {
            throw exception(STORE_BACKGROUND_DEFAULT_NOT_OPERABLE);
        }
        if (Objects.equals(template.getPublishStatus(), reqVO.getPublishStatus())) {
            Set<Long> affectedStoreIds = cacheService.resolveAffectedStoreIds(template);
            log.info("门店背景模板发布状态未变化，执行缓存刷新，模板编号={}，发布状态={}，影响门店数={}",
                    template.getBackgroundId(), reqVO.getPublishStatus(), affectedStoreIds.size());
            cacheService.refreshStoresAfterCommit(affectedStoreIds);
            return;
        }
        Set<Long> affectedStoreIds = cacheService.resolveAffectedStoreIds(template);
        log.info("门店背景模板发布状态变更，模板编号={}，原状态={}，新状态={}，影响门店数={}",
                template.getBackgroundId(), template.getPublishStatus(), reqVO.getPublishStatus(),
                affectedStoreIds.size());
        StoreBackgroundTemplateDO update = new StoreBackgroundTemplateDO();
        update.setBackgroundId(template.getBackgroundId());
        update.setPublishStatus(reqVO.getPublishStatus());
        update.setReleaseTime(LocalDateTime.now());
        templateMapper.updateById(update);
        cacheService.refreshStoresAfterCommit(affectedStoreIds);
    }

    /**
     * 删除关闭状态的自定义模板，并重建原适用门店的缓存。
     *
     * @param id 模板编号
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        StoreBackgroundTemplateDO template = validateExists(id);
        if (Objects.equals(template.getDefaultFlag(), DEFAULT_FLAG_SYSTEM)) {
            throw exception(STORE_BACKGROUND_DEFAULT_NOT_OPERABLE);
        }
        if (!Objects.equals(template.getPublishStatus(), STATUS_CLOSED)) {
            throw exception(STORE_BACKGROUND_PUBLISHED_NOT_EDITABLE);
        }
        Set<Long> affectedStoreIds = cacheService.resolveAffectedStoreIds(template);
        deleteRelations(id);
        templateMapper.deleteById(id);
        cacheService.refreshStoresAfterCommit(affectedStoreIds);
    }

    /**
     * 将模板数据转换为管理端响应对象，并计算实际适用门店。
     *
     * @param template 模板数据
     * @return 管理端模板信息
     */
    private StoreBackgroundRespVO buildResponse(StoreBackgroundTemplateDO template) {
        StoreBackgroundRespVO response = BeanUtils.toBean(template, StoreBackgroundRespVO.class);
        List<Long> storeIds = backgroundStoreMapper.selectList(
                        new LambdaQueryWrapperX<StoreBackgroundStoreDO>()
                                .eq(StoreBackgroundStoreDO::getBackgroundId, template.getBackgroundId()))
                .stream().map(StoreBackgroundStoreDO::getStoreId).filter(Objects::nonNull).distinct().toList();
        List<Long> tagIds = backgroundTagMapper.selectList(
                        new LambdaQueryWrapperX<StoreBackgroundTagDO>()
                                .eq(StoreBackgroundTagDO::getBackgroundId, template.getBackgroundId()))
                .stream().map(StoreBackgroundTagDO::getTagId).filter(Objects::nonNull).distinct().toList();
        response.setStoreIds(storeIds);
        response.setTagIds(tagIds);

        Set<Long> appliedStoreIds = cacheService.resolveAffectedStoreIds(template);
        response.setApplyStoreCount(appliedStoreIds.size());
        if (Objects.equals(template.getDefaultFlag(), DEFAULT_FLAG_SYSTEM)
                || (Objects.equals(template.getAppScope(), APP_SCOPE_STORE)
                && Objects.equals(template.getStoreScope(), STORE_SCOPE_ALL))) {
            response.setApplyStoreNames(Collections.singletonList("全部门店"));
            return response;
        }
        if (!appliedStoreIds.isEmpty()) {
            List<String> storeNames = storeInfoMapper.selectList(
                            new LambdaQueryWrapperX<SystemStoreInfoDO>()
                                    .select(SystemStoreInfoDO::getStoreName)
                                    .in(SystemStoreInfoDO::getStoreId, appliedStoreIds))
                    .stream().map(SystemStoreInfoDO::getStoreName).filter(Objects::nonNull).toList();
            response.setApplyStoreNames(storeNames);
        } else {
            response.setApplyStoreNames(Collections.emptyList());
        }
        return response;
    }

    /**
     * 校验模板是否存在。
     *
     * @param id 模板编号
     * @return 模板数据
     */
    private StoreBackgroundTemplateDO validateExists(Long id) {
        StoreBackgroundTemplateDO template = id == null ? null : templateMapper.selectById(id);
        if (template == null) {
            throw exception(STORE_BACKGROUND_NOT_EXISTS);
        }
        return template;
    }

    /**
     * 校验模板名称是否重复。
     *
     * @param id 当前模板编号，新增时为空
     * @param templateName 模板名称
     */
    private void validateNameUnique(Long id, String templateName) {
        Long count = templateMapper.selectCount(new LambdaQueryWrapperX<StoreBackgroundTemplateDO>()
                .eq(StoreBackgroundTemplateDO::getTemplateName, templateName.trim())
                .neIfPresent(StoreBackgroundTemplateDO::getBackgroundId, id));
        if (count != null && count > 0) {
            throw exception(STORE_BACKGROUND_NAME_DUPLICATE);
        }
    }

    /**
     * 校验模板应用范围以及门店、标签选择是否完整。
     *
     * @param reqVO 模板保存参数
     */
    private void validateScope(StoreBackgroundSaveReqVO reqVO) {
        if (Objects.equals(reqVO.getAppScope(), APP_SCOPE_STORE)) {
            if (!Objects.equals(reqVO.getStoreScope(), STORE_SCOPE_ALL)
                    && !Objects.equals(reqVO.getStoreScope(), STORE_SCOPE_PART)) {
                throw exception(STORE_BACKGROUND_SCOPE_INVALID);
            }
            if (Objects.equals(reqVO.getStoreScope(), STORE_SCOPE_PART)
                    && normalizeIds(reqVO.getStoreIds()).isEmpty()) {
                throw exception(STORE_BACKGROUND_SCOPE_INVALID);
            }
            return;
        }
        if (Objects.equals(reqVO.getAppScope(), APP_SCOPE_TAG)
                && !normalizeIds(reqVO.getTagIds()).isEmpty()) {
            return;
        }
        throw exception(STORE_BACKGROUND_SCOPE_INVALID);
    }

    /**
     * 保存模板与门店或标签的关联关系。
     *
     * @param backgroundId 模板编号
     * @param reqVO 模板保存参数
     */
    private void saveRelations(Long backgroundId, StoreBackgroundSaveReqVO reqVO) {
        if (Objects.equals(reqVO.getAppScope(), APP_SCOPE_STORE)
                && Objects.equals(reqVO.getStoreScope(), STORE_SCOPE_PART)) {
            List<StoreBackgroundStoreDO> relations = normalizeIds(reqVO.getStoreIds()).stream().map(storeId -> {
                StoreBackgroundStoreDO relation = new StoreBackgroundStoreDO();
                relation.setBackgroundId(backgroundId);
                relation.setStoreId(storeId);
                return relation;
            }).toList();
            backgroundStoreMapper.insertBatch(relations);
        }
        if (Objects.equals(reqVO.getAppScope(), APP_SCOPE_TAG)) {
            List<StoreBackgroundTagDO> relations = normalizeIds(reqVO.getTagIds()).stream().map(tagId -> {
                StoreBackgroundTagDO relation = new StoreBackgroundTagDO();
                relation.setBackgroundId(backgroundId);
                relation.setTagId(tagId);
                return relation;
            }).toList();
            backgroundTagMapper.insertBatch(relations);
        }
    }

    /**
     * 删除模板原有的门店和标签关联关系。
     *
     * @param backgroundId 模板编号
     */
    private void deleteRelations(Long backgroundId) {
        backgroundStoreMapper.delete(new LambdaQueryWrapper<StoreBackgroundStoreDO>()
                .eq(StoreBackgroundStoreDO::getBackgroundId, backgroundId));
        backgroundTagMapper.delete(new LambdaQueryWrapper<StoreBackgroundTagDO>()
                .eq(StoreBackgroundTagDO::getBackgroundId, backgroundId));
    }

    /**
     * 过滤空编号并去重，避免重复保存关联关系。
     *
     * @param ids 待处理编号
     * @return 规范化后的编号列表
     */
    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null) {
            return Collections.emptyList();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }
}
