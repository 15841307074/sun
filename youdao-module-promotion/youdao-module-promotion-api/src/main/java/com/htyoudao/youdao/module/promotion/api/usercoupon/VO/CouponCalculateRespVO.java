package com.htyoudao.youdao.module.promotion.api.usercoupon.VO;

import com.htyoudao.youdao.framework.common.exception.ErrorCode;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author dht
 */
@Data
public class CouponCalculateRespVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 656725239457847927L;

    private Long id;

    private String couponName;

    private Integer couponType;

    private String singleIds;

    private BigDecimal reduceAmount;

    private String couponImageUrl;

    private String remark;

    private Integer isCommon;

    private Integer doorsillType;

    private BigDecimal doorsill;

    private Integer isCommonStore;

    private BigDecimal reliefOrDiscount;

    private BigDecimal payAmount;

    private Date expirationTime;

    private Date vaildStartTime;

    private Integer habit;

    private String couponBgImageUrl;

    private Long userCouponId;

    /**
     * 参与优惠的商品数量
     */
    private int goodSize;

    /**
     * 参与优惠的商品ids
     */
    private List<Long> commodityIds;

    /**
     * 减免金额
     */
    private BigDecimal money;

    private Long couponId;

    /**
     * 门店ids
     */
    private List<Long> storeIdList;

    private ErrorCode errorCode;
}
