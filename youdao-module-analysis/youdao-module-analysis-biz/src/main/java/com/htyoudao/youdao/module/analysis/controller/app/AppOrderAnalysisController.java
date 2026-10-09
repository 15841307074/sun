package com.htyoudao.youdao.module.analysis.controller.app;


import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.PAY_AMOUNT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.VALID_ORDERS;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.RangeTimeRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationCurrentRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.LineChartRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisChartVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisRatioResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.order.AnalysisOrderVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.order.OrderAmountRangeVO;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.service.IAggregationService;
import com.htyoudao.youdao.module.analysis.service.IOrderAggregationService;
import com.htyoudao.youdao.module.analysis.service.IStoreAggService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DC - 订单")
@RestController
@RequestMapping("/analysis/app/order")
public class AppOrderAnalysisController {

    @Resource
    private IAggregationService aggregationService;

    @Resource
    private IOrderAggregationService orderAggregationService;

    @Resource
    private IStoreAggService storeAggService;

    @Operation(summary = "订单指标")
    @PostMapping("/view")
    @PermitAll
    public CommonResult<AnalysisVO<AnalysisOrderVO>> appOrderView(@RequestBody @Valid AggregationRequestVO requestVO) {
        if (CollectionUtils.isEmpty(requestVO.getStoreIds())){
            throw exception(ErrorCodeConstants.STORE_IS_EMPTY);
        }
        return CommonResult.success(orderAggregationService.query(requestVO));
    }


    @Operation(summary = "折线图")
    @PostMapping("/chart")
    @PermitAll
    public CommonResult<AnalysisChartVO> appOrderLineChart(@RequestBody @Valid LineChartRequestVO requestVO) {
        if (CollectionUtils.isEmpty(requestVO.getStoreIds())){
            throw exception(ErrorCodeConstants.STORE_IS_EMPTY);
        }
        return CommonResult.success(aggregationService.lineChart(requestVO));
    }


    @Operation(summary = "订单分析-收入构成")
    @PostMapping("/range/amount")
    @PermitAll
    public CommonResult<OrderAmountRangeVO> appOrderAmountRange(@RequestBody @Valid AggregationCurrentRequestVO requestVO) {
        if (CollectionUtils.isEmpty(requestVO.getStoreIds())){
            throw exception(ErrorCodeConstants.STORE_IS_EMPTY);
        }
        return CommonResult.success(orderAggregationService.orderAmountRange(requestVO));
    }

    @Operation(summary = "订单分析-时间段分布-订单量")
    @PostMapping("/range/time/valid/orders")
    @PermitAll
    public CommonResult<AnalysisRatioResult> appOrderTimeValidOrdersRange(@RequestBody @Valid AggregationRequestVO requestVO) {
        if (CollectionUtils.isEmpty(requestVO.getStoreIds())){
            throw exception(ErrorCodeConstants.STORE_IS_EMPTY);
        }
        return CommonResult.success(orderAggregationService.getOrderTimeRangeVO(requestVO, VALID_ORDERS));
    }

    @Operation(summary = "订单分析-时间段分布-顾客实付")
    @PostMapping("/range/time/pay/amount")
    @PermitAll
    public CommonResult<AnalysisRatioResult> appOrderTimePayAmountRange(@RequestBody @Valid AggregationRequestVO requestVO) {
        if (CollectionUtils.isEmpty(requestVO.getStoreIds())){
            throw exception(ErrorCodeConstants.STORE_IS_EMPTY);
        }
        return CommonResult.success(orderAggregationService.getOrderTimeRangeVO(requestVO, PAY_AMOUNT));
    }

    @Operation(summary = "订单分析-时间段分布-自定义时间段,自定义指标")
    @PostMapping("/range/time")
    @PermitAll
    public CommonResult<AnalysisRatioResult> appOrderRangeTime(@RequestBody @Valid RangeTimeRequestVO requestVO) {
        if (CollectionUtils.isEmpty(requestVO.getAggregationRequest().getStoreIds())){
            throw exception(ErrorCodeConstants.STORE_IS_EMPTY);
        }
        return CommonResult.success(orderAggregationService.orderRangeTime(requestVO));
    }


    @Operation(summary = "订单分析-实付分布")
    @PostMapping("/range/pay")
    @PermitAll
    public CommonResult<AnalysisRatioResult> appOrderPayRange(@RequestBody @Valid AggregationRequestVO requestVO) {
        if (CollectionUtils.isEmpty(requestVO.getStoreIds())){
            throw exception(ErrorCodeConstants.STORE_IS_EMPTY);
        }
        return CommonResult.success(orderAggregationService.orderPayRange(requestVO, VALID_ORDERS));
    }

}
