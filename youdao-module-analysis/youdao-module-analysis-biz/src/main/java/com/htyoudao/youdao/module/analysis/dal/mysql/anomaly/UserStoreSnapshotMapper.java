package com.htyoudao.youdao.module.analysis.dal.mysql.anomaly;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.UserStoreSnapshotDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * 人×门店快照 Mapper
 *
 * <p>职责：负责 {@link UserStoreSnapshotDO}（管理人员与所辖门店的周期快照）的持久化访问。
 * 在「经营异常提醒」功能中，快照用于确定某个周期内每个管理人员负责哪些门店，
 * 从而把门店维度的异常按管理人员聚合生成报告。提供按周期/用户查询门店列表、
 * 统计门店数，以及重跑前物理删除旧快照的能力。
 *
 * @see UserStoreSnapshotDO
 */
@Mapper
public interface UserStoreSnapshotMapper extends BaseMapperX<UserStoreSnapshotDO> {

    /**
     * 给其他同事的查询函数用：按「周期类型 + 周期 + userIds」查门店列表
     *
     * @param periodType  周期类型（1=周 2=月）
     * @param periodStart 周期开始日
     * @param userIds     用户ID集合
     * @return 命中条件的快照记录列表（即这些用户在该周期内所辖的门店）
     */
    default List<UserStoreSnapshotDO> selectListByPeriodAndUsers(Integer periodType,
                                                                  LocalDate periodStart,
                                                                  Collection<Long> userIds) {
        return selectList(new LambdaQueryWrapperX<UserStoreSnapshotDO>()
                .eq(UserStoreSnapshotDO::getPeriodType, periodType)    // 精确匹配周期类型（1=周 2=月）
                .eq(UserStoreSnapshotDO::getPeriodStart, periodStart)  // 精确匹配周期开始日
                .in(UserStoreSnapshotDO::getUserId, userIds));         // 用户ID落在给定集合内（IN 查询）
    }

    /**
     * 报告任务用：统计某用户在某周期的门店数（store_count）
     *
     * @param periodType  周期类型
     * @param periodStart 周期开始日
     * @param userId      用户ID
     * @return 门店数（该用户在该周期内所辖门店的条数）
     */
    default Long selectCountByPeriodAndUser(Integer periodType, LocalDate periodStart, Long userId) {
        return selectCount(new LambdaQueryWrapperX<UserStoreSnapshotDO>()
                .eq(UserStoreSnapshotDO::getPeriodType, periodType)    // 精确匹配周期类型
                .eq(UserStoreSnapshotDO::getPeriodStart, periodStart)  // 精确匹配周期开始日
                .eq(UserStoreSnapshotDO::getUserId, userId));          // 精确匹配用户ID
    }

    /**
     * 物理删除某周期的全部快照（重跑时先删后插）
     *
     * <p>用 @Delete 原生 SQL 绕过 @TableLogic 逻辑删除，
     * 因为门店会被划走，必须能真正删掉行，否则唯一键会冲突。
     *
     * @param periodType  周期类型
     * @param periodStart 周期开始日
     * @return 删除行数
     */
    @Delete("DELETE FROM analysis_user_store_snapshot " +              // 物理删除快照表（不走逻辑删除）
            "WHERE period_type = #{periodType} AND period_start = #{periodStart}") // 按「周期类型 + 周期开始日」定位待删行
    int physicalDeleteByPeriod(@Param("periodType") Integer periodType,   // 周期类型入参（1=周 2=月）
                               @Param("periodStart") LocalDate periodStart); // 周期开始日入参
}
