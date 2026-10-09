package com.htyoudao.youdao.module.system.service.org;

import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgTreeNode;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.mysql.org.OrgMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import com.htyoudao.youdao.module.system.service.storeuser.StoreUserService;
import com.htyoudao.youdao.module.system.util.store.PinyinUtil;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.meta.When;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 组织机构 Service 实现类
 *
 * @author dht
 */
@Service
public class OrgTreeServiceImpl implements OrgTreeService {

    @Resource
    private OrgMapper orgMapper;

    @Resource
    private SystemStoreInfoService systemStoreInfoService;

    @Resource
    private StoreUserService storeUserService;

    @Resource
    private SystemStoreInfoMapper systemStoreInfoMapper;


    @Override
    @DataPermission(enable = false)
    public List<OrgTreeNode> getFullOrgTree(Long businessId) {
        // 获取所有有效组织
        LambdaQueryWrapperX<OrgDO> queryWrapperX = new LambdaQueryWrapperX();
        queryWrapperX.eq(OrgDO::getBusinessId, businessId);
        queryWrapperX.select(OrgDO::getName, OrgDO::getLevel, OrgDO::getParentId, OrgDO::getId, OrgDO::getSort);
        List<OrgDO> allOrgList = orgMapper.selectList(queryWrapperX);
        return buildOrgTree(allOrgList, true);
    }



    @Override
    public List<OrgTreeNode> getUserVisibleOrgTree(Long userId, Boolean withStore) {
        // 获取完整组织树
        List<OrgTreeNode> fullTree = this.getFullOrgTree(BusinessContextHolder.getRequiredBusinessId());

        // 获取用户关联的组织ID
        HashSet<Long> userOrgIds = new HashSet<>(orgMapper.orgByUserId(userId));
        if (userOrgIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取用户有操作权限的组织ID（关联组织及其所有下级）
        Set<Long> operableOrgIds = getUserOperableOrgIds(fullTree, userOrgIds);


        // 构建可见组织树，标记操作权限
        List<OrgTreeNode> orgTreeNodes = buildVisibleOrgTreeFromFullTree(fullTree, operableOrgIds, userOrgIds);


        if (withStore){
            // 组织节点店数 map
            Map<Long, List<SystemStoreInfoDO>> storeMap = systemStoreInfoService.selectStoreByOrgIds(operableOrgIds)
                .stream().collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));

            // 递归为组织树添加门店节点
            addStoresToOrgTree(orgTreeNodes, storeMap);
        }

