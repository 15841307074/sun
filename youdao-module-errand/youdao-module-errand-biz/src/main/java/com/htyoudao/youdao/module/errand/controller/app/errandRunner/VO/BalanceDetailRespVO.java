package com.htyoudao.youdao.module.errand.controller.app.errandRunner.VO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 余额明细响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "余额明细响应参数")
public class BalanceDetailRespVO {

    @Schema(description = "总余额（可用余额+冻结金额）")
    private BigDecimal totalBalance;

    @Schema(description = "可用余额")
    private BigDecimal availableBalance;

    @Schema(description = "冻结金额")
    private BigDecimal frozenBalance;
}
