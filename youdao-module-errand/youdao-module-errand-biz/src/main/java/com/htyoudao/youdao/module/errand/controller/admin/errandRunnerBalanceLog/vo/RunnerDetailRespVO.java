package com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RunnerDetailRespVO {

    @Schema(description = "余额")
    private BigDecimal balance;

    @Schema(description = "可用余额")
    private BigDecimal availableBalance;
}
