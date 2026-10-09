package com.htyoudao.youdao.module.promotion.service.activityMz;

import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMzDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.MzGiftDTO;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.api.enums.activity.MzDiscountTypeEnum;
import com.htyoudao.youdao.module.promotion.api.enums.activity.MzGiftInventoryTypeEnum;
import com.htyoudao.youdao.module.promotion.api.enums.activity.MzPlaceOrderProductEnum;
import com.htyoudao.youdao.module.promotion.api.enums.activity.MzPlaceOrderTypeEnum;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMz.ActivityMzDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzCommodity.ActivityMzCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift.ActivityMzGiftDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMz.ActivityMzMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMzCommodity.ActivityMzCommodityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMzGift.ActivityMzGiftMapper;
import com.htyoudao.youdao.module.promotion.service.activity.ApplicableActivityQueryService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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
public class ActivityMZAppServiceImpl implements ActivityMZAppService {

    @Resource
    private ActivityMzMapper mzMapper;

    @Resource
    private ActivityMzCommodityMapper mzCommodityMapper;

    @Resource
    private ActivityMzGiftMapper mzGiftMapper;

    @Resource
    private ApplicableActivityQueryService applicableActivityQueryService;

    @Value("${activity.mz.switch:1}")
    private String mzSwitch;


    @Override
    public Map<Long, List<ActivityMzDTO>> selectMZActivity(Long storeId, Collection<Long> commodityIds) {
        // 活动开关关闭
        if (Objects.equals(mzSwitch, "0")) {
            return Map.of();
        }

        List<ActivityDO> allActivities = applicableActivityQueryService.selectApplicable(
                storeId, List.of(ActivityTypeEnum.MZ.getCode()));
        return selectMZActivity(storeId, commodityIds, allActivities);
    }

    @Override
    public Map<Long, List<ActivityMzDTO>> selectMZActivity(Long storeId, Collection<Long> commodityIds,
                                                           List<ActivityDO> allActivities) {
        if (Objects.equals(mzSwitch, "0")) {
            return Map.of();
        }
        Set<Long> commodityIdSet = commodityIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        if (commodityIdSet.isEmpty() || CollectionUtils.isEmpty(allActivities)) {
            return Map.of();
        }
        Map<Long, ActivityDO> activityDOMap = allActivities.stream()
                .collect(Collectors.toMap(ActivityDO::getId, Function.identity()));

        List<Long> list = allActivities.stream().map(ActivityDO::getId).toList();
        List<ActivityMzDO> activityMzDOS = mzMapper.selectList(ActivityMzDO::getActivityId, list);
        if (CollectionUtils.isEmpty(activityMzDOS)) {
            return Map.of();
        }

        // 查询赠品配置，按门店过滤：共用库存取 storeId 为 null 的行，独立库存取当前请求门店的行
        List<Long> mzActivityIds = activityMzDOS.stream().map(ActivityMzDO::getActivityId).distinct().toList();
        List<Long> independentActivityIds = activityMzDOS.stream()
                .filter(mz -> Objects.equals(mz.getGiftInventoryType(), MzGiftInventoryTypeEnum.INDEPENDENT.getCode()))
                .map(ActivityMzDO::getActivityId).toList();
        Set<Long> independentActivityIdSet = new HashSet<>(independentActivityIds);
        List<Long> sharedActivityIds = mzActivityIds.stream()
                .filter(activityId -> !independentActivityIdSet.contains(activityId)).toList();
        Map<Long, List<ActivityMzGiftDO>> giftsByActivity = mzGiftMapper
                .selectApplicableGifts(sharedActivityIds, independentActivityIds, storeId).stream()
                .collect(Collectors.groupingBy(ActivityMzGiftDO::getActivityId));

        // 拆分活动商品范围，先按活动品类型限制 placeOrderType 分支：
        // 1. 按品类限制（BY_CATEGORY）：活动对门店全部商品生效，不看 placeOrderProduct、不查关联表；
        //    参与品类（categoryType：全部/仅单品/仅套餐，见 MzCategoryTypeEnum）由 commodity 侧按商品类型过滤
        // 2. 按商品限制（BY_PRODUCT）：再看参与活动商品 placeOrderProduct——
        //    全部商品（ALL）适用所有传入商品；指定商品（SPECIFIED）查 activity_mz_commodity 关联表匹配
        List<ActivityMzDO> specificApplicableActivities = activityMzDOS.stream()
            .filter(a -> Objects.equals(a.getPlaceOrderType(), MzPlaceOrderTypeEnum.BY_PRODUCT.getCode())
                && Objects.equals(a.getPlaceOrderProduct(), MzPlaceOrderProductEnum.SPECIFIED.getCode()))
            .toList();

        Map<Long, Set<Long>> resultMap = new HashMap<>();

        // 全部商品适用的活动：指定商品匹配之外的所有活动，含——
        // 按品类限制的活动；按商品限制且全部商品的活动；
        // placeOrderType 为 null 的存量数据按"按品类限制"兼容处理（全部适用，与旧逻辑一致）
        Set<Long> allActivityIds = activityMzDOS.stream()
            .filter(a -> !Objects.equals(a.getPlaceOrderType(), MzPlaceOrderTypeEnum.BY_PRODUCT.getCode())
                || !Objects.equals(a.getPlaceOrderProduct(), MzPlaceOrderProductEnum.SPECIFIED.getCode()))
            .map(ActivityMzDO::getActivityId)
            .collect(Collectors.toSet());
        for (Long commodityId : commodityIdSet) {
            resultMap.put(commodityId, new HashSet<>(allActivityIds));
        }

        // 处理指定商品适用的活动
        if (!specificApplicableActivities.isEmpty()) {
            List<Long> activityIds = specificApplicableActivities.stream().map(ActivityMzDO::getActivityId)
                .distinct()
                .toList();

            List<ActivityMzCommodityDO> commodityLinks = mzCommodityMapper
                    .selectByActivityIdsAndCommodityIds(activityIds, commodityIdSet);
            for (ActivityMzCommodityDO commodityLink : commodityLinks) {
                Set<Long> activityIdSet = resultMap.getOrDefault(commodityLink.getCommodityId(), new HashSet<>());
                activityIdSet.add(commodityLink.getActivityId());
                resultMap.put(commodityLink.getCommodityId(), activityIdSet);
            }
        }

        Map<Long, ActivityMzDTO> dtoByActivityId = new HashMap<>();
        for (ActivityMzDO mzDO : activityMzDOS) {
            List<ActivityMzGiftDO> gifts = giftsByActivity.get(mzDO.getActivityId());
            if (!CollectionUtils.isEmpty(gifts)) {
                dtoByActivityId.put(mzDO.getActivityId(),
                        toDTO(activityDOMap.get(mzDO.getActivityId()), mzDO, gifts));
            }
        }

        // 转换为 DTO 结果
        Map<Long, List<ActivityMzDTO>> finalResult = new HashMap<>();

        for (Map.Entry<Long, Set<Long>> entry : resultMap.entrySet()) {
            // 用于按tag去重，保留最新创建的对象
            Map<String, ActivityMzDTO> tagToLatestDTO = new HashMap<>();

            for (Long activityId : entry.getValue()) {
                ActivityMzDTO dto = dtoByActivityId.get(activityId);
                if (dto == null) {
                    continue;
                }

                // 按tag去重，保留最新创建的
                ActivityMzDTO existingDTO = tagToLatestDTO.get(dto.getTag());
                if (existingDTO == null || existingDTO.getCreateTime().isBefore(dto.getCreateTime())) {
                    tagToLatestDTO.put(dto.getTag(), dto);
                }
            }

            finalResult.put(entry.getKey(), new ArrayList<>(tagToLatestDTO.values()));
        }

        return finalResult;
    }

