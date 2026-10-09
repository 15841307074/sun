package com.htyoudao.youdao.module.system.api.org;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.collection.CollectionUtils;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.api.org.dto.OrgRespDTO;
import com.htyoudao.youdao.module.system.api.org.dto.StoreOrgDTO;
import com.htyoudao.youdao.module.system.dal.dataobject.org.OrgDO;
import com.htyoudao.youdao.module.system.service.org.OrgService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
@Validated
public class OrgApiImpl implements OrgApi {
   @Resource
   private OrgService orgService;

    @Override
    public CommonResult<Set<Long>> getStoreIdsByUser(Long businessId) {
        return success(orgService.getStoreIdsByUserRpc(businessId));
    };
    @Override
    public CommonResult<Set<Long>> getStoreIdListByOrgID(Long orgId,Long businessId) {
        return success(orgService.getStoreIdListByOrgIDRpc(orgId,businessId));
    };

    public CommonResult<Set<OrgRespDTO>> getChildOrgList(Long orgId){
        Set<OrgDO> set = orgService.getChildOrgList(orgId);
        return success(CollectionUtils.convertSet(set, s -> BeanUtils.toBean(s, OrgRespDTO.class)));
    }

    @Override
    public CommonResult<Set<StoreOrgDTO>> getOrgListByStoreId(List<Long> storeIds) {
        return success(orgService.getOrgListByStoreId(storeIds));
    }

    @Override
    public CommonResult<Set<StoreOrgDTO>> getOrgListByStoreIdV2(List<Long> storeIds) {
        return success(orgService.getOrgListByStoreIdV2(storeIds));
    }

    @Override
    public CommonResult<Set<StoreOrgDTO>> getAllOrgListByStoreIdV2() {
        return success(orgService.getAllOrgListByStoreIdV2());
    }

    @Override
    public CommonResult<Set<Long>> getAllStoreIdListByOrgID(Long orgId) {
        return success(orgService.getStoreIdListByOrgID(orgId));
    }
}
