package com.htyoudao.youdao.module.analysis.dal.mysql.anomaly;

import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.GoodsZeroOrderAnomalyMonthlyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 货品 0 订单异常月表 Mapper（只读）
 *
 * <p>职责：负责 {@link GoodsZeroOrderAnomalyMonthlyDO}（货品零订单异常的月度数据）的只读访问。
 * 在「经营异常提醒」功能中，作为上游数据源之一：定时任务按月查询出现零订单异常的门店ID集合，
 * 再结合管理人员-门店快照聚合生成月报告。
 *
 * @see GoodsZeroOrderAnomalyMonthlyDO
 */
@Mapper
public interface GoodsZeroOrderAnomalyMonthlyMapper extends BaseMapperX<GoodsZeroOrderAnomalyMonthlyDO> {

    /**
     * 查某月异常门店ID集合（去重）
     *
     * @param statMonth  统计月份（月1号）
     * @param businessId 项目ID
     * @return 异常 store_id 列表
     */
    @Select("SELECT DISTINCT store_id FROM goods_zero_order_anomaly_monthly " +  // 去重查询该月出现零订单异常的门店ID
            "WHERE stat_month = #{statMonth} AND business_id = #{businessId} AND deleted = 0") // 限定月份+项目+未逻辑删除
    List<Long> selectDistinctStoreIds(@Param("statMonth") LocalDate statMonth,   // 统计月份入参（该月1号）
                                      @Param("businessId") Long businessId);     // 项目ID入参
}
