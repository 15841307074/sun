package com.htyoudao.youdao.module.system.service.org;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.system.api.permission.dto.DeptDataPermissionRespDTO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgStoreTreeRespVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgTreeNode;
import com.htyoudao.youdao.module.system.controller.admin.orgstore.vo.OrgStoreListRespVO;
import com.htyoudao.youdao.module.system.controller.admin.orgstore.vo.OrgStoreOptionRespVO;
import com.htyoudao.youdao.module.system.controller.admin.orgstore.vo.OrgStoreSelectReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreTagDO;
import com.htyoudao.youdao.module.system.dal.dataobject.tagvalue.TagValueDO;
import com.htyoudao.youdao.module.system.dal.mysql.org.OrgMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreTagMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreUserMapper;
import com.htyoudao.youdao.module.system.dal.mysql.tagvalue.TagValueMapper;
import com.htyoudao.youdao.module.system.enums.StoreStatusEnum;
import com.htyoudao.youdao.module.system.service.orguser.OrgUserService;
import com.htyoudao.youdao.module.system.service.permission.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@RequiredArgsConstructor
public class OrgStoreSelectServiceImpl implements OrgStoreSelectService {

    private final OrgMapper orgMapper;
    private final SystemStoreInfoMapper storeMapper;
    private final SystemStoreTagMapper storeTagMapper;
    private final TagValueMapper tagValueMapper;
    private final PermissionService permissionService;
    private final OrgUserService orgUserService;
    private final SystemStoreUserMapper storeUserMapper;

    @Override
    public PageResult<OrgStoreOptionRespVO> storePage(OrgStoreSelectReqVO request) {
        QueryContext context = resolveContext(request);
        LambdaQueryWrapper<SystemStoreInfoDO> query = storeQuery(request, context, selectionKeyword(request));
        if (query == null) {
            return PageResult.empty();
        }
        PageResult<SystemStoreInfoDO> page = storeMapper.selectPage(request, query);
        List<OrgStoreOptionRespVO> options = page.getList().stream()
                .map(store -> toOption(store, context, OrgStoreOptionRespVO.class))
                .toList();
        return new PageResult<>(options, page.getTotal());
    }

    @Override
    public List<OrgStoreListRespVO> storeList(OrgStoreSelectReqVO request) {
        QueryContext context = resolveContext(request);
        return findStores(request, context, selectionKeyword(request)).stream().map(store -> {
            OrgStoreListRespVO option = toOption(store, context, OrgStoreListRespVO.class);
            option.setId(store.getStoreId());
            option.setName(store.getStoreName());
            option.setParentId(parentOrRoot(context.orgBranchIds(), store.getStoreId(), store.getOrgId()));
            option.setLevel(storeLevel(context, store));
            return option;
        }).toList();
    }

    @Override
    public List<Long> storeIds(OrgStoreSelectReqVO request) {
        QueryContext context = resolveContext(request);
        LambdaQueryWrapper<SystemStoreInfoDO> query = storeQuery(request, context, selectionKeyword(request));
        if (query == null) {
            return List.of();
        }
        query.select(SystemStoreInfoDO::getStoreId);
        return storeMapper.selectList(query).stream().map(SystemStoreInfoDO::getStoreId).toList();
    }

    @Override
    public List<OrgTreeNode> tree(OrgStoreSelectReqVO request) {
        TreeData data = loadTree(request, request.getKeyword(), request.getStoreName());
        Map<Long, OrgTreeNode> nodes = new LinkedHashMap<>();
        for (OrgDO org : data.organizations()) {
            OrgTreeNode node = BeanUtils.toBean(org, OrgTreeNode.class);
            node.setIsStore(0);
            node.setOperable(data.operableOrgIds().contains(org.getId()));
            nodes.put(org.getId(), node);
        }

        List<OrgTreeNode> roots = new ArrayList<>();
        for (OrgTreeNode node : nodes.values()) {
            attach(nodes, roots, node);
        }
        for (SystemStoreInfoDO store : data.stores()) {
            OrgTreeNode node = new OrgTreeNode();
            node.setId(store.getStoreId());
            node.setName(store.getStoreName());
            node.setParentId(store.getOrgId());
            node.setLevel(storeLevel(data.context(), store));
            node.setIsStore(1);
            node.setOperable(true);
            node.setStoreUseStatus(Objects.equals(store.getUseStatus(), 2));
            attach(nodes, roots, node);
        }
        return roots;
    }

