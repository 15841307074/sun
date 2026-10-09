package com.htyoudao.youdao.module.promotion.service.activityMz;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMz.vo.ActivityMzInfoRespVO.ActivityMzGiftRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMz.vo.ActivityMzInfoRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMz.vo.ActivityMzSaveReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMz.ActivityMzDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift.ActivityMzGiftDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStoreTag.ActivityStoreTagDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMz.ActivityMzMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityStoreTag.ActivityStoreTagMapper;
import com.htyoudao.youdao.module.promotion.api.enums.LogRecordConstants;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityMzCommodity.ActivityMzCommodityService;
import com.htyoudao.youdao.module.promotion.service.activityMzGift.ActivityMzGiftService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.system.api.orgstore.OrgStoreApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreInfoDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.MZ_GIFT_THRESHOLD_DUPLICATE;

/**
 * 满赠活动 Service 实现
 */
@Service
@Slf4j
@RefreshScope
public class ActivityMzServiceImpl extends ServiceImpl<ActivityMzMapper, ActivityMzDO> implements ActivityMzService {

    @Resource
    private ActivityMzMapper activityMzMapper;

    @Resource
    private ActivityMzGiftService activityMzGiftService;

    @Resource
    private ActivityMzCommodityService activityMzCommodityService;

    @Resource
    private ActivityStoreService activityStoreService;

    @Resource
    private ActivityStoreTagMapper activityStoreTagMapper;

    @DubboReference
    private StoreApi storeApi;

    @DubboReference
    private CommodityApi commodityApi;

    @DubboReference
    private OrgStoreApi orgStoreApi;

    @Resource
    private ActivityService activityService;

    @Resource
    private ActivityMzCacheService activityMzCacheService;

    /**
     * 创建满赠活动
     */
    @Override
    @Transactional
    @LogRecord(type = LogRecordConstants.PROMOTION_ACTIVITY_TYPE,
            subType = LogRecordConstants.PROMOTION_ACTIVITY_CREATE_TYPE,
            bizNo = "{{#activityId}}",
            success = LogRecordConstants.PROMOTION_ACTIVITY_CREATE_SUCCESS)
    public void createActivityMz(ActivityMzSaveReqVO activitySaveReqVO) {
        // 1. 创建公共活动记录
        ActivityDO activityDO = getActivityDO(activitySaveReqVO);
        Long activityId = activityService.createActivity(activityDO);

        // 2. 创建满赠活动配置
        ActivityMzDO activityMzDO = new ActivityMzDO();
        BeanUtils.copyProperties(activitySaveReqVO, activityMzDO);
        activityMzDO.setActivityId(activityId);
        activityMzMapper.insert(activityMzDO);

        // 解析实际门店ID（全部门店时自动查询所有门店；标签范围时按标签反查绑定门店）
        List<Long> resolvedStoreIds = resolveStoreIds(activitySaveReqVO, activitySaveReqVO.getTagIds());

        // 保存活动标签关联（应用范围=标签时），标签范围活动的 activity_store 绑定依赖标签反查的门店
        rebuildActivityStoreTags(activityId, activitySaveReqVO.getAppScope(), activitySaveReqVO.getTagIds());

        // 3. 创建赠送商品列表（根据库存类型区分门店独立/共用）
        if (ObjectUtil.isNotEmpty(activitySaveReqVO.getGiftList())) {
            // 校验门槛值不能重复
            validateGiftThresholds(activitySaveReqVO.getGiftList());

            List<ActivityMzGiftDO> giftDOList = buildGiftDOList(activitySaveReqVO.getGiftList());

            if (Integer.valueOf(2).equals(activitySaveReqVO.getGiftInventoryType())) {
                // 门店独立库存：每个门店单独一份赠品数据；无门店则不创建
                if (ObjectUtil.isNotEmpty(resolvedStoreIds)) {
                    for (Long storeId : resolvedStoreIds) {
                        activityMzGiftService.createBatchWithStoreId(giftDOList, activityId, storeId);
                    }
                }
            } else {
                // 共用库存：一份赠品数据
                activityMzGiftService.createBatch(giftDOList, activityId);
            }
        }

        // 4. 创建关联门店
        if (ObjectUtil.isNotEmpty(resolvedStoreIds)) {
            activityStoreService.createBatch(resolvedStoreIds, activityId);
        }

        // 5. 创建关联商品（下单类型=按商品限制 且 下单商品=指定商品 时才插入）
        if (Integer.valueOf(2).equals(activitySaveReqVO.getPlaceOrderType())
                && Integer.valueOf(2).equals(activitySaveReqVO.getPlaceOrderProduct())
                && ObjectUtil.isNotEmpty(activitySaveReqVO.getCommodityIds())) {
            activityMzCommodityService.createBatch(activitySaveReqVO.getCommodityIds(), activityId);
        }

        // 6. 初始化库存缓存
        List<ActivityMzGiftDO> cachedGiftList = activityMzGiftService.selectByActivityId(activityId);
        activityMzCacheService.initCacheOnCreate(activityId, activitySaveReqVO.getGiftInventoryType(),
                activitySaveReqVO.getUserLimitType(), activitySaveReqVO.getUserLimitValue(),
                cachedGiftList, resolvedStoreIds);

        LogRecordContext.putVariable("activity", activityDO);
        LogRecordContext.putVariable("activityId", activityId);
    }

