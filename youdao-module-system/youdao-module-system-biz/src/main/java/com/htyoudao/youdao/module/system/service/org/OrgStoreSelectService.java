package com.htyoudao.youdao.module.system.service.org;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgStoreTreeRespVO;
import com.htyoudao.youdao.module.system.controller.admin.org.vo.OrgTreeNode;
import com.htyoudao.youdao.module.system.controller.admin.orgstore.vo.OrgStoreListRespVO;
import com.htyoudao.youdao.module.system.controller.admin.orgstore.vo.OrgStoreOptionRespVO;
import com.htyoudao.youdao.module.system.controller.admin.orgstore.vo.OrgStoreSelectReqVO;

import java.util.List;

/**
 * 组织门店选择 Service 接口。
 */
public interface OrgStoreSelectService {

    List<OrgTreeNode> tree(OrgStoreSelectReqVO request);

    List<OrgStoreTreeRespVO> flatTree(OrgStoreSelectReqVO request);

    PageResult<OrgStoreOptionRespVO> storePage(OrgStoreSelectReqVO request);

    List<OrgStoreListRespVO> storeList(OrgStoreSelectReqVO request);

    List<Long> storeIds(OrgStoreSelectReqVO request);
}
