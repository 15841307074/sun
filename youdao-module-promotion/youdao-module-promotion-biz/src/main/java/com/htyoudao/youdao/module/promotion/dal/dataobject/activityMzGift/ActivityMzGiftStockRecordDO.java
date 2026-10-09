package com.htyoudao.youdao.module.promotion.dal.dataobject.activityMzGift;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 满赠赠品库存锁定流水。
 *
 * <p>记录订单提交、支付、取消及退款过程中赠品库存的状态变化，用于库存回滚、
 * 幂等校验和超时锁定释放。</p>
 */
@Data
@TableName("activity_mz_gift_stock_record")
public class ActivityMzGiftStockRecordDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 8432745994872618229L;

    /**
     * 主键。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 满赠活动 ID。
     */
    private Long activityId;

    /**
     * 满赠赠品配置 ID；动态门店仅维护 Redis 库存时为空，不回写数据库库存汇总。
     */
    private Long activityMzGiftId;

    /**
     * 下单门店 ID。
     */
    private Long storeId;

    /**
     * 订单编号。
     */
    private String orderSn;

    /**
     * 下单会员 ID；点餐机等无会员场景可为空。
     */
    private Long memberId;

    /**
     * 赠品连锁商品 ID。
     */
    private Long giftCommodityId;

    /**
     * 赠品门店 SKU ID。
     */
    private Long giftSkuId;

    /**
     * 实际锁定的赠品数量。
     */
    private Integer quantity;

    /**
     * 库存模式：1-所有门店共用库存，2-门店独立库存。
     */
    private Integer inventoryType;

    /**
     * 库存流水状态：1-锁定中，2-已支付消耗，3-取消释放，4-退款恢复。
     */
    private Integer status;

    /**
     * 库存锁定到期时间，逾期未支付时可释放库存。
     */
    private LocalDateTime lockExpireTime;

    /**
     * 支付成功时间。
     */
    private LocalDateTime paidTime;

    /**
     * 取消订单并释放库存的时间。
     */
    private LocalDateTime releasedTime;

    /**
     * 退款并恢复库存的时间。
     */
    private LocalDateTime refundedTime;
}
