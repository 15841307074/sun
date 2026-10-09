package com.htyoudao.youdao.module.errand.controller.admin.errandRunner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.errand.controller.admin.errandRunner.VO.*;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunner.ErrandRunnerDO;
import com.htyoudao.youdao.module.errand.service.errandRunner.ErrandRunnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "跑腿员")
@RestController
@RequestMapping("/errand/runner")
@Validated
public class ErrandRunnerController {

    @Autowired
    private ErrandRunnerService errandRunnerService;


    @GetMapping("/page")
    @Operation(summary = "分页查询跑腿员")
    public CommonResult<Page<ErrandRunnerPageVO>> page(@Valid ErrandRunnerPageReqVO reqVO) {
        Page<ErrandRunnerPageVO> page = errandRunnerService.pageQuery(reqVO);
        return CommonResult.success(page);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPermission('errand:runner:delete')")
    @Operation(summary = "删除跑腿员")
    public CommonResult<Boolean> delete(@PathVariable Long id) {
        return CommonResult.success(errandRunnerService.removeById(id));
    }

    @GetMapping("/{id}")
//    @PreAuthorize("@ss.hasPermission('errand:runner:info')")
    @Operation(summary = "根据ID查询跑腿员详情（包含变更记录）")
    public CommonResult<ErrandRunnerDetailVO> getById(@PathVariable Long id) {
        return CommonResult.success(errandRunnerService.getDetailById(id));
    }

    @PostMapping("/audit")
    @PreAuthorize("@ss.hasPermission('errand:runner:audit')")
    @Operation(summary = "审核跑腿员")
    public CommonResult<Boolean> audit(@Valid @RequestBody AuditReqVO reqVO) {
        boolean result = errandRunnerService.auditRunner(
                reqVO.getRunnerId(),
                reqVO.getAuditStatus(),
                reqVO.getAuditReason()
        );
        return CommonResult.success(result);
    }


    @GetMapping("/check-available/{runnerId}")
    @Operation(summary = "检查跑腿员是否可用")
    public CommonResult<Boolean> checkRunnerAvailable(@PathVariable Long runnerId) {
        boolean available = errandRunnerService.isRunnerAvailable(runnerId);
        return CommonResult.success(available);
    }


    @PostMapping("/ban")
    @PreAuthorize("@ss.hasPermission('errand:runner:ban')")
    @Operation(summary = "封禁跑腿员")
    public CommonResult<Boolean> ban(@Valid @RequestBody BanReqVO reqVO) {
        boolean result = errandRunnerService.banRunner(reqVO.getRunnerId());
        return CommonResult.success(result);
    }

    @PostMapping("/unban")
    @PreAuthorize("@ss.hasPermission('errand:runner:ban')")
    @Operation(summary = "解封跑腿员")
    public CommonResult<Boolean> unban(@Valid @RequestBody UnbanReqVO reqVO) {
        boolean result = errandRunnerService.unbanRunner(reqVO.getRunnerId());
        return CommonResult.success(result);
    }

    @GetMapping("/ban-status/{runnerId}")
    @Operation(summary = "查询封禁状态")
    public CommonResult<Integer> getBanStatus(@PathVariable Long runnerId) {
        ErrandRunnerDO runner = errandRunnerService.getById(runnerId);
        if (runner == null) {
            return CommonResult.error(404, "跑腿员不存在");
        }
        return CommonResult.success(runner.getBanStatus());
    }
}
