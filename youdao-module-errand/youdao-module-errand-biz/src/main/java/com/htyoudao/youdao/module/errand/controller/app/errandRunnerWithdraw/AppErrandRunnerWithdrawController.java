package com.htyoudao.youdao.module.errand.controller.app.errandRunnerWithdraw;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.errand.controller.app.errandRunnerWithdraw.VO.WithdrawApplyVO;
import com.htyoudao.youdao.module.errand.controller.app.errandRunnerWithdraw.VO.WithdrawStatisticsVO;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerWithdraw.ErrandRunnerWithdrawDO;
import com.htyoudao.youdao.module.errand.service.errandRunnerWithdraw.ErrandRunnerWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "跑腿员提现管理", description = "跑腿员提现相关接口")
@RestController
@RequestMapping("/errand/runner_withdraw")
@Validated
public class AppErrandRunnerWithdrawController {

    @Autowired
    private ErrandRunnerWithdrawService withdrawService;


    @GetMapping("/page")
    @Operation(summary = "分页查询提现记录")
    public CommonResult<Page<ErrandRunnerWithdrawDO>> page(@RequestParam(defaultValue = "1") Integer current,
                                                           @RequestParam(defaultValue = "10") Integer size,
                                                           @RequestParam(required = false) Long runnerId,
                                                           @RequestParam(required = false) Integer status) {
        Page<ErrandRunnerWithdrawDO> page = new Page<>(current, size);
        LambdaQueryWrapper<ErrandRunnerWithdrawDO> wrapper = new LambdaQueryWrapper<>();

        if (runnerId != null) {
            wrapper.eq(ErrandRunnerWithdrawDO::getRunnerId, runnerId);
        }
        if (status != null) {
            wrapper.eq(ErrandRunnerWithdrawDO::getStatus, status);
        }

        wrapper.orderByDesc(ErrandRunnerWithdrawDO::getCreateTime);
        return CommonResult.success(withdrawService.page(page, wrapper));
    }

    @GetMapping("/statistics/{runnerId}")
    @Operation(summary = "统计提现信息")
    public CommonResult<WithdrawStatisticsVO> statistics(@PathVariable Long runnerId) {
        BigDecimal totalAmount = withdrawService.getTotalSuccessAmount(runnerId);
        BigDecimal totalServiceFee = withdrawService.getTotalServiceFee(runnerId);
        BigDecimal todayAmount = withdrawService.getTodayWithdrawAmount(runnerId);

        WithdrawStatisticsVO vo = WithdrawStatisticsVO.builder()
                .totalWithdrawAmount(totalAmount)
                .totalServiceFee(totalServiceFee)
                .todayWithdrawAmount(todayAmount)
                .build();
        return CommonResult.success(vo);
    }



}
