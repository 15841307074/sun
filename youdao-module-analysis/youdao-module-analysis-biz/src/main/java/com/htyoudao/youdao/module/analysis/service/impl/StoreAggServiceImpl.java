package com.htyoudao.youdao.module.analysis.service.impl;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.DC_PAY_AMOUNT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.INVALID_ORDERS;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.MINI_AVERAGE_PAYMENT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.MINI_CUSTOMER_COUNT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.MINI_NEW_CUSTOMER_COUNT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.MINI_PAY_AMOUNT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.MINI_REPEAT_BUYERS;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.MINI_VALID_ORDERS;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.NETR_AMT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.PAY_AMOUNTS;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.STORE_NAME;

import cn.hutool.core.bean.BeanUtil;
import co.elastic.clients.elasticsearch._types.SortOrder;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.analysis.api.inventory.DTO.InventoryAggregationRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.LineChartRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.StorePageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisChartVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisTopVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStoreVO;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.enums.EsDateFormat;
import com.htyoudao.youdao.module.analysis.enums.EventType;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import com.htyoudao.youdao.module.analysis.service.IAggregationService;
import com.htyoudao.youdao.module.analysis.service.IEsAggregationService;
import com.htyoudao.youdao.module.analysis.service.IEventService;
import com.htyoudao.youdao.module.analysis.service.IScfOrderFullService;
import com.htyoudao.youdao.module.analysis.service.IStoreAggService;
import com.htyoudao.youdao.module.analysis.service.dto.AggOrgDTO;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import com.htyoudao.youdao.module.analysis.service.dto.EventQueryDTO;
import com.htyoudao.youdao.module.system.api.org.OrgApi;
import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
public class StoreAggServiceImpl implements IStoreAggService {

    @Resource
    private IEsAggregationService service;

    @Resource
    private IEventService eventService;

    @Resource
    private IAggregationService aggregationService;

    @Resource
    private IScfOrderFullService scfOrderFullService;

    @DubboReference
    private OrgApi orgApi;


    @Override
    public AnalysisVO<AnalysisStoreVO> query(AggregationRequestVO requestVO) {
        //核心指标概述
        final List<MetricsConfig> metrics = List.of(MINI_PAY_AMOUNT, MINI_VALID_ORDERS, MINI_AVERAGE_PAYMENT,
            MINI_CUSTOMER_COUNT, MINI_REPEAT_BUYERS, INVALID_ORDERS, NETR_AMT);

        AnalysisVO<Map<String, Double>> query = aggregationService.query(requestVO, metrics, true);
        AnalysisStoreVO currentVO = new AnalysisStoreVO(query.getCurrent());
        AnalysisStoreVO beforeVO = new AnalysisStoreVO(query.getBefore());
        return new AnalysisVO<>(currentVO, beforeVO);
    }

    @Override
    public AnalysisChartVO generalChart(LineChartRequestVO requestVO) {
        String code = requestVO.getCode();
        EsDateFormat type = EsDateFormat.valueOf(requestVO.getType());
        LocalDateTime[] currentTime = {requestVO.getCurrentTimeStart(), requestVO.getCurrentTimeEnd()};
        LocalDateTime[] beforeTime = {requestVO.getBeforeTimeStart(), requestVO.getBeforeTimeEnd()};

        MetricsConfig metric = mapGeneralChartMetric(code);
        if (metric == null) {
            throw exception(ErrorCodeConstants.METRIC_NOT_FOUND);
        }
        Map<String, Double> currentResult = service.analyzeByTimeGranularity(requestVO.buildCurrentRequest(), type, metric);
        Map<String, Double> beforeResult = service.analyzeByTimeGranularity(requestVO.buildBeforeRequest(), type, metric);

        List<String> currentTimeSlots = generateTimeSlots(currentTime, type);
        List<String> beforeTimeSlots = generateTimeSlots(beforeTime, type);
        currentResult = fillMissingValues(currentResult, currentTimeSlots);
        beforeResult = fillMissingValues(beforeResult, beforeTimeSlots);
        final Map<String, Double> finalCurrentResult = currentResult;
        final Map<String, Double> finalBeforeResult = beforeResult;

        AnalysisChartVO analysisChartVO = new AnalysisChartVO();
        analysisChartVO.setTimes(currentTimeSlots);
        analysisChartVO.setCurrentValues(currentTimeSlots.stream()
            .map(time -> finalCurrentResult.getOrDefault(time, 0.0))
            .toList());
        analysisChartVO.setBeforeValues(beforeTimeSlots.stream()
            .map(time -> finalBeforeResult.getOrDefault(time, 0.0))
            .toList());
        return analysisChartVO;
    }


