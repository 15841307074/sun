package com.htyoudao.youdao.module.bpm.framework.flowable.core.candidate.strategy.user;

import com.htyoudao.youdao.framework.common.util.string.StrUtils;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import com.htyoudao.youdao.module.system.api.permission.PermissionApi;
import com.htyoudao.youdao.module.system.api.permission.RoleApi;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 角色 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author kyle
 */
@Component
public class BpmTaskCandidateRoleStrategy implements BpmTaskCandidateStrategy {

    @DubboReference
    private RoleApi roleApi;
    @DubboReference
    private PermissionApi permissionApi;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.ROLE;
    }

    @Override
    public void validateParam(String param) {
        Set<Long> roleIds = StrUtils.splitToLongSet(param);
        roleApi.validRoleList(roleIds);
    }

    @Override
    public Set<Long> calculateUsers(String param) {
        Set<Long> roleIds = StrUtils.splitToLongSet(param);
        return permissionApi.getUserRoleIdListByRoleIds(roleIds).getCheckedData();
    }

}
