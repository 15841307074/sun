package com.htyoudao.youdao.module.promotion.service.activityJD;

import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.ActivityJDEventReqVO;

import java.util.Map;

import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.ActivityJdRequestVO;

public interface ActivityJDAnalysisService {
    Map<String, Object> getLotteryLogAnalysis(String id);

    Map<String, Object> getLotteryLogDailyAnalysis(ActivityJDEventReqVO activityJDEventReqVO);


    Map<String, Double> query(ActivityJdRequestVO requestVO);
}