    @Override
    public PageResult<AnalysisStorePageVO> storePage(AggregationPageRequest requestVO) {
        List<MetricsConfig> metrics = List.of(DC_PAY_AMOUNT, MINI_PAY_AMOUNT, MINI_VALID_ORDERS,
            MINI_AVERAGE_PAYMENT, MINI_CUSTOMER_COUNT, MINI_REPEAT_BUYERS, MINI_NEW_CUSTOMER_COUNT, STORE_NAME);

        //查询关注的指标
        if (!CollectionUtils.isEmpty(requestVO.getMetrics())){
            metrics = requestVO.getMetrics().stream().map(MetricsConfig::valueOf).toList();
        }

        PageResult<AnalysisStorePageVO> pageResult = aggregationService.groupPage(requestVO, "storeId", metrics, requestVO.getShowUV());
        setGylData(pageResult.getList(), requestVO);

        return pageResult;
    }

    @Override
    public PageResult<AnalysisStorePageVO> storeTopN(AggregationRequestVO requestVO, Integer n,
        MetricsConfig metricsConfig, String orderType) {
        final List<MetricsConfig> metrics = List.of(metricsConfig, MetricsConfig.STORE_NAME);
        return storeTopN(requestVO, n, metricsConfig, orderType, metrics);
    }

    @Override
    public PageResult<AnalysisStorePageVO> storeTopN(AggregationRequestVO requestVO, Integer n,
        MetricsConfig metricsConfig, String orderType, List<MetricsConfig> metrics) {
        AggregationPageRequest pageRequest = new AggregationPageRequest();
        BeanUtil.copyProperties(requestVO, pageRequest);
        pageRequest.setPageNo(1);
        pageRequest.setPageSize(n);
        pageRequest.setOrderField(metricsConfig.getCode());
        pageRequest.setOrderType(orderType);
        return aggregationService.groupPage(pageRequest, "storeId", metrics, false);

    }

    @Override
    public PageResult<AnalysisStorePageVO> cityTopN(AggregationRequestVO requestVO, Integer n,
        MetricsConfig metricsConfig, String orderType) {
        final List<MetricsConfig> metrics = List.of(metricsConfig);
        return cityTopN(requestVO, n, metricsConfig, orderType, metrics);
    }

    @Override
    public PageResult<AnalysisStorePageVO> cityTopN(AggregationRequestVO requestVO, Integer n,
        MetricsConfig orderMetric, String orderType, List<MetricsConfig> metrics) {
        AggregationPageRequest pageRequest = new AggregationPageRequest();
        BeanUtil.copyProperties(requestVO, pageRequest);
        pageRequest.setPageNo(1);
        pageRequest.setPageSize(n);
        pageRequest.setOrderField(orderMetric.getCode());
        pageRequest.setOrderType(orderType);
        return aggregationService.groupPage(pageRequest, "city.keyword", metrics, false);

    }


    @Override
    public PageResult<AnalysisStorePageVO> cityPage(AggregationPageRequest requestVO) {
        List<MetricsConfig> metrics = List.of(DC_PAY_AMOUNT, MINI_PAY_AMOUNT, MINI_VALID_ORDERS,
            MINI_AVERAGE_PAYMENT, MINI_CUSTOMER_COUNT, MINI_REPEAT_BUYERS, MINI_NEW_CUSTOMER_COUNT);

        //查询关注的指标
        if (!CollectionUtils.isEmpty(requestVO.getMetrics())){
            metrics = requestVO.getMetrics().stream().map(MetricsConfig::valueOf).toList();
        }

        PageResult<AnalysisStorePageVO> pageResult = aggregationService.groupPage(requestVO, "city.keyword", metrics, requestVO.getShowUV());

        setGylData(pageResult.getList(), requestVO);

        return pageResult;
    }

