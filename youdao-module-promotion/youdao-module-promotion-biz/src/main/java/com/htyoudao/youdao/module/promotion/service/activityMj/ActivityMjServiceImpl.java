package com.htyoudao.youdao.module.promotion.service.activityMj;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMj.vo.ActivityMjInfoRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityMj.vo.ActivityMjSaveReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStoreTag.ActivityStoreTagDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityStoreTag.ActivityStoreTagMapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMj.ActivityMjDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMj.ActivityMjMapper;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityMjCommodity.ActivityMjCommodityService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.system.api.orgstore.OrgStoreApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.mzt.logapi.context.LogRecordContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.List;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author Yangqinglin
 */
@Service
@Slf4j
@RefreshScope
public class ActivityMjServiceImpl extends ServiceImpl<ActivityMjMapper, ActivityMjDO> implements
        ActivityMjService {

    @Resource
    private ActivityMjMapper activityMjMapper;

    @Resource
    private ActivityMjCommodityService activityMjCommodityService;

    @Resource
    private ActivityStoreService activityStoreService;

    @DubboReference
    private StoreApi storeApi;

    @DubboReference
    private CommodityApi commodityApi;

    @DubboReference
    private OrgStoreApi orgStoreApi;

    @Resource
    private ActivityService activityService;

    @Resource
    private ActivityStoreTagMapper activityStoreTagMapper;


    /**
     * 创建活动
     *
     * @param activitySaveReqVO
     */
    @Override
    @Transactional
    public void createActivityMj(ActivityMjSaveReqVO activitySaveReqVO) {

        ActivityDO activityDO = getActivityDO(activitySaveReqVO);
        Long activityId = activityService.createActivity(activityDO);

        ActivityMjDO activityMjDO = new ActivityMjDO();
        BeanUtils.copyProperties(activitySaveReqVO, activityMjDO);
        activityMjDO.setDiscountSettings(convertListToString(activitySaveReqVO.getSettingList()));
        activityMjDO.setActivityId(activityId);
        activityMjMapper.insert(activityMjDO);

        rebuildActivityStoreTags(activityId, activitySaveReqVO.getAppScope(), activitySaveReqVO.getTagIds());

        if (ObjectUtil.isNotEmpty(activitySaveReqVO.getStoreIds())) {
            activityStoreService.createBatch(activitySaveReqVO.getStoreIds(), activityId);
        }
        if (ObjectUtil.isNotEmpty(activitySaveReqVO.getCommodityIds())) {
            activityMjCommodityService.createBatch(activitySaveReqVO.getCommodityIds(), activityId);
        }
        LogRecordContext.putVariable("activity", activityMjDO);
    }

    private String convertListToString(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    @Override
    @Transactional
    public void updateActivityMj(ActivityMjSaveReqVO activitySaveReqVO) {
        Long activityId = activitySaveReqVO.getActivityId();
        List<Long> oldTagIds = selectTagIdsByActivityId(activityId);
        ActivityDO oldActivityDO = activityService.selectById(activityId);
        Integer oldAppScope = oldActivityDO != null ? oldActivityDO.getAppScope() : null;

        ActivityDO activityDO = getActivityDO(activitySaveReqVO);
        activityService.updateActivity(activityDO);

        ActivityMjDO activityMjDO = new ActivityMjDO();
        BeanUtils.copyProperties(activitySaveReqVO, activityMjDO);
        activityMjDO.setDiscountSettings(convertListToString(activitySaveReqVO.getSettingList()));
        //修改活动
        activityMjMapper.updateById(activityMjDO);

        // 与满赠保持一致：未切换范围且标签为空时沿用旧标签，切换范围后不沿用。
        boolean appScopeSwitched = oldAppScope != null && !oldAppScope.equals(activitySaveReqVO.getAppScope());
        List<Long> effectiveTagIds = Collections.emptyList();
        if (Integer.valueOf(1).equals(activitySaveReqVO.getAppScope())) {
            if (ObjectUtil.isNotEmpty(activitySaveReqVO.getTagIds())) {
                effectiveTagIds = activitySaveReqVO.getTagIds();
            } else if (!appScopeSwitched) {
                effectiveTagIds = oldTagIds;
            }
        }
        rebuildActivityStoreTags(activityId, activitySaveReqVO.getAppScope(), effectiveTagIds);

        //删除活动关联门店
        activityStoreService.deleteByActivityId(activityMjDO.getActivityId());
        //删除活动关联商品
        activityMjCommodityService.deleteByActivityId(activityMjDO.getActivityId());
        if (ObjectUtil.isNotEmpty(activitySaveReqVO.getStoreIds())) {
            activityStoreService.createBatch(activitySaveReqVO.getStoreIds(), activityMjDO.getActivityId());
        }
        if (ObjectUtil.isNotEmpty(activitySaveReqVO.getCommodityIds())) {
            activityMjCommodityService.createBatch(activitySaveReqVO.getCommodityIds(), activityMjDO.getActivityId());
        }
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

    private ActivityDO getActivityDO(ActivityMjSaveReqVO activitySaveReqVO) {
        ActivityDO activityDO = new ActivityDO();
        BeanUtils.copyProperties(activitySaveReqVO, activityDO);
        if(ObjectUtil.isNotEmpty(activitySaveReqVO.getDiscountStackable())){
            activityDO.setStackableActivities(activitySaveReqVO.getDiscountStackable().toString());
        }
        activityDO.setId(activitySaveReqVO.getActivityId());
        activityDO.setWeekNumbers(convertListToString(activitySaveReqVO.getWeekNumberList()));
        activityDO.setDayNumbers(convertListToString(activitySaveReqVO.getDayNumberList()));
        activityDO.setTimeRange(convertListToString(activitySaveReqVO.getTimeRangeList()));
        activityDO.setActivityType(ActivityTypeEnum.MJ.getCode());
        activityDO.setActivityRemark(activitySaveReqVO.getActivityRemark());
        activityDO.setDiscountStackable(activitySaveReqVO.getDiscountStackable());
        if(ObjectUtil.isNotEmpty(activitySaveReqVO.getStackableActivitieList())){
            activityDO.setStackableActivities(convertListToString(activitySaveReqVO.getStackableActivitieList()));
        }
        return activityDO;
    }

    @Override
    public void deleteActivityMj(Long id) {
        activityService.deleteActivity(id);
        LambdaQueryWrapper<ActivityMjDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityMjDO::getActivityId, id);
        activityMjMapper.delete(queryWrapper);
        activityStoreService.deleteByActivityId(id);
        activityMjCommodityService.deleteByActivityId(id);
    }


    @Override
    public ActivityMjInfoRespVO selectInfo(Long id) {
        ActivityDO activityDO = activityService.selectById(id);
        ActivityMjDO activityMjDO = activityMjMapper.selectOne(ActivityMjDO::getActivityId, id);
        if (activityDO == null || activityMjDO == null) {
            return null;
        }

        ActivityMjInfoRespVO activityInfoRespVO = new ActivityMjInfoRespVO();
        BeanUtils.copyProperties(activityDO, activityInfoRespVO);
        BeanUtils.copyProperties(activityMjDO, activityInfoRespVO);
        if(ObjectUtil.isEmpty(activityDO.getDayNumbers())){
            activityInfoRespVO.setDayNumberList(new ArrayList<>());
        }

        if(ObjectUtil.isEmpty(activityDO.getWeekNumbers())){
            activityInfoRespVO.setWeekNumberList(new ArrayList<>());
        }

        if (ObjectUtil.isNotEmpty(activityDO.getStackableActivities())) {
            String[] parts = activityDO.getStackableActivities().split(",");
            List<Integer> integerList = new ArrayList<>();
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        integerList.add(Integer.parseInt(trimmed));
                    } catch (NumberFormatException e) {
                        log.warn("满减活动可叠加活动标识解析失败, value={}", trimmed);
                    }
                }
            }
            activityInfoRespVO.setStackableActivitieList(integerList);
        }

        // 与满赠详情一致：标签范围只回显标签，门店范围仅在部分门店模式下回显门店。
        if (Integer.valueOf(1).equals(activityDO.getAppScope())) {
            activityInfoRespVO.setTagIds(selectTagIdsByActivityId(id));
            activityInfoRespVO.setStoreInfoDTOS(new ArrayList<>());
        } else {
            activityInfoRespVO.setTagIds(new ArrayList<>());
            if (Integer.valueOf(2).equals(activityInfoRespVO.getActivityStore())) {
                activityInfoRespVO.setStoreInfoDTOS(activityStoreService.storesByActivityId(activityMjDO.getActivityId()));
            } else {
                activityInfoRespVO.setStoreInfoDTOS(new ArrayList<>());
            }
        }

        //设置商品信息
        if (activityInfoRespVO.getActivityProduct().equals(2)) {
            activityInfoRespVO.setCommodityDTOList(activityMjCommodityService.listByActivityId(activityMjDO.getActivityId()));
        }else {
            activityInfoRespVO.setCommodityDTOList(new ArrayList<>());
        }

        return activityInfoRespVO;
    }




}
