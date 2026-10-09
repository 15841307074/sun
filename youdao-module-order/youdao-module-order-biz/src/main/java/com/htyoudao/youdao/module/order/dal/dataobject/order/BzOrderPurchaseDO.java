package com.htyoudao.youdao.module.order.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;

@Data
@TableName("bz_order_purchase")
public class BzOrderPurchaseDO extends BusinessBaseDO {
    @Serial
    private static final long serialVersionUID = 6793974795180311793L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String purchaseId;

    /**
     * 连锁库spuId
     */
    private Long commodityId;

    /**
     * 店铺spuId (和其他表保持一致)
     */
    private Long goodsId;

    /**
     * 店铺skuId
     */
    private Long storeSkuId;

    /**
     * 连锁库skuId
     */
    private Long originalSkuId;

    private String orderSn;

    private String purchaseImage;

    private String purchaseName;

    private BigDecimal purchasePrice;

    private BigDecimal strikePrice;

    private Integer isSingle;
}
