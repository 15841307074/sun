package com.htyoudao.youdao.module.promotion.dal.dataobject.packagestoreclaimnum;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 优惠券包门店领取数量 DO
 * @author dht
 */
@TableName("package_store_claim_num")
@Data
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageStoreClaimNumDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 优惠券id
     */
    private Long packageId;
    /**
     * 门店id
     */
    private Long storeId;
    /**
     * 领取数量
     */
    private Integer claimNum;
}