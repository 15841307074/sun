package com.htyoudao.youdao.module.promotion.api.usercoupon.VO;

import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author DHT
 */
@Data
@ToString(callSuper = true)
public class GetReduceAmountRspVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8101027258043127679L;

    private Long id;

    private String couponCode;

    private String couponName;

    private Integer couponType;

    private String storeId;

    private BigDecimal reduceAmount;
}
