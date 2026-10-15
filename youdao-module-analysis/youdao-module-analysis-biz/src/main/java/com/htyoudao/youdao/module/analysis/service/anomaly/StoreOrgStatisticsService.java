package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.StoreOrgStatisticsItemRespVO;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.StoreOrgStatisticsRespVO;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.StoreOrgSnapshotDO;
import com.htyoudao.youdao.module.analysis.dal.mysql.anomaly.StoreOrgSnapshotMapper;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.StoreOrgSnapshotApi;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgPathNodeDTO;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants.*;

/** 月度门店组织统计服务。 */
@Service
public class StoreOrgStatisticsService {

    private static final String UNKNOWN_PROVINCE = "未设置";
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM");
    private static final TypeReference<List<StoreOrgPathNodeDTO>> PATH_TYPE = new TypeReference<>() {
    };

    @Resource
    private StoreOrgSnapshotMapper snapshotMapper;
    @Resource
    private ObjectMapper objectMapper;
    @DubboReference
    private StoreOrgSnapshotApi storeOrgSnapshotApi;

    public StoreOrgStatisticsRespVO getStatistics(Long userId, Long orgId, String month) {
        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        LocalDate snapshotMonth = parseMonth(month);
        Set<Long> manageableOrgIds = storeOrgSnapshotApi.listManageableOrgIds(userId).getCheckedData();
        if (manageableOrgIds == null || !manageableOrgIds.contains(orgId)) {
            throw exception(STORE_ORG_PERMISSION_DENIED);
        }

        List<StoreOrgSnapshotDO> snapshots = snapshotMapper.selectListByMonth(businessId, snapshotMonth);
        if (snapshots.isEmpty()) {
            throw exception(STORE_ORG_SNAPSHOT_NOT_FOUND);
        }

        Map<GroupProvinceKey, Long> counts = new HashMap<>();
        Map<Long, StoreOrgPathNodeDTO> groupNodes = new HashMap<>();
        String selectedOrgName = null;
        long total = 0L;
        for (StoreOrgSnapshotDO snapshot : snapshots) {
            List<StoreOrgPathNodeDTO> path = readPath(snapshot.getOrgPathJson());
            int selectedIndex = indexOf(path, orgId);
            if (selectedIndex < 0) {
                continue;
            }
            StoreOrgPathNodeDTO selectedNode = path.get(selectedIndex);
            if (selectedOrgName == null) {
                selectedOrgName = selectedNode.getName();
            }
            StoreOrgPathNodeDTO groupNode = selectedIndex + 1 < path.size()
                    ? path.get(selectedIndex + 1) : selectedNode;
            String province = normalizeProvince(snapshot.getStoreProvince());
            GroupProvinceKey key = new GroupProvinceKey(groupNode.getId(), province);
            counts.merge(key, 1L, Long::sum);
            groupNodes.putIfAbsent(groupNode.getId(), groupNode);
            total++;
        }

        List<StoreOrgStatisticsItemRespVO> items = counts.entrySet().stream()
                .sorted(groupingComparator(groupNodes))
                .map(entry -> toItem(entry, groupNodes))
                .toList();

        StoreOrgStatisticsRespVO response = new StoreOrgStatisticsRespVO();
        response.setMonth(month);
        response.setOrgId(orgId.toString());
        response.setOrgName(selectedOrgName);
        response.setTotalStoreCount(total);
        response.setItems(items);
        return response;
    }

    private LocalDate parseMonth(String month) {
        try {
            return YearMonth.parse(month, MONTH_FORMATTER).atDay(1);
        } catch (DateTimeParseException | NullPointerException e) {
            throw exception(STORE_ORG_MONTH_INVALID);
        }
    }

    private List<StoreOrgPathNodeDTO> readPath(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, PATH_TYPE);
        } catch (JsonProcessingException e) {
            throw exception(STORE_ORG_SNAPSHOT_DATA_INVALID);
        }
    }

    private int indexOf(List<StoreOrgPathNodeDTO> path, Long orgId) {
        for (int i = 0; i < path.size(); i++) {
            if (Objects.equals(path.get(i).getId(), orgId)) {
                return i;
            }
        }
        return -1;
    }

    private String normalizeProvince(String province) {
        return province == null || province.isBlank() ? UNKNOWN_PROVINCE : province.trim();
    }

    private Comparator<Map.Entry<GroupProvinceKey, Long>> groupingComparator(
            Map<Long, StoreOrgPathNodeDTO> groupNodes) {
        return Comparator
                .comparing((Map.Entry<GroupProvinceKey, Long> entry) -> groupNodes.get(entry.getKey().orgId()).getSort(),
                        Comparator.nullsLast(Integer::compareTo))
                .thenComparing(entry -> groupNodes.get(entry.getKey().orgId()).getLevel(),
                        Comparator.nullsLast(Integer::compareTo))
                .thenComparing(entry -> entry.getKey().orgId(), Comparator.nullsLast(Long::compareTo))
                .thenComparing(entry -> entry.getKey().province());
    }

    private StoreOrgStatisticsItemRespVO toItem(Map.Entry<GroupProvinceKey, Long> entry,
                                                 Map<Long, StoreOrgPathNodeDTO> groupNodes) {
        StoreOrgPathNodeDTO group = groupNodes.get(entry.getKey().orgId());
        StoreOrgStatisticsItemRespVO item = new StoreOrgStatisticsItemRespVO();
        item.setGroupOrgId(group.getId().toString());
        item.setGroupOrgName(group.getName());
        item.setProvinceName(entry.getKey().province());
        item.setStoreCount(entry.getValue());
        return item;
    }

    private record GroupProvinceKey(Long orgId, String province) {
    }
}
