package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActicitySeckillPageRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.LineChartRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivitySeckillMemberPageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivitySeckillMemberVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivitySeckillStorePageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisChartVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

public interface IAggregationSeckillService {


    AnalysisVO<Map<String,Double>> query(AggregationRequestVO requestVO, List<MetricsConfig> metrics, boolean uv);

    AnalysisChartVO lineChart(@Valid LineChartRequestVO requestVO);

    PageResult<AnalysisStorePageVO> groupPage(AggregationPageRequest requestVO, String groupField, List<MetricsConfig> metrics, Boolean showUv);

    PageResult<ActivitySeckillStorePageVO> activityGroupPage(ActicitySeckillPageRequestVO requestVO, String storeId, List<MetricsConfig> metrics, Boolean b,int type);

    Boolean activityData(ActicitySeckillPageRequestVO requestVO, String storeId, List<MetricsConfig> metrics, Boolean b,int type);

    PageResult<ActivitySeckillMemberVO> activityRecordsPage(List<MetricsConfig> metrics, ActicitySeckillPageRequestVO requestVO);
}
