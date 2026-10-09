package com.htyoudao.youdao.module.system.service.org;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.system.SystemServerApplication;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgTreeNode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SystemServerApplication.class)
class OrgTreeServiceImplTest {

    @Autowired
    private OrgTreeService orgTreeService;



    @Test
    public void testStore(){
        Long businessId = 10L;
        Long userId = 1986444624767873026L;//门店
        testNewOrgTreeMethods(businessId, userId, true);
    }

    @Test
    public void testOrg(){
        Long businessId = 10L;
        Long userId = 1828829686504730626L;//组织
        testNewOrgTreeMethods(businessId, userId, true);
    }

    @Test
    public void testStoreOrg(){
        Long businessId = 10L;
        Long userId = 1828808302076456961L;//既是组织 又是门店
        testNewOrgTreeMethods(businessId, userId, true);
    }

    @Test
    public void testDuplicateStoreFiltering() {
        Long businessId = 10L;
        Long userId = 1828808302076456961L;//既是组织 又是门店
        
        BusinessContextHolder.setBusinessId(businessId);
        
        // 获取用户可见组织树，包含门店
        List<OrgTreeNode> visibleTree = orgTreeService.getUserShowOrgTree(userId, true);
        
        // 提取所有门店ID
        Set<Long> storeIds = new HashSet<>();
        Set<Long> duplicateStoreIds = new HashSet<>();
        
        // 遍历组织树，检查是否有重复的门店ID
        collectStoreIds(visibleTree, storeIds, duplicateStoreIds);
        
        // 验证没有重复的门店
        assertTrue(duplicateStoreIds.isEmpty(), "组织树中存在重复的门店: " + duplicateStoreIds);
        
        System.out.println("门店去重测试通过，没有重复的门店");
    }

    private void collectStoreIds(List<OrgTreeNode> tree, Set<Long> storeIds, Set<Long> duplicateStoreIds) {
        for (OrgTreeNode node : tree) {
            if (node.getIsStore() != null && node.getIsStore() == 1) {
                if (!storeIds.add(node.getId())) {
                    duplicateStoreIds.add(node.getId());
                }
            }
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                collectStoreIds(node.getChildren(), storeIds, duplicateStoreIds);
            }
        }
    }


    public void testNewOrgTreeMethods(Long businessId, Long userId, Boolean withStore) {

        BusinessContextHolder.setBusinessId(businessId);

        // 3. 获取用户可见组织树（区分权限）
        List<OrgTreeNode> visibleTree = orgTreeService.getUserShowOrgTree(userId, withStore);


        System.out.println("用户可见组织树节点数: " + countNodes(visibleTree));

        // 打印权限信息
        printOperableInfo(visibleTree);
    }

    private int countNodes(List<OrgTreeNode> tree) {
        int count = 0;
        for (OrgTreeNode node : tree) {
            count += countNodesRecursive(node);
        }
        return count;
    }

    private int countNodesRecursive(OrgTreeNode node) {
        int count = 1;
        if (node.getChildren() != null) {
            for (OrgTreeNode child : node.getChildren()) {
                count += countNodesRecursive(child);
            }
        }
        return count;
    }

    private void printOperableInfo(List<OrgTreeNode> tree) {
        for (OrgTreeNode node : tree) {
            printNodeOperableInfo(node, 0);
        }
    }

    private void printNodeOperableInfo(OrgTreeNode node, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + node.getName() + " [可操作: " + node.getOperable() + "]");
        if (node.getChildren() != null) {
            for (OrgTreeNode child : node.getChildren()) {
                printNodeOperableInfo(child, depth + 1);
            }
        }
    }
}