package com.htyoudao.youdao.module.errand.controller.app.errandRunnerBalanceLog.VO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 跑腿员余额统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "跑腿员余额统计信息")
public class BalanceStatisticsVO {

    @Schema(description = "总收入金额")
    private BigDecimal totalIncome;

    @Schema(description = "总提现金额")
    private BigDecimal totalWithdraw;

    @Schema(description = "当前可用余额（如需实时查询可调用跑腿员表）")
    private BigDecimal currentBalance;

    @Schema(description = "待入账金额")
    private BigDecimal pendingAmount;

    @Schema(description = "累计赚取金额")
    private BigDecimal totalEarned;

    @Schema(description = "今日收入")
    private BigDecimal todayIncome;

    @Schema(description = "本周收入")
    private BigDecimal weekIncome;

    @Schema(description = "本月收入")
    private BigDecimal monthIncome;
}
