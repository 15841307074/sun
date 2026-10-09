package com.htyoudao.youdao.module.order.core.submit.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DiscountResultDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -8830152922505785147L;

    private BigDecimal discountAmount;
    private BigDecimal finalAmount;

}