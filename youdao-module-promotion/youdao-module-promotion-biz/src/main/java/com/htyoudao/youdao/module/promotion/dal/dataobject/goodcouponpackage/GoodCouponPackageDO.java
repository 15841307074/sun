package com.htyoudao.youdao.module.promotion.dal.dataobject.goodcouponpackage;

import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;

/**
 * 优惠券包关系 DO
 *
 * @author 13149747939
 */
@TableName("good_coupon_package")
@KeySequence("good_coupon_package_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodCouponPackageDO  extends BusinessBaseDO {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    /**
     * 优惠券包id
     */
    private Long packageId;
    /**
     * 优惠券id
     */
    private Long couponId;
    /**
     * 数量
     */
    private Integer num;
    /**
     * 展示标题
     */
    private String displayTitle;
    /**
     * 展示金额
     */
    private String displayAmount;
    /**
     * 划线内容
     */
    private String underlinedContent;

}