    /**
     * 设置供应链 数据 > 订货总额
     *
     * @param list
     */
    private void setGylData(List<AnalysisStorePageVO> list, AggregationRequestVO request) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        LocalDateTime beforeTimeStart = request.getBeforeTimeStart();
        LocalDateTime beforeTimeEnd = request.getBeforeTimeEnd();

        LocalDateTime currentTimeStart = request.getCurrentTimeStart();
        LocalDateTime currentTimeEnd = request.getCurrentTimeEnd();

        for (AnalysisStorePageVO storePageVO : list) {
            List<Long> storeIds = storePageVO.getStoreIds();

            Double currentGylOrderAmount = scfOrderFullService.getGylOrderAmount(storeIds, currentTimeStart, currentTimeEnd);
            Double beforeGylOrderAmount = scfOrderFullService.getGylOrderAmount(storeIds, beforeTimeStart, beforeTimeEnd);

            storePageVO.getCurrentValue().setGylOrderAmount(currentGylOrderAmount);
            storePageVO.getBeforeValues().setGylOrderAmount(beforeGylOrderAmount);
        }
    }


    @Override
    public PageResult<AnalysisStorePageVO> orgPage(StorePageRequest requestVO) {
        EsAggDTO esAggDTO = requestVO.buildCurrentRequest();

        final String groupField = "storeId";
        List<MetricsConfig> metrics = List.of(DC_PAY_AMOUNT, MINI_PAY_AMOUNT, MINI_VALID_ORDERS,
            MINI_AVERAGE_PAYMENT, MINI_CUSTOMER_COUNT, MINI_REPEAT_BUYERS, MINI_NEW_CUSTOMER_COUNT);

        //查询关注的指标
        if (!CollectionUtils.isEmpty(requestVO.getMetrics())){
            metrics = requestVO.getMetrics().stream().map(MetricsConfig::valueOf).toList();
        }

        String orderField = requestVO.getOrderField();
        MetricsConfig orderMetric = MetricsConfig.getEnumByCode(orderField);
        SortOrder sortOrder = Objects.equals(requestVO.getOrderType(), "asc") ? SortOrder.Asc : SortOrder.Desc;

        List<AggOrgDTO> groupList = new ArrayList<>();

        for (int i = 0; i < requestVO.getOrgIds().size(); i++) {
            AggOrgDTO orgDTO = new AggOrgDTO();
            Long orgId = requestVO.getOrgIds().get(i);
            orgDTO.setOrgId(orgId);
            orgDTO.setOrgName(requestVO.getOrgNames().get(i));
            //组织数据 需要隔离数据 只展示自己有权限的门店数据
            if (CollectionUtils.isEmpty(requestVO.getOrgStoreIds())){
                List<Long> storeIds = getStoreIds(orgId);
                orgDTO.setStoreIds(storeIds);
            }else {
                orgDTO.setStoreIds(requestVO.getOrgStoreIds().get(i));
            }
            groupList.add(orgDTO);
        }

        PageResult<AnalysisTopVO> currentResult = service.paginatedRangeAggregation(esAggDTO, groupList, metrics,
            groupField, orderMetric, sortOrder, requestVO.getPageNo(), requestVO.getPageSize());

        //截取当前时段 org list
        List<String> currentOrgList = currentResult.getList().stream().map(AnalysisTopVO::getKey).toList();
        List<AggOrgDTO> beforeGroup = groupList.stream().filter(g -> currentOrgList.contains(g.getOrgId().toString()))
            .toList();

        //
        esAggDTO = requestVO.buildBeforeRequest();
        PageResult<AnalysisTopVO> beforeResult = service.paginatedRangeAggregation(esAggDTO, beforeGroup, metrics,
            groupField, null, null, requestVO.getPageNo(), requestVO.getPageSize());

        //设置 uv
        if (requestVO.getShowUV()){
            fillUV(currentResult.getList(), requestVO.buildCurrentRequest().getTimes());
            fillUV(beforeResult.getList(), requestVO.buildBeforeRequest().getTimes());
        }

        // 组装结果集
        List<AnalysisStorePageVO> groupVOS = buildResultMap(currentResult);
        populateBeforeValues(groupVOS, beforeResult);

        PageResult<AnalysisStorePageVO> result = new PageResult<>();
        result.setList(groupVOS);
        result.setTotal(currentResult.getTotal());

        setGylData(result.getList(), requestVO);
        return result;
    }

    @Override
    public BigDecimal storeTotalAmount(InventoryAggregationRequest aggregationRequest) {
        final List<MetricsConfig> metrics = List.of(PAY_AMOUNTS);
        return aggregationService.selectTotalAmount(aggregationRequest, metrics);
    }

    private List<Long> getStoreIds(Long orgId) {
        return orgApi.getStoreIdListByOrgID(orgId, BusinessContextHolder.getBusinessId()).getCheckedData().stream()
            .toList();
    }


    private List<AnalysisStorePageVO> buildResultMap(PageResult<AnalysisTopVO> currentResult) {

        List<AnalysisStorePageVO> list = new ArrayList<>();
        for (AnalysisTopVO item : currentResult.getList()) {
            AnalysisStorePageVO vo = new AnalysisStorePageVO();
            vo.setKey(item.getKey());
            vo.setName(item.getName());
            vo.setCurrentValue(new AnalysisStoreVO(item.getValues()));
            vo.setStoreIds(item.getStoreIds());
            list.add(vo);
        }
        return list;
    }

    private void fillUV(List<AnalysisTopVO> itemList, LocalDateTime[] timeRange) {
        if (CollectionUtils.isEmpty(itemList)) {
            return;
        }

        for (AnalysisTopVO analysisTopVO : itemList) {
            if (CollectionUtils.isEmpty(analysisTopVO.getStoreIds())) {
                continue;
            }
            Long uv = getUV(timeRange, analysisTopVO.getStoreIds());
            analysisTopVO.getValues().put("uv", uv.doubleValue());
        }
    }


    private void populateBeforeValues(List<AnalysisStorePageVO> resultPageList,
        PageResult<AnalysisTopVO> beforeResult) {
        Map<String, AnalysisTopVO> resultMap = beforeResult.getList().stream()
            .collect(Collectors.toMap(AnalysisTopVO::getKey, Function.identity()));
        for (AnalysisStorePageVO storePageVO : resultPageList) {
            AnalysisTopVO vo = resultMap.getOrDefault(storePageVO.getKey(), new AnalysisTopVO());
            storePageVO.setBeforeValues(new AnalysisStoreVO(vo.getValues()));
        }
    }

    private Long getUV(LocalDateTime[] times, List<Long> storeIds) {
        EventQueryDTO queryDTO = new EventQueryDTO();
        queryDTO.setStoreIds(storeIds);
        queryDTO.setEventType(EventType.IN_STORE);
        queryDTO.setStartTime(times[0]);
        queryDTO.setEndTime(times[1]);
        return eventService.queryUV(queryDTO);
    }

    private MetricsConfig mapGeneralChartMetric(String code) {
        return switch (code) {
            case "payAmount" -> MINI_PAY_AMOUNT;
            case "validOrders" -> MINI_VALID_ORDERS;
            case "averagePayment" -> MINI_AVERAGE_PAYMENT;
            case "customerCount" -> MINI_CUSTOMER_COUNT;
            case "invalidOrders" -> INVALID_ORDERS;
            default -> null;
        };
    }

    private List<String> generateTimeSlots(LocalDateTime[] times, EsDateFormat type) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(type.getFormat());
        LocalDateTime current = times[0];
        List<String> slots = new ArrayList<>();
        while (!current.isAfter(times[1])) {
            slots.add(formatter.format(current));
            if (type == EsDateFormat.DAY) {
                current = current.plusDays(1);
            } else {
                current = current.plusHours(1);
            }
        }
        return slots;
    }

    private Map<String, Double> fillMissingValues(Map<String, Double> source, List<String> timeSlots) {
        Map<String, Double> result = new LinkedHashMap<>();
        for (String timeSlot : timeSlots) {
            result.put(timeSlot, source.getOrDefault(timeSlot, 0.0));
        }
        return result;
    }
}
