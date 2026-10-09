package com.htyoudao.youdao.module.commodity.api.DTO;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 加购商品
 * </p>
 *
 * @author zhangjihe
 * @since 2025-04-29
 */
@Data
public class AfterInfoDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6903519445584503856L;

    private Long afterId;

    /**
     * 连锁库spuId
     */
    private Long commodityId;

    /**
     * 店铺spuId
     */
    private Long spuId;

    /**
     * 店铺skuId
     */
    private Long skuId;

    /**
     * 连锁库skuId
     */
    private Long originalSkuId;

    private String skuName;

    private String imageUrl;

    private BigDecimal afterPrice;

    private BigDecimal skuPrice;

    private BigDecimal strikePrice;

    private Integer manyCopy;

    private BigDecimal packageFee;

    private ErrorCode errorCode;
}
