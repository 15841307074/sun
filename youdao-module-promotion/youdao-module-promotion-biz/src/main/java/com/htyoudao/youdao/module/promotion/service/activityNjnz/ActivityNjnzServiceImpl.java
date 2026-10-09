package com.htyoudao.youdao.module.promotion.service.activityNjnz;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants.NJNZ_INCOMPLETE_PARAMETERS;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.commodity.api.CommodityApi;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityDataAnalysisRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityInfoRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityNjnzSaveReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStoreTag.ActivityStoreTagDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityStoreTag.ActivityStoreTagMapper;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnz.ActivityNjnzDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityNjnz.ActivityNjnzMapper;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.api.enums.activity.NjnzDiscountTypeEnum;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.activityNjnzCommodity.ActivityNjnzCommodityService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.system.api.orgstore.OrgStoreApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.mzt.logapi.context.LogRecordContext;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author villky
 */
@Service
@Slf4j
@RefreshScope
public class ActivityNjnzServiceImpl extends ServiceImpl<ActivityNjnzMapper, ActivityNjnzDO> implements
    ActivityNjnzService {

    @Resource
    private ActivityNjnzMapper activityNjnzMapper;

    @Resource
    private ActivityNjnzCommodityService activityNjnzCommodityService;

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
     * 赋值第几件打几折
     *
     * @author villky
     */
    private void setItemNum(ActivityNjnzDO activityNjnzDO) {
        Integer discountType = activityNjnzDO.getDiscountType();
        if (discountType.equals(NjnzDiscountTypeEnum.SECOND_HALF_PRICE.getCode())) {
            //如果是第二件半价
            activityNjnzDO.setDiscountItemNum(2);
            activityNjnzDO.setDiscountRate(5.0);
        } else if (discountType.equals(NjnzDiscountTypeEnum.BUY_ONE_GET_ONE.getCode())) {
            //如果是买一赠一
            activityNjnzDO.setDiscountItemNum(2);
            activityNjnzDO.setDiscountRate(0.0);
        } else if (discountType.equals(NjnzDiscountTypeEnum.CUSTOM.getCode())) {
            //如果是自定义优惠 需要两个参数都得传
            if (ObjectUtil.isEmpty(activityNjnzDO.getDiscountItemNum()) | ObjectUtil.isEmpty(
                activityNjnzDO.getDiscountRate())) {
                throw exception(NJNZ_INCOMPLETE_PARAMETERS);
            }
        }

    }


    /**
     * 创建活动
     *
     * @param activitySaveReqVO
     */
    @Override
    @Transactional
    public void createActivityNjnz(ActivityNjnzSaveReqVO activitySaveReqVO) {

        ActivityDO activityDO = getActivityDO(activitySaveReqVO);
        Long activityId = activityService.createActivity(activityDO);

        ActivityNjnzDO activityNjnzDO = new ActivityNjnzDO();
        BeanUtils.copyProperties(activitySaveReqVO, activityNjnzDO);
        //赋值第几件打几折
        setItemNum(activityNjnzDO);
        activityNjnzDO.setActivityId(activityId);
        activityNjnzMapper.insert(activityNjnzDO);

        rebuildActivityStoreTags(activityId, activitySaveReqVO.getAppScope(), activitySaveReqVO.getTagIds());

        if (ObjectUtil.isNotEmpty(activitySaveReqVO.getStoreIds())) {
            activityStoreService.createBatch(activitySaveReqVO.getStoreIds(), activityId);
        }
        if (ObjectUtil.isNotEmpty(activitySaveReqVO.getCommodityIds())) {
            activityNjnzCommodityService.createBatch(activitySaveReqVO.getCommodityIds(), activityId);
        }
        LogRecordContext.putVariable("activity", activityNjnzDO);
    }

    private String convertListToString(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    @Override
    @Transactional
    public void updateActivityNjnz(ActivityNjnzSaveReqVO activitySaveReqVO) {
        Long activityId = activitySaveReqVO.getActivityId();
        List<Long> oldTagIds = selectTagIdsByActivityId(activityId);
        ActivityDO oldActivityDO = activityService.selectById(activityId);
        Integer oldAppScope = oldActivityDO != null ? oldActivityDO.getAppScope() : null;

        ActivityDO activityDO = getActivityDO(activitySaveReqVO);
        activityService.updateActivity(activityDO);

        ActivityNjnzDO activityNjnzDO = new ActivityNjnzDO();
        BeanUtils.copyProperties(activitySaveReqVO, activityNjnzDO);
        //赋值第几件打几折
        setItemNum(activityNjnzDO);
        //修改活动
        activityNjnzMapper.updateById(activityNjnzDO);

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
        activityStoreService.deleteByActivityId(activityNjnzDO.getActivityId());
        //删除活动关联商品
        activityNjnzCommodityService.deleteByActivityId(activityNjnzDO.getActivityId());
        if (ObjectUtil.isNotEmpty(activitySaveReqVO.getStoreIds())) {
            activityStoreService.createBatch(activitySaveReqVO.getStoreIds(), activityNjnzDO.getActivityId());
        }
        if (ObjectUtil.isNotEmpty(activitySaveReqVO.getCommodityIds())) {
            activityNjnzCommodityService.createBatch(activitySaveReqVO.getCommodityIds(), activityNjnzDO.getActivityId());
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

    private ActivityDO getActivityDO(ActivityNjnzSaveReqVO activitySaveReqVO) {
        ActivityDO activityDO = new ActivityDO();
        BeanUtils.copyProperties(activitySaveReqVO, activityDO);
        activityDO.setId(activitySaveReqVO.getActivityId());
        activityDO.setWeekNumbers(convertListToString(activitySaveReqVO.getWeekNumberList()));
        activityDO.setDayNumbers(convertListToString(activitySaveReqVO.getDayNumberList()));
        activityDO.setTimeRange(convertListToString(activitySaveReqVO.getTimeRangeList()));
        activityDO.setActivityType(ActivityTypeEnum.NJ_NZ.getCode());
        activityDO.setActivityRemark(activitySaveReqVO.getActivityRemark());
        return activityDO;
    }

    @Override
    public void deleteActivityNjnz(Long id) {
        activityService.deleteActivity(id);
        LambdaQueryWrapper<ActivityNjnzDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityNjnzDO::getActivityId, id);
        activityNjnzMapper.delete(queryWrapper);
        activityStoreService.deleteByActivityId(id);
        activityNjnzCommodityService.deleteByActivityId(id);
    }


    @Override
    public ActivityInfoRespVO selectInfo(Long id) {
        ActivityDO activityDO = activityService.selectById(id);
        ActivityNjnzDO activityNjnzDO = activityNjnzMapper.selectOne(ActivityNjnzDO::getActivityId, id);
        if (activityDO == null || activityNjnzDO == null) {
            return null;
        }

        ActivityInfoRespVO activityInfoRespVO = new ActivityInfoRespVO();
        BeanUtils.copyProperties(activityDO, activityInfoRespVO);
        BeanUtils.copyProperties(activityNjnzDO, activityInfoRespVO);

        Double commodityCount = getCommodityCount(id);
        if (commodityCount > 0) {
            activityInfoRespVO.setIsEditable(false);
        }


        // 与满赠详情一致：标签范围只回显标签，门店范围仅在部分门店模式下回显门店。
        if (Integer.valueOf(1).equals(activityDO.getAppScope())) {
            activityInfoRespVO.setTagIds(selectTagIdsByActivityId(id));
            activityInfoRespVO.setStoreInfoDTOS(new ArrayList<>());
        } else {
            activityInfoRespVO.setTagIds(new ArrayList<>());
            if (Integer.valueOf(2).equals(activityInfoRespVO.getActivityStore())) {
                activityInfoRespVO.setStoreInfoDTOS(activityStoreService.storesByActivityId(activityNjnzDO.getActivityId()));
            } else {
                activityInfoRespVO.setStoreInfoDTOS(new ArrayList<>());
            }
        }

        //设置商品信息
        if (activityInfoRespVO.getActivityProduct().equals(2)) {
            activityInfoRespVO.setCommodityDTOList(activityNjnzCommodityService.listByActivityId(activityNjnzDO.getActivityId()));
        }else {
            activityInfoRespVO.setCommodityDTOList(new ArrayList<>());
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
                        log.warn("n件n折活动可叠加活动标识解析失败, value={}", trimmed);
                    }
                }
            }
            activityInfoRespVO.setStackableActivitieList(integerList);
        }


        return activityInfoRespVO;
    }

    private Double getCommodityCount(Long id) {
        //todo
        return 0.0;
    }

    @Override
    public ActivityDataAnalysisRespVO getDataAnalysis(Long id) {
        return null;
    }
}
