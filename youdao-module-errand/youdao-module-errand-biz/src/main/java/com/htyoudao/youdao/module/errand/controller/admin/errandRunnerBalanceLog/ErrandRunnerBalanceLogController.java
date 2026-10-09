package com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo.ErrandRunnerBalanceLogReqVO;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo.ErrandRunnerBalanceLogRespVO;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo.ExportBalanceLogReqVO;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo.RunnerDetailRespVO;
import com.htyoudao.youdao.module.errand.service.errandRunnerBalanceLog.ErrandRunnerBalanceLogService;
import com.htyoudao.youdao.module.errand.service.errandRunnerWithdraw.ErrandRunnerWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "跑腿员余额流水管理后台", description = "跑腿员余额流水管理后台")
@RestController
@RequestMapping("/errand/runner_balance_log")
@Validated
public class ErrandRunnerBalanceLogController {

    @Resource
    private ErrandRunnerBalanceLogService balanceLogService;

    @Resource
    private ErrandRunnerWithdrawService withdrawService;

    @PostMapping("/exportErrandList")
    @Operation(summary = "按时间导出余额流水")
    public CommonResult<Void> exportErrandList(@RequestBody ExportBalanceLogReqVO reqVO) {
        return CommonResult.success(withdrawService.exportErrandList(reqVO));
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询单人余额流水")
    public CommonResult<PageResult<ErrandRunnerBalanceLogRespVO>> selectPageByMemberId(@RequestBody ErrandRunnerBalanceLogReqVO reqVO) {
        return CommonResult.success(balanceLogService.selectByMemberId(reqVO));
    }

    @GetMapping("/getRunnerDetailByMemberId")
    @Operation(summary = "查询单人余额详情")
    public CommonResult<RunnerDetailRespVO> getRunnerDetailByMemberId(@RequestParam("memberId") Long memberId) {
        return CommonResult.success(balanceLogService.getRunnerDetailByMemberId(memberId));
    }
}