    /**
     * 修改满赠活动
     */
    @Override
    @Transactional
    @LogRecord(type = LogRecordConstants.PROMOTION_ACTIVITY_TYPE,
            subType = LogRecordConstants.PROMOTION_ACTIVITY_UPDATE_TYPE,
            bizNo = "{{#activityId}}",
            success = LogRecordConstants.PROMOTION_ACTIVITY_UPDATE_SUCCESS)
    public void updateActivityMz(ActivityMzSaveReqVO activitySaveReqVO) {
        Long activityId = activitySaveReqVO.getActivityId();

        // 0. 提前获取旧门店ID和旧库存类型（用于后续清理孤立缓存key和检测模式切换，因为同事务内DB删除后查不到旧数据）
        List<Long> oldStoreIds = activityStoreService.selectStoreIdsByActivityId(activityId);
        List<Long> oldTagIds = selectTagIdsByActivityId(activityId);
        ActivityDO oldActivityDO = activityService.selectById(activityId);
        Integer oldAppScope = oldActivityDO != null ? oldActivityDO.getAppScope() : null;
        ActivityMzCacheService.ActivityMzMeta oldMeta = activityMzCacheService.getActivityMetaPublic(activityId);
        Integer oldGiftInventoryType = oldMeta != null ? oldMeta.getGiftInventoryType() : null;

        // 1. 更新公共活动记录
        ActivityDO activityDO = getActivityDO(activitySaveReqVO);
        activityService.updateActivity(activityDO);

        // 2. 更新满赠活动配置
        ActivityMzDO activityMzDO = new ActivityMzDO();
        BeanUtils.copyProperties(activitySaveReqVO, activityMzDO);
        activityMzMapper.updateById(activityMzDO);

        // 应用范围是否切换：切换时 activity_store / activity_store_tag 都按新范围严格重建，不沿用旧数据
        boolean appScopeSwitched = oldAppScope != null && !oldAppScope.equals(activitySaveReqVO.getAppScope());

        // 重建活动标签关联：应用范围=标签时保存标签（未切换且前端未传时沿用旧标签；切换后不沿用，避免旧标签泄漏到新范围），
        // 应用范围=门店时清理历史标签关联
        List<Long> effectiveTagIds = Collections.emptyList();
        if (Integer.valueOf(1).equals(activitySaveReqVO.getAppScope())) {
            if (ObjectUtil.isNotEmpty(activitySaveReqVO.getTagIds())) {
                effectiveTagIds = activitySaveReqVO.getTagIds();
            } else if (!appScopeSwitched) {
                effectiveTagIds = oldTagIds;
            }
        }
        rebuildActivityStoreTags(activityId, activitySaveReqVO.getAppScope(), effectiveTagIds);

        // 解析实际门店ID（标签范围按标签反查，全部门店时查询所有门店）
        List<Long> resolvedStoreIds = resolveStoreIds(activitySaveReqVO, effectiveTagIds);
        // 仅门店范围且部分门店模式（前端未传 storeIds）时回退旧门店关联；
        // 标签范围不回退——反查失败会抛异常中断更新，空结果只意味着标签未绑定任何门店，
        // 属合法状态需清空重建，沿用旧标签解析出的门店会把旧范围的绑定泄漏进新标签
        if (ObjectUtil.isEmpty(resolvedStoreIds) && !appScopeSwitched
                && !Integer.valueOf(1).equals(activitySaveReqVO.getAppScope())
                && Integer.valueOf(2).equals(activitySaveReqVO.getActivityStore())) {
            resolvedStoreIds = oldStoreIds;
        }

        // 3. 处理赠送商品（根据库存类型区分门店独立/共用）
        if (ObjectUtil.isNotEmpty(activitySaveReqVO.getGiftList())) {
            // 校验门槛值不能重复
            validateGiftThresholds(activitySaveReqVO.getGiftList());
        }
        List<ActivityMzGiftDO> giftDOList = ObjectUtil.isNotEmpty(activitySaveReqVO.getGiftList())
                ? buildGiftDOList(activitySaveReqVO.getGiftList()) : Collections.emptyList();

        // 共用→独立模式切换：先清除旧的共用赠品记录（storeId=null），避免遗留
        if (!Integer.valueOf(2).equals(oldGiftInventoryType) && Integer.valueOf(2).equals(activitySaveReqVO.getGiftInventoryType())) {
            activityMzGiftService.deleteByActivityId(activityId);
        }

        if (Integer.valueOf(2).equals(activitySaveReqVO.getGiftInventoryType())) {
            if (ObjectUtil.isEmpty(resolvedStoreIds)) {
                // 门店独立库存但门店集合为空（如 appScope 切换到标签范围且标签未命中任何门店）：
                // 清理旧门店的独立赠品数据，无门店则不创建，与 create 行为保持一致
                activityMzGiftService.deleteByActivityId(activityId);
            } else {
                // 门店独立库存：增量处理门店变更（含保留门店赠品重建），门店集合来自新应用范围解析
                handleStoreGiftUpdate(activityId, resolvedStoreIds, giftDOList, oldStoreIds);
            }
        } else {
            // 共用库存：删除重建
            activityMzGiftService.deleteByActivityId(activityId);
            if (ObjectUtil.isNotEmpty(giftDOList)) {
                activityMzGiftService.createBatch(giftDOList, activityId);
            }
        }

        // 4. 删除并重建关联门店
        activityStoreService.deleteByActivityId(activityId);
        if (ObjectUtil.isNotEmpty(resolvedStoreIds)) {
            activityStoreService.createBatch(resolvedStoreIds, activityId);
        }

        // 5. 删除并重建关联商品（下单类型=按商品限制 且 下单商品=指定商品 时才插入）
        activityMzCommodityService.deleteByActivityId(activityId);
        if (Integer.valueOf(2).equals(activitySaveReqVO.getPlaceOrderType())
                && Integer.valueOf(2).equals(activitySaveReqVO.getPlaceOrderProduct())
                && ObjectUtil.isNotEmpty(activitySaveReqVO.getCommodityIds())) {
            activityMzCommodityService.createBatch(activitySaveReqVO.getCommodityIds(), activityId);
        }

        // 6. 刷新库存缓存（传入旧门店ID和旧库存类型，用于清理孤立key和检测模式切换）
        List<ActivityMzGiftDO> cachedGiftList = activityMzGiftService.selectByActivityId(activityId);
        activityMzCacheService.refreshCacheOnUpdate(activityId, activitySaveReqVO.getGiftInventoryType(),
                activitySaveReqVO.getUserLimitType(), activitySaveReqVO.getUserLimitValue(),
                cachedGiftList, resolvedStoreIds, oldStoreIds, oldGiftInventoryType);

        LogRecordContext.putVariable("activity", activityDO);
        LogRecordContext.putVariable("activityId", activityId);
    }

