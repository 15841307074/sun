package com.htyoudao.youdao.module.analysis.controller.admin;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.GeneralRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.LineChartRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.MetricsSettingRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisChartVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.general.AnalysisEntryVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.dal.redis.AnalysisMetricsRedisDao;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import com.htyoudao.youdao.module.analysis.service.IAggregationService;
import com.htyoudao.youdao.module.analysis.service.IEsAggregationService;
import com.htyoudao.youdao.module.analysis.service.IOrderAggregationService;
import com.htyoudao.youdao.module.analysis.service.IStoreAggService;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "总览")
@RestController
@RequestMapping("/analysis/general")
public class GeneralAnalysisController {

    @Resource
    private IAggregationService aggregationService;

    @Resource
    private IOrderAggregationService orderAggregationService;

    @Resource
    private AnalysisMetricsRedisDao metricsRedisDao;

    @Resource
    private IStoreAggService storeAggService;

    @Resource
    private IEsAggregationService service;


    @Operation(summary = "获取关注指标")
    @GetMapping("/metrics/list")
    public CommonResult<List<String>> metricsList(@RequestParam String key) {
        return CommonResult.success(metricsRedisDao.getMetricsByKey(key));
    }

    @Operation(summary = "设置关注指标")
    @PostMapping("/metrics/setting")
    public CommonResult<Boolean> metricsSetting(@RequestBody @Valid MetricsSettingRequestVO requestVO) {
        metricsRedisDao.saveOrUpdateMetrics(requestVO.getKey(), requestVO.getMetrics());
        return CommonResult.success(true);
    }

    @Operation(summary = "总览指标")
    @PostMapping("/view")
    public CommonResult<AnalysisVO<AnalysisEntryVO>> generalView(@RequestBody GeneralRequestVO requestVO) {

        if (requestVO.getCurrentTimeStart() == null && requestVO.getCurrentTimeEnd() == null) {
            LocalDateTime now = LocalDateTime.now();
            requestVO.setCurrentTimeStart(now.toLocalDate().atStartOfDay());
            requestVO.setCurrentTimeEnd(now);
        }

        if (requestVO.getBeforeTimeStart() == null && requestVO.getBeforeTimeEnd() == null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime yesterday = now.minusDays(1);
            requestVO.setBeforeTimeStart(yesterday.toLocalDate().atStartOfDay());
            requestVO.setBeforeTimeEnd(yesterday.withHour(now.getHour()).withMinute(now.getMinute()).withSecond(now.getSecond()));
        }
        return CommonResult.success(orderAggregationService.generalView(requestVO));
    }

    @Operation(summary = "老板助手 - 管理页面 - 指标")
    @PostMapping("/view/beforeList")
    public CommonResult<Map<String, List<Double>>> generalViewBeforeList(@RequestBody @Valid List<Long> storeIds) {
        List<MetricsConfig> metricsConfigs = List.of(
            MetricsConfig.ORDER_AMOUNT,
            MetricsConfig.PAY_AMOUNT,
            MetricsConfig.CUSTOMER_COUNT,
            MetricsConfig.AVERAGE_PAYMENT
        );
        LocalDateTime now = LocalDateTime.now();

        EsAggDTO esAggDTO = new EsAggDTO();
        esAggDTO.setTimes(new LocalDateTime[]{now.toLocalDate().atStartOfDay(), now});
        esAggDTO.setStoreIds(storeIds);
        Map<String, Double> currentValue = service.batchAggregateMetrics(esAggDTO, metricsConfigs);

        // 设置昨日时间段（昨日0点到昨日此时）
        LocalDateTime yesterday = now.minusDays(1);
        esAggDTO.setTimes(new LocalDateTime[]{yesterday.toLocalDate().atStartOfDay(),
            yesterday.withHour(now.getHour()).withMinute(now.getMinute()).withSecond(now.getSecond())});
        Map<String, Double> yesterdayValue = service.batchAggregateMetrics(esAggDTO, metricsConfigs);

        //设置上周时间段
        LocalDateTime lastWeek = now.minusWeeks(1);
        esAggDTO.setTimes(new LocalDateTime[]{lastWeek.toLocalDate().atStartOfDay(), lastWeek});
        Map<String, Double> lastWeekValue = service.batchAggregateMetrics(esAggDTO, metricsConfigs);

        Map<String, List<Double>> resultMap = new HashMap<>();

        // 遍历所有指标配置
        for (MetricsConfig config : metricsConfigs) {
            String metricKey = config.getCode();

            List<Double> values = new ArrayList<>();

            // 获取当前时间段的值，如果没有则使用0.0
            values.add(currentValue.getOrDefault(metricKey, 0.0));

            // 获取昨日此时段的值，如果没有则使用0.0
            values.add(yesterdayValue.getOrDefault(metricKey, 0.0));

            // 获取上周此时段的值，如果没有则使用0.0
            values.add(lastWeekValue.getOrDefault(metricKey, 0.0));

            resultMap.put(metricKey, values);
        }

        return CommonResult.success(resultMap);
    }

    @Operation(summary = "折线图")
    @PostMapping("/chart")
    public CommonResult<AnalysisChartVO> lineChart(@RequestBody @Valid LineChartRequestVO requestVO) {
        return CommonResult.success(aggregationService.lineChart(requestVO));
    }


    @Operation(summary = "门店top5")
    @PostMapping("/store/top/5")
    @PreAuthorize("@ss.hasPermission('analysis:general:store')")
    public CommonResult<List<AnalysisStorePageVO>> storeTop5(@RequestBody @Valid AggregationRequestVO requestVO) {
        MetricsConfig orderMetric = MetricsConfig.VALID_ORDERS;
        List<MetricsConfig> metrics = List.of(MetricsConfig.VALID_ORDERS, MetricsConfig.ORDER_AMOUNT,
            MetricsConfig.STORE_NAME);
        PageResult<AnalysisStorePageVO> pageResult = storeAggService.storeTopN(requestVO, 5, orderMetric, "desc",
            metrics);
        return CommonResult.success(pageResult.getList());
    }


    @Operation(summary = "城市top5")
    @PostMapping("/city/top/5")
    @PreAuthorize("@ss.hasPermission('analysis:general:city')")
    public CommonResult<List<AnalysisStorePageVO>> cityTop5(@RequestBody @Valid AggregationRequestVO requestVO) {
        MetricsConfig orderMetric = MetricsConfig.VALID_ORDERS;
        List<MetricsConfig> metrics = List.of(MetricsConfig.VALID_ORDERS, MetricsConfig.ORDER_AMOUNT);
        PageResult<AnalysisStorePageVO> pageResult = storeAggService.cityTopN(requestVO, 5, orderMetric, "desc",
            metrics);
        return CommonResult.success(pageResult.getList());
    }
}
