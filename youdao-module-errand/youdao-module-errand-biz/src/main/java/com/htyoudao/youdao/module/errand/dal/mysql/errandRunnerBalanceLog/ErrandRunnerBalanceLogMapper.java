package com.htyoudao.youdao.module.errand.dal.mysql.errandRunnerBalanceLog;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerBalanceLog.ErrandRunnerBalanceLogDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * 跑腿员余额流水Mapper接口
 */
@Mapper
public interface ErrandRunnerBalanceLogMapper extends BaseMapper<ErrandRunnerBalanceLogDO> {

    /**
     * 根据跑腿员ID查询余额流水
     * @param runnerId 跑腿员ID
     * @return 流水列表
     */
    @Select("SELECT * FROM bz_errand_runner_balance_log WHERE runner_id = #{runnerId} ORDER BY create_time DESC")
    List<ErrandRunnerBalanceLogDO> selectByRunnerId(@Param("runnerId") Long runnerId);

    /**
     * 根据业务流水号查询（防重）
     * @param bizNo 业务流水号
     * @return 流水记录
     */
    @Select("SELECT * FROM bz_errand_runner_balance_log WHERE biz_no = #{bizNo} LIMIT 1")
    ErrandRunnerBalanceLogDO selectByBizNo(@Param("bizNo") String bizNo);

    /**
     * 统计跑腿员总收入
     * @param runnerId 跑腿员ID
     * @return 总收入金额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM bz_errand_runner_balance_log " +
            "WHERE runner_id = #{runnerId} AND direction = 1 AND flow_type = 1")
    BigDecimal sumIncomeByRunnerId(@Param("runnerId") Long runnerId);

    /**
     * 统计跑腿员总提现金额
     * @param runnerId 跑腿员ID
     * @return 总提现金额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM bz_errand_runner_balance_log " +
            "WHERE runner_id = #{runnerId} AND direction = 2 AND flow_type = 2")
    BigDecimal sumWithdrawByRunnerId(@Param("runnerId") Long runnerId);
}
