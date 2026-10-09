package com.htyoudao.youdao.module.promotion.dal.dataobject.activityMj;

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
 * @author Yangqinglin
 * 营销活动表 -满减满折
 */
@TableName(value = "activity_mj", autoResultMap = true) // 由于 SQL Server 的 system_user 是关键字，所以使用 system_users
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMjDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = -7070525505627059503L;
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动主id
     */
    private Long activityId;

    /**
     * 优惠类型（优惠类型 （1满N元，2满N件））
     */
    private Integer discountType;

    /**
     * 优惠折扣（1满xx元/件减xx元 2满xx元/件减xx折）
     */
    private Integer discountOffer;

    /**
     * 优惠设置
     */
    private String discountSettings;

    /**
     * 优惠规则（1阶梯优惠 2循环优惠）
     */
    private Integer discountRules;

    /**
     * 活动商品（1全部商品可用 2指定商品可用）
     */
    private Integer activityProduct;


}