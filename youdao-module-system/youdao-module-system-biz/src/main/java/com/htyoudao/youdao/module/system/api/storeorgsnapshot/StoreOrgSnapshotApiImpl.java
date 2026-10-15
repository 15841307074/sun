package com.htyoudao.youdao.module.system.api.storeorgsnapshot;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.storeorgsnapshot.dto.StoreOrgSnapshotDTO;
import com.htyoudao.youdao.module.system.service.storeorgsnapshot.StoreOrgSnapshotQueryService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/** 门店组织快照 Dubbo API 实现。 */
@DubboService
@Validated
public class StoreOrgSnapshotApiImpl implements StoreOrgSnapshotApi {

    @Resource
    private StoreOrgSnapshotQueryService queryService;

    @Override
    public CommonResult<List<StoreOrgSnapshotDTO>> listNormalStoreOrgData() {
        return success(queryService.listNormalStoreOrgData());
    }

    @Override
    public CommonResult<Set<Long>> listManageableOrgIds(Long userId) {
        return success(queryService.listManageableOrgIds(userId));
    }
}
