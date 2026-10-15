package com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import com.htyoudao.youdao.module.analysis.enums.AnomalyTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 经营异常提醒 - 人维度报告 DO
 *
 * <p>对应表 {@code analysis_anomaly_user_report}。
 *
 * <p>这是整个需求的核心表。定时任务跑完上游 ClickHouse 数据后，
 * 按「人」聚合出结果：这个人这个周期名下有没有异常门店。
 *
 * <p>颗粒度：一个人 + 一个周期 + 一种异常类型 = 一条记录。
 * 写入规则：<b>有异常才写，没有异常不写</b>（避免给没有异常的人发消息）。
 *
 * @see AnomalyTypeEnum
 */
@TableName("analysis_anomaly_user_report")
@Data
@EqualsAndHashCode(callSuper = true)
public class AnomalyUserReportDO extends BusinessBaseDO {

    /**
     * 主键。不是数据库自增，是 MyBatis-Plus ASSIGN_ID 雪花算法生成，插入时由代码赋值
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接收人用户ID，对应 system_users.id。颗粒度到人，一个人一条
     */
    private Long userId;

    /**
     * 异常类型
     *
     * @see AnomalyTypeEnum
     */
    private Integer anomalyType;

    /**
     * 周期开始日。周度存该周的周一（如 2026-09-14），月度存该月1号（如 2026-08-01）
     */
    private LocalDate periodStart;

    /**
     * 统计门店：这个人管辖的门店总数。
     * 周度 / 月度都取 analysis_user_store_snapshot 的快照数
     */
    private Integer storeCount;

    /**
     * 异常指标：这个人的异常门店一共涉及几个异常指标（0~6）。
     * <b>只有周度（anomaly_type=1）用这个字段</b>，月度记录里固定为 0
     */
    private Integer anomalyMetricCount;

    /**
     * 异常门店：这个人管辖的门店里，有几家存在异常。
     * <b>只有月度（anomaly_type=2/3）用这个字段</b>，周度记录里固定为 0
     */
    private Integer anomalyStoreCount;
}
