package com.htyoudao.youdao.module.promotion.service.activityNjnz;

import static com.htyoudao.youdao.module.promotion.api.enums.activity.NjnzDiscountTypeEnum.BUY_ONE_GET_ONE;
import static com.htyoudao.youdao.module.promotion.api.enums.activity.NjnzDiscountTypeEnum.SECOND_HALF_PRICE;

import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityNjnzDTO;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnz.ActivityNjnzDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityNjnzCommodity.ActivityNjnzCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityNjnz.ActivityNjnzMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityNjnzCommodity.ActivityNjnzCommodityMapper;
import com.htyoudao.youdao.module.promotion.api.enums.activity.NjnzDiscountTypeEnum;
import com.htyoudao.youdao.module.promotion.api.enums.activity.NjnzProductScopeEnum;
import com.htyoudao.youdao.module.promotion.service.activity.ApplicableActivityQueryService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
@RefreshScope
public class ActivityNjnzAppServiceImpl implements ActivityNjnzAppService {

    @Resource
    private ActivityNjnzMapper njnzMapper;

    @Resource
    private ActivityNjnzCommodityMapper njnzCommodityMapper;

    @Resource
    private ApplicableActivityQueryService applicableActivityQueryService;

    @Value("${activity.njnz.switch:1}")
    private String njnzSwitch;


    @Override
    public Map<Long, List<ActivityNjnzDTO>> selectNjnzActivity(Long storeId, Collection<Long> commodityIds) {
        // 活动开关关闭
        if (Objects.equals(njnzSwitch, "0")) {
            return Map.of();
        }

        List<ActivityDO> allActivities = applicableActivityQueryService.selectApplicable(
                storeId, List.of(ActivityTypeEnum.NJ_NZ.getCode()));
        return selectNjnzActivity(commodityIds, allActivities);
    }

