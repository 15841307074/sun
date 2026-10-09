package com.htyoudao.youdao.module.promotion.service.activity;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.util.TimeValidationUtil;
import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;

/** 查询当前门店、日期和时段内可用的活动主数据。 */
@Service
public class ApplicableActivityQueryService {

    @Resource
    private ActivityMapper activityMapper;

    public List<ActivityDO> selectApplicable(Long storeId, Collection<Integer> activityTypes) {
        if (storeId == null || activityTypes == null || activityTypes.isEmpty()) {
            return List.of();
        }
        LocalDate now = LocalDate.now();
        LambdaQueryWrapperX<ActivityDO> query = new LambdaQueryWrapperX<>();
        query.in(ActivityDO::getActivityType, activityTypes)
                .eq(ActivityDO::getIsEnabled, 1)
                .le(ActivityDO::getStartDate, now)
                .ge(ActivityDO::getEndDate, now)
                .apply("a.business_id = {0}", BusinessContextHolder.getRequiredBusinessId());
        return activityMapper.listQuery(query, List.of(storeId)).stream()
                .filter(activity -> TimeValidationUtil.isTimeValid(
                        activity.getStartDate(), activity.getEndDate(), activity.getDayNumbers(),
                        activity.getWeekNumbers(), activity.getTimeRange()))
                .toList();
    }
}
