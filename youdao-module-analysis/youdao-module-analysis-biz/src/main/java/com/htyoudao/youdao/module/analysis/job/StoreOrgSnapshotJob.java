package com.htyoudao.youdao.module.analysis.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.service.anomaly.StoreOrgSnapshotService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** 月度门店组织快照任务。 */
@Component
@Slf4j
public class StoreOrgSnapshotJob {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM");

    @Resource
    private StoreOrgSnapshotService snapshotService;
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 参数示例：{"businessId":10}；历史补跑：{"businessId":10,"month":"2026-08"}。
     */
    @XxlJob("analysisStoreOrgSnapshotMonthJobHandler")
    public void snapshotMonth() {
        try {
            JobParam param = parseParam(XxlJobHelper.getJobParam());
            BusinessContextHolder.setBusinessId(param.businessId());
            int count = snapshotService.saveMonthlySnapshot(param.snapshotMonth());
            log.info("[snapshotMonth] businessId={}, month={}, count={}",
                    param.businessId(), param.snapshotMonth(), count);
            XxlJobHelper.log("月度门店组织快照完成: businessId=" + param.businessId()
                    + ", month=" + param.snapshotMonth() + ", count=" + count);
        } finally {
            BusinessContextHolder.clear();
        }
    }

    private JobParam parseParam(String rawParam) {
        if (rawParam == null || rawParam.isBlank()) {
            throw new IllegalArgumentException("任务参数不能为空，必须传 businessId");
        }
        try {
            JsonNode root = objectMapper.readTree(rawParam.trim());
            JsonNode businessIdNode = root.get("businessId");
            if (businessIdNode == null || businessIdNode.asText().isBlank()) {
                throw new IllegalArgumentException("任务参数 businessId 必传");
            }
            Long businessId = Long.valueOf(businessIdNode.asText());
            if (businessId <= 0) {
                throw new IllegalArgumentException("任务参数 businessId 必须大于 0");
            }

            JsonNode monthNode = root.get("month");
            LocalDate snapshotMonth = monthNode == null || monthNode.asText().isBlank()
                    ? YearMonth.now().minusMonths(1).atDay(1)
                    : parseMonth(monthNode.asText());
            return new JobParam(businessId, snapshotMonth);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("任务参数格式错误，应为 JSON，例如 {\"businessId\":10}", e);
        }
    }

    private LocalDate parseMonth(String month) {
        try {
            return YearMonth.parse(month, MONTH_FORMATTER).atDay(1);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("任务参数 month 格式错误，应为 yyyy-MM", e);
        }
    }

    private record JobParam(Long businessId, LocalDate snapshotMonth) {
    }
}
