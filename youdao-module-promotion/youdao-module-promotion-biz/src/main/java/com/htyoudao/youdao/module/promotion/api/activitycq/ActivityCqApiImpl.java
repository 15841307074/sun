package com.htyoudao.youdao.module.promotion.api.activitycq;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.sharding.core.enums.DsNameConstants;
import com.htyoudao.youdao.module.promotion.api.activityjk.DTO.ActivityJkOrderReqDTO;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.controller.app.activityCQ.vo.ActivityCqReqVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityCq.ActivityCqDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityStore.ActivityStoreDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityCommodity.ActivityCommodityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityCq.ActivityCqMapper;
import com.htyoudao.youdao.module.promotion.service.activityCqApp.ActivityCqAppService;
import com.htyoudao.youdao.module.promotion.service.activityStrore.ActivityStoreService;
import com.htyoudao.youdao.module.promotion.util.TimeValidationUtil;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@DubboService
@Validated
@DS(DsNameConstants.SHARDING)
public class ActivityCqApiImpl implements ActivityCqApi {

    @Resource
    private ActivityCqMapper activityCqMapper;
    @Resource
    private ActivityMapper activityMapper;
    @Resource
    private ActivityStoreService activityStoreService;
    @Resource
    private ActivityCommodityMapper activityCommodityMapper;
    @Resource
    private ActivityCqAppService activityCqAppService;

    @Override
    public void getActivityCqList(ActivityJkOrderReqDTO reqDTO) {
        if (reqDTO == null || reqDTO.getStoreId() == null || reqDTO.getMemberId() == null) {
            return;
        }
        List<ActivityCqDO> activityCqList = activityCqMapper.selectList(new LambdaQueryWrapperX<ActivityCqDO>()
                .eq(ActivityCqDO::getPlaceOrderStatus, 1));
        if (activityCqList.isEmpty()) {
            return;
        }
        Map<Long, ActivityCqDO> activityCqMap = activityCqList.stream()
                .filter(item -> item.getActivityId() != null)
                .collect(Collectors.toMap(ActivityCqDO::getActivityId, item -> item, (left, right) -> left));
        List<Long> activityIds = new ArrayList<>(activityCqMap.keySet());
        List<ActivityDO> activityList = activityMapper.selectList(new LambdaQueryWrapperX<ActivityDO>()
                .in(ActivityDO::getId, activityIds)
                .eq(ActivityDO::getActivityType, ActivityTypeEnum.CQ.getCode())
                .eq(ActivityDO::getIsEnabled, 1));
        if (activityList.isEmpty()) {
            return;
        }
        List<Long> limitedStoreActivityIds = activityList.stream()
                .filter(activity -> Objects.equals(activity.getActivityStore(), 0))
                .map(ActivityDO::getId)
                .collect(Collectors.toList());
        Map<Long, List<Long>> activityStoreMap = limitedStoreActivityIds.isEmpty()
                ? Collections.emptyMap()
                : activityStoreService.list(new LambdaQueryWrapperX<ActivityStoreDO>()
                        .in(ActivityStoreDO::getActivityId, limitedStoreActivityIds)).stream()
                .collect(Collectors.groupingBy(
                        ActivityStoreDO::getActivityId,
                        Collectors.mapping(ActivityStoreDO::getStoreId, Collectors.toList())
        ));
        List<ActivityDO> candidateActivityList = activityList.stream()
                .filter(activity -> TimeValidationUtil.isTimeValid(
                        resolveCqStartDate(activity, activityCqMap.get(activity.getId())),
                        resolveCqEndDate(activity, activityCqMap.get(activity.getId())),
                        activity.getDayNumbers(),
                        activity.getWeekNumbers(), activity.getTimeRange()))
                .filter(activity -> isStoreMatched(activity, activityStoreMap.get(activity.getId()), reqDTO.getStoreId()))
                .collect(Collectors.toList());
        if (candidateActivityList.isEmpty()) {
            return;
        }
        Set<Long> candidateActivityIds = candidateActivityList.stream().map(ActivityDO::getId).collect(Collectors.toSet());
        Set<Long> specifiedCommodityActivityIds = activityCqList.stream()
                .filter(item -> item.getActivityId() != null && candidateActivityIds.contains(item.getActivityId()))
                .filter(item -> Objects.equals(item.getPlaceOrderProduct(), 2))
                .map(ActivityCqDO::getActivityId)
                .collect(Collectors.toSet());
        Map<Long, List<Long>> commodityMap = buildCommodityMap(specifiedCommodityActivityIds);
        for (ActivityDO activityDO : candidateActivityList) {
            ActivityCqDO activityCqDO = activityCqMap.get(activityDO.getId());
            if (activityCqDO == null) {
                continue;
            }
            if (!Objects.equals(activityCqDO.getDrawStatus(), 0)) {
                continue;
            }
            if (activityCqDO.getResultPublishTime() != null && !LocalDateTime.now().isBefore(activityCqDO.getResultPublishTime())) {
                continue;
            }
            if (!isOrderMatched(activityCqDO, commodityMap.getOrDefault(activityDO.getId(), Collections.emptyList()), reqDTO)) {
                continue;
            }
            ActivityCqReqVO activityCqReqVO = new ActivityCqReqVO();
            activityCqReqVO.setActivityId(activityDO.getId());
            activityCqReqVO.setMemberId(reqDTO.getMemberId());
            activityCqReqVO.setStoreId(reqDTO.getStoreId());
            try {
                activityCqAppService.validateJoined(activityDO.getId(), reqDTO.getMemberId());
                activityCqAppService.orderTask(activityCqReqVO);
            } catch (Exception e) {
                // 单个抽签活动不满足参与资格或任务处理失败时，不影响其他活动继续处理。
            }
        }
    }

