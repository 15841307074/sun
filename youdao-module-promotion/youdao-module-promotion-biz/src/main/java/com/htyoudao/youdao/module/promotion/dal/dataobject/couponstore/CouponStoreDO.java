package com.htyoudao.youdao.module.promotion.dal.dataobject.couponstore;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 优惠券门店关系 DO
 *
 * @author dht
 */
@TableName("coupon_store")
@KeySequence("coupon_store_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponStoreDO extends BusinessBaseDO {

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
     * 门店id
     */
    private Long storeId;
    /**
     * 门店名称
     */
    private String storeName;
    /**
     * 门店所属组织
     */
    private Long deptId;

    /**
     * 门店此券总数
     */
    private Integer totalNum;

    /**
     * 已领取数量
     */
    private Integer receivedNum;
    /**
     * 门店标签id
     */
    private Long tagId;
}