    @Override
    public List<OrgStoreTreeRespVO> flatTree(OrgStoreSelectReqVO request) {
        TreeData data = loadTree(request, selectionKeyword(request), null);
        Set<Long> includedOrgIds = data.organizations().stream().map(OrgDO::getId).collect(Collectors.toSet());
        Map<Long, Integer> storeCounts = new HashMap<>();
        for (SystemStoreInfoDO store : data.stores()) {
            Set<Long> parents = new HashSet<>();
            addOrgPath(parents, store.getOrgId(), data.context());
            parents.forEach(orgId -> storeCounts.merge(orgId, 1, Integer::sum));
        }

        List<OrgStoreTreeRespVO> nodes = new ArrayList<>();
        for (OrgDO org : data.organizations()) {
            OrgStoreTreeRespVO node = BeanUtils.toBean(org, OrgStoreTreeRespVO.class);
            node.setChildren(List.of());
            node.setParentId(parentOrRoot(includedOrgIds, org.getId(), org.getParentId()));
            node.setIsStore(0);
            node.setIsFlag(!data.operableOrgIds().contains(org.getId()));
            node.setStoreNum(storeCounts.getOrDefault(org.getId(), 0));
            nodes.add(node);
        }
        for (SystemStoreInfoDO store : data.stores()) {
            OrgStoreTreeRespVO node = BeanUtils.toBean(store, OrgStoreTreeRespVO.class);
            node.setId(store.getStoreId());
            node.setName(store.getStoreName());
            node.setParentId(parentOrRoot(includedOrgIds, store.getStoreId(), store.getOrgId()));
            node.setLevel(storeLevel(data.context(), store));
            node.setChildren(List.of());
            node.setIsStore(1);
            node.setIsFlag(false);
            nodes.add(node);
        }
        return nodes;
    }

    private QueryContext resolveContext(OrgStoreSelectReqVO request) {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        if (loginUserId == null) {
            throw exception(UNAUTHORIZED);
        }
        Long businessId = BusinessContextHolder.getBusinessId();
        if (businessId == null) {
            throw exception(BAD_REQUEST);
        }
        if (request.getBusinessId() != null && !businessId.equals(request.getBusinessId())) {
            throw exception(FORBIDDEN);
        }

        DeptDataPermissionRespDTO permission = permissionService.getDeptDataPermission(loginUserId, businessId);
        boolean all = Boolean.TRUE.equals(permission.getAll());
        if (request.getScope() == OrgStoreSelectReqVO.Scope.PROJECT && !all) {
            throw exception(FORBIDDEN);
        }

        Map<Long, OrgDO> orgs = new LinkedHashMap<>();
        orgMapper.selectList(Wrappers.<OrgDO>lambdaQuery()
                .select(OrgDO::getId, OrgDO::getName, OrgDO::getParentId, OrgDO::getLevel,
                        OrgDO::getAncestors, OrgDO::getSort)
                .eq(OrgDO::getBusinessId, businessId)
                .eq(OrgDO::getStatus, 1)
                .orderByAsc(OrgDO::getSort, OrgDO::getId))
                .forEach(org -> orgs.put(org.getId(), org));

        Set<Long> requestedOrgIds = new HashSet<>();
        if (request.getOrgIds() != null) {
            requestedOrgIds.addAll(request.getOrgIds());
        }
        if (request.getOrgId() != null) {
            requestedOrgIds.add(request.getOrgId());
        }
        Set<Long> orgFilter = orgs.keySet();
        if (!requestedOrgIds.isEmpty()) {
            requestedOrgIds.retainAll(orgs.keySet());
            orgFilter = Boolean.FALSE.equals(request.getIncludeChildren())
                    ? requestedOrgIds : descendants(orgs, requestedOrgIds);
        }
        Set<Long> assignedOrgIds = new HashSet<>(orgUserService.selectUserOrgIdsByBusinessId(loginUserId, businessId));
        assignedOrgIds.retainAll(orgs.keySet());
        Set<Long> orgBranchIds = descendants(orgs, assignedOrgIds);
        Set<Long> managedStoreIds = new HashSet<>(storeUserMapper.getStoreIdsByUserId(loginUserId, businessId));
        return new QueryContext(businessId, permission, orgs, orgFilter, orgBranchIds, managedStoreIds);
    }

