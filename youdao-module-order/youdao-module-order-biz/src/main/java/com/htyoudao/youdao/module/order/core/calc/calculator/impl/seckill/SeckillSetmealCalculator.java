package com.htyoudao.youdao.module.order.core.calc.calculator.impl.seckill;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSingleInfoDTO;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSkuInfoDTO;
import com.htyoudao.youdao.module.order.core.calc.DTO.CostInfoDTO;
import com.htyoudao.youdao.module.order.core.calc.calculator.PriceCalculator;
import com.htyoudao.youdao.module.order.core.calc.context.CalculatorContext;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.calc.v2.VO.SettlementReqV2VO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.ORDER_COMMODITY_IS_NOT_UP;

/**
 * <p>
 * 秒杀套餐策略
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-28
 */
public class SeckillSetmealCalculator implements PriceCalculator {
    @Override
    public CostInfoDTO calculate(CalculatorContext context) {
        SettlementReqV2VO.CommodityInfoVO commodity = context.commodity;
        Map<Long, StoreSkuInfoDTO> skuMap = context.data.skuMap();
        Map<Long, StoreSingleInfoDTO> singleMap = context.data.singles();

        //获取SKU信息
        StoreSkuInfoDTO sku = Optional.ofNullable(skuMap.get(commodity.getSkuId()))
                .orElseThrow(() -> exception(ORDER_COMMODITY_NOT_EXISTS));

        //上下架校验
        if (sku.getCommodityStoreSpuAppletStatus() == 0) {
            throw new ServiceException(new ErrorCode(ORDER_COMMODITY_IS_NOT_UP.getCode(), String.format(ORDER_COMMODITY_IS_NOT_UP.getMsg(), sku.getSpuName())));
        }

        CalculateCacheDataV2DTO.CommodityInfoVO cacheVO = new CalculateCacheDataV2DTO.CommodityInfoVO();
        //拷贝基础属性
        BeanUtils.copyProperties(commodity, cacheVO);
        BeanUtils.copyProperties(sku, cacheVO);

        cacheVO.setStackableActivities(sku.getStackableActivitieList());
        cacheVO.setIsPurchase(commodity.getIsPurchase());
        cacheVO.setActivityType(ActivityTypeEnum.SEC_KILL.getCode());
        cacheVO.setIsGetActivity(OrderConstants.YES);
        cacheVO.setImageUrl(this.getFirstCommaElement(sku.getImageUrl()));
        cacheVO.setCopies(commodity.getCopies());

        this.putActivityInfoJson(context.seckillInfo, cacheVO);

        //基础价格
        BigDecimal basePrice = sku.getSkuPrice();

        //套餐加价
        BigDecimal upPrice = this.processSingleItems(commodity, singleMap, cacheVO, context.isDc);

        //单价 包含加价
        cacheVO.setSkuPrice(basePrice.add(upPrice));

        //展示价格
        cacheVO.setGoodsShowPrice(sku.getSkuPrice().add(upPrice).multiply(BigDecimal.valueOf(commodity.getCopies())).setScale(2, RoundingMode.HALF_UP));

        //秒杀价格
        cacheVO.setSeckillPrice(sku.getSeckillPrice().add(upPrice).multiply(BigDecimal.valueOf(commodity.getCopies())).setScale(2, RoundingMode.HALF_UP));

        //计算优惠价格
        BigDecimal promotionDiscountAmount = sku.getSkuPrice().subtract(sku.getSeckillPrice()).multiply(BigDecimal.valueOf(commodity.getCopies())).setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO);

        //划线价格
        if(!ObjectUtils.isEmpty(sku.getStrikeThroughPrice())){
            cacheVO.setStrikeThroughPrice(sku.getStrikeThroughPrice().multiply(BigDecimal.valueOf(cacheVO.getCopies())));
        }

        cacheVO.setPromotionDiscountAmount(promotionDiscountAmount);

        //计算包装费
        BigDecimal packageFee = this.calculatePackagingFee(commodity.getCopies(), sku.getManyCopy(), sku.getPackageFee());

        // 商品价格 = (基础价格 + 子项加价) * 份数
        BigDecimal commodityAmount = basePrice
                .add(upPrice)
                .multiply(BigDecimal.valueOf(commodity.getCopies()));

        return CostInfoDTO.builder()
                .packingFee(packageFee)
                .promotionDiscountAmount(promotionDiscountAmount)
                .activityDiscountAmount(BigDecimal.ZERO)
                .deliveryFee(BigDecimal.ZERO)
                .commodityAmount(commodityAmount)
                .cacheVOList(Collections.singletonList(cacheVO))
                .build();
    }

    private void putActivityInfoJson(CalculateCacheDataV2DTO.SeckillInfo seckillInfo, CalculateCacheDataV2DTO.CommodityInfoVO cacheVO) {
        Map<String, Object> activityMap = new HashMap<>();
        activityMap.put("sessionId", seckillInfo.getSessionId());
        activityMap.put("activityId", seckillInfo.getActivityId());
        activityMap.put("activityType", ActivityTypeEnum.SEC_KILL.getCode());
        activityMap.put("activityName", seckillInfo.getActivityName());
        activityMap.put("activityTag", "秒杀活动");
        cacheVO.setActivityDiscountDetail(JSON.toJSONString(activityMap));
    }

    private BigDecimal processSingleItems(SettlementReqV2VO.CommodityInfoVO commodity, Map<Long, StoreSingleInfoDTO> singleMap,
                                          CalculateCacheDataV2DTO.CommodityInfoVO cacheVO, Boolean isDc) {
        List<CalculateCacheDataV2DTO.SingleInfoVO> singleVOs = new ArrayList<>();
        BigDecimal totalUpPrice = BigDecimal.ZERO;

        for (SettlementReqV2VO.SingleFlavorVO singleFlavor : commodity.getSingleFlavorList()) {
            StoreSingleInfoDTO single = Optional.ofNullable(singleMap.get(singleFlavor.getSingleId()))
                    .orElseThrow(() -> exception(ORDER_COMMODITY_VALID_SINGLE));

            //点餐机上下架校验
            if (isDc && (ObjectUtils.isEmpty(single.getStoreStatus()) || single.getStoreStatus() == 0)) {
                throw exception(ORDER_COMMODITY_VALID_SINGLE);
            }

            //小程序上下架校验
            if (!isDc && (ObjectUtils.isEmpty(single.getWxStatus()) || single.getWxStatus() == 0)) {
                throw exception(ORDER_COMMODITY_VALID_SINGLE);
            }

            CalculateCacheDataV2DTO.SingleInfoVO singleVO = new CalculateCacheDataV2DTO.SingleInfoVO();
            BeanUtils.copyProperties(single, singleVO);
            singleVO.setNumber(singleFlavor.getNum());

            List<StoreSingleInfoDTO.FlavorInfoVO> flavorInfoVOS = BeanCopyUtils.copyBeanList(singleFlavor.getFlavors(), StoreSingleInfoDTO.FlavorInfoVO.class);
            singleVO.setFlavors(flavorInfoVOS);
            singleVOs.add(singleVO);

            // 子项加价 = 单价 * 数量
            BigDecimal itemPrice = single.getSinglePrice().multiply(BigDecimal.valueOf(singleFlavor.getNum()));
            totalUpPrice = totalUpPrice.add(itemPrice);
        }

        cacheVO.setSingleList(singleVOs);
        return totalUpPrice.multiply(BigDecimal.valueOf(commodity.getCopies()));
    }
}
