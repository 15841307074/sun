package com.htyoudao.youdao.module.promotion.api.usercoupon.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author dht
 */
@Data
public class MemberCouponDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String memberMobile;

    private Long memberId;

    private String memberName;

    private List<Long> couponIds;

    private List<CouponNumDTO> couponNumDTOList;
}
