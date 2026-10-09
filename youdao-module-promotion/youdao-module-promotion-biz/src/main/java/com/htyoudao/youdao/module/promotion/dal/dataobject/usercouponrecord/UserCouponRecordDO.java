package com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 优惠券使用记录 DO
 *
 * @author 13149747939
 */
@TableName("user_coupon_record")
@KeySequence("user_coupon_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponRecordDO extends BusinessBaseDO {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 用户优惠券id
     */
    private Long userCouponId;
    /**
     * 优惠券id
     */
    private Long couponId;
    /**
     * 用券成交额
     */
    private BigDecimal totalAmount;
    /**
     * 优惠额
     */
    private BigDecimal couponAmount;
    /**
     * 商品数量
     */
    private Integer itemNum;
    /**
     * 是否是门店通用券 0 是 1指定门店券
     */
    private Integer isCommon;
    /**
     * 下单门店
     */
    private Long storeId;

    private Integer couponSource;

}