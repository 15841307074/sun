package com.htyoudao.youdao.module.analysis.controller.admin;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AdAnalysisRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad.AdOverviewVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad.AdPositionStatVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad.AdStatVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.ad.AdTrendPointVO;
import com.htyoudao.youdao.module.analysis.service.IAdEventService;
import com.htyoudao.youdao.module.analysis.service.dto.AdEventQueryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "广告分析")
@RestController
@RequestMapping("/analysis/ad")
public class AdvertisingAnalysisController {

    @Resource
    private IAdEventService adEventService;

    @Operation(summary = "广告数据概览统计接口")
    @PostMapping("/overview")
    public CommonResult<AdOverviewVO> getAdOverview(@RequestBody @Valid AdAnalysisRequest request) {
        return CommonResult.success(adEventService.queryOverview(buildQueryDTO(request)));
    }

    @Operation(summary = "广告每日趋势统计接口")
    @PostMapping("/trend")
    public CommonResult<List<AdTrendPointVO>> getAdTrend(@RequestBody @Valid AdAnalysisRequest request) {
        return CommonResult.success(adEventService.queryTrendByDay(buildQueryDTO(request)));
    }

    @Operation(summary = "广告按位置分组统计接口")
    @PostMapping("/by-position")
    public CommonResult<List<AdPositionStatVO>> getAdByPosition(@RequestBody @Valid AdAnalysisRequest request) {
        return CommonResult.success(adEventService.queryByPosition(buildQueryDTO(request)));
    }

    @Operation(summary = "广告按广告分组统计接口")
    @PostMapping("/by-ad")
    public CommonResult<List<AdStatVO>> getAdByAd(@RequestBody @Valid AdAnalysisRequest request) {
        return CommonResult.success(adEventService.queryByAd(buildQueryDTO(request)));
    }

    /**
     * 将看板请求参数转换为广告事件查询DTO
     */
    private AdEventQueryDTO buildQueryDTO(AdAnalysisRequest request) {
        AdEventQueryDTO queryDTO = new AdEventQueryDTO();
        queryDTO.setStartTime(request.getStartTime());
        queryDTO.setEndTime(request.getEndTime());
        queryDTO.setAdIds(request.getAdIds());
        queryDTO.setAdInfoPositions(request.getAdInfoPositions());
        queryDTO.setStoreIds(request.getStoreIds());
        queryDTO.setChannelIds(request.getChannelIds());
        return queryDTO;
    }

}
