package com.htyoudao.youdao.module.promotion.service.activity;

import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;

public interface ActivityAppService {
    Boolean checkCanJoin(Long activityId);

    Boolean checkSurveyCanJoin(Long id);

    /**
     * 使用已加载的活动信息校验社群参与资格。
     */
    Boolean checkCanJoin(ActivityDO activityDO);
}
