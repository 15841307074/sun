package com.htyoudao.youdao.module.order.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@TableName("bz_order_product")
@Data
public class BzOrderProductDO extends BusinessBaseDO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2024394679676919290L;

    @TableId(value = "order_product_id", type = IdType.ASSIGN_ID)
    private Long orderProductId;

    private String orderSn;

    private Long storeId;

    private String storeName;

    private Long memberId;

    private String goodsName;

    private String goodsImage;

    private Long activityId;

    private String activityName;

    private Integer activityType;

    private String specValues;

    private BigDecimal goodsShowPrice;

    private BigDecimal skuStrikePrice;

    private Integer goodsNum;

    private BigDecimal activityDiscountAmount;

    private BigDecimal promotionDiscountAmount;

    private BigDecimal platformActivityAmount;

    private BigDecimal platformVoucherAmount;

    private BigDecimal moneyAmount;

    private BigDecimal commissionRate;

    private BigDecimal commissionAmount;

    private BigDecimal attachCommissionAmount;

    private Long spellTeamId;

    private Integer isGift;

    private Integer giftId;

    private Integer returnNumber;

    private Integer isComment;

    private Date commentTime;

    private Integer sendIntegral;

    private Integer isSingle;

    private String flavorName;

    private String flavorValue;

    private Long categoryId;

    /**
     * 连锁库spuId
     */
    private Long commodityId;

    /**
     * 店铺spuId
     */
    private Long goodsId;

    /**
     * 连锁库SKU ID
     */
    private Long originalSkuId;

    /**
     * 店铺skuId
     */
    private Long storeSkuId;

    private String categoryName;

    private String activityDiscountDetail;

    private Long couponId;

    private Long userCouponId;

    private String couponName;

}
