package com.htyoudao.youdao.module.errand.controller.app.errandRunner;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.errand.controller.app.errandRunner.VO.*;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunner.ErrandRunnerDO;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerWithdraw.ErrandRunnerWithdrawDO;
import com.htyoudao.youdao.module.errand.dal.mysql.errandRunner.ErrandRunnerMapper;
import com.htyoudao.youdao.module.errand.service.errandRunner.ErrandRunnerService;
import com.htyoudao.youdao.module.errand.service.errandRunnerWithdraw.ErrandRunnerWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "App - 跑腿员")
@RestController
@RequestMapping("/errand/runner")
@Validated
public class AppErrandRunnerController {

    @Autowired
    private ErrandRunnerService errandRunnerService;

    @Autowired
    private ErrandRunnerWithdrawService withdrawService;

    @Autowired
    private ErrandRunnerMapper errandRunnerMapper;

    /**
     * 提交骑手入驻申请。
     */
    @PostMapping("/apply")
    @Operation(summary = "提交骑手入驻申请")
    public CommonResult<Long> apply(@Valid @RequestBody AppErrandRunnerApplyReqVO reqVO) {
       Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(errandRunnerService.apply(memberId, reqVO));
    }

    /**
     * 查询当前会员的骑手入驻详情和审核信息。
     */
    @GetMapping("/apply-info")
    @Operation(summary = "查询骑手入驻详情")
    public CommonResult<AppErrandRunnerRespVO> getApplyInfo() {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(errandRunnerService.getApplyInfo(memberId));
    }

    /**
     * 校验当前会员是否可作为跑腿员接单。
     */
    @GetMapping("/available/check")
    @Operation(summary = "校验跑腿员接单资格")
    public CommonResult<Boolean> checkCurrentRunnerAvailable() {
        return success(errandRunnerService.checkCurrentRunnerAvailable());
    }

    /**
     * 修改骑手入驻申请资料。
     */
    @PostMapping("/updateApply")
    @Operation(summary = "修改骑手入驻申请")
    public CommonResult<Boolean> updateApply(@Valid @RequestBody AppErrandRunnerApplyReqVO reqVO) {
         Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(errandRunnerService.updateApply(memberId, reqVO));
    }

    /**
     * 前端弹窗展示完成后，标记为已弹窗。
     */
    @PostMapping("/popup-status/read")
    @Operation(summary = "标记骑手弹窗已展示")
    public CommonResult<Boolean> markPopupShown() {
        Long memberId = SecurityFrameworkUtils.getLoginUserId();
        return success(errandRunnerService.markPopupShown(memberId));
    }


    /**
     * 发起提现申请
     */
    @PostMapping("/withdraw")
    @Operation(summary = "提现（需要交易密码）")
    public CommonResult<ErrandRunnerWithdrawDO> withdraw(@Valid @RequestBody WithdrawReqVO reqVO) {
        // 1. 验证交易密码
        errandRunnerService.verifyTranPassword(WebFrameworkUtils.getLoginUserId(), reqVO.getTranPassword());

        // 2. 发起提现申请
        ErrandRunnerWithdrawDO withdraw = withdrawService.applyWithdraw(WebFrameworkUtils.getLoginUserId(), reqVO.getAmount(),reqVO.getOpenId());

        return success(withdraw);
    }

    /**
     * 获取余额明细和流水分页的组合数据。
     */
    @GetMapping("/balance/detail-with-log")
    @Operation(summary = "获取余额明细及流水（组合接口）")
    public CommonResult<BalanceDetailWithLogRespVO> getBalanceDetailWithLog(@Valid BalanceLogPageReqVO reqVO) {
        BalanceDetailWithLogRespVO result = errandRunnerService.getBalanceDetailWithLog(WebFrameworkUtils.getLoginUserId(), reqVO);
        return success(result);
    }

    /**
     * 获取跑腿员余额明细。
     */
    @GetMapping("/balance/detail")
    @Operation(summary = "获取余额明细")
    public CommonResult<BalanceDetailRespVO> getBalanceDetail() {
        ErrandRunnerDO runner = errandRunnerMapper.selectByMemberId(WebFrameworkUtils.getLoginUserId());
        if (runner == null) {

            BalanceDetailRespVO  balanceDetailRespVO = new BalanceDetailRespVO();
            BigDecimal bigDecimal = new BigDecimal(0);
            balanceDetailRespVO.setAvailableBalance(bigDecimal);
            balanceDetailRespVO.setTotalBalance(bigDecimal);
            balanceDetailRespVO.setFrozenBalance(bigDecimal);

            return success(balanceDetailRespVO);
        }
        BalanceDetailRespVO result = errandRunnerService.getBalanceDetail(WebFrameworkUtils.getLoginUserId(),runner);
        return success(result);
    }

    /**
     * 分页查询跑腿员余额流水。
     */
    @GetMapping("/balance/log/page")
    @Operation(summary = "分页查询余额流水")
    public CommonResult<BalanceLogPageRespVO> pageBalanceLog(@Valid BalanceLogPageReqVO reqVO) {
        ErrandRunnerDO runner = errandRunnerMapper.selectByMemberId(WebFrameworkUtils.getLoginUserId());
        if (runner == null) {

            BalanceLogPageRespVO balanceLogPageRespVO= new BalanceLogPageRespVO();
            balanceLogPageRespVO.setTotal(0L);
            List<BalanceLogVO> records = new ArrayList<>();
            balanceLogPageRespVO.setRecords(records);
            return success(balanceLogPageRespVO);
        }
        BalanceLogPageRespVO result = errandRunnerService.pageBalanceLog(WebFrameworkUtils.getLoginUserId(), reqVO,runner);
        return success(result);
    }


}
