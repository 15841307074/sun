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

    /** 参数示例：{"businessId":10}；历史补跑可增加 periodStart。 */
    @XxlJob("analysisComplaintAnomalyWeekJobHandler")
    public void calculateWeek() {
        execute(XxlJobHelper.getJobParam(), LocalDate.now());
    }

    void execute(String rawParam, LocalDate today) {
        try {
            JobParam param = parseParam(rawParam, today);
            BusinessContextHolder.setBusinessId(param.businessId());
            int count = clickHouseService.calculateAndSaveWeek(param.businessId(), param.periodStart());
            log.info("[calculateWeek] businessId={}, periodStart={}, versionRows={}",
                    param.businessId(), param.periodStart(), count);
            XxlJobHelper.log("周度投诉异常指标计算完成: businessId=" + param.businessId()
                    + ", periodStart=" + param.periodStart() + ", versionRows=" + count);
        } finally {
            BusinessContextHolder.clear();
        }
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
            LocalDate periodStart = periodNode == null || periodNode.asText().isBlank()
                    ? today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1)
                    : LocalDate.parse(periodNode.asText());
            if (periodStart.getDayOfWeek() != DayOfWeek.MONDAY) {
                throw new IllegalArgumentException("任务参数 periodStart 必须是周一");
            }
            return new JobParam(businessId, periodStart);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("任务参数 periodStart 格式错误，应为 yyyy-MM-dd", e);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "任务参数格式错误，应为 JSON，例如 {\"businessId\":10,\"periodStart\":\"2026-10-05\"}", e);
        }
    }

    record JobParam(Long businessId, LocalDate periodStart) {
    }
}
