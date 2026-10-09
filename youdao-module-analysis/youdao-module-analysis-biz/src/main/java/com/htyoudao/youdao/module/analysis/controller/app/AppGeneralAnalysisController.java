package com.htyoudao.youdao.module.analysis.controller.app;


import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.GeneralRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.LineChartRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.MetricsSettingRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisChartVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.general.AnalysisEntryVO;
import com.htyoudao.youdao.module.analysis.dal.redis.AnalysisMetricsRedisDao;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.service.IAggregationService;
import com.htyoudao.youdao.module.analysis.service.IOrderAggregationService;
import com.htyoudao.youdao.module.analysis.service.IStoreAggService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DC - 总览")
@RestController
@RequestMapping("/analysis/app/general")
public class AppGeneralAnalysisController {

    @Resource
    private IAggregationService aggregationService;

    @Resource
    private IOrderAggregationService orderAggregationService;

    @Resource
    private AnalysisMetricsRedisDao metricsRedisDao;

    @Resource
    private IStoreAggService storeAggService;

    @Operation(summary = "获取关注指标")
    @GetMapping("/metrics/list")
    public CommonResult<List<String>> appMetricsList(@RequestParam String key) {
        return CommonResult.success(metricsRedisDao.getMetricsByKey(key));
    }

    @Operation(summary = "设置关注指标")
    @PostMapping("/metrics/setting")
    public CommonResult<Boolean> appMetricsSetting(@RequestBody MetricsSettingRequestVO requestVO) {
        metricsRedisDao.saveOrUpdateMetrics(requestVO.getKey(), requestVO.getMetrics());
        return CommonResult.success(true);
    }

    @Operation(summary = "总览指标")
    @PostMapping("/view")
    @PermitAll
    public CommonResult<AnalysisVO<AnalysisEntryVO>> appGeneralView(@RequestBody @Valid GeneralRequestVO requestVO) {
        if (CollectionUtils.isEmpty(requestVO.getStoreIds())){
            throw exception(ErrorCodeConstants.STORE_IS_EMPTY);
        }

        return CommonResult.success(orderAggregationService.generalView(requestVO));
    }


    @Operation(summary = "折线图")
    @PostMapping("/chart")
    @PermitAll
    public CommonResult<AnalysisChartVO> appLineChart(@RequestBody @Valid LineChartRequestVO requestVO) {
        if (CollectionUtils.isEmpty(requestVO.getStoreIds())){
            throw exception(ErrorCodeConstants.STORE_IS_EMPTY);
        }
        return CommonResult.success(aggregationService.lineChart(requestVO));
    }


}
