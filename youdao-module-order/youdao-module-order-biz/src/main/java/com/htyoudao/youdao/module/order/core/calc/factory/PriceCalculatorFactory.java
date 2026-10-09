package com.htyoudao.youdao.module.order.core.calc.factory;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.htyoudao.youdao.module.order.core.calc.calculator.PriceCalculator;
import com.htyoudao.youdao.module.order.core.calc.calculator.impl.PurchaseCalculator;
import com.htyoudao.youdao.module.order.core.calc.calculator.impl.SetmealCalculator;
import com.htyoudao.youdao.module.order.core.calc.calculator.impl.SingleCalculator;
import com.htyoudao.youdao.module.order.core.calc.calculator.impl.seckill.SeckillSetmealCalculator;
import com.htyoudao.youdao.module.order.core.calc.calculator.impl.seckill.SeckillSingleCalculator;
import com.htyoudao.youdao.module.order.core.calc.v2.VO.SettlementReqV2VO;
import com.htyoudao.youdao.module.order.enums.OrderConstants;
import com.htyoudao.youdao.module.order.enums.SetmealTypeEnum;

/**
 * <p>
 * 提供不同的计算对象
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-28
 */
public class PriceCalculatorFactory {

    public static PriceCalculator getCalculator(SettlementReqV2VO.CommodityInfoVO commodity) {
        if (OrderConstants.YES.equals(commodity.getIsPurchase())) {
            return new PurchaseCalculator();
        }
        if(!ObjectUtils.isEmpty(commodity.getIsSeckill()) && commodity.getIsSeckill()){
            return commodity.getSetmealType() == SetmealTypeEnum.SINGLE.getCode() ?
                    new SeckillSingleCalculator() : new SeckillSetmealCalculator();
        }

        return commodity.getSetmealType() == SetmealTypeEnum.SINGLE.getCode() ?
                new SingleCalculator() : new SetmealCalculator();
    }

}
