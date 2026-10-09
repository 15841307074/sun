package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.*;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityNjnzOrderVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityNjnzStorePageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisChartVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

public interface IActivityAggregationService {


    Map<String, Double> query(ActivityNjnzRequestVO requestVO);

    PageResult<ActivityNjnzStorePageVO> activityNjnzStorePage(@Valid ActicitytyNjnzPageRequestVO requestVO);

    Boolean exportData(ActicitytyNjnzPageRequestVO requestVO);

    void exportStoreData(ActicitytyNjnzPageRequestVO requestVO);
}
