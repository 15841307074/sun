package com.htyoudao.youdao.module.analysis.service.anomaly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.StoreOrgStatisticsNodeRespVO;
import com.htyoudao.youdao.module.analysis.controller.app.vo.anomaly.StoreOrgStatisticsRespVO;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.StoreOrgSnapshotDO;
import com.htyoudao.youdao.module.analysis.dal.mysql.anomaly.StoreOrgSnapshotMapper;
import com.htyoudao.youdao.module.analysis.dal.dataobject.anomaly.UserStoreSnapshotDO;
import com.htyoudao.youdao.module.analysis.dal.mysql.anomaly.UserStoreSnapshotMapper;
import com.htyoudao.youdao.module.analysis.enums.PeriodTypeEnum;
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
    private UserStoreSnapshotMapper userStoreSnapshotMapper;
    @Resource
    private ObjectMapper objectMapper;
    @DubboReference
    private StoreOrgSnapshotApi storeOrgSnapshotApi;

    /** 按登录人在当前项目所属的组织，查询指定月份的门店统计。 */
    public StoreOrgStatisticsRespVO getStatistics(Long userId, String month) {
        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        LocalDate snapshotMonth = parseMonth(month);
        Long orgId = storeOrgSnapshotApi.getUserDefaultOrgId(userId).getCheckedData();
        Set<Long> manageableOrgIds = storeOrgSnapshotApi.listManageableOrgIds(userId).getCheckedData();
        if (orgId == null || manageableOrgIds == null || !manageableOrgIds.contains(orgId)) {
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
            // 只统计组织路径中包含所选组织的门店。
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
            // 从所选组织的直属子级开始，逐层累计各节点及其下级门店数。
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

    /**
     * 查询用户在指定月份负责、且当时归属所选组织本级或下级的门店。
     * 仅供模块内调用；人员范围和组织层级均取当月快照，不回查实时组织和门店状态。
     *
     * @param userId 后台用户ID，由可信业务调用方传入
     * @param month 快照月份，格式 yyyy-MM，例如 2026-08
     * @param orgId 当月快照中的组织ID，包含该组织自身和全部下级
     * @return 当月门店快照，按门店ID升序且去重；无匹配门店时返回空列表
     */
    public List<StoreOrgSnapshotDO> getMonthOrgStores(Long userId, String month, Long orgId) {
        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        LocalDate snapshotMonth = parseMonth(month);
        if (userId == null || userId <= 0 || orgId == null || orgId <= 0) {
            throw exception(STORE_ORG_PERMISSION_DENIED);
        }
        List<StoreOrgSnapshotDO> snapshots = snapshotMapper.selectListByMonth(businessId, snapshotMonth);
        if (snapshots.isEmpty()) {
            throw exception(STORE_ORG_SNAPSHOT_NOT_FOUND);
        }
        // 人员权限同样取指定月份，避免人员调组织后历史查询范围发生变化。
        Set<Long> storeIds = new HashSet<>();
        for (UserStoreSnapshotDO row : userStoreSnapshotMapper.selectList(
                new LambdaQueryWrapperX<UserStoreSnapshotDO>()
                        .eq(UserStoreSnapshotDO::getBusinessId, businessId)
                        .eq(UserStoreSnapshotDO::getPeriodType, PeriodTypeEnum.MONTH.getType())
                        .eq(UserStoreSnapshotDO::getPeriodStart, snapshotMonth)
                        .eq(UserStoreSnapshotDO::getUserId, userId)
                        .eq(UserStoreSnapshotDO::getDeleted, false))) {
            if (row.getStoreId() != null) storeIds.add(row.getStoreId());
        }
        if (storeIds.isEmpty()) return List.of();

        Map<Long, StoreOrgSnapshotDO> matched = new TreeMap<>();
        for (StoreOrgSnapshotDO row : snapshots) {
            if (!storeIds.contains(row.getStoreId())) continue;
            // 直属门店直接命中；下级门店通过完整祖先链判断，不能按实时组织递归。
            if (Objects.equals(row.getOrgId(), orgId)) {
                matched.putIfAbsent(row.getStoreId(), row);
                continue;
            }
            List<StoreOrgPathNodeDTO> path = readPath(row.getOrgPathJson());
            if (path == null || path.stream().anyMatch(Objects::isNull)) {
                throw exception(STORE_ORG_SNAPSHOT_DATA_INVALID);
            }
            if (indexOf(path, orgId) >= 0) matched.putIfAbsent(row.getStoreId(), row);
        }
        return new ArrayList<>(matched.values());
    }

    /** 校验 yyyy-MM 月份参数，并转换为当月第一天。 */
    private LocalDate parseMonth(String month) {
        try {
            return YearMonth.parse(month, MONTH_FORMATTER).atDay(1);
        } catch (DateTimeParseException | NullPointerException e) {
            throw exception(STORE_ORG_MONTH_INVALID);
        }
    }

    /** 将快照中的组织路径 JSON 解析为从根组织到直属组织的节点列表。 */
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

    /** 查找所选组织在完整组织路径中的位置。 */
    private int indexOf(List<StoreOrgPathNodeDTO> path, Long orgId) {
        for (int i = 0; i < path.size(); i++) {
            if (Objects.equals(path.get(i).getId(), orgId)) {
                return i;
            }
        }
        return -1;
    }

    /** 按组织排序值、层级和 ID 稳定排序。 */
    private Comparator<NodeAccumulator> nodeComparator() {
        return Comparator
                .comparing((NodeAccumulator accumulator) -> accumulator.node.getSort(),
                        Comparator.nullsLast(Integer::compareTo))
                .thenComparing(accumulator -> accumulator.node.getLevel(),
                        Comparator.nullsLast(Integer::compareTo))
                .thenComparing(accumulator -> accumulator.node.getId(), Comparator.nullsLast(Long::compareTo));
    }

    /** 将内部累计节点递归转换为接口响应节点。 */
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
