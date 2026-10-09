package com.htyoudao.youdao.module.errand.service.errandRunnerApplyChangeLog;


import com.baomidou.mybatisplus.extension.service.IService;

import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerApplyChangeLog.ErrandRunnerApplyChangeLogDO;

import java.util.List;


/**
 * 跑骑手入驻申请资料变更记录
 */
public interface ErrandRunnerApplyChangeLogService extends IService<ErrandRunnerApplyChangeLogDO> {


    /**
     * 根据跑腿员ID获取最新的变更记录批次号
     *
     * @param runnerId 跑腿员ID
     * @return 最新的批次号
     */
    String getLatestBatchNoByRunnerId(Long runnerId);

    /**
     * 根据批次号获取变更记录
     *
     * @param batchNo 批次号
     * @return 变更记录列表
     */
    List<ErrandRunnerApplyChangeLogDO> getByBatchNo(String batchNo);

    /**
     * 批量更新变更记录审核状态
     *
     * @param batchNo 批次号
     * @param auditStatus 审核状态
     * @param failReason 失败原因
     */
    void batchUpdateAuditStatus(String batchNo, Integer auditStatus);

}
