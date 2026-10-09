package com.htyoudao.youdao.module.analysis.controller.admin;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.LineChartRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.StorePageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisChartVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStoreVO;
import com.htyoudao.youdao.module.analysis.dal.es.ScmOrderDetailDocument;
import com.htyoudao.youdao.module.analysis.service.IScfOrderFullService;
import com.htyoudao.youdao.module.analysis.service.IStoreAggService;
import com.htyoudao.youdao.module.analysis.service.impl.ScfOrderFullServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "门店")
@RestController
@RequestMapping("/analysis/store")
public class StoreAnalysisController {


    @Resource
    private IStoreAggService aggregationService;


    @Resource
    private IScfOrderFullService orderFullService;

    @Operation(summary = "核心指标概述")
    @PostMapping("/general/view")
    public CommonResult<AnalysisVO<AnalysisStoreVO>> storeGeneralView(@RequestBody @Valid AggregationRequestVO requestVO) {
        return CommonResult.success(aggregationService.query(requestVO));
    }

    @Operation(summary = "核心指标折线图")
    @PostMapping("/general/chart")
    public CommonResult<AnalysisChartVO> storeGeneralChart(@RequestBody @Valid LineChartRequestVO requestVO) {
        return CommonResult.success(aggregationService.generalChart(requestVO));
    }

    @Operation(summary = "门店分页")
    @PostMapping("/page")
    public CommonResult<PageResult<AnalysisStorePageVO>> storePage(@RequestBody @Valid AggregationPageRequest requestVO) {
        extracted(requestVO);

        return CommonResult.success(aggregationService.storePage(requestVO));
    }

    @Operation(summary = "城市分页")
    @PostMapping("/city/page")
    public CommonResult<PageResult<AnalysisStorePageVO>> cityPage(@RequestBody @Valid AggregationPageRequest requestVO) {
        extracted(requestVO);

        return CommonResult.success(aggregationService.cityPage(requestVO));
    }

    private void extracted(AggregationPageRequest requestVO) {
        if (!CollectionUtils.isEmpty(requestVO.getMetrics()) && requestVO.getMetrics().contains("UV")){
            requestVO.setShowUV(true);
            requestVO.getMetrics().remove("UV");
        }

        if (!CollectionUtils.isEmpty(requestVO.getMetrics()) && requestVO.getMetrics().contains("GYL_ORDER_AMOUNT")){
            requestVO.setShowGYL(true);
            requestVO.getMetrics().remove("GYL_ORDER_AMOUNT");
        }
    }

    @Operation(summary = "组织数据")
    @PostMapping("/org/page")
    public CommonResult<PageResult<AnalysisStorePageVO>> orgPage(@RequestBody @Valid StorePageRequest requestVO) {
        extracted(requestVO);
        return CommonResult.success(aggregationService.orgPage(requestVO));
    }


    @Operation(summary = "获取供应链订货明细")
    @PostMapping("/gyl/order/list")
    public CommonResult<List<ScmOrderDetailDocument>> gylOrderList(@RequestBody @Valid AggregationRequestVO requestVO) {
        return CommonResult.success(orderFullService.gylOrderList(requestVO.getStoreIds(), requestVO.getCurrentTimeStart(), requestVO.getCurrentTimeEnd()));
    }
}
