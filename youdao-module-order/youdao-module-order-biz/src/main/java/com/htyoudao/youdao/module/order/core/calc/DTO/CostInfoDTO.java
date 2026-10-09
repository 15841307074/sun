package com.htyoudao.youdao.module.order.core.calc.DTO;

import com.htyoudao.youdao.module.order.core.calc.v2.DTO.CalculateCacheDataV2DTO;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 费用
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-09
 */
@Builder
@Data
public class CostInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8265469765475892547L;

    /**
     * 优惠金额
     */
    private BigDecimal activityDiscountAmount;

    /**
     * 优惠金额
     */
    private BigDecimal promotionDiscountAmount;

    /**
     * 实付金额
     */
    private BigDecimal commodityAmount;

    /**
     * 加购金额
     */
    private BigDecimal afterAmount;

    /**
     * 打包费
     */
    private BigDecimal packingFee;

    /**
     * 配送费
     */
    private BigDecimal deliveryFee;

    /**
     * 商品信息
     */
    private List<CalculateCacheDataV2DTO.CommodityInfoVO> cacheVOList;
}
