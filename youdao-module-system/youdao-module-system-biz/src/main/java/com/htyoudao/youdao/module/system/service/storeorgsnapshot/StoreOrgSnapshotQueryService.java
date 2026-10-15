package com.htyoudao.youdao.module.system.service.storeorgsnapshot;

import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgSnapshotDTO;

import java.util.List;
import java.util.Set;

/** 门店组织快照所需的系统侧查询服务。 */
public interface StoreOrgSnapshotQueryService {

    List<StoreOrgSnapshotDTO> listNormalStoreOrgData();

    Set<Long> listManageableOrgIds(Long userId);
}
