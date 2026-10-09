package com.htyoudao.youdao.module.order.dal.DTO;

import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;

@Data
public class OrderReportWithRateDTO extends OrderReportDTO {
    @Serial
    private static final long serialVersionUID = 4379951648130893476L;

    private BigDecimal rate; // 占比，小数
}

