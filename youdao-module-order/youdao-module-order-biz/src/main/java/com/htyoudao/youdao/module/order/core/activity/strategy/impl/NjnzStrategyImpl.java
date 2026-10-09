package com.htyoudao.youdao.module.order.core.activity.strategy.impl;

import com.alibaba.fastjson2.JSON;
import com.htyoudao.youdao.module.order.core.activity.DTO.CalculatorResDTO;
import com.htyoudao.youdao.module.order.core.activity.strategy.ActivityStrategy;
import com.htyoudao.youdao.module.order.core.calc.v1.DTO.CalculateCacheDataDTO;
import com.htyoudao.youdao.module.order.core.submit.DTO.ActivityNjnzInfoDTO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityBaseDTO;
import com.htyoudao.youdao.module.promotion.api.activity.DTO.ActivityNjnzDTO;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.ORDER_ACTIVITY_TYPE_ERROR;

/**
 * <p>
 * 现金单 包括点餐机快速下单
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-25
 */
@Slf4j
@Component
public class NjnzStrategyImpl implements ActivityStrategy {

    @Override
    public ActivityTypeEnum getActivityType() {
        return ActivityTypeEnum.NJ_NZ;
    }

    @Override
    public CalculatorResDTO calculate(CalculateCacheDataDTO.CommodityInfoVO commodityInfoVO, ActivityBaseDTO obj) {
        CalculatorResDTO returnObj = CalculatorResDTO.builder().build();

        // 类型安全检查
        if (!(obj instanceof ActivityNjnzDTO activity)) {
            throw exception(ORDER_ACTIVITY_TYPE_ERROR);
        }
        returnObj.setActivityName(activity.getActivityName());
        returnObj.setCreateTime(activity.getCreateTime());

        // 商品数量
        Integer copies = commodityInfoVO.getCopies();
        if (copies == null || copies <= 0) {
            return returnObj;
        }

        int discountItemNum = activity.getDiscountItemNum();
        if (discountItemNum <= 0) {
            return returnObj;
        }

        int onActivityNum = copies / discountItemNum;
        if (onActivityNum == 0) {
            return returnObj;
        }

        int remainingNum = copies % discountItemNum;

        // 获取价格
        BigDecimal skuPrice = commodityInfoVO.getSkuPrice() != null ? commodityInfoVO.getSkuPrice() : BigDecimal.ZERO;
        double discountRate = activity.getDiscountRate();
        if (discountRate < 0) {
            discountRate = 0;
        }

        // 优惠金额
        BigDecimal promotionDiscountAmountSingle = skuPrice.multiply(BigDecimal.valueOf((10 - discountRate) / 10))
                .setScale(2, RoundingMode.HALF_UP);

        List<CalculateCacheDataDTO.CommodityInfoVO> cacheVOList = new ArrayList<>();

        // 构建 part1（无优惠）
        CalculateCacheDataDTO.CommodityInfoVO part1 = this.buildPartWithoutDiscount(commodityInfoVO, activity, onActivityNum, remainingNum);
        cacheVOList.add(part1);

        // 构建 part2（有优惠）
        CalculateCacheDataDTO.CommodityInfoVO part2 = this.buildPartWithDiscount(commodityInfoVO, activity, onActivityNum, skuPrice, promotionDiscountAmountSingle);
        cacheVOList.add(part2);

        // 设置返回值
        returnObj.setShouldPayPackageFeeCount(part1.getCopies());
        returnObj.setPromotionDiscountAmount(part2.getPromotionDiscountAmount());
        returnObj.setCacheVOList(cacheVOList);

        return returnObj;
    }

    /**
     * 构建没有优惠
     *
     * @param commodityInfoVO
     * @param activity
     * @param onActivityNum
     * @return
     */
    private CalculateCacheDataDTO.CommodityInfoVO buildPartWithoutDiscount(
            CalculateCacheDataDTO.CommodityInfoVO commodityInfoVO,
            ActivityNjnzDTO activity,
            int onActivityNum,
            int remainingNum) {

        CalculateCacheDataDTO.CommodityInfoVO part = commodityInfoVO.clone();
        BeanUtils.copyProperties(activity, part);
        part.setActivityTag(activity.getTag());
        part.setActivityId(activity.getId());

        int totalCopies = onActivityNum * (activity.getDiscountItemNum() - 1) + remainingNum;
        part.setCopies(totalCopies);

        BigDecimal strikeThroughPrice = this.getStrikeThroughPrice(commodityInfoVO.getStrikeThroughPrice(), totalCopies);
        part.setStrikeThroughPrice(strikeThroughPrice);

        part.setPromotionDiscountAmount(BigDecimal.ZERO);
        part.setGoodsShowPrice(this.calculateGoodsShowPrice(commodityInfoVO.getSkuPrice(), totalCopies));

        this.putActivityInfoJson(activity, part);
        return part;
    }

    private void putActivityInfoJson(ActivityNjnzDTO activity, CalculateCacheDataDTO.CommodityInfoVO part) {
        ActivityNjnzInfoDTO activityNjnzInfoDTO = new ActivityNjnzInfoDTO();
        BeanUtils.copyProperties(activity, activityNjnzInfoDTO);
        activityNjnzInfoDTO.setActivityTag(activity.getTag());

        part.setActivityDiscountDetail(JSON.toJSONString(activityNjnzInfoDTO));
    }

    /**
     * 构建优惠
     *
     * @return
     */
    private CalculateCacheDataDTO.CommodityInfoVO buildPartWithDiscount(
            CalculateCacheDataDTO.CommodityInfoVO commodityInfoVO,
            ActivityNjnzDTO activity,
            int onActivityNum,
            BigDecimal skuPrice,
            BigDecimal promotionDiscountAmountSingle) {

        CalculateCacheDataDTO.CommodityInfoVO part = commodityInfoVO.clone();
        BeanUtils.copyProperties(activity, part);
        part.setActivityTag(activity.getTag());
        part.setIsGetActivity(OrderConstants.YES);
        part.setActivityId(activity.getId());
        part.setCopies(onActivityNum);

        BigDecimal strikeThroughPrice = this.getStrikeThroughPrice(commodityInfoVO.getStrikeThroughPrice(), onActivityNum);
        part.setStrikeThroughPrice(strikeThroughPrice);

        BigDecimal discountAmount = promotionDiscountAmountSingle.multiply(BigDecimal.valueOf(onActivityNum))
                .setScale(2, RoundingMode.HALF_UP);
        part.setPromotionDiscountAmount(discountAmount);

        BigDecimal goodsShowPrice = this.calculateGoodsShowPrice(skuPrice, onActivityNum)
                .subtract(discountAmount)
                .setScale(2, RoundingMode.HALF_UP);
        part.setGoodsShowPrice(goodsShowPrice);

        this.putActivityInfoJson(activity, part);

        return part;
    }

    private BigDecimal getStrikeThroughPrice(BigDecimal basePrice, int copies) {
        return basePrice != null ? basePrice.multiply(BigDecimal.valueOf(copies)) : BigDecimal.ZERO;
    }

    private BigDecimal calculateGoodsShowPrice(BigDecimal price, int copies) {
        return price.multiply(BigDecimal.valueOf(copies)).setScale(2, RoundingMode.HALF_UP);
    }
}
