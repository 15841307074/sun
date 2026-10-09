package com.htyoudao.youdao.module.system.api.orgstore;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.org.OrgApi;

import com.htyoudao.youdao.module.system.service.org.OrgService;
import com.htyoudao.youdao.module.system.service.store.SystemStoreInfoService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
public class OrgStoreApiImpl implements OrgStoreApi {
   @Resource
   private OrgService orgService;

    @Resource
    private SystemStoreInfoService systemStoreInfoService;

    @Override
    public CommonResult<Set<Long>> getStoreIdsByUser(Long businessId) {
        return success(orgService.getStoreIdsByUserRpc(businessId));
    }

    @Override
    public CommonResult<List<Long>> selectByOrgStoreList(Long orgId) {
        return success(systemStoreInfoService.selectByOrgStoreList(orgId));
    }

    ;
}