    private static String selectionKeyword(OrgStoreSelectReqVO request) {
        return request.getKeyword() != null ? request.getKeyword() : request.getStoreName();
    }

    private LambdaQueryWrapper<SystemStoreInfoDO> storeQuery(OrgStoreSelectReqVO request,
                                                              QueryContext context, String storeName) {
        Set<Long> allowedStoreIds = context.permission().getStoreIds();
        boolean all = Boolean.TRUE.equals(context.permission().getAll());
        Set<Long> scopedOrgIds = new HashSet<>(context.orgBranchIds());
        scopedOrgIds.retainAll(context.orgFilter());
        boolean hasScopedOrgs = !scopedOrgIds.isEmpty();
        boolean hasManagedStores = !context.managedStoreIds().isEmpty();
        if ((!all && allowedStoreIds.isEmpty())
                || context.orgFilter().isEmpty()
                || (!hasScopedOrgs && !hasManagedStores)
                || (request.getIncludeStoreIds() != null && request.getIncludeStoreIds().isEmpty())) {
            return null;
        }

        LambdaQueryWrapper<SystemStoreInfoDO> query = Wrappers.<SystemStoreInfoDO>lambdaQuery()
                .select(SystemStoreInfoDO::getStoreId, SystemStoreInfoDO::getStoreName, SystemStoreInfoDO::getOrgId,
                        SystemStoreInfoDO::getStoreCity, SystemStoreInfoDO::getStoreCityName, SystemStoreInfoDO::getStoreAddress,
                        SystemStoreInfoDO::getStoreStatus, SystemStoreInfoDO::getOpenStatus, SystemStoreInfoDO::getUseStatus,
                        SystemStoreInfoDO::getIdentificationTemplate, SystemStoreInfoDO::getIsAllProduct,
                        SystemStoreInfoDO::getIsAllCommdity, SystemStoreInfoDO::getIsSameLine, SystemStoreInfoDO::getWarehouseId,
                        SystemStoreInfoDO::getWarehouseName, SystemStoreInfoDO::getProjectId, SystemStoreInfoDO::getProjectCode,
                        SystemStoreInfoDO::getProjectName, SystemStoreInfoDO::getDeliveryLineId,
                        SystemStoreInfoDO::getDeliveryLineName, SystemStoreInfoDO::getTiktokId)
                .eq(SystemStoreInfoDO::getBusinessId, context.businessId())
                .eq(request.getChannel() != OrgStoreSelectReqVO.Channel.SUPPLY_CHAIN, SystemStoreInfoDO::getStoreSource, "0")
                .isNotNull(request.getChannel() == OrgStoreSelectReqVO.Channel.DOUYIN, SystemStoreInfoDO::getTiktokId)
                .in(!all, SystemStoreInfoDO::getStoreId, allowedStoreIds)
                .and(scope -> scope.in(hasScopedOrgs, SystemStoreInfoDO::getOrgId, scopedOrgIds)
                        .or(hasScopedOrgs && hasManagedStores)
                        .nested(hasManagedStores, managed -> managed
                                .in(SystemStoreInfoDO::getStoreId, context.managedStoreIds())
                                .in(SystemStoreInfoDO::getOrgId, context.orgFilter())))
                .in(request.getIncludeStoreIds() != null, SystemStoreInfoDO::getStoreId, request.getIncludeStoreIds())
                .eq(request.getUseStatus() != null, SystemStoreInfoDO::getUseStatus, request.getUseStatus())
                .eq(request.getWarehouseId() != null, SystemStoreInfoDO::getWarehouseId, request.getWarehouseId())
                .eq(request.getUserId() != null, SystemStoreInfoDO::getUserId, request.getUserId())
                .orderByAsc(SystemStoreInfoDO::getStoreName, SystemStoreInfoDO::getStoreId);

        if (request.getExcludeStoreIds() != null && !request.getExcludeStoreIds().isEmpty()) {
            query.notIn(SystemStoreInfoDO::getStoreId, request.getExcludeStoreIds());
        }
        List<Integer> storeStatuses = request.getStoreStatus();
        boolean hasStoreStatuses = storeStatuses != null && !storeStatuses.isEmpty();
        if (hasStoreStatuses) {
            query.in(SystemStoreInfoDO::getStoreStatus, new HashSet<>(storeStatuses));
        }
        if (Boolean.TRUE.equals(request.getFilterClosedStore())
                || (request.getFilterClosedStore() == null && !hasStoreStatuses)) {
            query.and(status -> status.ne(SystemStoreInfoDO::getStoreStatus, StoreStatusEnum.SHUTDOWN.getStatus())
                    .or().isNull(SystemStoreInfoDO::getStoreStatus));
        }
        if (StringUtils.hasText(storeName)) {
            query.like(SystemStoreInfoDO::getStoreName, storeName.trim());
        }

        List<Long> tagIds = request.getTagIds();
        Integer tagType = request.getType();
        if (Objects.equals(tagType, 3)) {
            query.notExists("""
                    SELECT 1 FROM system_store_tag tag
                    WHERE tag.store_id = system_store_info.store_id
                      AND tag.deleted = 0
                    """);
        } else if (tagIds != null && !tagIds.isEmpty()) {
            Set<Long> storeIds = findTaggedStoreIds(resolveTagValueIds(tagIds), Objects.equals(tagType, 2));
            if (storeIds.isEmpty()) {
                return null;
            }
            query.in(SystemStoreInfoDO::getStoreId, storeIds);
        }
        return query;
    }

