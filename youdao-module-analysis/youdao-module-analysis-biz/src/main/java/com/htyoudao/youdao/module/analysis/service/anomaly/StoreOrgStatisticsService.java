package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.StoreOrgStatisticsNodeRespVO;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.StoreOrgStatisticsRespVO;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.StoreOrgSnapshotApi;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgPathNodeDTO;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgSnapshotDTO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants.*;

/** 实时门店组织统计，仅通过 system Dubbo API 读取当前项目数据。 */
@Service
public class StoreOrgStatisticsService {

    @DubboReference
    private StoreOrgSnapshotApi storeOrgSnapshotApi;

    /** 按当前登录用户所属的组织，统计本级及全部下级的实时正常营业门店。 */
    public StoreOrgStatisticsRespVO getStatistics(Long userId) {
        BusinessContextHolder.getRequiredBusinessId();
        if (userId == null || userId <= 0) throw exception(STORE_ORG_PERMISSION_DENIED);
        Long orgId = storeOrgSnapshotApi.getUserDefaultOrgId(userId).getCheckedData();
        List<StoreOrgSnapshotDTO> stores = getOrgStores(userId, orgId);

        Map<Long, NodeAccumulator> rootNodes = new HashMap<>();
        String selectedOrgName = null;
        long direct = 0L;
        for (StoreOrgSnapshotDTO store : stores) {
            List<StoreOrgPathNodeDTO> path = readPath(store);
            int selectedIndex = indexOf(path, orgId);
            if (selectedOrgName == null) selectedOrgName = path.get(selectedIndex).getName();
            if (selectedIndex == path.size() - 1) {
                direct++;
                continue;
            }
            // 从所选组织的直属子级开始，逐层累计当前组织树的门店数。
            Map<Long, NodeAccumulator> children = rootNodes;
            for (int i = selectedIndex + 1; i < path.size(); i++) {
                StoreOrgPathNodeDTO node = path.get(i);
                NodeAccumulator accumulator = children.computeIfAbsent(node.getId(), ignored -> new NodeAccumulator(node));
                accumulator.storeCount++;
                if (i == path.size() - 1) accumulator.directStoreCount++;
                children = accumulator.children;
            }
        }

        StoreOrgStatisticsRespVO response = new StoreOrgStatisticsRespVO();
        response.setOrgId(orgId.toString());
        response.setOrgName(selectedOrgName);
        response.setTotalStoreCount((long) stores.size());
        response.setDirectStoreCount(direct);
        response.setItems(rootNodes.values().stream().sorted(nodeComparator()).map(this::toNode).toList());
        return response;
    }

    /** 模块内查询：校验用户当前组织权限，返回指定组织本级及下级的实时门店，按门店ID去重排序。 */
    public List<StoreOrgSnapshotDTO> getOrgStores(Long userId, Long orgId) {
        BusinessContextHolder.getRequiredBusinessId();
        if (userId == null || userId <= 0 || orgId == null || orgId <= 0) throw exception(STORE_ORG_PERMISSION_DENIED);
        Set<Long> manageable = storeOrgSnapshotApi.listManageableOrgIds(userId).getCheckedData();
        if (manageable == null || !manageable.contains(orgId)) throw exception(STORE_ORG_PERMISSION_DENIED);

        // 该API每次读取系统当前门店与组织数据，其他业务仍复用原API契约。
        List<StoreOrgSnapshotDTO> stores = storeOrgSnapshotApi.listNormalStoreOrgData().getCheckedData();
        if (stores == null) throw exception(STORE_ORG_DATA_INVALID);
        Map<Long, StoreOrgSnapshotDTO> matched = new TreeMap<>();
        for (StoreOrgSnapshotDTO store : stores) {
            if (store == null || store.getStoreId() == null) throw exception(STORE_ORG_DATA_INVALID);
            if (indexOf(readPath(store), orgId) >= 0) matched.putIfAbsent(store.getStoreId(), store);
        }
        return new ArrayList<>(matched.values());
    }

    /** 无所属组织的门店不进入组织统计；组织链异常时返回明确业务提示。 */
    private List<StoreOrgPathNodeDTO> readPath(StoreOrgSnapshotDTO store) {
        List<StoreOrgPathNodeDTO> path = store.getOrgPath();
        if (path == null) return List.of();
        if (path.stream().anyMatch(node -> node == null || node.getId() == null)) throw exception(STORE_ORG_DATA_INVALID);
        return path;
    }

    private int indexOf(List<StoreOrgPathNodeDTO> path, Long orgId) {
        for (int i = 0; i < path.size(); i++) {
            if (Objects.equals(path.get(i).getId(), orgId)) return i;
        }
        return -1;
    }

    /** 按组织排序值、层级和ID稳定排序。 */
    private Comparator<NodeAccumulator> nodeComparator() {
        return Comparator.comparing((NodeAccumulator accumulator) -> accumulator.node.getSort(), Comparator.nullsLast(Integer::compareTo))
                .thenComparing(accumulator -> accumulator.node.getLevel(), Comparator.nullsLast(Integer::compareTo))
                .thenComparing(accumulator -> accumulator.node.getId());
    }

    private StoreOrgStatisticsNodeRespVO toNode(NodeAccumulator accumulator) {
        StoreOrgStatisticsNodeRespVO node = new StoreOrgStatisticsNodeRespVO();
        node.setOrgId(accumulator.node.getId().toString());
        node.setOrgName(accumulator.node.getName());
        node.setLevel(accumulator.node.getLevel());
        node.setDirectStoreCount(accumulator.directStoreCount);
        node.setStoreCount(accumulator.storeCount);
        node.setChildren(accumulator.children.values().stream().sorted(nodeComparator()).map(this::toNode).toList());
        return node;
    }

    private static final class NodeAccumulator {
        private final StoreOrgPathNodeDTO node;
        private final Map<Long, NodeAccumulator> children = new HashMap<>();
        private long directStoreCount;
        private long storeCount;
        private NodeAccumulator(StoreOrgPathNodeDTO node) { this.node = node; }
    }
}
