package com.htyoudao.youdao.module.order.core.activity.DTO;

import com.htyoudao.youdao.module.order.core.calc.v1.DTO.CalculateCacheDataDTO;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 *  优惠活动计算返回结果
 * </p>
 *
 * @author zhangjihe
 * @since 2025-06-07
 */
@Builder
@Data
public class CalculatorResDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -1605531519084766285L;

    /**
     * 缓存商品数据
     */
    private List<CalculateCacheDataDTO.CommodityInfoVO> cacheVOList;

    /**
     * 优惠金额
     */
    private BigDecimal promotionDiscountAmount;

    /**
     * 活动创建时间
     */
    private LocalDateTime createTime;

    /**
     * 命中优惠活动名称
     */
    private String activityName;

    /**
     * 应收打包费数量
     */
    private Integer shouldPayPackageFeeCount;
}
