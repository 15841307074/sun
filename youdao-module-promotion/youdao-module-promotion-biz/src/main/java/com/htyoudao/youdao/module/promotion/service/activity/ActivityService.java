package com.htyoudao.youdao.module.promotion.service.activity;


import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.promotion.controller.admin.activity.vo.ActivityDataRespVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityPageReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityNjnz.vo.ActivityPageRespVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;

import java.util.List;
import java.util.Map;
import java.util.Set;


public interface ActivityService {

    PageResult<ActivityPageRespVO> getPage(ActivityPageReqVO activityPageReqVO);

    Long createActivity(ActivityDO activityDO);

    void updateActivity(ActivityDO activityDO);

    void deleteActivity(Long id);

    void updateStatus(Long id , Integer status);

    ActivityDO selectById(Long id);

    PageResult<ActivityPageRespVO> selectActivityList(ActivityPageReqVO activityPageReqVO);

    Map<Long, String> selectActivityByIds(Set<Long> activityIds);
}
