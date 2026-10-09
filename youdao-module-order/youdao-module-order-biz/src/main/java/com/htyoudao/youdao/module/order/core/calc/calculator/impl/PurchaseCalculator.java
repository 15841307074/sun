package com.htyoudao.youdao.module.order.core.calc.calculator.impl;

import com.htyoudao.youdao.module.commodity.api.DTO.AfterInfoDTO;
import com.htyoudao.youdao.module.order.core.calc.DTO.CostInfoDTO;
import com.htyoudao.youdao.module.order.core.calc.calculator.PriceCalculator;
import com.htyoudao.youdao.module.order.core.calc.context.CalculatorContext;
import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.calc.v2.VO.SettlementReqV2VO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Collections;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.order.api.enums.ErrorCodeConstants.ORDER_COMMODITY_AFTER_REMOVE;

/**
 * <p>
 * 加购策略
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-28
 */
public class PurchaseCalculator implements PriceCalculator {
    public CostInfoDTO calculate(CalculatorContext context) {
        AfterInfoDTO after = context.data.afters().get(context.commodity.getAfterId());
        if (ObjectUtils.isEmpty(after)) {
            throw exception(ORDER_COMMODITY_AFTER_REMOVE);
        }
        SettlementReqV2VO.CommodityInfoVO commodity = context.commodity;

        CalculateCacheDataV2DTO.CommodityInfoVO cacheVO = new CalculateCacheDataV2DTO.CommodityInfoVO();
        BeanUtils.copyProperties(after, cacheVO);
        cacheVO.setIsPurchase(OrderConstants.YES);
        cacheVO.setSpuName(after.getSkuName());
        cacheVO.setStrikeThroughPrice(after.getStrikePrice());

        BigDecimal payAmount = after.getSkuPrice()
                .subtract(after.getAfterPrice())
                .multiply(BigDecimal.valueOf(context.commodity.getCopies()));
        cacheVO.setSkuPrice(payAmount);
        cacheVO.setGoodsShowPrice(payAmount);
        cacheVO.setCopies(1);
        cacheVO.setImageUrl(
                this.getFirstCommaElement(after.getImageUrl())
        );

        //计算包装费
        BigDecimal packageFee = this.calculatePackagingFee(commodity.getCopies(), after.getManyCopy(), after.getPackageFee());

        return CostInfoDTO.builder()
                .commodityAmount(payAmount)
                .packingFee(packageFee)
                .deliveryFee(BigDecimal.ZERO)
                .activityDiscountAmount(BigDecimal.ZERO)
                .promotionDiscountAmount(BigDecimal.ZERO)
                .afterAmount(payAmount)
                .cacheVOList(Collections.singletonList(cacheVO))
                .build();
    }
}
