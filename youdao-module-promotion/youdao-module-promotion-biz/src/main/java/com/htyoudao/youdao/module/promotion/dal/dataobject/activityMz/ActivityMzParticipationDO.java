package com.htyoudao.youdao.module.promotion.dal.dataobject.activityMz;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;

import java.io.Serial;
import java.time.LocalDate;

/**
 * 满赠活动用户参与记录。
 *
 * <p>用于统计用户每天或整个活动期间的有效参与次数。锁定中和已支付记录占用参与次数，
 * 取消释放或退款后的记录不再占用参与次数。</p>
 */
@Data
@TableName("activity_mz_participation")
public class ActivityMzParticipationDO extends BusinessBaseDO {

    @Serial
    private static final long serialVersionUID = 3772266949197332179L;

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
     * 参与活动的会员 ID。
     */
    private Long memberId;

    /**
     * 占用本次参与次数的订单编号。
     */
    private String orderSn;

    /**
     * 参与日期，用于按天限制参与次数。
     */
    private LocalDate participationDate;

    /**
     * 参与状态：1-锁定中，2-已支付，3-取消释放，4-已退款。
     */
    private Integer status;
}
