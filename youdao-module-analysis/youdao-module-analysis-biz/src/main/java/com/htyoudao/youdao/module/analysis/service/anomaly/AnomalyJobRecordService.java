package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.AnomalyJobRecordDO;
import com.htyoudao.youdao.module.analysis.dal.mysql.anomaly.AnomalyJobRecordMapper;
import com.htyoudao.youdao.module.analysis.enums.AnomalyJobTypeEnum;
import com.htyoudao.youdao.module.analysis.enums.PeriodTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 定时任务执行记录服务
 *
 * <p>两个职责：
 * <ol>
 *   <li>{@link #isPredecessorSuccess} — 任务执行前检查前置任务是否成功</li>
 *   <li>{@link #recordSuccess} / {@link #recordFailure} — 任务执行后记录结果</li>
 * </ol>
 *
 * <p>重跑幂等：同一任务同一周期只有一条记录，重跑时更新状态（先查后插/更新）。
 */
@Service
@Slf4j
public class AnomalyJobRecordService {

    /** 任务成功状态 */
    private static final int STATUS_SUCCESS = 1;
    /** 任务失败状态 */
    private static final int STATUS_FAILURE = 0;
    /** error_msg 最大长度 */
    private static final int MAX_ERROR_MSG_LENGTH = 512;

    @Resource
    private AnomalyJobRecordMapper recordMapper;

    /**
     * 检查前置任务是否成功
     *
     * <p>根据枚举里定义的 predecessors 依赖关系，检查全部前置任务的执行记录。
     * 无前置依赖（快照、上游数据任务）时直接返回 true。
     *
     * @param jobType     当前任务类型
     * @param periodStart 周期开始日
     * @return true=前置任务已成功可以继续，false=前置任务未成功或没跑
     */
    public boolean isPredecessorSuccess(AnomalyJobTypeEnum jobType, LocalDate periodStart) {
        // 所有依赖按同一项目、同一周期逐项检查，缺记录、运行中、失败都不能放行。
        for (AnomalyJobTypeEnum predecessor : jobType.getPredecessors()) {
            AnomalyJobRecordDO record = recordMapper.selectByTypeAndPeriod(predecessor.getType(), periodStart);
            if (record == null) {
                log.warn("[isPredecessorSuccess] 当前任务 {} 前置任务 {} 周期 {} 无执行记录",
                        jobType.getDescription(), predecessor.getDescription(), periodStart);
                return false;
            }
            if (!Integer.valueOf(STATUS_SUCCESS).equals(record.getJobStatus())) {
                log.warn("[isPredecessorSuccess] 当前任务 {} 前置任务 {} 周期 {} 未成功，status={} errorMsg={}",
                        jobType.getDescription(), predecessor.getDescription(), periodStart,
                        record.getJobStatus(), record.getErrorMsg());
                return false;
            }
        }
        return true;
    }

    /**
     * 记录任务执行成功
     *
     * @param jobType     任务类型
     * @param periodStart 周期开始日
     * @param recordCount 处理记录数
     * @param startTime   任务开始时间
     */
    public void recordSuccess(AnomalyJobTypeEnum jobType, LocalDate periodStart,
                               int recordCount, LocalDateTime startTime) {
        recordResult(jobType, periodStart, STATUS_SUCCESS, recordCount, null, startTime);
    }

    /**
     * 记录任务执行失败
     *
     * @param jobType     任务类型
     * @param periodStart 周期开始日
     * @param errorMsg    失败原因
     * @param startTime   任务开始时间
     */
    public void recordFailure(AnomalyJobTypeEnum jobType, LocalDate periodStart,
                               String errorMsg, LocalDateTime startTime) {
        // 截断超长错误信息
        if (errorMsg != null && errorMsg.length() > MAX_ERROR_MSG_LENGTH) {
            errorMsg = errorMsg.substring(0, MAX_ERROR_MSG_LENGTH);
        }
        recordResult(jobType, periodStart, STATUS_FAILURE, 0, errorMsg, startTime);
    }

    /** 任务开始重建即撤销上次成功标记；上游任务同样可调用，异常退出后不沿用旧成功。 */
    public void recordRunning(AnomalyJobTypeEnum jobType, LocalDate periodStart, LocalDateTime startTime) {
        recordResult(jobType, periodStart, 2, 0, null, startTime);
    }

    /**
     * 记录执行结果（有则更新，无则插入）
     */
    private void recordResult(AnomalyJobTypeEnum jobType, LocalDate periodStart,
                               int status, int recordCount, String errorMsg,
                               LocalDateTime startTime) {
        LocalDateTime endTime = status == 2 ? null : LocalDateTime.now();

        // 先查是否已有记录（重跑幂等）
        AnomalyJobRecordDO existing = recordMapper.selectByTypeAndPeriod(jobType.getType(), periodStart);
        if (existing != null) {
            // 显式set允许成功重跑时清空旧错误，运行中清空上次结束时间。
            recordMapper.update(null, new LambdaUpdateWrapper<AnomalyJobRecordDO>()
                    .eq(AnomalyJobRecordDO::getId, existing.getId())
                    .eq(AnomalyJobRecordDO::getBusinessId, BusinessContextHolder.getRequiredBusinessId())
                    .set(AnomalyJobRecordDO::getJobStatus, status)
                    .set(AnomalyJobRecordDO::getRecordCount, recordCount)
                    .set(AnomalyJobRecordDO::getJobHandler, jobType.getHandler())
                    .set(AnomalyJobRecordDO::getErrorMsg, errorMsg)
                    .set(AnomalyJobRecordDO::getStartTime, startTime)
                    .set(AnomalyJobRecordDO::getEndTime, endTime));
        } else {
            // 首次：插入新记录
            AnomalyJobRecordDO record = new AnomalyJobRecordDO();
            record.setBusinessId(BusinessContextHolder.getRequiredBusinessId());
            record.setJobType(jobType.getType());    // 任务类型
            record.setJobHandler(jobType.getHandler()); // handler 名称（冗余）
            record.setPeriodType(jobType.isMonthly()
                    ? PeriodTypeEnum.MONTH.getType()
                    : PeriodTypeEnum.WEEK.getType()); // 周期类型
            record.setPeriodStart(periodStart);      // 周期开始日
            record.setJobStatus(status);             // 执行状态
            record.setRecordCount(recordCount);      // 处理记录数
            record.setErrorMsg(errorMsg);            // 失败原因
            record.setStartTime(startTime);          // 开始时间
            record.setEndTime(endTime);              // 结束时间
            recordMapper.insert(record);
        }
        log.info("[recordResult] {} 周期 {} 状态={} 记录数={}",
                jobType.getDescription(), periodStart, status == 2 ? "运行中" : status == STATUS_SUCCESS ? "成功" : "失败", recordCount);
    }
}
