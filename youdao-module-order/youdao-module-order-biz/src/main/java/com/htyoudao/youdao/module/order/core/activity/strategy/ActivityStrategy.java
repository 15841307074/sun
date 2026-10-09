package com.htyoudao.youdao.module.order.core.activity.strategy;

import com.htyoudao.youdao.module.order.core.activity.DTO.CalculatorResDTO;
import com.htyoudao.youdao.module.order.core.calc.v1.DTO.CalculateCacheDataDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityBaseDTO;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;

/**
 * <p>
 * 营销活动计算策略基类
 * </p>
 *
 * @author zhangjihe
 * @since 2025-06-07
 */
public interface ActivityStrategy {

    /**
     * 获取活动类型
     *
     * @return
     */
    ActivityTypeEnum getActivityType();

    /**
     * 计算
     *
     * @param commodityInfoVO
     * @param activity
     * @return
     */
    CalculatorResDTO calculate(CalculateCacheDataDTO.CommodityInfoVO commodityInfoVO, ActivityBaseDTO activity);
}
