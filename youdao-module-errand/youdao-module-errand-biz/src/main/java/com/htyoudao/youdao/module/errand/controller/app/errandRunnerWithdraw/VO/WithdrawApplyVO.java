package com.htyoudao.youdao.module.errand.controller.app.errandRunnerWithdraw.VO;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 提现申请VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "提现申请参数")
public class WithdrawApplyVO {

    @NotNull(message = "跑腿员ID不能为空")
    @Schema(description = "跑腿员ID", required = true)
    private Long runnerId;

    @NotNull(message = "跑腿员会员ID不能为空")
    @Schema(description = "跑腿员会员ID", required = true)
    private Long runnerMemberId;

    @NotNull(message = "提现金额不能为空")
    @Positive(message = "提现金额必须大于0")
    @Schema(description = "提现金额", required = true)
    private BigDecimal amount;

    @Schema(description = "手续费")
    private BigDecimal serviceFee;

    @Schema(description = "实际到账金额")
    private BigDecimal actualAmount;
}
