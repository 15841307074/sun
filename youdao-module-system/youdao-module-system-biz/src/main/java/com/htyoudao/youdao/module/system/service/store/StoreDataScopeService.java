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

/** 门店数据权限公共服务：本人担任店长的门店，与所属组织及下级门店的并集。
 * 不自动改变任何接口权限，由业务入口显式调用；异步任务应在请求线程提前取得范围。
 */
@Service
public class StoreDataScopeService {
    @Resource private OrgUserService orgUserService;
    @Resource private OrgMapper orgMapper;
    @Resource private SystemStoreUserMapper storeUserMapper;
    @Resource private SystemStoreInfoMapper storeMapper;

    /** 供同步业务入口使用；返回值语义与 getAccessibleStoreIds 相同。 */
    public Set<Long> getCurrentUserAccessibleStoreIds() {
        return getAccessibleStoreIds(
                com.htyoudao.youdao.framework.context.BusinessContextHolder.getBusinessId(),
                com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils.getLoginUserId());
    }

    /**
     * 显式计算指定项目、用户的可见门店，便于查询、统计及异步任务复用。
     * null：无组织且非店长，由调用方沿用原有权限规则，不代表自动开放所有数据。
     * 非空集合：必须作为查询限制；仅含 -1 表示无可见门店，不能省略该限制。
     */
    public Set<Long> getAccessibleStoreIds(Long businessId, Long userId) {
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
