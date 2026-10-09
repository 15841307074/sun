package com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;


@TableName("activity_jd_coupon_package")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActivityJDCouponPackageDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -7070525505627059503L;

    // 主键
    @TableId(type = IdType.ASSIGN_ID)
    private Long goodsId;

    private Long id;

    private Long goodsType = 1L;

    private String packageName;

    private String imageUrl;

    private Integer inventory;

    private Integer redeemPoints;

    private Long activityId;

}
