package com.htyoudao.youdao.module.promotion.dal.dataobject.couponcommodity;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 优惠券门店关系 DO
 *
 * @author dht
 */
@TableName("coupon_commodity")
@KeySequence("coupon_commodity_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponCommodityDO extends BusinessBaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 优惠券id
     */
    private Long couponId;
    /**
     * 商品id
     */
    private Long commodityId;
    /**
     * 商品名称
     */
    private String commodityName;

    /**
     * 适用类型 2 指定可用 3 指定不可用
     */
    private Integer type;

}