    private Date resolveCqStartDate(ActivityDO activityDO, ActivityCqDO activityCqDO) {
        if (activityCqDO != null && activityCqDO.getStartDateTime() != null) {
            return Date.from(activityCqDO.getStartDateTime().atZone(ZoneId.of("Asia/Shanghai")).toInstant());
        }
        return activityDO == null ? null : activityDO.getStartDate();
    }

    private Date resolveCqEndDate(ActivityDO activityDO, ActivityCqDO activityCqDO) {
        if (activityCqDO != null && activityCqDO.getEndDateTime() != null) {
            return Date.from(activityCqDO.getEndDateTime().atZone(ZoneId.of("Asia/Shanghai")).toInstant());
        }
        return activityDO == null ? null : activityDO.getEndDate();
    }

    private boolean isOrderMatched(ActivityCqDO activityCqDO, List<Long> configuredCommodityIds, ActivityJkOrderReqDTO reqDTO) {
        if (activityCqDO == null || reqDTO == null) {
            return false;
        }
        boolean categoryRestricted = Objects.equals(activityCqDO.getPlaceOrderType(), 1);
        if (categoryRestricted && !isCategoryMatched(activityCqDO.getCategoryType(), reqDTO.getOrderProductType())) {
            return false;
        }
        if (!categoryRestricted && Objects.equals(activityCqDO.getPlaceOrderProduct(), 2)) {
            if (configuredCommodityIds.isEmpty() || reqDTO.getCommodityIds() == null || reqDTO.getCommodityIds().isEmpty()) {
                return false;
            }
            boolean matched = reqDTO.getCommodityIds().stream().anyMatch(configuredCommodityIds::contains);
            if (!matched) {
                return false;
            }
        }
        if (Objects.equals(activityCqDO.getPaymentThreshold(), 1)) {
            BigDecimal paymentAmount = reqDTO.getPaymentAmount();
            BigDecimal thresholdAmount = activityCqDO.getPaymentCount();
            if (paymentAmount == null || thresholdAmount == null || paymentAmount.compareTo(thresholdAmount) < 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isCategoryMatched(Integer categoryType, Integer orderProductType) {
        if (categoryType == null || Objects.equals(categoryType, 1)) {
            return true;
        }
        if (orderProductType == null) {
            return false;
        }
        if (Objects.equals(categoryType, 2)) {
            return Objects.equals(orderProductType, 1) || Objects.equals(orderProductType, 3);
        }
        if (Objects.equals(categoryType, 3)) {
            return Objects.equals(orderProductType, 2) || Objects.equals(orderProductType, 3);
        }
        return true;
    }

    private boolean isStoreMatched(ActivityDO activityDO, List<Long> relatedStores, Long storeId) {
        if (activityDO != null && Objects.equals(activityDO.getActivityStore(), 1)) {
            return true;
        }
        return relatedStores == null || relatedStores.isEmpty() || relatedStores.contains(storeId);
    }

    private Map<Long, List<Long>> buildCommodityMap(Set<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return activityCommodityMapper.selectList(new LambdaQueryWrapperX<ActivityCommodityDO>()
                        .in(ActivityCommodityDO::getActivityId, new ArrayList<>(activityIds)))
                .stream()
                .filter(item -> item.getActivityId() != null && item.getCommodityId() != null)
                .collect(Collectors.groupingBy(
                        ActivityCommodityDO::getActivityId,
                        Collectors.mapping(ActivityCommodityDO::getCommodityId, Collectors.toList())
                ));
    }

}
