package com.htyoudao.youdao.module.order.dal.dataobject.order;

/**
 * <p>
 * 支付表
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-03
 */

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("bz_order_pay")
public class BzOrderPayDO extends BusinessBaseDO {
    @Serial
    private static final long serialVersionUID = 4089068153569694668L;

    @TableId(value = "pay_id")
    private Long payId;

    private String paySn;

    private String orderSn;

    private BigDecimal payAmount;

    private Long memberId;

    private String apiPayState;

    private LocalDateTime callbackTime;

    private String tradeSn;

    private String paymentName;

    private String paymentCode;

    private Long projectOwnerShip;
}
