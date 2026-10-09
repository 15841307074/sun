package com.htyoudao.youdao.module.promotion.api.usercoupon.DTO;

import lombok.Data;

import java.io.Serializable;

@Data
public class CouponNumDTO implements Serializable {

    private Long couponId;

    private Integer num;
}
