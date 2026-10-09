package com.htyoudao.youdao.module.order.api.order.dto;

import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author dht
 */
@Data
public class BzOrderDTO implements java.io.Serializable{

    @Serial
    private static final long serialVersionUID = 1L;

    private Long memberId;

    private BigDecimal sum;

    private Integer count;

    private Long storeId;

    private LocalDateTime createTime;
}
