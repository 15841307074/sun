package com.htyoudao.youdao.module.errand.api.runner;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.errand.api.runner.dto.ErrandRunnerDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;



import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "RPC 服务 - 跑腿员")
public interface ErrandRunnerApi {

    @Operation(summary = "根据会员ID查询跑腿员")
    CommonResult<ErrandRunnerDTO> getRunnerByMemberId(@RequestParam("memberId") Long memberId);

    @Operation(summary = "根据会员ID查询可接单跑腿员")
    CommonResult<ErrandRunnerDTO> getAvailableRunnerByMemberId(@RequestParam("memberId") Long memberId);

    @Operation(summary = "跑腿赏金冻结入账")
    CommonResult<Boolean> createRewardIncome(@RequestParam("runnerMemberId") Long runnerMemberId,
                                             @RequestParam("orderSn") String orderSn,
                                             @RequestParam("amount") BigDecimal amount);

    @Operation(summary = "跑腿赏金退款扣回")
    CommonResult<Boolean> refundRewardDeduct(@RequestParam("runnerMemberId") Long runnerMemberId,
                                             @RequestParam("orderSn") String orderSn,
                                             @RequestParam("amount") BigDecimal amount);
    /**
     * 根据会员ID查询可接单跑腿员余额
     * @param memberIds memberIds
     * @return Map<Long, BigDecimal>
     */
    Map<Long, BigDecimal> getRunnerBalanceByMemberId(List<Long> memberIds);
}
