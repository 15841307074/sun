package com.htyoudao.youdao.module.errand.controller.app.errandRunnerBalanceLog;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.errand.controller.app.errandRunnerBalanceLog.VO.BalanceStatisticsVO;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerBalanceLog.ErrandRunnerBalanceLogDO;
import com.htyoudao.youdao.module.errand.service.errandRunnerBalanceLog.ErrandRunnerBalanceLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "跑腿员余额流水管理", description = "跑腿员余额流水相关接口")
@RestController
@RequestMapping("/errand/runner_balance_log")
@Validated
public class AppErrandRunnerBalanceLogController {



    @Autowired
    private  ErrandRunnerBalanceLogService balanceLogService;

    @GetMapping("/page")
    @Operation(summary = "分页查询余额流水")
    public CommonResult<Page<ErrandRunnerBalanceLogDO>> page(@RequestParam(defaultValue = "1") Integer current,
                                                       @RequestParam(defaultValue = "10") Integer size,
                                                       @RequestParam(required = false) Long runnerId,
                                                       @RequestParam(required = false) Integer flowType,
                                                       @RequestParam(required = false) Integer direction) {
        Page<ErrandRunnerBalanceLogDO> page = new Page<>(current, size);
        LambdaQueryWrapper<ErrandRunnerBalanceLogDO> wrapper = new LambdaQueryWrapper<>();

        if (runnerId != null) {
            wrapper.eq(ErrandRunnerBalanceLogDO::getRunnerId, runnerId);
        }
        if (flowType != null) {
            wrapper.eq(ErrandRunnerBalanceLogDO::getFlowType, flowType);
        }
        if (direction != null) {
            wrapper.eq(ErrandRunnerBalanceLogDO::getDirection, direction);
        }

        wrapper.orderByDesc(ErrandRunnerBalanceLogDO::getCreateTime);
        return CommonResult.success(balanceLogService.page(page, wrapper));
    }

    @GetMapping("/statistics/{runnerId}")
    @Operation(summary = "统计跑腿员余额信息")
    public CommonResult<BalanceStatisticsVO> statistics(@PathVariable Long runnerId) {
        BigDecimal totalIncome = balanceLogService.getTotalIncome(runnerId);
        BigDecimal totalWithdraw = balanceLogService.getTotalWithdraw(runnerId);

        BalanceStatisticsVO vo = BalanceStatisticsVO.builder()
                .totalIncome(totalIncome)
                .totalWithdraw(totalWithdraw)
                .build();
        return CommonResult.success(vo);
    }
}
