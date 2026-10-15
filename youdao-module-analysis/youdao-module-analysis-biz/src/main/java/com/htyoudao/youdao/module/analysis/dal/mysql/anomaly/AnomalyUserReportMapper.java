package com.htyoudao.youdao.module.analysis.dal.mysql.anomaly;

import com.htyoudao.youdao.framework.common.pojo.PageParam;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.AnomalyUserReportDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 经营异常报告 Mapper
 */
@Mapper
public interface AnomalyUserReportMapper extends BaseMapperX<AnomalyUserReportDO> {

    /**
     * App 接口「经营异常消息查询」分页：查自己的报告，按周期倒序
     *
     * @param pageParam    分页参数
     * @param userId       当前登录用户ID（只查自己的）
     * @param anomalyType  异常类型（可选筛选，null = 全部类型）
     */
    default PageResult<AnomalyUserReportDO> selectPage(PageParam pageParam, Long userId, Integer anomalyType) {
        return selectPage(pageParam, new LambdaQueryWrapperX<AnomalyUserReportDO>()
                .eq(AnomalyUserReportDO::getUserId, userId)
                .eqIfPresent(AnomalyUserReportDO::getAnomalyType, anomalyType)
                .orderByDesc(AnomalyUserReportDO::getPeriodStart));
    }

    /**
     * 推送任务用：取某个周期 + 某种异常类型的全部报告记录
     *
     * @param anomalyType 异常类型
     * @param periodStart 周期开始日
     */
    default List<AnomalyUserReportDO> selectListByPeriod(Integer anomalyType, LocalDate periodStart) {
        return selectList(new LambdaQueryWrapperX<AnomalyUserReportDO>()
                .eq(AnomalyUserReportDO::getAnomalyType, anomalyType)
                .eq(AnomalyUserReportDO::getPeriodStart, periodStart));
    }
}
