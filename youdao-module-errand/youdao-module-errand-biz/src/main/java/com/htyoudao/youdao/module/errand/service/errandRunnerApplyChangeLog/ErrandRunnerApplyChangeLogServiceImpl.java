package com.htyoudao.youdao.module.errand.service.errandRunnerApplyChangeLog;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htyoudao.youdao.module.errand.dal.dataobject.errandRunnerApplyChangeLog.ErrandRunnerApplyChangeLogDO;
import com.htyoudao.youdao.module.errand.dal.mysql.errandRunnerApplyChangeLog.ErrandRunnerApplyChangeLogMapper;
import com.htyoudao.youdao.module.errand.enums.errandRunner.ErrandRunnerAuditStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 跑骑手入驻申请资料变更记录实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class ErrandRunnerApplyChangeLogServiceImpl
        extends ServiceImpl<ErrandRunnerApplyChangeLogMapper, ErrandRunnerApplyChangeLogDO>
        implements ErrandRunnerApplyChangeLogService {



    private final ErrandRunnerApplyChangeLogMapper changeLogMapper;

    @Override
    public String getLatestBatchNoByRunnerId(Long runnerId) {
        LambdaQueryWrapper<ErrandRunnerApplyChangeLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerApplyChangeLogDO::getRunnerId, runnerId)
                .eq(ErrandRunnerApplyChangeLogDO::getRunnerId, runnerId)
                .orderByDesc(ErrandRunnerApplyChangeLogDO::getCreateTime)
                .last("LIMIT 1");

        ErrandRunnerApplyChangeLogDO latestLog = changeLogMapper.selectOne(wrapper);
        if (latestLog == null) {
            return null;
        }
        return latestLog.getBatchNo();
    }

    @Override
    public List<ErrandRunnerApplyChangeLogDO> getByBatchNo(String batchNo) {
        LambdaQueryWrapper<ErrandRunnerApplyChangeLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerApplyChangeLogDO::getBatchNo, batchNo)
                .orderByAsc(ErrandRunnerApplyChangeLogDO::getCreateTime);
        return changeLogMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateAuditStatus(String batchNo, Integer auditStatus) {
        LambdaQueryWrapper<ErrandRunnerApplyChangeLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrandRunnerApplyChangeLogDO::getBatchNo, batchNo)
                .eq(ErrandRunnerApplyChangeLogDO::getAuditStatus, ErrandRunnerAuditStatusEnum.PENDING.getCode());

        List<ErrandRunnerApplyChangeLogDO> changeLogs = changeLogMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(changeLogs)) {
            log.warn("没有待审核的变更记录: batchNo={}", batchNo);
            return;
        }

        for (ErrandRunnerApplyChangeLogDO changeLog : changeLogs) {
            changeLog.setAuditStatus(auditStatus);
            changeLog.setUpdateTime(LocalDateTime.now());
            changeLogMapper.updateById(changeLog);
        }

        log.info("批量更新变更记录审核状态完成: batchNo={}, auditStatus={}, 数量={}",
                batchNo, auditStatus, changeLogs.size());
    }
}