    private ActivityMzDTO toDTO(ActivityDO activityDO, ActivityMzDO activityMzDO, List<ActivityMzGiftDO> gifts) {
        if (activityMzDO == null) {
            return null;
        }
        ActivityMzDTO dto = new ActivityMzDTO();
        dto.setId(activityDO.getId());
        dto.setActivityId(activityMzDO.getActivityId());
        dto.setActivityType(ActivityTypeEnum.MZ.getCode());
        dto.setDiscountStackable(Objects.equals(activityDO.getDiscountStackable(), 1) ? 1 : 0);
        dto.setDiscountType(activityMzDO.getDiscountType());
        dto.setDiscountRules(activityMzDO.getDiscountRules());
        dto.setPlaceOrderType(activityMzDO.getPlaceOrderType());
        dto.setCategoryType(activityMzDO.getCategoryType());
        dto.setActivityName(activityDO.getActivityName());
        dto.setActivityRemark(activityDO.getActivityRemark());
        dto.setUserLimitType(activityMzDO.getUserLimitType());
        dto.setUserLimitValue(activityMzDO.getUserLimitValue());
        if (Objects.equals(activityDO.getDiscountStackable(), 1)
                && !StringUtils.isBlank(activityDO.getStackableActivities())) {
            dto.setStackableActivities(
                Arrays.stream(activityDO.getStackableActivities().split(","))
                    .map(Integer::valueOf).toList()
            );
        } else {
            dto.setStackableActivities(List.of());
        }
        dto.setCreateTime(activityDO.getCreateTime());

        // 赠品列表按门槛升序排列
        List<ActivityMzGiftDO> sortedGifts = gifts.stream()
            .sorted(Comparator.comparing(ActivityMzGiftDO::getThreshold,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
        dto.setGifts(sortedGifts.stream().map(this::toGiftDTO).toList());

        // tag：取最低门槛档位 + 首个赠品名称，如"满20元赠可乐"、"满2件赠可乐"
        ActivityMzGiftDO lowestGift = sortedGifts.get(0);
        dto.setTag(buildTag(activityMzDO.getDiscountType(), lowestGift));
        return dto;
    }

    private MzGiftDTO toGiftDTO(ActivityMzGiftDO giftDO) {
        MzGiftDTO giftDTO = new MzGiftDTO();
        giftDTO.setThreshold(giftDO.getThreshold());
        giftDTO.setGiftCommodityId(giftDO.getGiftCommodityId());
        giftDTO.setGiftCommodityName(giftDO.getGiftCommodityName());
        giftDTO.setGiftPrice(giftDO.getGiftPrice());
        giftDTO.setGiftImage(giftDO.getGiftImage());
        // 赠品行已按门店过滤，库存与当前门店对应
        giftDTO.setActivityInventory(giftDO.getActivityInventory());
        giftDTO.setRemainingInventory(giftDO.getRemainingInventory());
        return giftDTO;
    }

    private String buildTag(Integer discountType, ActivityMzGiftDO lowestGift) {
        String thresholdStr = lowestGift.getThreshold() == null
            ? "" : lowestGift.getThreshold().stripTrailingZeros().toPlainString();
        String giftName = StringUtils.isBlank(lowestGift.getGiftCommodityName())
            ? "" : lowestGift.getGiftCommodityName();
        if (discountType != null && Objects.equals(discountType, MzDiscountTypeEnum.BUY_N_ITEMS_GIFT.getCode())) {
            return String.format("满%s件赠%s", thresholdStr, giftName);
        }
        return String.format("满%s元赠%s", thresholdStr, giftName);
    }
}
