package com.htyoudao.youdao.module.promotion.dal.dataobject.activityMz;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 满赠活动配置表
 */
@TableName(value = "activity_mz", autoResultMap = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMzDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动主表id
     */
    private Long activityId;

    /**
     * 优惠类型（1满N元赠商品，2满N件赠商品）
     */
    private Integer discountType;

    /**
     * 优惠规则（1阶梯优惠 2循环优惠）
     */
    private Integer discountRules;

    /**
     * 下单类型（1按品类限制，2按商品限制）
     */
    private Integer placeOrderType;

    /**
     * 参与品类（1全部，2仅单品，3仅套餐；place_order_type=1时生效）
     */
    private Integer categoryType;

    /**
     * 下单商品（1全部商品，2指定商品；place_order_type=2时生效）
     */
    private Integer placeOrderProduct;

    /**
     * 门店奖励库存（1共用库存，2独立库存）
     */
    private Integer giftInventoryType;

    /**
     * 用户参与限制（0不限制，1每人每天，2每人最多）
     */
    private Integer userLimitType;

    /**
     * 用户参与限制次数（user_limit_type>0时生效）
     */
    private Integer userLimitValue;

}
