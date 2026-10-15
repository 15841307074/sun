package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.StoreOrgSnapshotDO;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.StoreOrgSnapshotApi;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgPathNodeDTO;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgSnapshotDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** 月度门店组织快照生成服务。 */
@Service
@Slf4j
public class StoreOrgSnapshotService {

    @DubboReference
    private StoreOrgSnapshotApi storeOrgSnapshotApi;
    @Resource
    private StoreOrgSnapshotPersistenceService persistenceService;
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 先完整读取并校验系统侧数据，再进入替换事务，避免远程异常清空已有快照。
     */
    public int saveMonthlySnapshot(LocalDate snapshotMonth) {
        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        List<StoreOrgSnapshotDTO> source = storeOrgSnapshotApi.listNormalStoreOrgData().getCheckedData();
        if (source == null || source.isEmpty()) {
            throw new IllegalStateException("当前项目没有可用于快照的正常营业门店，保留原快照");
        }

        validateSource(source);
        List<StoreOrgSnapshotDO> snapshots = source.stream()
                .map(item -> toSnapshotDO(businessId, snapshotMonth, item))
                .toList();
        persistenceService.replace(businessId, snapshotMonth, snapshots);
        log.info("[saveMonthlySnapshot] businessId={}, month={}, count={}",
                businessId, snapshotMonth, snapshots.size());
        return snapshots.size();
    }

    private void validateSource(List<StoreOrgSnapshotDTO> source) {
        Set<Long> storeIds = new HashSet<>();
        for (StoreOrgSnapshotDTO item : source) {
            if (item.getStoreId() == null || !storeIds.add(item.getStoreId())) {
                throw new IllegalStateException("门店组织快照源数据存在空门店ID或重复门店ID");
            }
            if (item.getOrgId() == null) {
                continue;
            }
            List<StoreOrgPathNodeDTO> path = item.getOrgPath();
            if (path == null || path.isEmpty()
                    || !Objects.equals(path.get(path.size() - 1).getId(), item.getOrgId())) {
                throw new IllegalStateException("门店 " + item.getStoreId() + " 的组织路径不完整");
            }
        }
    }

    private StoreOrgSnapshotDO toSnapshotDO(Long businessId, LocalDate snapshotMonth,
                                             StoreOrgSnapshotDTO source) {
        StoreOrgSnapshotDO snapshot = new StoreOrgSnapshotDO();
        snapshot.setBusinessId(businessId);
        snapshot.setSnapshotMonth(snapshotMonth);
        snapshot.setStoreId(source.getStoreId());
        snapshot.setStoreName(source.getStoreName());
        snapshot.setStoreProvince(source.getStoreProvince());
        snapshot.setOrgId(source.getOrgId());
        snapshot.setOrgName(source.getOrgName());
        snapshot.setParentOrgId(source.getParentOrgId());
        snapshot.setParentOrgName(source.getParentOrgName());
        snapshot.setOrgPathJson(writePath(source.getOrgPath()));
        snapshot.setDeleted(false);
        return snapshot;
    }

    private String writePath(List<StoreOrgPathNodeDTO> path) {
        try {
            return objectMapper.writeValueAsString(path == null ? List.of() : path);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("门店组织路径序列化失败", e);
        }
    }
}
