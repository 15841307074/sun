package com.htyoudao.youdao.module.system.api.storeorgsnapshot;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgSnapshotDTO;

import java.util.List;
import java.util.Set;

/**
 * RPC 服务 - 门店组织快照数据。
 *
 * <p>项目范围只取调用链中的 BusinessContextHolder，不允许调用方显式传入项目 ID。</p>
 */
public interface StoreOrgSnapshotApi {

    /**
     * 查询当前项目全部正常营业门店及其完整组织链。
     */
    CommonResult<List<StoreOrgSnapshotDTO>> listNormalStoreOrgData();

    /**
     * 查询用户在当前项目内可管理的组织（用户直属组织及其全部下级）。
     */
    CommonResult<Set<Long>> listManageableOrgIds(Long userId);
}
