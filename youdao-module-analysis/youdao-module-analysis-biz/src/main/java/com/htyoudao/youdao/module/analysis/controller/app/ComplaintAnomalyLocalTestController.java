package com.htyoudao.youdao.module.analysis.controller.app;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.analysis.job.ComplaintAnomalyWeekJob;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/** 仅供本地环境手动验证周度投诉异常计算任务。 */
@Tag(name = "本地测试 - 经营异常")
@Profile({"local", "dev-local"})
@RestController
@RequestMapping("/analysis/local-test")
public class ComplaintAnomalyLocalTestController {

    @Resource
    private ComplaintAnomalyWeekJob complaintAnomalyWeekJob;

    /**
     * 手动执行指定项目、指定自然周的投诉异常指标计算。
     *
     * @return 写入或更新 MySQL 周异常表的行数
     */
    @PostMapping("/complaint-anomaly-week")
    @Operation(summary = "本地执行周度投诉异常指标任务")
    @PermitAll
    public CommonResult<Integer> runComplaintAnomalyWeek(
            @Parameter(description = "项目ID", required = true, example = "10")
            @RequestParam("businessId") Long businessId,
            @Parameter(description = "统计周的周一，格式 yyyy-MM-dd", required = true, example = "2026-10-05")
            @RequestParam("periodStart")
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate periodStart) {
        return success(complaintAnomalyWeekJob.execute(businessId, periodStart));
    }
}
