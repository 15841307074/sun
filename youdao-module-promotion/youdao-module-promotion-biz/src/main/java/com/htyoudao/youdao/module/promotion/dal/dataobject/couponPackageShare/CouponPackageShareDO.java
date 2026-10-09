package com.htyoudao.youdao.module.promotion.dal.dataobject.couponPackageShare;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

/**
 * 优惠券分享子 DO
 *
 * @author dht
 */
@TableName("coupon_package_share")
@KeySequence("coupon_package_share_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponPackageShareDO extends BusinessBaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;


    private Long packageId;


    private String shareTitle;


    private String shareNote;


    private String shareLittleImgUrl;


    private String wxShareUrl;


    private String htmlShareUrl;


    private String erCodeUrl;


    private String postersImgUrl;


    private String shareLargeImgUrl;
}
