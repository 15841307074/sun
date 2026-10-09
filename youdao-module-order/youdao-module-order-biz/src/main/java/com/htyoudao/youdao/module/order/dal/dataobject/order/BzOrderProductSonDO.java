package com.htyoudao.youdao.module.order.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@TableName("bz_order_product_son")
@Data
public class BzOrderProductSonDO extends BusinessBaseDO {
    @Serial
    private static final long serialVersionUID = 5784991063134711194L;

    @TableId(value = "order_product_id", type = IdType.ASSIGN_ID)
    private Long orderProductId;

    private String orderSn;

    /**
     * 对应套餐的门店spuId （没有意义）
     */
    private Long goodsId;

    /**
     * 连锁库spuId
     */
    private Long commodityId;

    /**
     * 连锁库skuId
     */
    private Long originalSkuId;

    private String goodsName;

    private String goodsImage;

    private String specValues;

    private BigDecimal goodsShowPrice;

    private Integer goodsNum;

    private Boolean isGift;

    private Integer giftId;

    private Long parentGoodsId;

//    以下表中不存在字段

    @TableField(exist = false)
    private String skuName;

    @TableField(exist = false)
    private List<Map<String, String>> flavors;
}
