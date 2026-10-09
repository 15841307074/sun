package com.htyoudao.youdao.module.analysis.controller.admin;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.MarketingRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.marketing.MarketingChannelDataVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.marketing.MarketingOverviewVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.marketing.MarketingShopDataVO;
import com.htyoudao.youdao.module.analysis.service.IMarketingAggregationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "营销分析")
@RestController
@RequestMapping("/analysis/marketing")
public class MarketingAnalysisController {

    @Resource
    private IMarketingAggregationService marketingAggregationService;

    @Operation(summary = "数据概览统计接口")
    @PostMapping("/overview")
    public CommonResult<MarketingOverviewVO> getMarketingOverview(@RequestBody @Valid MarketingRequest request) {
        return CommonResult.success(marketingAggregationService.getMarketingOverview(request));
    }

    @Operation(summary = "渠道数据底表接口")
    @PostMapping("/channel-data")
    public CommonResult<List<MarketingChannelDataVO>> getMarketingChannelData(@RequestBody @Valid MarketingRequest request) {
        return CommonResult.success(marketingAggregationService.getMarketingChannelData(request));
    }

    @Operation(summary = "门店数据底表接口")
    @PostMapping("/shop-data")
    public CommonResult<PageResult<MarketingShopDataVO>> getMarketingShopData(@RequestBody @Valid MarketingRequest request) {
        return CommonResult.success(marketingAggregationService.getMarketingShopData(request));
    }

}
