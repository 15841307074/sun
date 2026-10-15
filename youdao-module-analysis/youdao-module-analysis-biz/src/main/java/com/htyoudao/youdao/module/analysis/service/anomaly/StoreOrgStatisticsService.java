package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.StoreOrgStatisticsNodeRespVO;
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

        Map<Long, NodeAccumulator> rootNodes = new HashMap<>();
        String selectedOrgName = null;
        long total = 0L;
        long direct = 0L;
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
            total++;
            if (selectedIndex == path.size() - 1) {
                direct++;
                continue;
            }
            Map<Long, NodeAccumulator> children = rootNodes;
            for (int i = selectedIndex + 1; i < path.size(); i++) {
                StoreOrgPathNodeDTO node = path.get(i);
                NodeAccumulator accumulator = children.computeIfAbsent(node.getId(),
                        ignored -> new NodeAccumulator(node));
                accumulator.storeCount++;
                if (i == path.size() - 1) {
                    accumulator.directStoreCount++;
                }
                children = accumulator.children;
            }
        }

        List<StoreOrgStatisticsNodeRespVO> items = rootNodes.values().stream()
                .sorted(nodeComparator())
                .map(this::toNode)
                .toList();

        StoreOrgStatisticsRespVO response = new StoreOrgStatisticsRespVO();
        response.setMonth(month);
        response.setOrgId(orgId.toString());
        response.setOrgName(selectedOrgName);
        response.setTotalStoreCount(total);
        response.setDirectStoreCount(direct);
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

    private Comparator<NodeAccumulator> nodeComparator() {
        return Comparator
                .comparing((NodeAccumulator accumulator) -> accumulator.node.getSort(),
                        Comparator.nullsLast(Integer::compareTo))
                .thenComparing(accumulator -> accumulator.node.getLevel(),
                        Comparator.nullsLast(Integer::compareTo))
                .thenComparing(accumulator -> accumulator.node.getId(), Comparator.nullsLast(Long::compareTo));
    }

    private StoreOrgStatisticsNodeRespVO toNode(NodeAccumulator accumulator) {
        StoreOrgStatisticsNodeRespVO node = new StoreOrgStatisticsNodeRespVO();
        node.setOrgId(accumulator.node.getId().toString());
        node.setOrgName(accumulator.node.getName());
        node.setLevel(accumulator.node.getLevel());
        node.setDirectStoreCount(accumulator.directStoreCount);
        node.setStoreCount(accumulator.storeCount);
        node.setChildren(accumulator.children.values().stream()
                .sorted(nodeComparator())
                .map(this::toNode)
                .toList());
        return node;
    }

    private static final class NodeAccumulator {
        private final StoreOrgPathNodeDTO node;
        private final Map<Long, NodeAccumulator> children = new HashMap<>();
        private long directStoreCount;
        private long storeCount;

        private NodeAccumulator(StoreOrgPathNodeDTO node) {
            this.node = node;
        }
    }
}
