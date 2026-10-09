package com.htyoudao.youdao.module.analysis.controller.admin;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActicitytyNjnzPageRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActivityNjnzRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityNjnzOrderVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityNjnzStorePageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.order.AnalysisOrderVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.service.IActivityAggregationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "买n件n折订单")
@RestController
@RequestMapping("/analysis/activityNjnz")
@PermitAll
public class ActivityNjnzOrderAnalysisController {


    @Autowired
    private IActivityAggregationService activityAggregationService;

    @Operation(summary = "买n打n折活动订单指标")
    @PostMapping("/view")
    public CommonResult<Map<String, Double>> activityNjnzOrderView(@RequestBody @Valid ActivityNjnzRequestVO requestVO) {
        return CommonResult.success(activityAggregationService.query(requestVO));
    }


    @Operation(summary = "门店下数据分析")
    @PostMapping("/page")
    public CommonResult<PageResult<ActivityNjnzStorePageVO>> activityNjnzStorePage(@RequestBody @Valid ActicitytyNjnzPageRequestVO requestVO) {
        return CommonResult.success(activityAggregationService.activityNjnzStorePage(requestVO));
    }

    @PreAuthorize("@ss.hasPermission('promotion:activityNJNZ:export')")
    @Operation(summary = "数据分析导出")
    @PostMapping("/exportData")
    public CommonResult<Boolean> exportData(@RequestBody ActicitytyNjnzPageRequestVO requestVO) {
        try {
            activityAggregationService.exportData(requestVO);
        }catch (Exception e){
            throw new RuntimeException("导出失败");
        }
        return CommonResult.success(true);
    }


    @PreAuthorize("@ss.hasPermission('promotion:activityNJNZ:export')")
    @Operation(summary = "门店明细导出")
    @PostMapping("/exportStoreData")
    public CommonResult<Boolean> exportStoreData(@RequestBody ActicitytyNjnzPageRequestVO requestVO) {
        activityAggregationService.exportStoreData(requestVO);
        return CommonResult.success(true);
    }


}
