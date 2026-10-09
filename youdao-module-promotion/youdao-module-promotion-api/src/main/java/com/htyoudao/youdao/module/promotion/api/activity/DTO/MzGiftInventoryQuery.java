package com.htyoudao.youdao.module.promotion.api.activity.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 满赠赠品库存批量查询条件。 */
@Data
public class MzGiftInventoryQuery implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long activityId;
    private Long storeId;
    private Long giftCommodityId;
}
