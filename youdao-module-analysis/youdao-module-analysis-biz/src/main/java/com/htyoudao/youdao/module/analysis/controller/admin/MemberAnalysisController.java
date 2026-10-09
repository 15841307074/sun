package com.htyoudao.youdao.module.analysis.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggMemberRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.LineChartRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.LineChatMemberRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisChartVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisRatioResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.member.AnalysisExpressVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.member.AnalysisMemberVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.member.AnalysisMemberVisitOrderVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.member.AnalysisSettlementVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.controller.app.EventController;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.enums.EventType;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import com.htyoudao.youdao.module.analysis.service.IAggregationService;
import com.htyoudao.youdao.module.analysis.service.IEsAggregationService;
import com.htyoudao.youdao.module.analysis.service.IEventService;
import com.htyoudao.youdao.module.analysis.service.IMemberAggregationService;
import com.htyoudao.youdao.module.analysis.service.IStoreAggService;
import com.htyoudao.youdao.module.analysis.service.dto.EventQueryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.*;

/**
 * @author dht
 */

@Tag(name = "顾客")
@RestController
@RequestMapping("/analysis/member")
public class MemberAnalysisController {

    @Resource
    private IAggregationService aggregationService;

    @Resource
    private IEsAggregationService service;

    @Resource
    private IMemberAggregationService memberAggregationService;

    @Resource
    private IStoreAggService storeAggService;

    @Resource
    private IEventService eventService;


    @Operation(summary = "顾客指标")
    @PostMapping("/view")
    public CommonResult<AnalysisVO<AnalysisMemberVO>> memberView(@RequestBody @Valid AggMemberRequestVO requestVO) {
        return CommonResult.success(memberAggregationService.query(requestVO));
    }


    @Operation(summary = "折线图")
    @PostMapping("/chart")
    public CommonResult<AnalysisChartVO> memberLineChart(@RequestBody @Valid LineChatMemberRequestVO requestVO) {
        LineChartRequestVO lineChart = BeanUtil.toBean(requestVO.buildBaseRequest(), LineChartRequestVO.class);
        lineChart.setCode(requestVO.getCode());

        if (requestVO.getType().equals(1)) {
            lineChart.setType("HOUR");
        }else {
            lineChart.setType("DAY");
        }

        return CommonResult.success(aggregationService.lineChart(lineChart));
    }


    @Operation(summary = "顾客分布-下单频次")
    @PostMapping("/range/frequency")
    public CommonResult<AnalysisRatioResult> memberFrequencyRange(@RequestBody @Valid AggMemberRequestVO requestVO) {
        return CommonResult.success(memberAggregationService.frequencyRange(requestVO, CUSTOMER_COUNT));
    }

    @Operation(summary = "顾客分布-下单间隔")
    @PostMapping("/range/delay/days")
    public CommonResult<AnalysisRatioResult> memberDelayDaysRange(@RequestBody @Valid AggMemberRequestVO requestVO) {
        return CommonResult.success(memberAggregationService.memberDelayDaysRange(requestVO, CUSTOMER_COUNT));
    }

    @Operation(summary = "顾客分布-单均实付")
    @PostMapping("/range/pay")
    public CommonResult<AnalysisRatioResult> memberPayRange(@RequestBody @Valid AggMemberRequestVO requestVO) {
        AggregationRequestVO baseRequest = requestVO.buildBaseRequest();
        return CommonResult.success(memberAggregationService.storePayRange(baseRequest, CUSTOMER_COUNT));
    }


    @Operation(summary = "饼图-新老占比")
    @PostMapping("/express/chart")
    public CommonResult<AnalysisVO<AnalysisExpressVO>> expressChart(@RequestBody @Valid AggMemberRequestVO reqVO) {
        List<MetricsConfig> metrics = List.of(
            OLD_CUSTOMER_COUNT,
            NEW_CUSTOMER_COUNT
        );
        AnalysisVO<Map<String, Double>> query = aggregationService.query(reqVO.buildBaseRequest(), metrics, false);
        AnalysisExpressVO currentVO = BeanUtil.toBean(query.getCurrent(), AnalysisExpressVO.class);
        AnalysisExpressVO beforeVO = BeanUtil.toBean(query.getBefore(), AnalysisExpressVO.class);
        return CommonResult.success(new AnalysisVO<>(currentVO, beforeVO));
    }


