package com.htyoudao.youdao.module.order.core.submit.DTO;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-08-01
 */
@Data
public class SubmitCacheDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 8088055701797711958L;

    private String orderSn;

    private String paySn;

    private Long businessId;

    private BigDecimal payAmount;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    private Long storeId;

    private String paymentCode;

    private Integer orderType;
}
