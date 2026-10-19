package com.htyoudao.youdao.module.system.service.storeorgsnapshot;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgPathNodeDTO;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgSnapshotDTO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.mysql.org.OrgMapper;
import com.htyoudao.youdao.module.system.dal.mysql.orguser.OrgUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 门店组织快照查询实现。
 *
 * <p>所有查询都显式附加当前项目条件，防止 Dubbo 调用缺失项目边界时误查全量数据。</p>
 */
@Service
public class StoreOrgSnapshotQueryServiceImpl implements StoreOrgSnapshotQueryService {

    @Resource
    private SystemStoreInfoMapper storeMapper;
    @Resource
    private OrgMapper orgMapper;
    @Resource
    private OrgUserMapper orgUserMapper;

    @Override
    public List<StoreOrgSnapshotDTO> listNormalStoreOrgData() {
        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        List<SystemStoreInfoDO> stores = storeMapper.selectList(
                new LambdaQueryWrapperX<SystemStoreInfoDO>()
                        .select(SystemStoreInfoDO::getStoreId, SystemStoreInfoDO::getStoreName,
                                SystemStoreInfoDO::getStoreProvince, SystemStoreInfoDO::getOrgId)
                        .eq(SystemStoreInfoDO::getBusinessId, businessId)
                        .eq(SystemStoreInfoDO::getStoreStatus, 0)
                        .eq(SystemStoreInfoDO::getDeleted, false)
                        .orderByAsc(SystemStoreInfoDO::getStoreId));
        Map<Long, OrgDO> orgMap = loadOrgMap(businessId);
        return stores.stream().map(store -> toSnapshotDTO(store, orgMap)).toList();
    }

    @Override
    public Set<Long> listManageableOrgIds(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        Set<Long> directOrgIds = new HashSet<>(
                orgUserMapper.selectUserOrgIdsByBusinessId(userId, businessId));
        if (directOrgIds.isEmpty()) {
            return Collections.emptySet();
        }

        Map<Long, OrgDO> orgMap = loadOrgMap(businessId);
        LinkedHashSet<Long> result = new LinkedHashSet<>();
        orgMap.values().stream()
                .sorted(Comparator.comparing(OrgDO::getLevel, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(OrgDO::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(OrgDO::getId))
                .filter(org -> isSelfOrDescendant(org, directOrgIds, orgMap))
                .map(OrgDO::getId)
                .forEach(result::add);
        return result;
    }

    @Override
    public Long getUserDefaultOrgId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        Long businessId = BusinessContextHolder.getRequiredBusinessId();
        Set<Long> directOrgIds = new HashSet<>(
                orgUserMapper.selectUserOrgIdsByBusinessId(userId, businessId));
        if (directOrgIds.isEmpty()) {
            return null;
        }
        // 默认组织只从当前项目的有效所属组织中选取，多个组织时保持稳定顺序。
        return loadOrgMap(businessId).values().stream()
                .filter(org -> directOrgIds.contains(org.getId()))
                .sorted(Comparator.comparing(OrgDO::getLevel, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(OrgDO::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(OrgDO::getId))
                .map(OrgDO::getId)
                .findFirst().orElse(null);
    }

    private Map<Long, OrgDO> loadOrgMap(Long businessId) {
        return orgMapper.selectList(new LambdaQueryWrapperX<OrgDO>()
                        .eq(OrgDO::getBusinessId, businessId)
                        .eq(OrgDO::getDeleted, false))
                .stream().collect(Collectors.toMap(OrgDO::getId, Function.identity(), (left, right) -> left));
    }

    private StoreOrgSnapshotDTO toSnapshotDTO(SystemStoreInfoDO store, Map<Long, OrgDO> orgMap) {
        StoreOrgSnapshotDTO dto = new StoreOrgSnapshotDTO();
        dto.setStoreId(store.getStoreId());
        dto.setStoreName(store.getStoreName());
        dto.setStoreProvince(store.getStoreProvince());
        dto.setOrgId(store.getOrgId());

        OrgDO org = orgMap.get(store.getOrgId());
        if (org == null) {
            dto.setOrgPath(Collections.emptyList());
            return dto;
        }
        dto.setOrgName(org.getName());
        OrgDO parent = orgMap.get(org.getParentId());
        if (parent != null) {
            dto.setParentOrgId(parent.getId());
            dto.setParentOrgName(parent.getName());
        }
        dto.setOrgPath(buildPath(org, orgMap));
        return dto;
    }

    private List<StoreOrgPathNodeDTO> buildPath(OrgDO org, Map<Long, OrgDO> orgMap) {
        LinkedList<StoreOrgPathNodeDTO> path = new LinkedList<>();
        Set<Long> visited = new HashSet<>();
        OrgDO current = org;
        while (current != null && visited.add(current.getId())) {
            path.addFirst(toPathNode(current));
            current = orgMap.get(current.getParentId());
        }
        return path;
    }

    private StoreOrgPathNodeDTO toPathNode(OrgDO org) {
        StoreOrgPathNodeDTO node = new StoreOrgPathNodeDTO();
        node.setId(org.getId());
        node.setName(org.getName());
        node.setParentId(org.getParentId());
        node.setLevel(org.getLevel());
        node.setSort(org.getSort());
        return node;
    }

    private boolean isSelfOrDescendant(OrgDO org, Set<Long> roots, Map<Long, OrgDO> orgMap) {
        Set<Long> visited = new HashSet<>();
        OrgDO current = org;
        while (current != null && visited.add(current.getId())) {
            if (roots.contains(current.getId())) {
                return true;
            }
            current = orgMap.get(current.getParentId());
        }
        return false;
    }
}
