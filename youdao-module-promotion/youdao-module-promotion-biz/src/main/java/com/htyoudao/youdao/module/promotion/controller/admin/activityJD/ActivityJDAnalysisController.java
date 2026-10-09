package com.htyoudao.youdao.module.promotion.controller.admin.activityJD;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.*;
import com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo.LotteryLogEventReqVO;
import com.htyoudao.youdao.module.promotion.service.activityJD.ActivityJDAnalysisService;
import com.htyoudao.youdao.module.promotion.service.activityJD.ActivityJDService;
import com.htyoudao.youdao.module.promotion.service.job.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@Tag(name = "后台pc - 集点活动-数据分析")
@RestController
@RequestMapping("/promotion/analysis/activity-jd")
@RequiredArgsConstructor
public class ActivityJDAnalysisController {

    @Resource
    private JobService jobService;


    @Resource
    private ActivityJDAnalysisService activityJDAnalysisService;


    @GetMapping("/getLotteryLogAnalysis/{id}")
    @Operation(summary = "获取集点活动数据概览")
    public CommonResult<Map<String, Object>> getLotteryLogAnalysis(@PathVariable("id") String id) {
        Map<String, Object> result = activityJDAnalysisService.getLotteryLogAnalysis(id);
        return CommonResult.success(result);
    }

    @PostMapping ("/getLotteryLogDailyAnalysis")
    @Operation(summary = "获取集点活动数据分析")
    public CommonResult<Map<String, Object>> getLotteryLogDailyAnalysis(@RequestBody @Valid ActivityJDEventReqVO activityJDEventReqVO) {
        Map<String, Object> result = activityJDAnalysisService.getLotteryLogDailyAnalysis(activityJDEventReqVO);
        return CommonResult.success(result);
    }


    @PermitAll
    @GetMapping("/resetPoints")
    @Operation(summary = "定时任务重置集点")
    public CommonResult<Boolean> resetPoints() {
        jobService.resetPoint();
        return CommonResult.success(true);
    }




}
