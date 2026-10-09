package com.htyoudao.youdao.module.promotion.service.activityMj;

import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityMJDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.DiscountSettingsDTO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMj.ActivityMjDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityMjCommodity.ActivityMjCommodityDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMj.ActivityMjMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.activityMjCommodity.ActivityMjCommodityMapper;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import com.htyoudao.youdao.module.promotion.api.enums.activity.MJProductScopeEnum;
import com.htyoudao.youdao.module.promotion.service.activity.ApplicableActivityQueryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.module.promotion.api.enums.activity.MJDiscountTypeEnum.BUY_N_ITEMS;

@Service
@Slf4j
@RefreshScope
public class ActivityMJAppServiceImpl implements ActivityMJAppService {

    @Value("${activity.mj.switch:1}")
    private String mjSwitch;

    @Resource
    private ActivityMjCommodityMapper mjCommodityMapper;

    @Resource
    private ActivityMjMapper mjMapper;

    @Resource
    private ApplicableActivityQueryService applicableActivityQueryService;

    @Override
    public Map<Long, List<ActivityMJDTO>> selectMJActivity(Long storeId, Collection<Long> commodityIds) {
        // 活动开关关闭
        if (Objects.equals(mjSwitch, "0")) {
            return Map.of();
        }

        List<ActivityDO> allActivities = applicableActivityQueryService.selectApplicable(
                storeId, List.of(ActivityTypeEnum.MJ.getCode()));
        return selectMJActivity(commodityIds, allActivities);
    }

    @Override
    public Map<Long, List<ActivityMJDTO>> selectMJActivity(Collection<Long> commodityIds,
                                                            List<ActivityDO> allActivities) {
        if (Objects.equals(mjSwitch, "0")) {
            return Map.of();
        }
        Set<Long> commodityIdSet = commodityIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());

        if (commodityIdSet.isEmpty() || CollectionUtils.isEmpty(allActivities)) {
            return Map.of();
        }

        Map<Long, ActivityDO> activityDOMap = allActivities.stream()
                .collect(Collectors.toMap(ActivityDO::getId, Function.identity()));

        List<Long> list = allActivities.stream().map(ActivityDO::getId).toList();
        List<ActivityMjDO> activityMjDOList = mjMapper.selectList(ActivityMjDO::getActivityId, list);

        // 拆分活动类型
        List<ActivityMjDO> allApplicableActivities = activityMjDOList.stream()
                .filter(a -> Objects.equals(a.getActivityProduct(), MJProductScopeEnum.ALL.getCode())).toList();

        List<ActivityMjDO> specificApplicableActivities = activityMjDOList.stream()
                .filter(a -> Objects.equals(a.getActivityProduct(), MJProductScopeEnum.SPECIFIC.getCode())).toList();

        Map<Long, Set<Long>> resultMap = new HashMap<>();

        // 全部商品适用的活动
        Set<Long> allActivityIds = allApplicableActivities.stream()
                .map(ActivityMjDO::getActivityId)
                .collect(Collectors.toSet());
        for (Long commodityId : commodityIdSet) {
            resultMap.put(commodityId, new HashSet<>(allActivityIds));
        }

        // 处理指定商品适用的活动
        if (!specificApplicableActivities.isEmpty()) {
            List<Long> activityIds = specificApplicableActivities.stream().map(ActivityMjDO::getActivityId)
                    .distinct()
                    .toList();

            List<ActivityMjCommodityDO> commodityLinks = mjCommodityMapper
                    .selectByActivityIdsAndCommodityIds(activityIds, commodityIdSet);
            for (ActivityMjCommodityDO commodityLink : commodityLinks) {
                Set<Long> activityIdSet = resultMap.getOrDefault(commodityLink.getCommodityId(), new HashSet<>());
                activityIdSet.add(commodityLink.getActivityId());
                resultMap.put(commodityLink.getCommodityId(), activityIdSet);
            }
        }

