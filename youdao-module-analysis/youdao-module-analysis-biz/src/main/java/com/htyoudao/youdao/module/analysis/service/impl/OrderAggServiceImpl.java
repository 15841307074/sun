package com.htyoudao.youdao.module.analysis.service.impl;


import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.*;

import com.htyoudao.youdao.module.analysis.controller.admin.vo.RangeTimeRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationCurrentRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.GeneralRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisRatioResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.general.AnalysisEntryVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.order.AnalysisOrderVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.order.OrderAmountRangeVO;
import com.htyoudao.youdao.module.analysis.dal.redis.AnalysisMetricsRedisDao;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import com.htyoudao.youdao.module.analysis.enums.OrderForm;
import com.htyoudao.youdao.module.analysis.service.IAggregationService;
import com.htyoudao.youdao.module.analysis.service.IEsAggregationService;
import com.htyoudao.youdao.module.analysis.service.IOrderAggregationService;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import com.htyoudao.youdao.module.analysis.service.dto.RangeDTO;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class OrderAggServiceImpl implements IOrderAggregationService {

    @Resource
    private IEsAggregationService service;

    @Resource
    private IAggregationService aggregationService;

    @Resource
    private AnalysisMetricsRedisDao metricsRedisDao;


    @Override
    public AnalysisVO<AnalysisOrderVO> query(AggregationRequestVO requestVO) {

        List<MetricsConfig> metrics = List.of(VALID_ORDERS, CANTEEN_FOOD_ORDERS ,TAKEAWAY_ORDERS ,PACK_ORDERS
            , CUSTOMER_COUNT, AVERAGE_PAYMENT, PAY_AMOUNT, INVALID_ORDERS, CASH_PAY_ORDERS);

        Integer orderFrom = requestVO.getOrderFrom();
        boolean showUv = OrderForm.WECHAT.getForm().equals(orderFrom) || OrderForm.ALIPAY.getForm().equals(orderFrom);

        AnalysisVO<Map<String, Double>> query = aggregationService.query(requestVO, metrics, showUv);
        AnalysisOrderVO currentVO = new AnalysisOrderVO(query.getCurrent());
        AnalysisOrderVO beforeVO = new AnalysisOrderVO(query.getBefore());
        return new AnalysisVO<>(currentVO, beforeVO);
    }


    @Override
    public AnalysisVO<AnalysisEntryVO> generalView(GeneralRequestVO requestVO) {
        if (requestVO.getMetrics().contains("repeatBuyersRate")){
            requestVO.getMetrics().add("repeatBuyers");
            requestVO.getMetrics().add("customerCount");
            requestVO.getMetrics().remove("repeatBuyersRate");
        }

        boolean uv = requestVO.getMetrics().contains("orderCountRate");
        if (uv){
            requestVO.getMetrics().add("customerCount");
            requestVO.getMetrics().remove("orderCountRate");
        }

        List<MetricsConfig> metrics = requestVO.getMetrics().stream()
            .map(MetricsConfig::getEnumByCode)
            .filter(Objects::nonNull)
            .toList();

        //查询
        AggregationRequestVO aggregationRequestVO = new AggregationPageRequest();
        BeanUtils.copyProperties(requestVO, aggregationRequestVO);
        AnalysisVO<Map<String, Double>> query = aggregationService.query(aggregationRequestVO, metrics, uv);
        AnalysisEntryVO currentVO = new AnalysisEntryVO(query.getCurrent());
        AnalysisEntryVO beforeVO = new AnalysisEntryVO(query.getBefore());
        return new AnalysisVO<>(currentVO, beforeVO);
    }


    @Override
    public OrderAmountRangeVO orderAmountRange(AggregationCurrentRequestVO requestVO) {

        //优惠前总额,打包费,配送费
        List<MetricsConfig> metrics = List.of(ORDER_AMOUNT, PACKING_CHARGE, MINIMUM_DELIVERY_FEE);
        Map<String, Double> currentResult = service.batchAggregateMetrics(requestVO.buildCurrentRequest(), metrics);
        OrderAmountRangeVO rangeVO = new OrderAmountRangeVO();
        double packingCharge = currentResult.getOrDefault(PACKING_CHARGE.getCode(), 0.0);
        double minimumDeliveryFee = currentResult.getOrDefault(MINIMUM_DELIVERY_FEE.getCode(), 0.0);
        double orderAmount = currentResult.getOrDefault(ORDER_AMOUNT.getCode(), 0.0) - packingCharge - minimumDeliveryFee;
        rangeVO.setOrderAmount(orderAmount);
        rangeVO.setPackingCharge(packingCharge);
        rangeVO.setMinimumDeliveryFee(minimumDeliveryFee);
        return rangeVO;
    }



    @Override
    public AnalysisRatioResult orderRangeTime(RangeTimeRequestVO requestVO) {
        EsAggDTO currentRequest = requestVO.getAggregationRequest().buildCurrentRequest();
        EsAggDTO beforeRequest = requestVO.getAggregationRequest().buildBeforeRequest();

        List<RangeDTO> list = requestVO.getRangeHours();

        MetricsConfig metricsConfig = getEnumByCode(requestVO.getCode());

        Map<String, Double> requestResult = service.rangeList(currentRequest, "star", metricsConfig, list);
        Map<String, Double> beforeResult = service.rangeList(beforeRequest, "star", metricsConfig, list);

        return new AnalysisRatioResult(requestResult, beforeResult, list);

    }

    @Override
    public AnalysisRatioResult getOrderTimeRangeVO(AggregationRequestVO requestVO, MetricsConfig metricsConfig) {
        EsAggDTO currentRequest = requestVO.buildCurrentRequest();
        EsAggDTO beforeRequest = requestVO.buildBeforeRequest();

        List<RangeDTO> list = new ArrayList<>();
        list.add(new RangeDTO("早餐", 4.0, 10.0));
        list.add(new RangeDTO("午餐", 10.0, 14.0));
        list.add(new RangeDTO("下午茶",14.0, 17.0));
        list.add(new RangeDTO("晚餐",17.0, 20.0));
        list.add(new RangeDTO("夜宵",20.0, 24.0));

        Map<String, Double> requestResult = service.rangeList(currentRequest, "star", metricsConfig, list);
        Map<String, Double> beforeResult = service.rangeList(beforeRequest, "star", metricsConfig, list);

        return new AnalysisRatioResult(requestResult, beforeResult, list);
    }


    @Override
    public AnalysisRatioResult orderPayRange(AggregationRequestVO requestVO, MetricsConfig metricsConfig) {
        EsAggDTO currentRequest = requestVO.buildCurrentRequest();
        List<RangeDTO> list = new ArrayList<>();
        list.add(new RangeDTO("0-7元", 0.0, 7.0));
        list.add(new RangeDTO("7-14元",7.0, 14.0));
        list.add(new RangeDTO("14-21元",14.0, 21.0));
        list.add(new RangeDTO("21元+",21.0, null));

        EsAggDTO beforeRequest = requestVO.buildBeforeRequest();
        Map<String, Double> currentResult = service.rangeList(currentRequest, "payAmount", metricsConfig, list);
        Map<String, Double> beforeResult = service.rangeList(beforeRequest, "payAmount", metricsConfig, list);
        return new AnalysisRatioResult(currentResult, beforeResult, list);
    }


}
