package com.htyoudao.youdao.module.analysis.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggMemberRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisRatioResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.member.AnalysisMemberVO;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import com.htyoudao.youdao.module.analysis.service.IAggregationService;
import com.htyoudao.youdao.module.analysis.service.IEsAggregationService;
import com.htyoudao.youdao.module.analysis.service.IMemberAggregationService;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import com.htyoudao.youdao.module.analysis.service.dto.RangeDTO;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author dht
 */
@Service
public class MemberAggregationServiceImpl implements IMemberAggregationService {

    @Resource
    private IEsAggregationService service;

    @Resource
    private IAggregationService aggregationService;

    @Override
    public AnalysisVO<AnalysisMemberVO> query(AggMemberRequestVO requestVO) {
        List<MetricsConfig> metrics = List.of(
            MetricsConfig.CUSTOMER_COUNT,
            MetricsConfig.REPEAT_BUYERS,
            MetricsConfig.NEW_CUSTOMER_COUNT,
            MetricsConfig.OLD_CUSTOMER_COUNT,
            MetricsConfig.MEMBER_CUSTOMER_COUNT
        );

        // 切换新客,老客,会员的时候 下面 后面的3个数据没有
        List<Integer> userTypes = List.of(1, 2, 3);
        if (requestVO.getUserType() != null && userTypes.contains(requestVO.getUserType())) {
            metrics = List.of(
                MetricsConfig.CUSTOMER_COUNT,
                MetricsConfig.REPEAT_BUYERS
            );
        }

        AggregationRequestVO baseRequest = requestVO.buildBaseRequest();
        AnalysisVO<Map<String, Double>> query = aggregationService.query(baseRequest, metrics, false);
        AnalysisMemberVO currentVO = new AnalysisMemberVO(query.getCurrent());
        AnalysisMemberVO beforeVO = new AnalysisMemberVO(query.getBefore());
        return new AnalysisVO<>(currentVO, beforeVO);
    }

    @Override
    public AnalysisRatioResult memberDelayDaysRange(AggMemberRequestVO requestVO, MetricsConfig metricsConfig) {
        EsAggDTO currentRequest = requestVO.buildBaseRequest().buildCurrentRequest();
        List<RangeDTO> list = new ArrayList<>();
        list.add(new RangeDTO("0-10天", 0.0, 10.0));
        list.add(new RangeDTO("11-20天", 11.0, 20.0));
        list.add(new RangeDTO("21-30天",21.0, 30.0));
        Map<String, Double> stringDoubleMap = service.rangeList(currentRequest, "delayDays", metricsConfig, list);
        return new AnalysisRatioResult(stringDoubleMap);
    }

    @Override
    public AnalysisRatioResult frequencyRange(AggMemberRequestVO requestVO, MetricsConfig metricsConfig) {
        EsAggDTO currentRequest = requestVO.buildBaseRequest().buildCurrentRequest();
        //查询每个顾客的下单次数，然后统计每个次数的顾客数量
        Map<Long, Double> longDoubleMap = service.frequencyAggGroup(currentRequest, metricsConfig);
        Map<String, Double> result = convertFrequencyMap(longDoubleMap);
        return new AnalysisRatioResult(result);
    }

    @Override
    public AnalysisRatioResult storePayRange(AggregationRequestVO requestVO, MetricsConfig metricsConfig) {
        EsAggDTO currentRequest = requestVO.buildCurrentRequest();
        List<RangeDTO> list = new ArrayList<>();
        list.add(new RangeDTO("0-7元", 0.0, 7.0));
        list.add(new RangeDTO("7-14元",7.0, 14.0));
        list.add(new RangeDTO("14-21元",14.0, 21.0));
        list.add(new RangeDTO("21元+",21.0, null));

        Map<String, Double> currentResult = service.rangeList(currentRequest, "payAmount", metricsConfig, list);
        return new AnalysisRatioResult(currentResult);
    }

    private Map<String, Double> convertFrequencyMap(Map<Long, Double> longDoubleMap) {
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<Long, Double> entry : longDoubleMap.entrySet()) {
            Long frequency = entry.getKey();
            Double value = entry.getValue();
            if (value == null) value = 0.0;

            String bucket;
            if (frequency == 1L) {
                bucket = "1单";
            } else if (frequency == 2L || frequency == 3L) {
                bucket = "2-3单";
            } else {
                bucket = ">3单";
            }
            result.merge(bucket, value, Double::sum);
        }
        return result;
    }
}
