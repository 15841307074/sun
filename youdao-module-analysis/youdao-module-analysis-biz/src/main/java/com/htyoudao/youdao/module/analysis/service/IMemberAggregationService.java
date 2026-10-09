package com.htyoudao.youdao.module.analysis.service;

import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggMemberRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisRatioResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.member.AnalysisMemberVO;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import jakarta.validation.Valid;

/**
 * @author dht
 */
public interface IMemberAggregationService {


    AnalysisVO<AnalysisMemberVO> query(@Valid AggMemberRequestVO requestVO);

    /**
     * 下单间隔
     * @param requestVO
     * @return
     */
    AnalysisRatioResult memberDelayDaysRange(@Valid AggMemberRequestVO requestVO, MetricsConfig metricsConfig);


    /**
     * 下单频次
     * @param requestVO
     * @param metricsConfig
     * @return
     */
    AnalysisRatioResult frequencyRange(@Valid AggMemberRequestVO requestVO, MetricsConfig metricsConfig);

    AnalysisRatioResult storePayRange(AggregationRequestVO baseRequest, MetricsConfig metricsConfig);

}