        return orgTreeNodes;
    }

    private void addStoresToOrgTree(List<OrgTreeNode> orgTreeNodes, Map<Long, List<SystemStoreInfoDO>> storeMap) {
        if (orgTreeNodes == null || orgTreeNodes.isEmpty()) {
            return;
        }

        for (OrgTreeNode orgNode : orgTreeNodes) {
            Long orgId = orgNode.getId();
            List<SystemStoreInfoDO> stores = storeMap.get(orgId);

            // 获取或初始化子节点列表
            List<OrgTreeNode> children = orgNode.getChildren();
            if (children == null) {
                children = new ArrayList<>();
                orgNode.setChildren(children);
            }

            // 添加门店节点
            if (!CollectionUtils.isEmpty(stores)) {
                Integer storeLevel = orgNode.getLevel() + 1;

                for (SystemStoreInfoDO store : stores) {
                    OrgTreeNode storeNode = getOrgStoreTreeNode(store, storeLevel, orgId);
                    children.add(storeNode);
                }
            }

            // 递归处理子组织节点
            if (orgNode.getChildren() != null && !orgNode.getChildren().isEmpty()) {
                addStoresToOrgTree(orgNode.getChildren(), storeMap);
            }
        }
    }

    @Override
    public List<OrgTreeNode> getUserStoreOrgTree(Long userId) {
        //storeUser + store
        List<SystemStoreInfoDO> systemStoreInfoDOS = systemStoreInfoMapper.selectList(SystemStoreInfoDO::getUserId, userId);

        // 提前提取已存在的storeId集合，提高contains查询性能
        Set<Long> existingStoreIds = systemStoreInfoDOS.stream()
            .map(SystemStoreInfoDO::getStoreId)
            .collect(Collectors.toSet());

        List<Long> storeIds = storeUserService.selectUserStoreIds(userId);

        // 过滤出需要新增的storeId
        List<Long> newStoreIds = storeIds.stream()
            .filter(storeId -> !existingStoreIds.contains(storeId))
            .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(newStoreIds)) {
            List<SystemStoreInfoDO> newStores = systemStoreInfoMapper.selectByIds(newStoreIds);
            systemStoreInfoDOS.addAll(newStores);
        }

        if (CollectionUtils.isEmpty(systemStoreInfoDOS)){
            return List.of();
        }

        Integer level = 1;
        List<OrgTreeNode> orgTreeNodes = new ArrayList<>();
        for (SystemStoreInfoDO systemStoreInfoDO : systemStoreInfoDOS) {
            OrgTreeNode storeTreeNode = getOrgStoreTreeNode(systemStoreInfoDO, level, null);
            orgTreeNodes.add(storeTreeNode);
        }
        return orgTreeNodes;
    }

    @Override
    public List<OrgTreeNode> getUserShowOrgTree(Long userId, Boolean withStore) {
        //精简出用户能看到的组织树 包含可见门店
        List<OrgTreeNode> userVisibleOrgTree = getUserVisibleOrgTree(userId, withStore);

        //获取用户关联门店的1级组织树
        if (withStore){
            // 提取已存在的门店ID
            Set<Long> existingStoreIds = extractStoreIds(userVisibleOrgTree);

            // 获取用户关联门店的1级组织树
            List<OrgTreeNode> userStoreOrgTree = getUserStoreOrgTree(userId);

            // 过滤掉已存在的门店
            List<OrgTreeNode> filteredStoreOrgTree = userStoreOrgTree.stream()
                .filter(node -> !existingStoreIds.contains(node.getId()))
                .collect(Collectors.toList());

            userVisibleOrgTree.addAll(filteredStoreOrgTree);
        }
        return userVisibleOrgTree;
    }

    @Override
    public List<OrgTreeNode> getShowOrgTree(Long userId,String keyword) {
        //精简出用户能看到的组织树 包含可见门店
        List<OrgTreeNode> userVisibleOrgTree = getVisibleOrgTree(userId);

        // 提取已存在的门店ID
        Set<Long> existingStoreIds = extractStoreIds(userVisibleOrgTree);

        // 获取用户关联门店的1级组织树
        List<OrgTreeNode> userStoreOrgTree = getUserStoreOrgTree(userId);

        // 过滤掉已存在的门店
        List<OrgTreeNode> filteredStoreOrgTree = userStoreOrgTree.stream()
                .filter(node -> !existingStoreIds.contains(node.getId()))
                .collect(Collectors.toList());

        userVisibleOrgTree.addAll(filteredStoreOrgTree);
        // 如果有关键词，进行模糊过滤
        if (StringUtils.isNotBlank(keyword)) {
            userVisibleOrgTree = filterOrgTreeByKeyword(userVisibleOrgTree, keyword.trim());
        }
        return userVisibleOrgTree;
    }

    /**
     * 递归过滤组织树，只保留匹配关键词的节点及其父节点路径
     */
    private List<OrgTreeNode> filterOrgTreeByKeyword(List<OrgTreeNode> nodes, String keyword) {
        if (CollectionUtils.isEmpty(nodes)) {
            return new ArrayList<>();
        }

        List<OrgTreeNode> filteredNodes = new ArrayList<>();

        for (OrgTreeNode node : nodes) {
            // 检查当前节点是否匹配
            boolean nodeMatches = matchesKeyword(node, keyword);

            // 递归过滤子节点
            List<OrgTreeNode> filteredChildren = filterOrgTreeByKeyword(node.getChildren(), keyword);

            // 如果当前节点匹配，或者有匹配的子节点，则保留该节点
            if (nodeMatches || !filteredChildren.isEmpty()) {
                // 直接使用原节点，但需要重新设置children
                if (nodeMatches) {
                    // 当前节点匹配，保留但不一定保留所有子节点，只保留匹配的子节点
                    node.setChildren(filteredChildren);
                } else {
                    // 当前节点不匹配但有匹配的子节点，只保留匹配的子节点
                    node.setChildren(filteredChildren);
                }
                filteredNodes.add(node);
            } else {
                // 不匹配的节点需要清空children，避免脏数据
                node.setChildren(new ArrayList<>());
            }
        }

        return filteredNodes;
    }

    /**
     * 判断节点是否匹配关键词（组织名称或门店名称）
     */
    private boolean matchesKeyword(OrgTreeNode node, String keyword) {
        if (node == null || StringUtils.isBlank(node.getName())) {
            return false;
        }

        // 不区分大小写模糊匹配
        return node.getName().toLowerCase().contains(keyword.toLowerCase());
    }

    public List<OrgTreeNode> getVisibleOrgTree(Long userId) {
        // 获取完整组织树
        List<OrgTreeNode> fullTree = this.getAllOrgTree(BusinessContextHolder.getRequiredBusinessId());

        // 获取用户关联的组织ID
        HashSet<Long> userOrgIds = new HashSet<>(orgMapper.orgByUserId(userId));
        if (userOrgIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取用户有操作权限的组织ID（关联组织及其所有下级）
        Set<Long> operableOrgIds = getUserOperableOrgIds(fullTree, userOrgIds);


        // 构建可见组织树，标记操作权限
        List<OrgTreeNode> orgTreeNodes = buildVisibleOrgTreeFromFullTree(fullTree, operableOrgIds, userOrgIds);


        // 组织节点店数 map
        Map<Long, List<SystemStoreInfoDO>> storeMap = systemStoreInfoService.selectStoreByOrgIds(operableOrgIds)
                .stream().collect(Collectors.groupingBy(SystemStoreInfoDO::getOrgId));

        // 递归为组织树添加门店节点
        addStoresToOrgTree(orgTreeNodes, storeMap);

        // 添加排序
        sortOrgTreeByPinyin(orgTreeNodes);

        return orgTreeNodes;
    }
    public List<OrgTreeNode> getAllOrgTree(Long businessId) {
        // 获取所有有效组织
        LambdaQueryWrapperX<OrgDO> queryWrapperX = new LambdaQueryWrapperX();
        queryWrapperX.eq(OrgDO::getBusinessId, businessId);
        queryWrapperX.select(OrgDO::getName, OrgDO::getLevel, OrgDO::getParentId, OrgDO::getId, OrgDO::getSort);
        List<OrgDO> allOrgList = orgMapper.selectList(queryWrapperX);
        return buildOrgTree(allOrgList, true);
    }

    /**
     * 递归对组织树中的节点（包括门店）按名称拼音首字母排序
     */
    private void sortOrgTreeByPinyin(List<OrgTreeNode> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }

        // 对当前层级节点排序
        // 排序规则：组织(isStore=0或null) > 门店(isStore=1) > 按拼音首字母排序
        nodes.sort(Comparator
                .comparing((OrgTreeNode node) -> {
                    // 组织排前面，门店排后面
                    Integer isStore = node.getIsStore();
                    if (isStore == null || isStore == 0) {
                        return 0; // 组织排在前面
                    } else {
                        return 1; // 门店排在后面
                    }
                })
                .thenComparing(node -> {
                    String name = node.getName();
                    if (StringUtils.isBlank(name)) {
                        return "ZZZZ"; // 空名称放最后
                    }
                    // 获取拼音首字母
                    String firstLetter = PinyinUtil.getFirstLetter(name);
                    // 让 # 排在最后
                    if ("#".equals(firstLetter)) {
                        return "ZZZZ";
                    }
                    return firstLetter;
                })
        );

        // 递归排序子节点
        for (OrgTreeNode node : nodes) {
            if (!CollectionUtils.isEmpty(node.getChildren())) {
                sortOrgTreeByPinyin(node.getChildren());
            }
        }
    }





    /**
     * 提取组织树中的所有门店ID
     */
    private Set<Long> extractStoreIds(List<OrgTreeNode> orgTreeNodes) {
        Set<Long> storeIds = new HashSet<>();
        if (orgTreeNodes == null || orgTreeNodes.isEmpty()) {
            return storeIds;
        }
        
        for (OrgTreeNode node : orgTreeNodes) {
            if (node.getIsStore() != null && node.getIsStore() == 1) {
                storeIds.add(node.getId());
            }
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                storeIds.addAll(extractStoreIds(node.getChildren()));
            }
        }
        
        return storeIds;
    }

    private OrgTreeNode getOrgStoreTreeNode(SystemStoreInfoDO systemStoreInfoDO, Integer level, Long parentId) {
        OrgTreeNode storeTreeNode = new OrgTreeNode();
        storeTreeNode.setOperable(true);
        storeTreeNode.setName(systemStoreInfoDO.getStoreName());
        storeTreeNode.setIsStore(1);
        storeTreeNode.setLevel(level);
        storeTreeNode.setParentId(parentId);
        storeTreeNode.setId(systemStoreInfoDO.getStoreId());
        storeTreeNode.setStoreUseStatus(Objects.equals(systemStoreInfoDO.getUseStatus(), 2));
        return storeTreeNode;
    }


    /**
     * 构建组织树（通用方法）
     */
    private List<OrgTreeNode> buildOrgTree(List<OrgDO> orgs, Boolean allOperable) {
        if (orgs.isEmpty()) {
            return new ArrayList<>();
        }

        // 转换为TreeNode
        Map<Long, OrgTreeNode> nodeMap = orgs.stream()
            .map(org -> {
                OrgTreeNode node = new OrgTreeNode();
                node.setId(org.getId());
                node.setName(org.getName());
                node.setParentId(org.getParentId());
                node.setLevel(org.getLevel());
                node.setOperable(allOperable);
                node.setChildren(new ArrayList<>());
                node.setIsStore(0);
                node.setSort(org.getSort());
                return node;
            })
            .collect(Collectors.toMap(OrgTreeNode::getId, Function.identity()));

        // 构建树结构
        List<OrgTreeNode> roots = new ArrayList<>();
        for (OrgTreeNode node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId().equals(0L)) {
                roots.add(node);
            } else {
                OrgTreeNode parent = nodeMap.get(node.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        }

        // 按名称排序
        roots.sort(Comparator.comparing(OrgTreeNode::getName));
        for (OrgTreeNode root : roots) {
            sortTreeNodes(root);
        }

        return roots;
    }


    /**
     * 递归排序树节点
     */
    private void sortTreeNodes(OrgTreeNode node) {
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            node.getChildren().sort(Comparator.comparing(OrgTreeNode::getName));
            for (OrgTreeNode child : node.getChildren()) {
                sortTreeNodes(child);
            }
        }
    }


    /**
     * 基于完整树获取用户有操作权限的组织ID（关联组织及其所有下级）
     */
    private Set<Long> getUserOperableOrgIds(List<OrgTreeNode> fullTree, Set<Long> userOrgIds) {
        Set<Long> operableOrgIds = new HashSet<>();

        // 递归遍历完整树，找到用户关联组织及其所有下级
        for (OrgTreeNode node : fullTree) {
            findOperableOrgs(node, userOrgIds, operableOrgIds);
        }

        return operableOrgIds;
    }

    /**
     * 递归查找有操作权限的组织
     */
    private void findOperableOrgs(OrgTreeNode node, Set<Long> userOrgIds, Set<Long> operableOrgIds) {
        // 如果当前节点是用户关联组织，或者父节点已经有操作权限，则当前节点有操作权限
        boolean isOperable = userOrgIds.contains(node.getId()) ||
            (node.getParentId() != null && operableOrgIds.contains(node.getParentId()));

        if (isOperable) {
            operableOrgIds.add(node.getId());
        }

        // 递归处理子节点
        if (node.getChildren() != null) {
            for (OrgTreeNode child : node.getChildren()) {
                findOperableOrgs(child, userOrgIds, operableOrgIds);
            }
        }
    }

    /**
     * 从完整树构建可见组织树
     */
    private List<OrgTreeNode> buildVisibleOrgTreeFromFullTree(List<OrgTreeNode> fullTree,
        Set<Long> operableOrgIds,
        Collection<Long> userOrgIds) {
        List<OrgTreeNode> visibleTree = new ArrayList<>();

        for (OrgTreeNode root : fullTree) {
            OrgTreeNode visibleRoot = buildVisibleTreeNode(root, operableOrgIds, userOrgIds);
            if (visibleRoot != null) {
                visibleTree.add(visibleRoot);
            }
        }

        return visibleTree;
    }

    /**
     * 递归构建可见树节点
     */
    private OrgTreeNode buildVisibleTreeNode(OrgTreeNode sourceNode, Set<Long> operableOrgIds, Collection<Long> userOrgIds) {
        // 检查当前节点是否应该显示（有操作权限的节点或其祖先）
        boolean shouldDisplay = shouldDisplayNode(sourceNode, operableOrgIds);

        if (!shouldDisplay) {
            return null;
        }

        // 创建新节点
        OrgTreeNode visibleNode = new OrgTreeNode();
        visibleNode.setId(sourceNode.getId());
        visibleNode.setName(sourceNode.getName());
        visibleNode.setParentId(sourceNode.getParentId());
        visibleNode.setLevel(sourceNode.getLevel());
        visibleNode.setSort(sourceNode.getSort());
        // 设置操作权限：用户关联组织及其下级有操作权限
        boolean isOperable = operableOrgIds.contains(sourceNode.getId());
        visibleNode.setOperable(isOperable);

        // 递归处理子节点
        if (sourceNode.getChildren() != null && !sourceNode.getChildren().isEmpty()) {
            List<OrgTreeNode> visibleChildren = new ArrayList<>();
            for (OrgTreeNode child : sourceNode.getChildren()) {
                OrgTreeNode visibleChild = buildVisibleTreeNode(child, operableOrgIds, userOrgIds);
                if (visibleChild != null) {
                    visibleChildren.add(visibleChild);
                }
            }
            visibleNode.setChildren(visibleChildren);
        }

        return visibleNode;
    }

    /**
     * 判断节点是否应该显示
     * 有操作权限的节点，或者有具有操作权限的后代节点
     */
    private boolean shouldDisplayNode(OrgTreeNode node, Set<Long> operableOrgIds) {
        // 如果当前节点有操作权限，肯定要显示
        if (operableOrgIds.contains(node.getId())) {
            return true;
        }

        // 如果子节点中有具有操作权限的节点，当前节点也要显示（作为路径）
        if (node.getChildren() != null) {
            for (OrgTreeNode child : node.getChildren()) {
                if (shouldDisplayNode(child, operableOrgIds)) {
                    return true;
                }
            }
        }

        return false;
    }
}