    private Set<Long> resolveTagValueIds(List<Long> tagIds) {
        Set<Long> selectedIds = new HashSet<>(tagIds);
        List<TagValueDO> values = tagValueMapper.selectList(Wrappers.<TagValueDO>lambdaQuery()
                .select(TagValueDO::getId, TagValueDO::getTagGroupId)
                .in(TagValueDO::getId, selectedIds)
                .or()
                .in(TagValueDO::getTagGroupId, selectedIds));
        Set<Long> groupsWithSelectedValues = new HashSet<>();
        for (TagValueDO value : values) {
            if (selectedIds.contains(value.getId())) {
                groupsWithSelectedValues.add(value.getTagGroupId());
            }
        }
        Set<Long> valueIds = new HashSet<>(selectedIds);
        for (TagValueDO value : values) {
            Long groupId = value.getTagGroupId();
            if (selectedIds.contains(groupId)) {
                valueIds.remove(groupId);
                // 同时传父组和具体子标签时，仅匹配已选的子标签。
                if (!groupsWithSelectedValues.contains(groupId)) {
                    valueIds.add(value.getId());
                }
            }
        }
        return valueIds;
    }

    private Set<Long> findTaggedStoreIds(Set<Long> tagIds, boolean anyTag) {
        // 门店权限和项目由外层查询约束，历史标签关系按 store_id 匹配。
        LambdaQueryWrapper<SystemStoreTagDO> query = Wrappers.<SystemStoreTagDO>lambdaQuery()
                .select(SystemStoreTagDO::getStoreId)
                .in(SystemStoreTagDO::getTagId, tagIds)
                .groupBy(SystemStoreTagDO::getStoreId);
        if (!anyTag) {
            query.having("COUNT(DISTINCT tag_id) = {0}", tagIds.size());
        }
        return storeTagMapper.selectList(query).stream().map(SystemStoreTagDO::getStoreId).collect(Collectors.toSet());
    }

    private List<SystemStoreInfoDO> findStores(OrgStoreSelectReqVO request, QueryContext context, String storeName) {
        LambdaQueryWrapper<SystemStoreInfoDO> query = storeQuery(request, context, storeName);
        return query == null ? List.of() : storeMapper.selectList(query);
    }

    private <T extends OrgStoreOptionRespVO> T toOption(SystemStoreInfoDO store, QueryContext context, Class<T> type) {
        T option = BeanUtils.toBean(store, type);
        OrgDO org = context.orgs().get(store.getOrgId());
        if (org != null) {
            option.setOrgName(org.getName());
        }
        return option;
    }

