package com.htyoudao.youdao.module.system.service.store;

import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreInfoDO;
import com.htyoudao.youdao.module.system.dal.dataobject.store.SystemStoreUserDO;
import com.htyoudao.youdao.module.system.dal.mysql.org.OrgMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreInfoMapper;
import com.htyoudao.youdao.module.system.dal.mysql.store.SystemStoreUserMapper;
import com.htyoudao.youdao.module.system.service.orguser.OrgUserService;
import com.htyoudao.youdao.module.system.enums.StoreUserEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.UNAUTHORIZED;

/** 在请求线程计算导出权限，异步分页只使用固定的门店范围。 */
@Service
public class StoreExportScopeService {
    @Resource private OrgUserService orgUserService;
    @Resource private OrgMapper orgMapper;
    @Resource private SystemStoreUserMapper storeUserMapper;
    @Resource private SystemStoreInfoMapper storeMapper;

    /** null 保留无组织且非店长账号的原有规则；空权限使用不可能的ID，禁止退化成全量导出。 */
    public Set<Long> resolve(Long businessId, Long userId) {
        if (businessId == null || userId == null) throw exception(UNAUTHORIZED);
        Set<Long> ownStores = storeUserMapper.selectList(new LambdaQueryWrapperX<SystemStoreUserDO>()
                .eq(SystemStoreUserDO::getBusinessId, businessId)
                .eq(SystemStoreUserDO::getUserId, userId)
                .eq(SystemStoreUserDO::getType, StoreUserEnum.STOREMANAGER.getStatus())
                .eq(SystemStoreUserDO::getDeleted, false)).stream()
                .map(SystemStoreUserDO::getStoreId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Long> roots = Optional.ofNullable(orgUserService.selectUserOrgIdsByBusinessId(userId, businessId))
                .orElseGet(Collections::emptyList).stream().filter(Objects::nonNull).distinct().toList();
        if (roots.isEmpty() && ownStores.isEmpty()) return null;
        Set<Long> organizations = new HashSet<>(roots);
        for (Long orgId : roots) {
            Set<Long> children = orgMapper.getChildIdListByStore(orgId);
            if (children != null) organizations.addAll(children);
        }
        Set<Long> scope = new HashSet<>(ownStores);
        if (!organizations.isEmpty()) {
            storeMapper.selectList(new LambdaQueryWrapperX<SystemStoreInfoDO>()
                    .eq(SystemStoreInfoDO::getBusinessId, businessId)
                    .eq(SystemStoreInfoDO::getDeleted, false)
                    .in(SystemStoreInfoDO::getOrgId, organizations)).stream()
                    .map(SystemStoreInfoDO::getStoreId).filter(Objects::nonNull).forEach(scope::add);
        }
        return scope.isEmpty() ? Set.of(-1L) : Set.copyOf(scope);
    }
}
