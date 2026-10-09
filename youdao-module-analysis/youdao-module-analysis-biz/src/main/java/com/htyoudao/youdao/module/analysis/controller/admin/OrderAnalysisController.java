package com.htyoudao.youdao.module.analysis.controller.admin;


import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.PAY_AMOUNT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.VALID_ORDERS;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.RangeTimeRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationCurrentRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.LineChartRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisChartVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisRatioResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.order.AnalysisOrderVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.order.OrderAmountRangeVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import com.htyoudao.youdao.module.analysis.service.IAggregationService;
import com.htyoudao.youdao.module.analysis.service.IChOrderAggregationService;
import com.htyoudao.youdao.module.analysis.service.IOrderAggregationService;
import com.htyoudao.youdao.module.analysis.service.IStoreAggService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "订单")
@RestController
@RequestMapping("/analysis/order")
public class OrderAnalysisController {

    @Resource
    private IAggregationService aggregationService;

    @Resource
    private IOrderAggregationService orderAggregationService;

    @Resource
    private IStoreAggService storeAggService;

    @Resource
    private IChOrderAggregationService chOrderAggregationService;

    @Operation(summary = "订单指标")
    @PostMapping("/view")
    public CommonResult<AnalysisVO<AnalysisOrderVO>> orderView(@RequestBody @Valid AggregationRequestVO requestVO) {
        return CommonResult.success(orderAggregationService.query(requestVO));
    }

    @Operation(summary = "订单指标(ClickHouse)")
    @PostMapping("/view/new")
    public CommonResult<AnalysisVO<AnalysisOrderVO>> orderViewNew(@RequestBody @Valid AggregationRequestVO requestVO) {
        return CommonResult.success(chOrderAggregationService.queryFromCh(requestVO));
    }


    @Operation(summary = "折线图")
    @PostMapping("/chart")
    public CommonResult<AnalysisChartVO> orderLineChart(@RequestBody @Valid LineChartRequestVO requestVO) {
        return CommonResult.success(aggregationService.lineChart(requestVO));
    }


    @Operation(summary = "订单分析-收入构成")
    @PostMapping("/range/amount")
    public CommonResult<OrderAmountRangeVO> orderAmountRange(@RequestBody @Valid AggregationCurrentRequestVO requestVO) {
        return CommonResult.success(orderAggregationService.orderAmountRange(requestVO));
    }

    @Operation(summary = "订单分析-时间段分布-订单量")
    @PostMapping("/range/time/valid/orders")
    public CommonResult<AnalysisRatioResult> orderTimeValidOrdersRange(@RequestBody @Valid AggregationRequestVO requestVO) {
        return CommonResult.success(orderAggregationService.getOrderTimeRangeVO(requestVO, VALID_ORDERS));
    }

    @Operation(summary = "订单分析-时间段分布-顾客实付")
    @PostMapping("/range/time/pay/amount")
    public CommonResult<AnalysisRatioResult> orderTimePayAmountRange(@RequestBody @Valid AggregationRequestVO requestVO) {
        return CommonResult.success(orderAggregationService.getOrderTimeRangeVO(requestVO, PAY_AMOUNT));
    }


    @Operation(summary = "订单分析-时间段分布-自定义时间段,自定义指标")
    @PostMapping("/range/time")
    public CommonResult<AnalysisRatioResult> orderRangeTime(@RequestBody @Valid RangeTimeRequestVO requestVO) {
        return CommonResult.success(orderAggregationService.orderRangeTime(requestVO));
    }



    @Operation(summary = "订单分析-实付分布")
    @PostMapping("/range/pay")
    public CommonResult<AnalysisRatioResult> orderRangeTime(@RequestBody @Valid AggregationRequestVO requestVO) {
        return CommonResult.success(orderAggregationService.orderPayRange(requestVO, VALID_ORDERS));
    }



    @Operation(summary = "门店top10")
    @PostMapping("/store/top/{code}")
    public CommonResult<List<AnalysisStorePageVO>> storeTop10(@PathVariable("code") String code,
        @RequestBody @Valid AggregationRequestVO requestVO) {
        MetricsConfig metricsConfig = MetricsConfig.getEnumByCode(code);
        if (metricsConfig == null) {
            return CommonResult.error(ErrorCodeConstants.METRIC_NOT_FOUND);
        }
        return CommonResult.success(storeAggService.storeTopN(requestVO, 10, metricsConfig, "desc").getList());
    }




    @Operation(summary = "城市top10")
    @PostMapping("/city/top/{code}")
    public CommonResult<List<AnalysisStorePageVO>> cityTop10(@PathVariable("code") String code,
        @RequestBody @Valid AggregationRequestVO requestVO) {
        MetricsConfig metricsConfig = MetricsConfig.getEnumByCode(code);
        if (metricsConfig == null) {
            return CommonResult.error(ErrorCodeConstants.METRIC_NOT_FOUND);
        }
        return CommonResult.success(storeAggService.cityTopN(requestVO, 10, metricsConfig, "desc").getList());
    }



    @Operation(summary = "门店倒数10")
    @PostMapping("/store/asc/{code}")
    public CommonResult<PageResult<AnalysisStorePageVO>> storeAsc(@PathVariable("code") String code,
        @RequestBody @Valid AggregationRequestVO requestVO) {
        MetricsConfig metricsConfig = MetricsConfig.getEnumByCode(code);
        if (metricsConfig == null) {
            return CommonResult.error(ErrorCodeConstants.METRIC_NOT_FOUND);
        }
        return CommonResult.success(storeAggService.storeTopN(requestVO, 10, metricsConfig, "asc"));
    }


    @Operation(summary = "城市倒数10")
    @PostMapping("/city/asc/{code}")
    public CommonResult<PageResult<AnalysisStorePageVO>> cityAsc(@PathVariable("code") String code,
        @RequestBody @Valid AggregationRequestVO requestVO) {
        MetricsConfig metricsConfig = MetricsConfig.getEnumByCode(code);
        if (metricsConfig == null) {
            return CommonResult.error(ErrorCodeConstants.METRIC_NOT_FOUND);
        }
        return CommonResult.success(storeAggService.cityTopN(requestVO, 10, metricsConfig, "asc"));
    }






}
