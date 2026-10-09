package com.htyoudao.youdao.module.analysis.controller.app;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.StorePageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStoreVO;
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

@Tag(name = "DC - 门店")
@RestController
@RequestMapping("/analysis/app/store")
public class AppStoreAnalysisController {


    @Resource
    private IStoreAggService aggregationService;

    @Operation(summary = "核心指标概述")
    @PostMapping("/general/view")
    @PermitAll
    public CommonResult<AnalysisVO<AnalysisStoreVO>> appStoreGeneralView(@RequestBody @Valid AggregationRequestVO requestVO) {
        return CommonResult.success(aggregationService.query(requestVO));
    }

    @Operation(summary = "门店分页")
    @PostMapping("/page")
    @PermitAll
    public CommonResult<PageResult<AnalysisStorePageVO>> appStorePage(@RequestBody @Valid AggregationPageRequest requestVO) {
        if (!CollectionUtils.isEmpty(requestVO.getMetrics())){
            requestVO.setShowUV(requestVO.getMetrics().contains("UV"));
        }
        return CommonResult.success(aggregationService.storePage(requestVO));
    }

}
