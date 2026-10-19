package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.AnomalyJobRecordDO;
import com.htyoudao.youdao.module.analysis.dal.mysql.anomaly.AnomalyJobRecordMapper;
import com.htyoudao.youdao.module.analysis.enums.AnomalyJobTypeEnum;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 无数据库验证多前置全部成功和上游任务记录的持久化契约。 */
class AnomalyJobPredecessorTest {
    private final LocalDate period = LocalDate.of(2026, 10, 1);
    private AnomalyJobRecordService service;
    private AnomalyJobRecordMapper mapper;
    @BeforeEach void setup() {
        BusinessContextHolder.setBusinessId(10L);
        service = new AnomalyJobRecordService();
        mapper = mock(AnomalyJobRecordMapper.class);
        ReflectionTestUtils.setField(service, "recordMapper", mapper);
    }
    @AfterEach void cleanup() { BusinessContextHolder.clear(); }

    @Test void eachAggregationRequiresItsActualPredecessors() {
        Map<AnomalyJobTypeEnum, List<AnomalyJobTypeEnum>> expected = Map.of(
                AnomalyJobTypeEnum.REPORT_WEEK, List.of(AnomalyJobTypeEnum.SNAPSHOT_WEEK, AnomalyJobTypeEnum.SOURCE_WEEK_ANOMALY),
                AnomalyJobTypeEnum.REPORT_MONTH_SALES, List.of(AnomalyJobTypeEnum.SNAPSHOT_MONTH),
                AnomalyJobTypeEnum.REPORT_MONTH_ZERO, List.of(AnomalyJobTypeEnum.SNAPSHOT_MONTH));
        for (var entry : expected.entrySet()) {
            assertEquals(entry.getValue(), entry.getKey().getPredecessors());
            for (var dependency : entry.getValue()) {
                for (Integer status : Arrays.asList(null, 0, 2, 9)) {
                    reset(mapper);
                    for (var required : entry.getValue()) {
                        when(mapper.selectByTypeAndPeriod(required.getType(), period)).thenReturn(record(1));
                    }
                    when(mapper.selectByTypeAndPeriod(dependency.getType(), period))
                            .thenReturn(status == null ? null : record(status));
                    assertFalse(service.isPredecessorSuccess(entry.getKey(), period));
                }
            }
            reset(mapper);
            for (var required : entry.getValue()) {
                when(mapper.selectByTypeAndPeriod(required.getType(), period)).thenReturn(record(1));
            }
            assertTrue(service.isPredecessorSuccess(entry.getKey(), period));
            for (var required : entry.getValue()) verify(mapper).selectByTypeAndPeriod(required.getType(), period);
        }
    }
    @Test void upstreamAndSnapshotsHaveNoPredecessorsWhilePushKeepsItsReportDependency() {
        for (var type : List.of(AnomalyJobTypeEnum.SNAPSHOT_WEEK, AnomalyJobTypeEnum.SNAPSHOT_MONTH,
                AnomalyJobTypeEnum.SOURCE_WEEK_ANOMALY)) {
            assertTrue(type.getPredecessors().isEmpty());
            assertTrue(service.isPredecessorSuccess(type, period));
        }
        verifyNoInteractions(mapper);
        assertEquals(List.of(AnomalyJobTypeEnum.REPORT_WEEK), AnomalyJobTypeEnum.PUSH_WEEK.getPredecessors());
        assertEquals(List.of(AnomalyJobTypeEnum.REPORT_MONTH_SALES), AnomalyJobTypeEnum.PUSH_MONTH_SALES.getPredecessors());
        assertEquals(List.of(AnomalyJobTypeEnum.REPORT_MONTH_ZERO), AnomalyJobTypeEnum.PUSH_MONTH_ZERO.getPredecessors());
    }
    @Test void existingIdentifiersStayStableAndNewIdentifiersAreUnique() {
        List<AnomalyJobTypeEnum> ordered = List.of(AnomalyJobTypeEnum.SNAPSHOT_WEEK, AnomalyJobTypeEnum.SNAPSHOT_MONTH,
                AnomalyJobTypeEnum.REPORT_WEEK, AnomalyJobTypeEnum.REPORT_MONTH_SALES, AnomalyJobTypeEnum.REPORT_MONTH_ZERO,
                AnomalyJobTypeEnum.PUSH_WEEK, AnomalyJobTypeEnum.PUSH_MONTH_SALES, AnomalyJobTypeEnum.PUSH_MONTH_ZERO,
                AnomalyJobTypeEnum.SOURCE_WEEK_ANOMALY);
        List<String> handlers = List.of("analysisUserStoreSnapshotWeekJobHandler", "analysisUserStoreSnapshotMonthJobHandler",
                "analysisWeekAnomalyJobHandler", "analysisMonthProductSalesJobHandler", "analysisMonthZeroOrderJobHandler",
                "analysisWeekAnomalyPushJobHandler", "analysisMonthProductSalesPushJobHandler", "analysisMonthZeroOrderPushJobHandler",
                "analysisComplaintAnomalyWeekJobHandler");
        for (int i = 0; i < ordered.size(); i++) {
            assertEquals(i + 1, ordered.get(i).getType());
            assertEquals(handlers.get(i), ordered.get(i).getHandler());
            assertSame(ordered.get(i), AnomalyJobTypeEnum.valueOfType(i + 1));
        }
        assertEquals(9, Arrays.stream(AnomalyJobTypeEnum.values()).map(AnomalyJobTypeEnum::getHandler).distinct().count());
        assertNull(AnomalyJobTypeEnum.valueOfType(null));
        assertNull(AnomalyJobTypeEnum.valueOfType(10));
        assertThrows(UnsupportedOperationException.class, () -> AnomalyJobTypeEnum.REPORT_WEEK.getPredecessors().clear());
    }
    @Test void upstreamSuccessRecordsCorrectProjectHandlerAndPeriodTypeEvenWithZeroRows() {
        for (var type : List.of(AnomalyJobTypeEnum.SOURCE_WEEK_ANOMALY)) {
            reset(mapper);
            LocalDate start = type.isMonthly() ? period : LocalDate.of(2026, 10, 12);
            service.recordSuccess(type, start, 0, LocalDateTime.now());
            var captor = org.mockito.ArgumentCaptor.forClass(AnomalyJobRecordDO.class);
            verify(mapper).insert(captor.capture());
            var row = captor.getValue();
            assertEquals(10L, row.getBusinessId()); assertEquals(type.getType(), row.getJobType());
            assertEquals(type.getHandler(), row.getJobHandler()); assertEquals(start, row.getPeriodStart());
            assertEquals(1, row.getPeriodType());
            assertEquals(1, row.getJobStatus()); assertEquals(0, row.getRecordCount());
        }
    }
    private AnomalyJobRecordDO record(int status) {
        AnomalyJobRecordDO record = new AnomalyJobRecordDO(); record.setJobStatus(status); return record;
    }
}
