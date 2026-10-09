package com.htyoudao.youdao.module.errand.dal.mysql.errandRunnerWithdraw;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerWithdraw.ErrandRunnerWithdrawDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * 跑腿员提现Mapper接口
 */
@Mapper
public interface ErrandRunnerWithdrawMapper extends BaseMapper<ErrandRunnerWithdrawDO> {

    /**
     * 根据提现单号查询
     * @param withdrawSn 提现单号
     * @return 提现记录
     */
    @Select("SELECT * FROM bz_errand_runner_withdraw WHERE withdraw_sn = #{withdrawSn} LIMIT 1")
    ErrandRunnerWithdrawDO selectByWithdrawSn(@Param("withdrawSn") String withdrawSn);

    /**
     * 根据跑腿员ID查询提现记录
     * @param runnerId 跑腿员ID
     * @return 提现记录列表
     */
    @Select("SELECT * FROM bz_errand_runner_withdraw WHERE runner_id = #{runnerId} ORDER BY create_time DESC")
    List<ErrandRunnerWithdrawDO> selectByRunnerId(@Param("runnerId") Long runnerId);

    /**
     * 统计跑腿员总提现金额（成功状态）
     * @param runnerId 跑腿员ID
     * @return 总提现金额
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM bz_errand_runner_withdraw " +
            "WHERE runner_id = #{runnerId} AND status = 2")
    BigDecimal sumSuccessAmountByRunnerId(@Param("runnerId") Long runnerId);

    /**
     * 统计跑腿员总手续费（成功状态）
     * @param runnerId 跑腿员ID
     * @return 总手续费
     */
    @Select("SELECT COALESCE(SUM(service_fee), 0) FROM bz_errand_runner_withdraw " +
            "WHERE runner_id = #{runnerId} AND status = 2")
    BigDecimal sumServiceFeeByRunnerId(@Param("runnerId") Long runnerId);

    /**
     * 更新提现状态为成功
     * @param id 提现记录ID
     * @param wechatBatchNo 微信批次号
     * @param wechatDetailNo 微信明细号
     * @return 更新结果
     */
    @Update("UPDATE bz_errand_runner_withdraw SET status = 2, wechat_batch_no = #{wechatBatchNo}, " +
            "wechat_detail_no = #{wechatDetailNo}, finish_time = NOW(), update_time = NOW() WHERE id = #{id}")
    int updateToSuccess(@Param("id") Long id,
                        @Param("wechatBatchNo") String wechatBatchNo,
                        @Param("wechatDetailNo") String wechatDetailNo);

    /**
     * 更新提现状态为失败
     * @param id 提现记录ID
     * @param failReason 失败原因
     * @return 更新结果
     */
    @Update("UPDATE bz_errand_runner_withdraw SET status = 3, fail_reason = #{failReason}, " +
            "finish_time = NOW(), update_time = NOW() WHERE id = #{id}")
    int updateToFail(@Param("id") Long id, @Param("failReason") String failReason);
}
