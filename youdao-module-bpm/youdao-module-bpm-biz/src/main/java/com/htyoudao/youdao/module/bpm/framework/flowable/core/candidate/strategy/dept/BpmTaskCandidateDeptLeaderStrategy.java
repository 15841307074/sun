package com.htyoudao.youdao.module.bpm.framework.flowable.core.candidate.strategy.dept;

import com.htyoudao.youdao.framework.common.util.string.StrUtils;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.candidate.BpmTaskCandidateStrategy;
import com.htyoudao.youdao.module.bpm.framework.flowable.core.enums.BpmTaskCandidateStrategyEnum;
import com.htyoudao.youdao.module.system.api.dept.DeptApi;
import com.htyoudao.youdao.module.system.api.dept.dto.DeptRespDTO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.util.collection.CollectionUtils.convertSet;

/**
 * 部门的负责人 {@link BpmTaskCandidateStrategy} 实现类
 *
 * @author kyle
 */
@Component
public class BpmTaskCandidateDeptLeaderStrategy implements BpmTaskCandidateStrategy {

    @DubboReference
    private DeptApi deptApi;

    @Override
    public BpmTaskCandidateStrategyEnum getStrategy() {
        return BpmTaskCandidateStrategyEnum.DEPT_LEADER;
    }

    @Override
    public void validateParam(String param) {
        Set<Long> deptIds = StrUtils.splitToLongSet(param);
        deptApi.validateDeptList(deptIds).checkError();
    }

    @Override
    public Set<Long> calculateUsers(String param) {
        Set<Long> deptIds = StrUtils.splitToLongSet(param);
        List<DeptRespDTO> depts = deptApi.getDeptList(deptIds).getCheckedData();
        return convertSet(depts, DeptRespDTO::getLeaderUserId);
    }

}
