package com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.*;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 满赠活动赠送商品表
 */
@TableName("activity_mz_gift")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMzGiftDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 门店ID（门店独立库存时关联门店，共用库存时为null）
     */
    private Long storeId;

    /**
     * 优惠门槛（满赠金额/件数）
     */
    private BigDecimal threshold;

    /**
     * 赠送商品ID
     */
    private Long giftCommodityId;

    /**
     * 赠送商品名称
     */
    private String giftCommodityName;

    /**
     * 赠送商品价格（连锁商品库售卖价）
     */
    private BigDecimal giftPrice;

    /**
     * 赠品图片
     */
    private String giftImage;

    /**
     * 活动库存（null表示不限制，0表示无库存，最大1000000）
     */
    private Integer activityInventory;

    /**
     * 剩余库存（系统计算回显）
     */
    private Integer remainingInventory;

    /** 未支付订单已锁定库存。 */
    private Integer lockedInventory;

    /** 已支付且尚未退款的净消耗库存。 */
    private Integer consumedInventory;

}
