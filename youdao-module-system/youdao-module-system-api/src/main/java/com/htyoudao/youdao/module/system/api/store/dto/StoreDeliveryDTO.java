package com.htyoudao.youdao.module.system.api.store.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 门店基础信息
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-30
 */
@Data
public class StoreDeliveryDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 7469094675732731962L;

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 门店是否在所选地址范围内(0-不在,1-在)
     */
    private Integer isDelivery;

    /**
     * 门店纬度
     */
    private Double storeLatitude;

    /**
     * 门店经度
     */
    private Double storeLongitude;

    /**
     * 距离
     */
    private Double distance;
}