    private TreeData loadTree(OrgStoreSelectReqVO request, String searchKeyword, String storeName) {
        QueryContext context = resolveContext(request);
        Set<Long> operableOrgIds = new HashSet<>(Boolean.TRUE.equals(context.permission().getAll())
                ? context.orgBranchIds() : context.permission().getOrgIds());
        operableOrgIds.retainAll(context.orgBranchIds());
        operableOrgIds.retainAll(context.orgFilter());

        boolean withStore = !Boolean.FALSE.equals(request.getWithStore());
        List<SystemStoreInfoDO> stores = withStore ? findStores(request, context, storeName) : List.of();
        Set<Long> keptOrgIds = new HashSet<>(operableOrgIds);
        if (StringUtils.hasText(searchKeyword)) {
            String keyword = searchKeyword.trim().toLowerCase(Locale.ROOT);
            Set<Long> visibleOrgIds = new HashSet<>();
            keptOrgIds.forEach(id -> addOrgPath(visibleOrgIds, id, context));
            stores.forEach(store -> addOrgPath(visibleOrgIds, store.getOrgId(), context));
            Set<Long> matchedOrgs = visibleOrgIds.stream()
                    .filter(id -> matches(context.orgs().get(id).getName(), keyword))
                    .collect(Collectors.toSet());
            Set<Long> matchingBranch = descendants(context.orgs(), matchedOrgs);
            keptOrgIds.retainAll(matchingBranch);
            keptOrgIds.addAll(matchedOrgs);
            stores = stores.stream().filter(store -> matchingBranch.contains(store.getOrgId())
                    || matches(store.getStoreName(), keyword)).toList();
        }
        if (withStore && StringUtils.hasText(storeName)) {
            keptOrgIds.clear();
        }

        Set<Long> displayOrgIds = new HashSet<>();
        keptOrgIds.forEach(id -> addOrgPath(displayOrgIds, id, context));
        stores.forEach(store -> addOrgPath(displayOrgIds, store.getOrgId(), context));
        List<OrgDO> organizations = context.orgs().values().stream()
                .filter(org -> displayOrgIds.contains(org.getId())).toList();
        return new TreeData(context, organizations, operableOrgIds, stores);
    }

    private static Set<Long> descendants(Map<Long, OrgDO> orgs, Set<Long> roots) {
        Map<Long, List<Long>> children = new HashMap<>();
        for (OrgDO org : orgs.values()) {
            children.computeIfAbsent(org.getParentId(), ignored -> new ArrayList<>()).add(org.getId());
        }
        Set<Long> result = new HashSet<>();
        ArrayDeque<Long> pending = new ArrayDeque<>(roots);
        while (!pending.isEmpty()) {
            Long id = pending.removeFirst();
            if (result.add(id)) {
                pending.addAll(children.getOrDefault(id, List.of()));
            }
        }
        return result;
    }

    private static void addOrgPath(Set<Long> ids, Long start, QueryContext context) {
        OrgDO org = context.orgs().get(start);
        while (org != null && context.orgBranchIds().contains(org.getId()) && ids.add(org.getId())) {
            org = context.orgs().get(org.getParentId());
        }
    }

    private static void attach(Map<Long, OrgTreeNode> orgs, List<OrgTreeNode> roots, OrgTreeNode node) {
        OrgTreeNode parent = orgs.get(node.getParentId());
        if (parent == null || parent == node) {
            node.setParentId(0L);
            roots.add(node);
        } else {
            parent.getChildren().add(node);
        }
    }

    private static Long parentOrRoot(Set<Long> orgIds, Long id, Long parentId) {
        return orgIds.contains(parentId) && !Objects.equals(id, parentId) ? parentId : 0L;
    }

    private static int storeLevel(QueryContext context, SystemStoreInfoDO store) {
        OrgDO parent = context.orgs().get(store.getOrgId());
        return parent == null || parent.getLevel() == null ? 1 : parent.getLevel() + 1;
    }

    private static boolean matches(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private record QueryContext(Long businessId, DeptDataPermissionRespDTO permission,
                                Map<Long, OrgDO> orgs, Set<Long> orgFilter, Set<Long> orgBranchIds,
                                Set<Long> managedStoreIds) {
    }

    private record TreeData(QueryContext context, List<OrgDO> organizations,
                            Set<Long> operableOrgIds, List<SystemStoreInfoDO> stores) {
    }
}
