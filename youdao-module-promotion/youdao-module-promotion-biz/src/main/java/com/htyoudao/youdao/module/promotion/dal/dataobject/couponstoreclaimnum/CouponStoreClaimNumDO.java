package com.htyoudao.youdao.module.promotion.dal.dataobject.couponstoreclaimnum;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

/**
 * 优惠券门店领取数量 DO
 *
 * @author dht
 */
@TableName("coupon_store_claim_num")
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponStoreClaimNumDO {

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
     * 领取数量
     */
    private Integer claimNum;
}