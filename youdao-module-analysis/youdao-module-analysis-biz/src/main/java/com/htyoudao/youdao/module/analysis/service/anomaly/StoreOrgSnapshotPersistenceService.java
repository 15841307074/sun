package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.StoreOrgSnapshotDO;
import com.htyoudao.youdao.module.analysis.dal.mysql.anomaly.StoreOrgSnapshotMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/** 只负责在单个事务内替换同项目同月份的快照。 */
@Service
public class StoreOrgSnapshotPersistenceService {

    @Resource
    private StoreOrgSnapshotMapper snapshotMapper;

    @Transactional(rollbackFor = Exception.class)
    public void replace(Long businessId, LocalDate snapshotMonth, List<StoreOrgSnapshotDO> snapshots) {
        snapshotMapper.physicalDeleteByMonth(businessId, snapshotMonth);
        if (!Boolean.TRUE.equals(snapshotMapper.insertBatch(snapshots, 500))) {
            throw new IllegalStateException("月度门店组织快照批量保存失败");
        }
    }
}
