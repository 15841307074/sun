package com.htyoudao.youdao.module.analysis.service.impl;


import static cn.hutool.core.date.DateUtil.formatDateTime;
import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.analysis.api.enums.ErrorCodeConstants.SELECT_AT_LEAST_ONE_OPTION;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.COMMODITY_COUNT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.STORE_NAME;
import static org.springframework.core.log.LogFormatUtils.formatValue;

import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.FilterAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.NestedAggregate;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.module.analysis.api.inventory.DTO.InventoryAggregationRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.OrderCreateTimeExcelRespVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActicitytyNjnzPageRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.LineChartRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityNjnzStoreExcelVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityNjnzStorePageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityNjnzStoreVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisChartVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisTopVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.TimeChartVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStoreVO;
import com.htyoudao.youdao.module.analysis.dal.es.BzOrder;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.enums.EsDateFormat;
import com.htyoudao.youdao.module.analysis.enums.EventType;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import com.htyoudao.youdao.module.analysis.service.IAggregationService;
import com.htyoudao.youdao.module.analysis.service.IEsAggregationService;
import com.htyoudao.youdao.module.analysis.service.IEventService;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import com.htyoudao.youdao.module.analysis.service.dto.EventQueryDTO;
import com.htyoudao.youdao.module.system.api.orgstore.OrgStoreApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import jakarta.annotation.Resource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class AggregationServiceImpl implements IAggregationService {

    @Resource
    private ElasticsearchClient client;//8.5.1


    @Resource
    private IEsAggregationService service;

    @Resource
    private IEventService eventService;


    @DubboReference
    private StoreApi storeApi;

    @Resource
    private ExcelActionService excelActionService;

    @DubboReference
    private OrgStoreApi orgStoreApi;




    @Override
    public AnalysisVO<Map<String, Double>> query(AggregationRequestVO requestVO, List<MetricsConfig> metrics, boolean uv) {
        //查询
        Map<String, Double> currentResult;
        Map<String, Double> beforeResult;

        if (uv) {
            currentResult = service.batchAggregateMetricsWithUV(requestVO.buildCurrentRequest(), metrics);
            beforeResult = service.batchAggregateMetricsWithUV(requestVO.buildBeforeRequest(), metrics);
        } else {
            currentResult = service.batchAggregateMetrics(requestVO.buildCurrentRequest(), metrics);
            beforeResult = service.batchAggregateMetrics(requestVO.buildBeforeRequest(), metrics);
        }

        return new AnalysisVO<>(currentResult, beforeResult);
    }


    @Override
    public AnalysisChartVO lineChart(LineChartRequestVO requestVO) {
        MetricsConfig metric = MetricsConfig.getEnumByCode(requestVO.getCode());
        if (metric == null) {
            throw exception(ErrorCodeConstants.METRIC_NOT_FOUND);
        }
        LocalDateTime[] currentTime = {requestVO.getCurrentTimeStart(), requestVO.getCurrentTimeEnd()};
        LocalDateTime[] beforeTime = {requestVO.getBeforeTimeStart(), requestVO.getBeforeTimeEnd()};
        EsDateFormat type = EsDateFormat.valueOf(requestVO.getType());

        EsAggDTO currentRequest = requestVO.buildCurrentRequest();
        EsAggDTO beforeRequest = requestVO.buildBeforeRequest();

        Map<String, Double> currentResult = service.analyzeByTimeGranularity(currentRequest, type, metric);
        //生成完整时间序列并补零
        List<String> currentTimeSlots = generateTimeSlots(currentTime, type);
        currentResult = fillMissingValues(currentResult, currentTimeSlots);

        Map<String, Double> beforeResult = service.analyzeByTimeGranularity(beforeRequest, type, metric);
        //生成完整时间序列并补零
        List<String> beforeTimeSlots = generateTimeSlots(beforeTime, type);
        beforeResult = fillMissingValues(beforeResult, beforeTimeSlots);


        AnalysisChartVO analysisChartVO = new AnalysisChartVO();
        analysisChartVO.setTimes(TimeChartVO.convert(currentResult).stream().map(TimeChartVO::getTime).toList());
        analysisChartVO.setCurrentValues(TimeChartVO.convert(currentResult).stream().map(TimeChartVO::getValue).toList());
        analysisChartVO.setBeforeValues(TimeChartVO.convert(beforeResult).stream().map(TimeChartVO::getValue).toList());
        return analysisChartVO;
    }

    //生成完整时间序列
    private List<String> generateTimeSlots(LocalDateTime[] times, EsDateFormat type) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(type.getFormat());

        LocalDateTime current = times[0];
        LocalDateTime endZdt = times[1];

        List<String> slots = new ArrayList<>();

        while (!current.isAfter(endZdt)) {
            slots.add(formatter.format(current));
            if (type == EsDateFormat.DAY) {
                current = current.plusDays(1);
            }
            if (type == EsDateFormat.HOUR) {
                current = current.plusHours(1);
            }
        }
        return slots;
    }



    @Override
    public PageResult<AnalysisStorePageVO> groupPage(AggregationPageRequest requestVO,
        String groupField, List<MetricsConfig> metrics, Boolean showUv) {
        EsAggDTO currentRequest = requestVO.buildCurrentRequest();
        currentRequest.setStoreName(requestVO.getStoreName());
        currentRequest.setCityName(requestVO.getCityName());
        currentRequest.setSelectNoShow(true);

        String orderField = requestVO.getOrderField();
        MetricsConfig orderMetric = MetricsConfig.getEnumByCode(orderField);
        SortOrder sortOrder = Objects.equals(requestVO.getOrderType(), "asc") ? SortOrder.Asc : SortOrder.Desc;
        PageResult<AnalysisTopVO> currentResult = service.paginatedGroupAggregation(
            currentRequest,
            metrics,
            groupField,
            orderMetric,
            sortOrder,
            requestVO.getPageNo(),
            requestVO.getPageSize()
        );

        //当前时段没数据时 返回空数据
        if (CollectionUtils.isEmpty(currentResult.getList())){
            PageResult<AnalysisStorePageVO> result = new PageResult<>();
            result.setList(Lists.newArrayList());
            result.setTotal(currentResult.getTotal());
            return result;
        }

        EsAggDTO beforeRequest = requestVO.buildBeforeRequest();
        beforeRequest.setStoreIds(extractStoreIds(currentResult));
        beforeRequest.setSelectNoShow(true);

        // 获取同比时间段数据
        PageResult<AnalysisTopVO> beforeResult = service.paginatedGroupAggregation(
            beforeRequest,
            metrics,
            groupField,
            null,
            null,
            1,
            requestVO.getPageSize()
        );

        //设置 uv
        if (showUv){
            for (AnalysisTopVO item : currentResult.getList()) {
                double uv = getUV(currentRequest.getTimes(), item.getStoreIds()).doubleValue();
                item.getValues().put("uv", uv);
            }

            for (AnalysisTopVO item : beforeResult.getList()) {
                double uv = getUV(currentRequest.getTimes(), item.getStoreIds()).doubleValue();
                item.getValues().put("uv", uv);
            }
        }

        // 组装结果集
        List<AnalysisStorePageVO> groupVOS = buildResultMap(currentResult, STORE_NAME.getCode());
        populateBeforeValues(groupVOS, beforeResult);
        PageResult<AnalysisStorePageVO> result = new PageResult<>();
        result.setList(groupVOS);
        result.setTotal(currentResult.getTotal());
        return result;
    }

    @Override
    public PageResult<ActivityNjnzStorePageVO> activityGroupPage(ActicitytyNjnzPageRequestVO requestVO, String groupField, List<MetricsConfig> metrics, Boolean showUv) {

        EsAggDTO currentRequest = requestVO.buildCurrentRequest();
        if(ObjectUtil.isNotEmpty(requestVO.getOrgId())){
            CommonResult<List<Long>> storeIdsByDeptId = orgStoreApi.selectByOrgStoreList(requestVO.getOrgId());
//            CommonResult<List<Long>> storeIdsByDeptId = storeApi.getStoreIdsByDeptId(requestVO.getOrgId());
            List<Long> data = storeIdsByDeptId.getData();
            if(ObjectUtil.isNotEmpty(data)){
                currentRequest.setStoreIds(data);
            }else{
                return new PageResult<>();
            }
        }
        if(ObjectUtil.isNotEmpty(requestVO.getStoreName())){
            currentRequest.setStoreName(requestVO.getStoreName());
        }

        List<Integer> orderList = new ArrayList<>();
        orderList.add(60);
        currentRequest.setOrderS(orderList);

        currentRequest.setActivityId(requestVO.getActivityId());

        PageResult<AnalysisTopVO> currentResult = service.paginatedGroupAggregation(
                currentRequest,
                metrics,
                groupField,
                requestVO.getPageNo(),
                requestVO.getPageSize()
        );


        // 组装结果集
        List<ActivityNjnzStorePageVO> groupVOS = buildResultMapTwo(currentResult);
        if(ObjectUtil.isNotEmpty(groupVOS)){
            for (ActivityNjnzStorePageVO groupVO : groupVOS) {
                String key = groupVO.getKey();
                Long aLong = Long.valueOf(key);
                CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(aLong);
                StoreDTO data = storeByStoreId.getData();
                if(ObjectUtil.isNotEmpty(data)){
                    groupVO.setName(data.getStoreName());
                }


            }
        }
//        populateBeforeValues(groupVOS, beforeResult);
        PageResult<ActivityNjnzStorePageVO> result = new PageResult<>();
        result.setList(groupVOS);
        result.setTotal(currentResult.getTotal());
        return result;
    }

    @Override
    public Boolean activityData(ActicitytyNjnzPageRequestVO requestVO, String groupField, List<MetricsConfig> metrics, Boolean b) {
        EsAggDTO currentRequest = requestVO.buildCurrentRequest();
        if(ObjectUtil.isNotEmpty(requestVO.getOrgId())){
            CommonResult<List<Long>> storeIdsByDeptId = storeApi.getStoreIdsByDeptId(requestVO.getOrgId());
            List<Long> data = storeIdsByDeptId.getData();
            if(ObjectUtil.isNotEmpty(data)){
                currentRequest.setStoreIds(data);
            }
        }
        if(ObjectUtil.isNotEmpty(requestVO.getStoreName())){
            currentRequest.setStoreName(requestVO.getStoreName());
        }
        List<Integer> orderList = new ArrayList<>();
        orderList.add(60);
        currentRequest.setOrderS(orderList);
        currentRequest.setActivityId(requestVO.getActivityId());
        List<AnalysisTopVO> currentResult = service.paginatedGroupAggregationList(
                currentRequest,
                metrics,
                groupField
        );
        // 组装结果集
//        List<ActivityNjnzStorePageVO> groupVOS = buildResultMapTwo(currentResult);
        List<ActivityNjnzStorePageVO> groupVOS = buildResultList(currentResult);
        if(ObjectUtil.isNotEmpty(groupVOS)){
            for (ActivityNjnzStorePageVO groupVO : groupVOS) {
                String key = groupVO.getKey();
                Long aLong = Long.valueOf(key);
                CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(aLong);
                StoreDTO data = storeByStoreId.getData();
                if(ObjectUtil.isNotEmpty(data)){
                    groupVO.setName(data.getStoreName());
                }
            }
        }

        if(ObjectUtil.isNotEmpty(groupVOS)){

            List<ActivityNjnzStoreExcelVO> list = new ArrayList<>();
            for (ActivityNjnzStorePageVO groupVO : groupVOS) {
                ActivityNjnzStoreExcelVO activityNjnzStoreExcelVO = new ActivityNjnzStoreExcelVO();
                activityNjnzStoreExcelVO.setName(groupVO.getName());
                activityNjnzStoreExcelVO.setCustomerCount(groupVO.getCurrentValue().getCustomerCount());
                activityNjnzStoreExcelVO.setCommodityCount(groupVO.getCurrentValue().getCommodityCount());
                activityNjnzStoreExcelVO.setOfferAmount(groupVO.getCurrentValue().getOfferAmount());
                activityNjnzStoreExcelVO.setOrderNumber(groupVO.getCurrentValue().getOrderNumber());
                activityNjnzStoreExcelVO.setPayAmount(groupVO.getCurrentValue().getPayAmount());
                list.add(activityNjnzStoreExcelVO);

            }
            Set<String> fields = new HashSet<>();
            fields.add("name");
            fields.add("customerCount");
            fields.add("orderNumber");
            fields.add("payAmount");
            fields.add("commodityCount");
            fields.add("offerAmount");
//            fields.add("averagePayment");

            if(!StringUtils.isEmpty(requestVO.getBeforeTimeStart())){
                LocalDateTime startTime = requestVO.getBeforeTimeStart();
                LocalDateTime endTime = requestVO.getBeforeTimeEnd();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                String fileName =  startTime.format(formatter) + "至" + endTime.format(formatter)+"活动订单导出表";
                Function<Object, List<ActivityNjnzStoreExcelVO>> queryFunction = (param) -> list;
                excelActionService.exportAsyncExcel(ActivityNjnzStoreExcelVO.class,requestVO, queryFunction, fileName, fields);
            }else{
                String fileName = "全部活动订单导出表";
                Function<Object, List<ActivityNjnzStoreExcelVO>> queryFunction = (param) -> list;
                excelActionService.exportAsyncExcel(ActivityNjnzStoreExcelVO.class,requestVO, queryFunction, fileName, fields);
            }

        }

        return true;
    }

    @Override
    public BigDecimal selectTotalAmount(InventoryAggregationRequest aggregationRequest, List<MetricsConfig> metrics) {
        aggregationRequest.setTimes(new LocalDateTime[]{aggregationRequest.getCurrentTimeStart(),aggregationRequest.getCurrentTimeEnd()});
        for (MetricsConfig metric : metrics) {
            try {
                // 1. 构建查询条件
                BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
                boolBuilder.filter(m -> m.term(
                        t -> t.field("businessId")
                                .value(FieldValue.of(BusinessContextHolder.getBusinessId()))
                ));

                boolBuilder.filter(m -> m.term(
                        t -> t.field("orderState")
                                .value(60)
                ));

                boolBuilder.filter(m -> m.term(
                        t -> t.field("storeId")
                                .value(aggregationRequest.getStoreId())
                ));
                LocalDateTime[] times = aggregationRequest.getTimes();
                boolBuilder.filter(m -> m.range(
                        r -> r.date(dr -> dr.field("createTime").gte(formatDateTime(times[0])).lte(formatDateTime(times[1])))));
                Map<String, Aggregation> aggs = new HashMap<>();
                aggs.put(metric.getCode(), buildSimpleAggregation(metric,metric.getField()));
                // 其他指标原有处理逻辑
                SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
                        .query(metric.addTermsCondition(boolBuilder))
                        .aggregations(aggs));
                SearchResponse<BzOrder> response = client.search(searchRequest, BzOrder.class);
                Aggregate rootAggregate = response.aggregations().get(metric.getCode());
                double aggregationValue = getAggregationValue(rootAggregate, metric);
                BigDecimal bigDecimalValue = BigDecimal.valueOf(aggregationValue);
                return bigDecimalValue;
            } catch (IOException e) {
                throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
            }
        }
        return BigDecimal.ZERO;
    }


    private String formatDateTime(LocalDateTime dateTime) {
        ZonedDateTime zoned = dateTime.atZone(ZoneId.of("Asia/Shanghai"));
        return zoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private double getAggregationValue(Aggregate aggregate, MetricsConfig metric) {
        return switch (metric.getAggType()) {
            case SUM -> aggregate.sum().value();
            case AVG -> aggregate.avg().value();
            case COUNT -> aggregate.valueCount().value();
            case MAX -> aggregate.max().value();
            case MIN -> aggregate.min().value();
            case CARDINALITY -> aggregate.cardinality().value();
            default -> 0.0;
        };
    }

    private Aggregation buildSimpleAggregation(MetricsConfig param, String fieldName) {
        return switch (param.getAggType()) {
            case SUM -> Aggregation.of(a -> a.sum(s -> s.field(fieldName)));
            case AVG -> Aggregation.of(a -> a.avg(avg -> avg.field(fieldName)));
            case COUNT -> Aggregation.of(a -> a.valueCount(vc -> vc.field(fieldName)));
            case MAX -> Aggregation.of(a -> a.max(m -> m.field(fieldName)));
            case MIN -> Aggregation.of(a -> a.min(m -> m.field(fieldName)));
            case CARDINALITY -> Aggregation.of(a -> a.cardinality(m -> m.field(fieldName)));
            case HIT -> Aggregation.of(a -> a.topHits(th -> th.size(1)
                    .source(so -> so.filter(f -> f.includes(fieldName)))));
            case MIN_COUNT -> Aggregation.of(a -> a.terms(t -> t.field(fieldName)
                    .minDocCount(2).size(10000)));
        };
    }

    private List<ActivityNjnzStorePageVO> buildResultList(List<AnalysisTopVO> currentResult) {
        List<ActivityNjnzStorePageVO> list = new ArrayList<>();
        for (AnalysisTopVO item : currentResult) {
            ActivityNjnzStorePageVO vo = new ActivityNjnzStorePageVO();
            vo.setKey(item.getKey());
            vo.setName(item.getName());
            vo.setStoreIds(item.getStoreIds());
            vo.setCurrentValue(new ActivityNjnzStoreVO(item.getValues()));
            list.add(vo);
        }
        return list;
    }

    private List<ActivityNjnzStorePageVO> buildResultMapTwo(PageResult<AnalysisTopVO> currentResult) {
        List<ActivityNjnzStorePageVO> list = new ArrayList<>();
        for (AnalysisTopVO item : currentResult.getList()) {
            ActivityNjnzStorePageVO vo = new ActivityNjnzStorePageVO();
            vo.setKey(item.getKey());
            vo.setName(item.getName());
            vo.setStoreIds(item.getStoreIds());
            vo.setCurrentValue(new ActivityNjnzStoreVO(item.getValues()));
            list.add(vo);
        }
        return list;
    }


    //补零
    private Map<String, Double> fillMissingValues(Map<String, Double> dataMap, List<String> allSlots) {
        Map<String, Double> result = new LinkedHashMap<>();
        allSlots.forEach(slot -> result.put(slot, dataMap.getOrDefault(slot, 0.0)));
        return result;
    }


    private List<Long> extractStoreIds(PageResult<AnalysisTopVO> result) {
        List<Long> storeIds = new ArrayList<>();
        for (AnalysisTopVO analysisTopVO : result.getList()) {
            storeIds.addAll(analysisTopVO.getStoreIds());
        }
        return storeIds;
    }

    private List<AnalysisStorePageVO> buildResultMap(PageResult<AnalysisTopVO> currentResult, String hitName) {

        List<AnalysisStorePageVO> list = new ArrayList<>();
        for (AnalysisTopVO item : currentResult.getList()) {
            AnalysisStorePageVO vo = new AnalysisStorePageVO();
            vo.setKey(item.getKey());
            vo.setName(item.getName());
            vo.setStoreIds(item.getStoreIds());
            vo.setCurrentValue(new AnalysisStoreVO(item.getValues()));
            list.add(vo);
        }
        return list;
    }


    private void populateBeforeValues(List<AnalysisStorePageVO> currentResult, PageResult<AnalysisTopVO> beforeResult) {
        Map<String, AnalysisTopVO> beforeMap = beforeResult.getList().stream()
            .collect(Collectors.toMap(AnalysisTopVO::getKey, Function.identity()));

        for (AnalysisStorePageVO storePageVO : currentResult) {
            AnalysisTopVO beforeTopVo = beforeMap.getOrDefault(storePageVO.getKey(), new AnalysisTopVO());
            storePageVO.setBeforeValues(new AnalysisStoreVO(beforeTopVo.getValues()));
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


}
