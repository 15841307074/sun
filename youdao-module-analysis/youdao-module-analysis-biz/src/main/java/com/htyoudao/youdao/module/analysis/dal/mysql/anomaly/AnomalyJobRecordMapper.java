package com.htyoudao.youdao.module.analysis.dal.mysql.anomaly;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.AnomalyJobRecordDO;
import org.apache.ibatis.annotations.Mapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;

import java.time.LocalDate;

/**
 * 定时任务执行记录 Mapper
 */
@Mapper
public interface AnomalyJobRecordMapper extends BaseMapperX<AnomalyJobRecordDO> {

    /**
     * 按任务类型 + 周期查执行记录（唯一，用于重跑时先查后更新）
     *
     * @param jobType     任务类型
     * @param periodStart 周期开始日
     * @return 执行记录，不存在返回 null
     */
    default AnomalyJobRecordDO selectByTypeAndPeriod(Integer jobType, LocalDate periodStart) {
        return selectOne(new LambdaQueryWrapperX<AnomalyJobRecordDO>()
                .eq(AnomalyJobRecordDO::getBusinessId, BusinessContextHolder.getRequiredBusinessId())
                .eq(AnomalyJobRecordDO::getJobType, jobType)           // 按任务类型查
                .eq(AnomalyJobRecordDO::getPeriodStart, periodStart)); // 按周期查
    }
}
