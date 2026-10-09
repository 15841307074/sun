package com.htyoudao.youdao.module.promotion.api.activity.DTO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 满赠活动赠品项
 */
@Data
public class MzGiftDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7421086221404665193L;

    /**
     * 优惠门槛（满赠金额/件数）
     */
    @Schema(description = "优惠门槛（满赠金额/件数）")
    private BigDecimal threshold;

    /**
     * 赠送商品ID
     */
    @Schema(description = "赠送商品ID")
    private Long giftCommodityId;

    /**
     * 赠送商品名称
     */
    @Schema(description = "赠送商品名称")
    private String giftCommodityName;

    /**
     * 赠送商品价格（连锁商品库售卖价）
     */
    @Schema(description = "赠送商品价格（连锁商品库售卖价）")
    private BigDecimal giftPrice;

    /**
     * 赠品图片
     */
    @Schema(description = "赠品图片")
    private String giftImage;

    /**
     * 活动库存（0或不填表示不限制）
     */
    @Schema(description = "活动库存（0或不填表示不限制）")
    private Integer activityInventory;

    /**
     * 剩余库存
     */
    @Schema(description = "剩余库存")
    private Integer remainingInventory;

}