        Map<Long, ActivityMjDO> collect = activityMjDOList.stream()
                .collect(Collectors.toMap(ActivityMjDO::getActivityId, Function.identity()));
        Map<Long, ActivityMJDTO> dtoByActivityId = collect.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> toDTO(activityDOMap.get(entry.getKey()), entry.getValue())));

        // 转换为 DTO 结果
        Map<Long, List<ActivityMJDTO>> finalResult = new HashMap<>();

        for (Map.Entry<Long, Set<Long>> entry : resultMap.entrySet()) {
            // 用于按tag去重，保留最新创建的对象
            List<ActivityMJDTO> tagToLatestDTO = new ArrayList<>();

            for (Long activityId : entry.getValue()) {
                ActivityMJDTO dto = dtoByActivityId.get(activityId);
                if (dto == null) {
                    continue;
                }
                tagToLatestDTO.add(dto);
            }

            finalResult.put(entry.getKey(), tagToLatestDTO);
        }

        return finalResult;
    }

    private ActivityMJDTO toDTO(ActivityDO activityDO, ActivityMjDO activityMjDO) {
        if (activityMjDO == null) {
            return null;
        }
        ActivityMJDTO dto = new ActivityMJDTO();
        dto.setId(activityDO.getId());
        dto.setActivityType(ActivityTypeEnum.MJ.getCode());
        dto.setDiscountStackable(Objects.equals(activityDO.getDiscountStackable(), 1) ? 1 : 0);
        dto.setDiscountType(activityMjDO.getDiscountType());
        dto.setActivityName(activityDO.getActivityName());
        dto.setDiscountOffer(activityMjDO.getDiscountOffer());
        dto.setDiscountSettings(activityMjDO.getDiscountSettings());
        dto.setDiscountRules(activityMjDO.getDiscountRules());
        dto.setActivityRemark(activityDO.getActivityRemark());
        //20-7,30-8
        String discountSettings = activityMjDO.getDiscountSettings();
        if (StringUtils.isNotBlank(discountSettings)) {
            List<DiscountSettingsDTO> discountSettingsList = Arrays.stream(discountSettings.split(","))
                    .filter(StringUtils::isNotBlank)
                    .map(line -> {
                        String[] discountArr = line.split("-");
                        DiscountSettingsDTO settingsDTO = new DiscountSettingsDTO();
                        String mPriceOrCount = discountArr.length > 0 ? discountArr[0].trim() : "";
                        String jPriceOrSale = discountArr.length > 1 ? discountArr[1].trim() : "";
                        settingsDTO.setMPriceOrCount(mPriceOrCount);
                        settingsDTO.setJPriceOrSale(jPriceOrSale);
                        return settingsDTO;
                    })
                    .toList();

            dto.setDiscountSettingsList(discountSettingsList);
            // 商品列表和订单详情展示最低门槛档位，例如“满10元减2元”“满3件打8折”。
            dto.setTag(buildTag(activityMjDO.getDiscountType(), activityMjDO.getDiscountOffer(), discountSettingsList));
        }



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
        return dto;
    }

    /**
     * 使用最低有效门槛生成满减满折标签。
     *
     * @param discountType  1-满N元，2-满N件
     * @param discountOffer 1-减金额，2-打折
     * @param settings      门槛及优惠值列表
     * @return 展示标签；没有合法档位时返回空字符串
     */
    private String buildTag(Integer discountType, Integer discountOffer, List<DiscountSettingsDTO> settings) {
        if (CollectionUtils.isEmpty(settings)) {
            return "";
        }

        Optional<DiscountTagSetting> lowestSetting = settings.stream()
                .map(setting -> {
                    try {
                        BigDecimal threshold = new BigDecimal(setting.getMPriceOrCount());
                        BigDecimal discount = new BigDecimal(setting.getJPriceOrSale());
                        return new DiscountTagSetting(threshold, discount);
                    } catch (Exception ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .min(Comparator.comparing(DiscountTagSetting::threshold));
        if (lowestSetting.isEmpty()) {
            return "";
        }

        DiscountTagSetting setting = lowestSetting.get();
        String thresholdUnit = Objects.equals(discountType, BUY_N_ITEMS.getCode()) ? "件" : "元";
        String discountText;
        if (Objects.equals(discountOffer, 1)) {
            discountText = "减" + formatNumber(setting.discount()) + "元";
        } else if (Objects.equals(discountOffer, 2)) {
            discountText = "打" + formatNumber(setting.discount()) + "折";
        } else {
            return "";
        }
        return "满" + formatNumber(setting.threshold()) + thresholdUnit + discountText;
    }

    /** 去除展示数字无意义的末尾零，例如 10.00 -> 10。 */
    private String formatNumber(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private record DiscountTagSetting(BigDecimal threshold, BigDecimal discount) {
    }
}