    /**
     * 删除满赠活动
     */
    @Override
    @LogRecord(type = LogRecordConstants.PROMOTION_ACTIVITY_TYPE,
            subType = LogRecordConstants.PROMOTION_ACTIVITY_DELETE_TYPE,
            bizNo = "{{#id}}",
            success = LogRecordConstants.PROMOTION_ACTIVITY_DELETE_SUCCESS)
    public void deleteActivityMz(Long id) {
        // 提前获取门店ID（在删除关联数据之前捕获，否则 clearCacheOnDelete 无法清理门店独立库存缓存 key）
        // 获取方式按应用范围区分：标签范围按活动配置的标签反查当前绑定门店（activity_store 由联动维护，可能滞后不全）；
        // 门店范围沿用 activity_store 绑定
        ActivityDO activityDO = activityService.selectById(id);
        List<Long> storeIds;
        if (activityDO != null && Integer.valueOf(1).equals(activityDO.getAppScope())) {
            storeIds = resolveStoreIdsByTagIds(selectTagIdsByActivityId(id));
        } else {
            storeIds = activityStoreService.selectStoreIdsByActivityId(id);
        }

        activityService.deleteActivity(id);
        LambdaQueryWrapper<ActivityMzDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityMzDO::getActivityId, id);
        activityMzMapper.delete(queryWrapper);
        activityMzGiftService.deleteByActivityId(id);
        activityStoreService.deleteByActivityId(id);
        activityStoreTagMapper.delete(new LambdaQueryWrapper<ActivityStoreTagDO>()
                .eq(ActivityStoreTagDO::getActivityId, id));
        activityMzCommodityService.deleteByActivityId(id);

