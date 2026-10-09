package com.htyoudao.youdao.module.commodity.dal.dataobject.afterorder;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.*;

/**
 * 订单生成后加购商品 DO
 *
 * @author dht
 */
@TableName("commodity_after_order")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AfterOrderDO extends BusinessBaseDO {

    /**
     * 订单生成后加购商品ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long afterId;

    /**
     * 商品ID
     */
    private Long commodityId;

    /**
     * 加购价格
     */
    private BigDecimal afterPrice;

    /**
     * 划线价格
     */
    private BigDecimal strikeThroughPrice;

    /**
     * 状态 1开启 0下架
     */
    private Integer status;

    /**
     * 商品名称
     */
    private String commodityName;

    /**
     * 商品缩略图
     */
    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private String thumbnailUrl;

}