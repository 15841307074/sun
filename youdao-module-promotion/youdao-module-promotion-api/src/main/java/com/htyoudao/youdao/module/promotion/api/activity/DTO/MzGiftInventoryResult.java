package com.htyoudao.youdao.module.promotion.api.activity.DTO;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** 满赠赠品库存批量查询结果。 */
@Data
public class MzGiftInventoryResult implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long activityId;
    private Long storeId;
    private Long giftCommodityId;
    /** null=缓存不存在，-1=不限库存，其他值=实时剩余库存。 */
    private Integer inventory;
}
