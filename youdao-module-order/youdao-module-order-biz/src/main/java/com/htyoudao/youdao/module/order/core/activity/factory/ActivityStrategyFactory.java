package com.htyoudao.youdao.module.order.core.activity.factory;

import com.htyoudao.youdao.module.order.core.activity.strategy.ActivityStrategy;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 活动类型策略工厂
 * </p>
 *
 * @author zhangjihe
 * @since 2025-06-07
 */
@Component
public class ActivityStrategyFactory {

    private final Map<ActivityTypeEnum, ActivityStrategy> strategyMap;

    @Autowired
    public ActivityStrategyFactory(List<ActivityStrategy> strategies) {
        //搜集所有策略
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(ActivityStrategy::getActivityType, Function.identity()));
    }

    public ActivityStrategy getStrategy(Integer activityType) {
        ActivityStrategy strategy = strategyMap.get(ActivityTypeEnum.getEnumByCode(activityType));
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的活动类型：" + activityType);
        }
        return strategy;
    }
}