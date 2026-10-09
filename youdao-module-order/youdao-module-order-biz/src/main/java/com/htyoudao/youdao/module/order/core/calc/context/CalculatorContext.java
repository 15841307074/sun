package com.htyoudao.youdao.module.order.core.calc.context;

import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import com.htyoudao.youdao.module.order.core.calc.v2.VO.SettlementReqV2VO;
import lombok.RequiredArgsConstructor;

/**
 * <p>
 * 数据承接类
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-28
 */
@RequiredArgsConstructor
public class CalculatorContext {

    /**
     * 商品信息
     */
    public final SettlementReqV2VO.CommodityInfoVO commodity;

    /**
     * 异步数据
     */
    public final AsyncData data;

    /**
     * 是否是点餐机
     */
    public final Boolean isDc;

    /**
     * 秒杀信息
     */
    public final CalculateCacheDataV2DTO.SeckillInfo seckillInfo;
}
