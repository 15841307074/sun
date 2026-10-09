package com.htyoudao.youdao.module.errand.controller.app.errandRunnerWithdraw.VO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 提现统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "提现统计信息")
public class WithdrawStatisticsVO {

    @Schema(description = "累计提现金额")
    private BigDecimal totalWithdrawAmount;

    @Schema(description = "累计手续费")
    private BigDecimal totalServiceFee;

    @Schema(description = "今日提现金额")
    private BigDecimal todayWithdrawAmount;

    @Schema(description = "提现次数")
    private Integer withdrawCount;

    @Schema(description = "成功次数")
    private Integer successCount;

    @Schema(description = "失败次数")
    private Integer failCount;
}
