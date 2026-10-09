package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActicitySeckillPageRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActivitySeckillRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivitySeckillMemberPageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivitySeckillMemberVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivitySeckillStorePageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.AnalysisSeckillChartVO;
import com.htyoudao.youdao.module.analysis.service.dto.EventQueryDTO;
import jakarta.validation.Valid;

import java.util.Map;

public interface IActivitySeckillService {
    

    Map<String, Double> query(ActivitySeckillRequestVO requestVO);

    PageResult<ActivitySeckillStorePageVO> activitySeckillStorePage(@Valid ActicitySeckillPageRequestVO requestVO,int type);

    Boolean exportData(ActicitySeckillPageRequestVO requestVO);

    void exportStoreData(ActicitySeckillPageRequestVO requestVO);

    void exportChannleDetailData(ActicitySeckillPageRequestVO requestVO);

    PageResult<ActivitySeckillMemberVO> activitySeckillRecordsPage(ActicitySeckillPageRequestVO requestVO);

    Map<String, Double> activitySeckillRecordsTotel(ActicitySeckillPageRequestVO requestVO);

    Boolean exportChannleData(ActicitySeckillPageRequestVO requestVO);

    AnalysisSeckillChartVO lineChart(@Valid EventQueryDTO requestVO);


}
