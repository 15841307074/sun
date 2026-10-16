package com.htyoudao.youdao.module.analysis.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.service.anomaly.ComplaintAnomalyClickHouseService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;

/** 周度投诉异常指标计算任务。 */
@Component
@Slf4j
public class ComplaintAnomalyWeekJob {

    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private ComplaintAnomalyClickHouseService clickHouseService;

    /**
     * 参数示例：{"businessId":10}。
     * periodStart 表示任务执行周的周一，实际统计它前一个完整自然周。
     */
    @XxlJob("analysisComplaintAnomalyWeekJobHandler")
    public void calculateWeek() {
        execute(XxlJobHelper.getJobParam(), LocalDate.now());
    }

    int execute(String rawParam, LocalDate today) {
        JobParam param = parseParam(rawParam, today);
        return execute(param.businessId(), param.periodStart());
    }

    /**
     * 执行指定项目、指定自然周的投诉异常指标计算。
     *
     * <p>供 XXL-Job 和本地测试接口共同复用，执行结束后始终清理项目上下文。</p>
     *
     * @return 本次写入或更新 MySQL 周异常表的行数
     */
    public int execute(Long businessId, LocalDate periodStart) {
        if (businessId == null || businessId <= 0) {
            throw new IllegalArgumentException("businessId 必须大于 0");
        }
        if (periodStart == null) {
            throw new IllegalArgumentException("periodStart 不能为空");
        }
        if (periodStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("periodStart 必须是周一");
        }
        try {
            BusinessContextHolder.setBusinessId(businessId);
            int count = clickHouseService.calculateAndSaveWeek(businessId, periodStart);
            log.info("[calculateWeek] businessId={}, periodStart={}, affectedRows={}",
                    businessId, periodStart, count);
            XxlJobHelper.log("周度投诉异常指标计算完成: businessId=" + businessId
                    + ", periodStart=" + periodStart + ", affectedRows=" + count);
            return count;
        } finally {
            BusinessContextHolder.clear();
        }
    }

    /** 按正式调度口径计算上一个完整自然周。 */
    public int executePreviousCompleteWeek(Long businessId) {
        return execute(businessId, previousCompleteWeekStart(LocalDate.now()));
    }

    /** 按指定执行周的周一，计算它前面的完整自然周。 */
    public int executePreviousCompleteWeek(Long businessId, LocalDate executionWeekStart) {
        if (executionWeekStart == null || executionWeekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("执行周开始日期必须是周一");
        }
        return execute(businessId, executionWeekStart.minusWeeks(1));
    }

    JobParam parseParam(String rawParam, LocalDate today) {
        if (rawParam == null || rawParam.isBlank()) {
            throw new IllegalArgumentException("任务参数不能为空，必须传 businessId");
        }
        try {
            JsonNode root = objectMapper.readTree(rawParam.trim());
            JsonNode businessIdNode = root.get("businessId");
            if (businessIdNode == null || businessIdNode.asText().isBlank()) {
                throw new IllegalArgumentException("任务参数 businessId 必传");
            }
            long businessId = Long.parseLong(businessIdNode.asText());
            if (businessId <= 0) {
                throw new IllegalArgumentException("任务参数 businessId 必须大于 0");
            }
            JsonNode periodNode = root.get("periodStart");
            LocalDate periodStart;
            if (periodNode == null || periodNode.asText().isBlank()) {
                periodStart = previousCompleteWeekStart(today);
            } else {
                LocalDate executionWeekStart = LocalDate.parse(periodNode.asText());
                if (executionWeekStart.getDayOfWeek() != DayOfWeek.MONDAY) {
                    throw new IllegalArgumentException("任务参数 periodStart 必须是执行周的周一");
                }
                periodStart = executionWeekStart.minusWeeks(1);
            }
            return new JobParam(businessId, periodStart);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("任务参数 periodStart 格式错误，应为 yyyy-MM-dd", e);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "任务参数格式错误，应为 JSON，例如 {\"businessId\":10,\"periodStart\":\"2026-10-12\"}", e);
        }
    }

    private LocalDate previousCompleteWeekStart(LocalDate today) {
        return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
    }

    record JobParam(Long businessId, LocalDate periodStart) {
    }
}
