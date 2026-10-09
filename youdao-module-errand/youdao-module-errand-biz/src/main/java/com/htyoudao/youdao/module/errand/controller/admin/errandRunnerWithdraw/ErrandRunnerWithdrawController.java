package com.htyoudao.youdao.module.errand.controller.admin.errandRunnerWithdraw;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunnerBalanceLog.vo.ExportBalanceLogReqVO;
import com.htyoudao.youdao.module.errand.service.errandRunnerWithdraw.ErrandRunnerWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "跑腿员提现管理", description = "跑腿员提现相关接口")
@RestController
@RequestMapping("/errand/runner_withdraw")
@Validated
public class ErrandRunnerWithdrawController {


}
