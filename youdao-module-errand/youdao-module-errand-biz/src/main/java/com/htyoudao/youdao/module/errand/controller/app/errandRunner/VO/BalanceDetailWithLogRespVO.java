package com.htyoudao.youdao.module.errand.controller.app.errandRunner.VO;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 余额明细及流水响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "余额明细及流水响应参数")
public class BalanceDetailWithLogRespVO {

    @Schema(description = "余额汇总信息")
    private BalanceDetailRespVO balanceDetail;

    @Schema(description = "流水分页信息")
    private BalanceLogPageRespVO balanceLogPage;
}
