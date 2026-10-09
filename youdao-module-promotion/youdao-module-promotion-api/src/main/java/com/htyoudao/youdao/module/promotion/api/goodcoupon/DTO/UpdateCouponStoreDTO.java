package com.htyoudao.youdao.module.promotion.api.goodcoupon.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class UpdateCouponStoreDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 标签id
     */
    private List<Long> tagIds;

    /**
     * 门店id
     */
    private Long storeId;

    /**
     * 门店name
     */
    private String storeName;
}
