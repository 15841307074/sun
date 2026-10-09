package com.htyoudao.youdao.module.bpm.framework.flowable.core.candidate.strategy.user;

import com.htyoudao.youdao.framework.common.util.string.StrUtils;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import com.htyoudao.youdao.module.system.api.dept.PostApi;
import com.htyoudao.youdao.module.system.api.user.AdminUserApi;
import com.htyoudao.youdao.module.system.api.user.dto.AdminUserRespDTO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 岗位 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author kyle
 */
@Component
public class BpmTaskCandidatePostStrategy implements BpmTaskCandidateStrategy {

    @DubboReference
    private PostApi postApi;
    @DubboReference
    private AdminUserApi adminUserApi;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.POST;
    }

    @Override
    public void validateParam(String param) {
        Set<Long> postIds = StrUtils.splitToLongSet(param);
        postApi.validPostList(postIds);
    }

    @Override
    public Set<Long> calculateUsers(String param) {
        Set<Long> postIds = StrUtils.splitToLongSet(param);
        // List<AdminUserRespDTO> users = adminUserApi.getUserListByPostIds(postIds).getCheckedData();
        List<AdminUserRespDTO> users = new ArrayList<>();
        return convertSet(users, AdminUserRespDTO::getId);
    }

}
