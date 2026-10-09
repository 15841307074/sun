package com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponpackage;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 用户优惠券包关系 DO
 *
 * @author 13149747939
 */
@TableName("user_coupon_package")
@KeySequence("user_coupon_package_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCouponPackageDO extends BusinessBaseDO {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 小程序用户id
     */
    private Long userId;
    /**
     * 优惠券包id
     */
    private Long packageId;
    /**
     * 0自领 1发放
     */
    private Integer packageSource;


    /**
     * 会员名称
     */
    private String memberNickName;

    /**
     * 会员手机号
     */
    private String memberMobile;

}