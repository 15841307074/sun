package com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.analysis.enums.AnomalyJobTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 经营异常提醒-定时任务执行记录 DO
 *
 * <p>对应表 {@code analysis_anomaly_job_record}。
 *
 * <p>记录每个定时任务每个周期的执行结果，后续任务执行前查这张表判断前置任务是否成功。
 * 如果前置任务失败或没跑，当前任务跳过，避免拿着空数据跑出错误结果。
 *
 * <p>重跑幂等：唯一键 uk_type_period(job_type, period_start, business_id)，
 * 同一任务同一周期只有一条记录，重跑时更新状态。
 *
 * @see AnomalyJobTypeEnum
 */
@TableName("analysis_anomaly_job_record")
@Data
@EqualsAndHashCode(callSuper = true)
public class AnomalyJobRecordDO extends BusinessBaseDO {

    /**
     * 主键，MyBatis-Plus ASSIGN_ID 雪花算法生成
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 任务类型
     *
     * @see AnomalyJobTypeEnum
     */
    private Integer jobType;

    /**
     * XXL-Job handler 名称，冗余存一份方便在数据库里直接看是哪个任务
     */
    private String jobHandler;

    /**
     * 周期类型：1=周 2=月
     */
    private Integer periodType;

    /**
     * 周期开始日，和报告表/快照表用的是同一个值
     */
    private LocalDate periodStart;

    /**
     * 执行状态：0=失败 1=成功 2=运行中（快照重建时撤销旧成功标记）
     */
    private Integer jobStatus;

    /**
     * 处理记录数（快照条数/报告人数/推送条数），排查用
     */
    private Integer recordCount;

    /**
     * 失败原因，异常消息截断 512 字符
     */
    private String errorMsg;

    /**
     * 任务开始时间
     */
    private LocalDateTime startTime;

    /**
     * 任务结束时间，成功或失败时写
     */
    private LocalDateTime endTime;
}