        // 清理库存缓存（传入提前捕获的门店ID）
        activityMzCacheService.clearCacheOnDelete(id, storeIds);
    }

    /**
     * 查询满赠活动详情
     */
    @Override
    public ActivityMzInfoRespVO selectInfo(Long id) {
        ActivityDO activityDO = activityService.selectById(id);
        ActivityMzDO activityMzDO = activityMzMapper.selectOne(ActivityMzDO::getActivityId, id);
        if (activityDO == null || activityMzDO == null) {
            return null;
        }

        ActivityMzInfoRespVO respVO = new ActivityMzInfoRespVO();
        BeanUtils.copyProperties(activityDO, respVO);
        BeanUtils.copyProperties(activityMzDO, respVO);

        // 设置日期/周几/时间段列表
        if (ObjectUtil.isEmpty(activityDO.getDayNumbers())) {
            respVO.setDayNumberList(new ArrayList<>());
        }
        if (ObjectUtil.isEmpty(activityDO.getWeekNumbers())) {
            respVO.setWeekNumberList(new ArrayList<>());
        }

        // 设置可叠加活动列表
        if (ObjectUtil.isNotEmpty(activityDO.getStackableActivities())) {
            String[] parts = activityDO.getStackableActivities().split(",");
            List<Integer> integerList = new ArrayList<>();
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        integerList.add(Integer.parseInt(trimmed));
                    } catch (NumberFormatException e) {
                        log.warn("满赠活动可叠加活动标识解析失败, value={}", trimmed);
                    }
                }
            }
            respVO.setStackableActivitieList(integerList);
        }

        // 回显应用范围：标签范围只回显标签集合，门店范围只回显门店列表（activityStore=2 部分门店时）
        if (Integer.valueOf(1).equals(activityDO.getAppScope())) {
            respVO.setTagIds(selectTagIdsByActivityId(id));
            respVO.setStoreInfoDTOS(new ArrayList<>());
        } else {
            respVO.setTagIds(new ArrayList<>());
            if (respVO.getActivityStore() != null && respVO.getActivityStore().equals(2)) {
                respVO.setStoreInfoDTOS(activityStoreService.storesByActivityId(activityMzDO.getActivityId()));
            } else {
                respVO.setStoreInfoDTOS(new ArrayList<>());
            }
        }

        // 设置赠送商品列表（独立库存模式下每个门店有独立记录，按 giftCommodityId 去重只保留一条，按门槛从小到大排序）
        List<ActivityMzGiftDO> giftList = activityMzGiftService.selectByActivityId(activityMzDO.getActivityId());
        if (ObjectUtil.isNotEmpty(giftList)) {
            Set<Long> seenGiftIds = new HashSet<>();
            List<ActivityMzGiftRespVO> giftVOList = new ArrayList<>();
            for (ActivityMzGiftDO gift : giftList) {
                if (seenGiftIds.add(gift.getGiftCommodityId())) {
                    ActivityMzGiftRespVO giftVO = new ActivityMzGiftRespVO();
                    BeanUtils.copyProperties(gift, giftVO);
                    giftVOList.add(giftVO);
                }
            }
            // 按门槛从小到大排序
            giftVOList.sort(Comparator.comparing(ActivityMzGiftRespVO::getThreshold, Comparator.nullsLast(Comparator.naturalOrder())));
            respVO.setGiftList(giftVOList);
        } else {
            respVO.setGiftList(new ArrayList<>());
        }

        // 设置关联商品信息（下单类型=按商品限制 且 下单商品=指定商品 时才有数据）
        if (Integer.valueOf(2).equals(respVO.getPlaceOrderType())
                && Integer.valueOf(2).equals(respVO.getPlaceOrderProduct())) {
            respVO.setCommodityDTOList(activityMzCommodityService.listByActivityId(activityMzDO.getActivityId()));
        } else {
            respVO.setCommodityDTOList(new ArrayList<>());
        }

        return respVO;
    }

    // ==================== 私有方法 ====================

    /**
     * 校验赠品门槛值不能重复
     */
    private void validateGiftThresholds(List<ActivityMzSaveReqVO.ActivityMzGiftReqVO> giftList) {
        Set<BigDecimal> thresholds = new HashSet<>();
        for (ActivityMzSaveReqVO.ActivityMzGiftReqVO gift : giftList) {
            if (gift.getThreshold() != null && !thresholds.add(gift.getThreshold())) {
                throw exception(MZ_GIFT_THRESHOLD_DUPLICATE);
            }
        }
    }

    /**
     * 构建赠送商品 DO 列表（不含 activityId / storeId，由调用方设置）
     */
    private List<ActivityMzGiftDO> buildGiftDOList(List<ActivityMzSaveReqVO.ActivityMzGiftReqVO> giftReqList) {
        return giftReqList.stream().map(gift -> {
            ActivityMzGiftDO giftDO = new ActivityMzGiftDO();
            BeanUtils.copyProperties(gift, giftDO);
            giftDO.setId(null); // 清空id，由雪花算法重新生成，避免逻辑删除后主键冲突
            giftDO.setRemainingInventory(gift.getActivityInventory());
            giftDO.setLockedInventory(0);
            giftDO.setConsumedInventory(0);
            return giftDO;
        }).collect(Collectors.toList());
    }

    /**
     * 处理门店独立库存模式下的赠品变更（增量 diff）
     * <p>
     * 1. 计算需要删除/新增/保留的门店
     * 2. 删除移除门店的赠品数据
     * 3. 为新增门店创建赠品数据
     * 4. 为保留门店重建赠品数据（删除旧赠品 + 创建新赠品，以更新赠品配置）
     *
     * @param oldStoreIds 变更前的旧门店ID列表（由调用方提前捕获）
     */
    private void handleStoreGiftUpdate(Long activityId, List<Long> newStoreIds, List<ActivityMzGiftDO> newGiftDOList,
                                       List<Long> oldStoreIds) {
        Set<Long> oldSet = new HashSet<>(ObjectUtil.isNotEmpty(oldStoreIds) ? oldStoreIds : Collections.emptyList());
        Set<Long> newSet = new HashSet<>(ObjectUtil.isNotEmpty(newStoreIds) ? newStoreIds : Collections.emptyList());

        // 需要删除的门店 = 旧门店 - 新门店
        Set<Long> toDeleteStoreIds = new HashSet<>(oldSet);
        toDeleteStoreIds.removeAll(newSet);

        // 需要新增的门店 = 新门店 - 旧门店
        Set<Long> toAddStoreIds = new HashSet<>(newSet);
        toAddStoreIds.removeAll(oldSet);

        // 保留的门店 = 旧门店 ∩ 新门店
        Set<Long> retainedStoreIds = new HashSet<>(oldSet);
        retainedStoreIds.retainAll(newSet);

        log.info("满赠活动[{}]门店独立库存变更 - 删除门店:{}, 新增门店:{}, 保留门店:{}",
                activityId, toDeleteStoreIds, toAddStoreIds, retainedStoreIds);

        // 删除移除门店的赠品数据
        if (!toDeleteStoreIds.isEmpty()) {
            activityMzGiftService.deleteByActivityIdAndStoreIds(activityId, new ArrayList<>(toDeleteStoreIds));
        }

        // 为新增门店创建赠品数据
        if (!toAddStoreIds.isEmpty() && ObjectUtil.isNotEmpty(newGiftDOList)) {
            for (Long storeId : toAddStoreIds) {
                activityMzGiftService.createBatchWithStoreId(newGiftDOList, activityId, storeId);
            }
        }

        // 为保留门店重建赠品数据（先删后建，更新赠品配置）
        if (!retainedStoreIds.isEmpty() && ObjectUtil.isNotEmpty(newGiftDOList)) {
            activityMzGiftService.deleteByActivityIdAndStoreIds(activityId, new ArrayList<>(retainedStoreIds));
            for (Long storeId : retainedStoreIds) {
                activityMzGiftService.createBatchWithStoreId(newGiftDOList, activityId, storeId);
            }
        }
    }

    /**
     * 解析实际门店ID列表
     * <p>
     * 应用范围=标签时按标签反查绑定门店；
     * 当 activityStore=1（全部门店）且前端未传 storeIds 时，
     * 通过 storeApi 查询所有门店并返回完整列表。
     *
     * @param activitySaveReqVO 请求VO
     * @param tagIds 应用范围=标签时生效的标签ID集合（update 场景可能已回退为旧标签）
     * @return 实际要使用的门店ID列表（可能为空）
     */
    private List<Long> resolveStoreIds(ActivityMzSaveReqVO activitySaveReqVO, List<Long> tagIds) {
        // 应用范围=标签：按标签反查当前绑定了这些标签的门店
        if (Integer.valueOf(1).equals(activitySaveReqVO.getAppScope())) {
            return resolveStoreIdsByTagIds(tagIds);
        }
        List<Long> storeIds = activitySaveReqVO.getStoreIds();
        // 全部门店 + 未指定门店ID → 查全部门店
        if (Integer.valueOf(1).equals(activitySaveReqVO.getActivityStore())
                && ObjectUtil.isEmpty(storeIds)) {
            CommonResult<List<StoreInfoDTO>> allStoreList = storeApi.getAllStoreList();
            if (allStoreList.isSuccess() && ObjectUtil.isNotEmpty(allStoreList.getData())) {
                storeIds = allStoreList.getData().stream()
                        .map(StoreInfoDTO::getStoreId)
                        .collect(Collectors.toList());
                log.info("满赠活动使用所有门店, 门店数={}", storeIds.size());
            } else {
                log.warn("获取所有门店列表失败或为空");
                storeIds = Collections.emptyList();
            }
        }
        return ObjectUtil.isNotEmpty(storeIds) ? storeIds : Collections.emptyList();
    }

    /**
     * 按标签反查门店：任一标签命中的门店均参与活动（activity_store 绑定、门店独立库存都依赖该列表）
     */
    private List<Long> resolveStoreIdsByTagIds(List<Long> tagIds) {
        List<Long> distinctTagIds = ObjectUtil.isEmpty(tagIds) ? Collections.emptyList()
                : tagIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (distinctTagIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, List<StoreInfoDTO>> storesByTagId = storeApi.getStoreIdsByTagIds(distinctTagIds);
        if (ObjectUtil.isEmpty(storesByTagId)) {
            log.warn("按标签查询门店失败或为空, tagIds={}", distinctTagIds);
            return Collections.emptyList();
        }
        return storesByTagId.values().stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .map(StoreInfoDTO::getStoreId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 查询活动配置的标签ID集合（activity_store_tag）
     */
    private List<Long> selectTagIdsByActivityId(Long activityId) {
        return activityStoreTagMapper.selectList(new LambdaQueryWrapper<ActivityStoreTagDO>()
                        .eq(ActivityStoreTagDO::getActivityId, activityId))
                .stream()
                .map(ActivityStoreTagDO::getTagId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 重建活动标签关联：应用范围=标签时先删后插；应用范围=门店时不保存并清理历史关联（覆盖范围切换场景）
     */
    private void rebuildActivityStoreTags(Long activityId, Integer appScope, List<Long> tagIds) {
        activityStoreTagMapper.delete(new LambdaQueryWrapper<ActivityStoreTagDO>()
                .eq(ActivityStoreTagDO::getActivityId, activityId));
        if (!Integer.valueOf(1).equals(appScope) || ObjectUtil.isEmpty(tagIds)) {
            return;
        }
        tagIds.stream().filter(Objects::nonNull).distinct().forEach(tagId -> {
            ActivityStoreTagDO tagDO = new ActivityStoreTagDO();
            tagDO.setActivityId(activityId);
            tagDO.setTagId(tagId);
            activityStoreTagMapper.insert(tagDO);
        });
    }

    /**
     * 构建公共活动 DO
     */
    private ActivityDO getActivityDO(ActivityMzSaveReqVO activitySaveReqVO) {
        ActivityDO activityDO = new ActivityDO();
        BeanUtils.copyProperties(activitySaveReqVO, activityDO);
        activityDO.setId(activitySaveReqVO.getActivityId());
        activityDO.setActivityType(ActivityTypeEnum.MZ.getCode());
        activityDO.setActivityRemark(activitySaveReqVO.getActivityRemark());
        activityDO.setDiscountStackable(activitySaveReqVO.getDiscountStackable());

        // 可叠加活动（1优惠券 2N件N折 3满减满折 4满赠活动，逗号分隔）
        activityDO.setStackableActivities(convertListToString(activitySaveReqVO.getStackableActivitieList()));

        // 日期/周几/时间段转字符串
        activityDO.setWeekNumbers(convertListToString(activitySaveReqVO.getWeekNumberList()));
        activityDO.setDayNumbers(convertListToString(activitySaveReqVO.getDayNumberList()));
        activityDO.setTimeRange(convertListToString(activitySaveReqVO.getTimeRangeList()));

        return activityDO;
    }

    private String convertListToString(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().map(Object::toString).collect(Collectors.joining(","));
    }
}
