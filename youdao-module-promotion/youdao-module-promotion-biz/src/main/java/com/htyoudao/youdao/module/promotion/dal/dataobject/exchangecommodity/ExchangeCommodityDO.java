package com.htyoudao.youdao.module.promotion.dal.dataobject.exchangecommodity;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 兑换券下单时必选商品 DO
 *
 * @author lzw
 */
@TableName("coupon_exchange_commodity")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeCommodityDO extends BusinessBaseDO {

    /**
     * id
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
     * 商品name
     */
    private String commodityName;
}