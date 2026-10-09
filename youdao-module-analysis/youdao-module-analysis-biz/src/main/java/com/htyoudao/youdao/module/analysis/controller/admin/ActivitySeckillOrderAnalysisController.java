package com.htyoudao.youdao.module.analysis.controller.admin;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActicitySeckillPageRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActivitySeckillRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivitySeckillMemberPageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivitySeckillMemberVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivitySeckillStorePageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.AnalysisSeckillChartVO;
import com.htyoudao.youdao.module.analysis.enums.EventType;
import com.htyoudao.youdao.module.analysis.service.IActivitySeckillService;
import com.htyoudao.youdao.module.analysis.service.dto.EventQueryDTO;
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

@Tag(name = "秒杀活动订单")
@RestController
@RequestMapping("/analysis/activitySeckill")
@PermitAll
public class ActivitySeckillOrderAnalysisController {

    @Autowired
    private IActivitySeckillService activitySeckillService;

    @Operation(summary = "秒杀活动活动订单指标")
    @PostMapping("/view")
    public CommonResult<Map<String, Double>> activitySeckillOrderView(@RequestBody @Valid ActivitySeckillRequestVO requestVO) {
        return CommonResult.success(activitySeckillService.query(requestVO));
    }

    @Operation(summary = "门店下数据分析")
    @PostMapping("/storePage")
    public CommonResult<PageResult<ActivitySeckillStorePageVO>> activitySeckillStorePage(@RequestBody @Valid ActicitySeckillPageRequestVO requestVO) {
        return CommonResult.success(activitySeckillService.activitySeckillStorePage(requestVO,1));
    }
    @Operation(summary = "渠道下数据分析")
    @PostMapping("/channelPage")
    public CommonResult<PageResult<ActivitySeckillStorePageVO>> activitySeckillChannelPage(@RequestBody @Valid ActicitySeckillPageRequestVO requestVO) {
        return CommonResult.success(activitySeckillService.activitySeckillStorePage(requestVO,2));
    }
    @Operation(summary = "秒杀活动数据趋势")
    @PostMapping("/trendView")
    public CommonResult<AnalysisSeckillChartVO> activitySeckillOrderTrendView(@RequestBody @Valid EventQueryDTO eventQueryDTO) {
        eventQueryDTO.setEventType(EventType.ACTIVITY);
        return CommonResult.success(activitySeckillService.lineChart(eventQueryDTO));
    }


    @Operation(summary = "秒杀参与记录总览数据")
    @PostMapping("/activitySeckillRecordsTotel")
    public CommonResult<Map<String, Double>> activitySeckillRecordsTotel(@RequestBody @Valid ActicitySeckillPageRequestVO requestVO) {
        return CommonResult.success(activitySeckillService.activitySeckillRecordsTotel(requestVO));
    }
    @Operation(summary = "秒杀参与记录")
    @PostMapping("/activitySeckillRecordsPage")
    public CommonResult<PageResult<ActivitySeckillMemberVO>> activitySeckillRecordsPage(@RequestBody @Valid ActicitySeckillPageRequestVO requestVO) {
        return CommonResult.success(activitySeckillService.activitySeckillRecordsPage(requestVO));
    }

   // @PreAuthorize("@ss.hasPermission('promotion:activitySeckill:export')")
    @Operation(summary = "门店数据分析导出")
    @PostMapping("/exportData")
    public CommonResult<Boolean> exportData(@RequestBody ActicitySeckillPageRequestVO requestVO) {
        try {
            activitySeckillService.exportData(requestVO);
        }catch (Exception e){
            throw new RuntimeException("导出失败");
        }
        return CommonResult.success(true);
    }

    @PreAuthorize("@ss.hasPermission('promotion:activitySeckill:export')")
    @Operation(summary = "门店明细导出")
    @PostMapping("/exportStoreData")
    public CommonResult<Boolean> exportStoreData(@RequestBody ActicitySeckillPageRequestVO requestVO) {
        activitySeckillService.exportStoreData(requestVO);
        return CommonResult.success(true);
    }
    @PreAuthorize("@ss.hasPermission('promotion:activitySeckill:export')")
    @Operation(summary = "渠道明细导出")
    @PostMapping("/exportChannleDetailData")
    public CommonResult<Boolean> exportChannleDetailData(@RequestBody ActicitySeckillPageRequestVO requestVO) {
        activitySeckillService.exportChannleDetailData(requestVO);
        return CommonResult.success(true);
    }
    @PreAuthorize("@ss.hasPermission('promotion:activitySeckill:export')")
    @Operation(summary = "渠道数据分析导出")
    @PostMapping("/exportChannleData")
    public CommonResult<Boolean> exportChannleData(@RequestBody ActicitySeckillPageRequestVO requestVO) {
        try {
            activitySeckillService.exportChannleData(requestVO);
        }catch (Exception e){
            throw new RuntimeException("导出失败");
        }
        return CommonResult.success(true);
    }

}
