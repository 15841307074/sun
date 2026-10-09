package com.htyoudao.youdao.module.analysis.controller.admin;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActicitytyNjnzPageRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActivityNjnzRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityMzStorePageRespVO;
import com.htyoudao.youdao.module.analysis.service.IActivityAggregationService;
import com.htyoudao.youdao.module.analysis.service.IActivityMzAggregationService;
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

@Tag(name = "满赠活动订单")
@RestController
@RequestMapping("/analysis/activityMz")
public class ActivityMzOrderAnalysisController {

    @Autowired
    private IActivityAggregationService activityAggregationService;

    @Autowired
    private IActivityMzAggregationService activityMzAggregationService;

//    @PermitAll
    @Operation(summary = "满赠活动订单指标")
    @PostMapping("/view")
    public CommonResult<Map<String, Double>> activityMzOrderView(@RequestBody @Valid ActivityNjnzRequestVO requestVO) {
        return CommonResult.success(activityAggregationService.query(requestVO));
    }

//    @PermitAll
    @Operation(summary = "门店下数据分析")
    @PostMapping("/page")
    public CommonResult<PageResult<ActivityMzStorePageRespVO>> activityMzStorePage(@RequestBody @Valid ActicitytyNjnzPageRequestVO requestVO) {
        return CommonResult.success(activityMzAggregationService.mzStorePage(requestVO));
    }

//    @PreAuthorize("@ss.hasPermission('promotion:activityMz:export')")
    @Operation(summary = "数据分析导出")
    @PostMapping("/exportData")
    public CommonResult<Boolean> exportData(@RequestBody ActicitytyNjnzPageRequestVO requestVO) {
        try {
            return CommonResult.success(activityMzAggregationService.mzExportData(requestVO));
        } catch (Exception e) {
            return CommonResult.error(500, "导出失败：" + e.getMessage());
        }
    }

//    @PreAuthorize("@ss.hasPermission('promotion:activityMz:export')")
    @Operation(summary = "门店明细导出")
    @PostMapping("/exportStoreData")
    public CommonResult<Boolean> exportStoreData(@RequestBody ActicitytyNjnzPageRequestVO requestVO) {
        try {
            activityAggregationService.exportStoreData(requestVO);
            return CommonResult.success(true);
        } catch (Exception e) {
            return CommonResult.error(500, "导出失败：" + e.getMessage());
        }
    }
}