    @Operation(summary = "饼图-会员占比")
    @PostMapping("/settlement/chart")
    public CommonResult<AnalysisVO<AnalysisSettlementVO>> settlementChart(@RequestBody @Valid AggMemberRequestVO reqVO) {
        List<MetricsConfig> metrics = List.of(
            MEMBER_CUSTOMER_COUNT,
            NOT_MEMBER_CUSTOMER_COUNT
        );
        AnalysisVO<Map<String, Double>> query = aggregationService.query(reqVO.buildBaseRequest(), metrics, false);
        AnalysisSettlementVO currentVO = BeanUtil.toBean(query.getCurrent(), AnalysisSettlementVO.class);
        AnalysisSettlementVO beforeVO = BeanUtil.toBean(query.getBefore(), AnalysisSettlementVO.class);
        return CommonResult.success(new AnalysisVO<>(currentVO, beforeVO));
    }



    @Operation(summary = "门店topN")
    @PostMapping("/store/top/{code}")
    public CommonResult<List<AnalysisStorePageVO>> storeTopN(@PathVariable("code") String code,
        @RequestBody @Valid AggregationRequestVO requestVO) {
        MetricsConfig metricsConfig = MetricsConfig.getEnumByCode(code);
        if (metricsConfig == null) {
            return CommonResult.error(ErrorCodeConstants.METRIC_NOT_FOUND);
        }
        return CommonResult.success(storeAggService.storeTopN(requestVO, 10, metricsConfig, "desc").getList());
    }



    @Operation(summary = "城市topN")
    @PostMapping("/city/top/{code}")
    public CommonResult<List<AnalysisStorePageVO>> cityTopN(@PathVariable("code") String code,
        @RequestBody @Valid AggregationRequestVO requestVO) {
        MetricsConfig metricsConfig = MetricsConfig.getEnumByCode(code);
        if (metricsConfig == null) {
            return CommonResult.error(ErrorCodeConstants.METRIC_NOT_FOUND);
        }
        return CommonResult.success(storeAggService.cityTopN(requestVO, 10, metricsConfig, "desc").getList());
    }


    @Operation(summary = "用户分析-进店与下单数据")
    @PostMapping("/visit-order")
    public CommonResult<AnalysisMemberVisitOrderVO> memberVisitOrderAnalysis(@RequestBody @Valid AggregationRequestVO request) {
        List<MetricsConfig> metrics = List.of(
            MetricsConfig.NEW_CUSTOMER_COUNT,
            MetricsConfig.OLD_CUSTOMER_COUNT
        );

        Map<String, Double> currentVo = service.batchAggregateMetrics(request.buildCurrentRequest(), metrics);

        EventQueryDTO queryDTO = new EventQueryDTO();
        queryDTO.setEventType(EventType.IN_STORE);
        queryDTO.setStartTime(request.getCurrentTimeStart());
        queryDTO.setEndTime(request.getCurrentTimeEnd());
        queryDTO.setStoreIds(request.getStoreIds());
        queryDTO.setIsNew(true);
        Long newVisitorCount = eventService.queryUV(queryDTO);

        queryDTO.setIsNew(false);
        Long oldVisitorCount = eventService.queryUV(queryDTO);

        AnalysisMemberVisitOrderVO resultVo = new AnalysisMemberVisitOrderVO(currentVo);
        resultVo.setNewVisitorCount(newVisitorCount);
        resultVo.setOldVisitorCount(oldVisitorCount);
        resultVo.calculateRates();
        return CommonResult.success(resultVo);
    }



}
