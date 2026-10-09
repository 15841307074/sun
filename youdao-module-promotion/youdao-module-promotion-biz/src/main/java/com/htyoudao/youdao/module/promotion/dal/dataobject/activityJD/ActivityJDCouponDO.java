package com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serial;
import java.math.BigDecimal;


@TableName("activity_jd_coupon")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActivityJDCouponDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -7070525505627059503L;

    // 主键
    @TableId(type = IdType.ASSIGN_ID)
    private Long goodsId;

    private Long id;

    private Long goodsType = 1L;

    private String couponName;

    private String imageUrl;

    private Integer inventory;

    private Integer redeemPoints;

    private Long activityId;
}
