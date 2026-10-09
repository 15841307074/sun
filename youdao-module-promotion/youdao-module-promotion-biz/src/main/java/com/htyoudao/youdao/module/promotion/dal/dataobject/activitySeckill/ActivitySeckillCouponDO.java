package com.htyoudao.youdao.module.promotion.dal.dataobject.activitySeckill;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import java.io.Serial;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;


@TableName("activity_seckill_coupon")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySeckillCouponDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -7070525505627059503L;

    // 主键
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 显示价格
     */
    private BigDecimal showPrice;

    /**
     * 显示标题
     */
    private String showTitle;

    /**
     * 活动库存
     */
    private Integer activityStock;

    /**
     * 每项限制数量
     */
    private Integer limitPerItem;

    private Long activityId;

}
