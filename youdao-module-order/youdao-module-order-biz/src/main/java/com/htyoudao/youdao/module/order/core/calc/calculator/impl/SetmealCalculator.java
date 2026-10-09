package com.htyoudao.youdao.module.order.core.calc.calculator.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.module.commodity.api.DTO.StoreSingleInfoDTO;
import com.htyoudao.youdao.module.order.core.calc.v1.VO.SettlementReqVO;
import com.htyoudao.youdao.module.order.core.calc.v1.DTO.CalculateCacheDataDTO;
import com.htyoudao.youdao.module.order.core.calc.DTO.CostInfoDTO;
import com.htyoudao.youdao.module.order.core.calc.calculator.PriceCalculator;
import com.htyoudao.youdao.module.order.core.calc.context.CalculatorContext;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.*;

/**
 * <p>
 * 套餐策略
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-28
 */
@Deprecated
public class SetmealCalculator implements PriceCalculator {
    @Override
    public CostInfoDTO calculate(CalculatorContext context) {
//        SettlementReqVO.CommodityInfoVO commodity = context.commodity;
//        Map<Long, StoreSkuInfoDTO> skuMap = context.data.skus();
//        Map<Long, StoreSingleInfoDTO> singleMap = context.data.singles();
//
//        // 获取SKU信息
//        StoreSkuInfoDTO sku = Optional.ofNullable(skuMap.get(commodity.getSkuId()))
//                .orElseThrow(() -> exception(ORDER_COMMODITY_NOT_EXISTS));
//
//        //商品校验
//        this.validateSku(commodity, sku, context.isDc);
//
//        CalculateCacheDataDTO.CommodityInfoVO cacheVO = new CalculateCacheDataDTO.CommodityInfoVO();
//        // 拷贝基础属性
//        BeanUtils.copyProperties(commodity, cacheVO);
//        BeanUtils.copyProperties(sku, cacheVO);
//        cacheVO.setStackableActivities(sku.getStackableActivitieList());
//        cacheVO.setIsPurchase(commodity.getIsPurchase());
//        cacheVO.setImageUrl(
//                this.getFirstCommaElement(sku.getImageUrl())
//        );
//
//        //基础价格
//        BigDecimal basePrice = sku.getSkuPrice();
//
//        //套餐加价
//        BigDecimal upPrice = this.processSingleItems(commodity, singleMap, cacheVO, context.isDc);
//
//        //单价 包含加价
//        cacheVO.setSkuPrice(basePrice.add(upPrice));
//
//        //聚合价格
//        cacheVO.setGoodsShowPrice(sku.getSkuPrice().add(upPrice).multiply(BigDecimal.valueOf(commodity.getCopies())).setScale(2, RoundingMode.HALF_UP));
//
//        //计算优惠价格
//        CalculatorResDTO calculatorResDTO = this.getPromotionDiscountAmount(context.data.activitys().get(commodity.getCommodityId()), cacheVO);
//
//        //计算包装费
//        BigDecimal packageFee = this.calculatePackagingFee(calculatorResDTO.getShouldPayPackageFeeCount(), sku.getManyCopy(), sku.getPackageFee());
//
//        // 商品价格 = (基础价格 + 子项加价) * 份数
//        BigDecimal commodityAmount = basePrice
//                .add(upPrice)
//                .multiply(BigDecimal.valueOf(commodity.getCopies()));
//
//        return CostInfoDTO.builder()
//                .packingFee(packageFee)
//                .promotionDiscountAmount(calculatorResDTO.getPromotionDiscountAmount())
//                .activityDiscountAmount(BigDecimal.ZERO)
//                .deliveryFee(BigDecimal.ZERO)
//                .commodityAmount(commodityAmount)
//                .afterAmount(BigDecimal.ZERO)
//                .cacheVOList(calculatorResDTO.getCacheVOList())
//                .build();

        return null;
    }

    private BigDecimal processSingleItems(SettlementReqVO.CommodityInfoVO commodity, Map<Long, StoreSingleInfoDTO> singleMap,
                                          CalculateCacheDataDTO.CommodityInfoVO cacheVO, Boolean isDc) {
        List<CalculateCacheDataDTO.SingleInfoVO> singleVOs = new ArrayList<>();
        BigDecimal totalUpPrice = BigDecimal.ZERO;

        for (Map<Long, Integer> itemMap : commodity.getSingleList()) {
            Map.Entry<Long, Integer> entry = itemMap.entrySet().iterator().next();
            Long singleId = entry.getKey();
            Integer quantity = entry.getValue();

            StoreSingleInfoDTO single = Optional.ofNullable(singleMap.get(singleId))
                    .orElseThrow(() -> exception(ORDER_COMMODITY_VALID_SINGLE));

            //点餐机上下架校验
            if (isDc && (ObjectUtils.isEmpty(single.getStoreStatus()) || single.getStoreStatus() == 0)) {
                throw exception(ORDER_COMMODITY_VALID_SINGLE);
            }

            //小程序上下架校验
            if (!isDc && (ObjectUtils.isEmpty(single.getWxStatus()) || single.getWxStatus() == 0)) {
                throw exception(ORDER_COMMODITY_VALID_SINGLE);
            }

            CalculateCacheDataDTO.SingleInfoVO singleVO = new CalculateCacheDataDTO.SingleInfoVO();
            BeanUtils.copyProperties(single, singleVO);
            singleVO.setNumber(quantity);

            singleVOs.add(singleVO);

            // 子项加价 = 单价 * 数量
            BigDecimal itemPrice = single.getSinglePrice().multiply(BigDecimal.valueOf(quantity));
            totalUpPrice = totalUpPrice.add(itemPrice);
        }

        cacheVO.setSingleList(singleVOs);
        return totalUpPrice;
    }
}