    @Override
    public Map<Long, List<ActivityNjnzDTO>> selectNjnzActivity(Collection<Long> commodityIds,
                                                                List<ActivityDO> allActivities) {
        if (Objects.equals(njnzSwitch, "0")) {
            return Map.of();
        }
        Set<Long> commodityIdSet = commodityIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (commodityIdSet.isEmpty() || CollectionUtils.isEmpty(allActivities)) {
            return Map.of();
        }
        Map<Long, ActivityDO> activityDOMap = allActivities.stream()
                .collect(Collectors.toMap(ActivityDO::getId, Function.identity()));
        List<Long> list = allActivities.stream().map(ActivityDO::getId).toList();
        List<ActivityNjnzDO> activityNjnzDOS = njnzMapper.selectList(ActivityNjnzDO::getActivityId, list);

        // 拆分活动类型
        List<ActivityNjnzDO> allApplicableActivities = activityNjnzDOS.stream()
            .filter(a -> Objects.equals(a.getActivityProduct(), NjnzProductScopeEnum.ALL.getCode())).toList();

        List<ActivityNjnzDO> specificApplicableActivities = activityNjnzDOS.stream()
            .filter(a -> Objects.equals(a.getActivityProduct(), NjnzProductScopeEnum.SPECIFIC.getCode())).toList();

        Map<Long, Set<Long>> resultMap = new HashMap<>();

        // 全部商品适用的活动
        Set<Long> allActivityIds = allApplicableActivities.stream()
            .map(ActivityNjnzDO::getActivityId)
            .collect(Collectors.toSet());
        for (Long commodityId : commodityIdSet) {
            resultMap.put(commodityId, new HashSet<>(allActivityIds));
        }

        // 处理指定商品适用的活动
        if (!specificApplicableActivities.isEmpty()) {
            List<Long> activityIds = specificApplicableActivities.stream().map(ActivityNjnzDO::getActivityId)
                .distinct()
                .toList();

            List<ActivityNjnzCommodityDO> commodityLinks = njnzCommodityMapper
                    .selectByActivityIdsAndCommodityIds(activityIds, commodityIdSet);
            for (ActivityNjnzCommodityDO commodityLink : commodityLinks) {
                Set<Long> activityIdSet = resultMap.getOrDefault(commodityLink.getCommodityId(), new HashSet<>());
                activityIdSet.add(commodityLink.getActivityId());
                resultMap.put(commodityLink.getCommodityId(), activityIdSet);
            }
        }

        Map<Long, ActivityNjnzDO> collect = activityNjnzDOS.stream()
            .collect(Collectors.toMap(ActivityNjnzDO::getActivityId, Function.identity()));
        Map<Long, ActivityNjnzDTO> dtoByActivityId = collect.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> toDTO(activityDOMap.get(entry.getKey()), entry.getValue())));

        // 转换为 DTO 结果
        Map<Long, List<ActivityNjnzDTO>> finalResult = new HashMap<>();

        for (Map.Entry<Long, Set<Long>> entry : resultMap.entrySet()) {
            // 用于按tag去重，保留最新创建的对象
            Map<String, ActivityNjnzDTO> tagToLatestDTO = new HashMap<>();

            for (Long activityId : entry.getValue()) {
                ActivityNjnzDTO dto = dtoByActivityId.get(activityId);
                if (dto == null) {
                    continue;
                }

                // 按tag去重，保留最新创建的
                ActivityNjnzDTO existingDTO = tagToLatestDTO.get(dto.getTag());
                if (existingDTO == null || existingDTO.getCreateTime().isBefore(dto.getCreateTime())) {
                    tagToLatestDTO.put(dto.getTag(), dto);
                }
            }

            finalResult.put(entry.getKey(), new ArrayList<>(tagToLatestDTO.values()));
        }

        return finalResult;
    }

    private ActivityNjnzDTO toDTO(ActivityDO activityDO, ActivityNjnzDO activityNjnzDO) {
        if (activityNjnzDO == null) {
            return null;
        }
        ActivityNjnzDTO dto = new ActivityNjnzDTO();
        dto.setId(activityDO.getId());
        dto.setActivityType(ActivityTypeEnum.NJ_NZ.getCode());
        dto.setDiscountStackable(Objects.equals(activityDO.getDiscountStackable(), 1) ? 1 : 0);
        dto.setDiscountType(activityNjnzDO.getDiscountType());
        dto.setActivityName(activityDO.getActivityName());
        dto.setActivityRemark(activityDO.getActivityRemark());
        dto.setDiscountRate(activityNjnzDO.getDiscountRate());
        dto.setDiscountItemNum(activityNjnzDO.getDiscountItemNum());
        if (Objects.equals(activityDO.getDiscountStackable(), 1)
                && !StringUtils.isBlank(activityDO.getStackableActivities())) {
            dto.setStackableActivities(
                Arrays.stream(activityDO.getStackableActivities().split(","))
                    .map(Integer::valueOf).toList()
            );
        } else {
            dto.setStackableActivities(List.of());
        }
        dto.setActivityRemark(activityDO.getActivityRemark());
        dto.setCreateTime(activityDO.getCreateTime());
        String tagFormat = "第%s件%s折";
        switch (NjnzDiscountTypeEnum.of(activityNjnzDO.getDiscountType())) {
            case SECOND_HALF_PRICE:
                dto.setTag(SECOND_HALF_PRICE.getDescription());
                break;
            case BUY_ONE_GET_ONE:
                dto.setTag(BUY_ONE_GET_ONE.getDescription());
                break;
            case CUSTOM:
                dto.setTag(formatDiscountTag(activityNjnzDO.getDiscountItemNum(), activityNjnzDO.getDiscountRate()));
                break;
            default:
                dto.setTag("");
                break;
        }
        return dto;
    }


    public String formatDiscountTag(int itemIndex, double discountRate) {
        String discountStr;
        // 判断是否为整数折扣
        if (discountRate % 1 == 0) {
            discountStr = String.valueOf((int) discountRate);
        } else {
            // 小数折扣转换为整数形式（如9.5→95）
            discountStr = String.valueOf((int) Math.round(discountRate * 10));
        }
        return String.format("第%s件%s折", itemIndex, discountStr);
    }
}
