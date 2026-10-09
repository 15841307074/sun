package com.htyoudao.youdao.module.system.service.org;


import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgTreeNode;
import java.util.List;

/**
 * 组织机构 Service 接口
 *
 * @author 零零玖零
 */
public interface OrgTreeService {

    /**
     * 获取当前项目下全部组织树
     */
    List<OrgTreeNode> getFullOrgTree(Long businessId);


    /**
     * 精简出用户能看到的组织树
     *
     * @param userId    用户ID
     * @param withStore 是否包含组织下门店
     * @return
     */
    List<OrgTreeNode> getUserVisibleOrgTree(Long userId, Boolean withStore);

    /**
     * 获取用户关联门店的1级组织树
     *
     * @param userId
     * @return
     */
    List<OrgTreeNode> getUserStoreOrgTree(Long userId);


    /**
     * 获取用户展示的组织树
     * 1.所属组织及下级组织 和所关联的门店 + 2.所属门店的一级组织树
     * @param userId
     * @return
     */
    List<OrgTreeNode> getUserShowOrgTree(Long userId, Boolean withStore);


    /**
     * 获取用户展示的组织树
     * 1.根据字母顺序排列门店
     * @param userId
     * @return
     */
    List<OrgTreeNode> getShowOrgTree(Long userId,String keyword);
}