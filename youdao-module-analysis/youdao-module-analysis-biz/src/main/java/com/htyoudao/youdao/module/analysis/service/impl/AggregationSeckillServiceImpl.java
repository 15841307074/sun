package com.htyoudao.youdao.module.analysis.service.impl;


import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch._types.SortOrder;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActicitySeckillPageRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.LineChartRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.*;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.*;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStoreVO;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.enums.EsDateFormat;
import com.htyoudao.youdao.module.analysis.enums.EventType;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import com.htyoudao.youdao.module.analysis.service.IAggregationSeckillService;
import com.htyoudao.youdao.module.analysis.service.IEsAggregationService;
import com.htyoudao.youdao.module.analysis.service.IEventService;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import com.htyoudao.youdao.module.analysis.service.dto.EventQueryDTO;
import com.htyoudao.youdao.module.system.api.orgstore.OrgStoreApi;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import com.htyoudao.youdao.module.system.api.store.dto.StoreDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.STORE_NAME;

@Slf4j
@Service
public class AggregationSeckillServiceImpl implements IAggregationSeckillService {

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
        if (CollectionUtils.isEmpty(currentResult.getList())) {
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
        if (showUv) {
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
    public PageResult<ActivitySeckillStorePageVO> activityGroupPage(ActicitySeckillPageRequestVO requestVO, String groupField, List<MetricsConfig> metrics, Boolean showUv, int type) {

        EsAggDTO currentRequest = requestVO.buildCurrentRequest();
        if (ObjectUtil.isNotEmpty(requestVO.getOrgId())) {
            CommonResult<List<Long>> storeIdsByDeptId = orgStoreApi.selectByOrgStoreList(requestVO.getOrgId());
//            CommonResult<List<Long>> storeIdsByDeptId = storeApi.getStoreIdsByDeptId(requestVO.getOrgId());
            List<Long> data = storeIdsByDeptId.getData();
            if (ObjectUtil.isNotEmpty(data)) {
                currentRequest.setStoreIds(data);
            } else {
                return new PageResult<>();
            }
        }
        if (ObjectUtil.isNotEmpty(requestVO.getStoreName())) {
            currentRequest.setStoreName(requestVO.getStoreName());
        }

        List<Integer> orderList = new ArrayList<>();
        orderList.add(60);
        currentRequest.setOrderS(orderList);

        currentRequest.setActivityId(requestVO.getActivityId());

        PageResult<AnalysisTopVO> currentResult = service.paginatedGrouSeckillpAggregation(
                currentRequest,
                metrics,
                groupField,
                requestVO.getPageNo(),
                requestVO.getPageSize(),
                type
        );


        // 组装结果集
        List<ActivitySeckillStorePageVO> groupVOS = buildResultMapTwo(currentResult);
        if (ObjectUtil.isNotEmpty(groupVOS)) {
            for (ActivitySeckillStorePageVO groupVO : groupVOS) {
                String key = groupVO.getKey();
                Long aLong = Long.valueOf(key);
                if (type == 1) {

                    CommonResult<StoreDTO> storeByStoreId = storeApi.getStoreByStoreId(aLong);
                    StoreDTO data = storeByStoreId.getData();
                    if (ObjectUtil.isNotEmpty(data)) {
                        groupVO.setName(data.getStoreName());
                    }
                }else {
                    groupVO.setName(groupVO.getKey());
                }

            }
        }
//        populateBeforeValues(groupVOS, beforeResult);
        PageResult<ActivitySeckillStorePageVO> result = new PageResult<>();
        result.setList(groupVOS);
        result.setTotal(currentResult.getTotal());
        return result;
    }

    @Override
    public Boolean activityData(ActicitySeckillPageRequestVO requestVO, String groupField, List<MetricsConfig> metrics, Boolean b,int type) {
        EsAggDTO currentRequest = requestVO.buildCurrentRequest();
        if (ObjectUtil.isNotEmpty(requestVO.getOrgId())) {
            CommonResult<List<Long>> storeIdsByDeptId = storeApi.getStoreIdsByDeptId(requestVO.getOrgId());
            List<Long> data = storeIdsByDeptId.getData();
            if (ObjectUtil.isNotEmpty(data)) {
                currentRequest.setStoreIds(data);
            }
        }
        if (ObjectUtil.isNotEmpty(requestVO.getStoreName())) {
            currentRequest.setStoreName(requestVO.getStoreName());
        }
        List<Integer> orderList = new ArrayList<>();
        orderList.add(60);
        currentRequest.setOrderS(orderList);
        currentRequest.setActivityId(requestVO.getActivityId());
        List<AnalysisTopVO> currentResult = service.paginatedSeckillGroupAggregationList(
                currentRequest,
                metrics,
                groupField
        );
        // 组装结果集
//        List<ActivitySeckillStorePageVO> groupVOS = buildResultMapTwo(currentResult);
        List<ActivitySeckillStorePageVO> groupVOS = buildResultList(currentResult);

        if (ObjectUtil.isNotEmpty(groupVOS)) {

            List<ActivitySeckillStoreExcelVO> list = new ArrayList<>();
            for (ActivitySeckillStorePageVO groupVO : groupVOS) {
                ActivitySeckillStoreExcelVO activitySeckillStoreExcelVO = new ActivitySeckillStoreExcelVO();
                activitySeckillStoreExcelVO.setName(groupVO.getName());
                activitySeckillStoreExcelVO.setCustomerCount(groupVO.getCurrentValue().getCustomerCount());
                activitySeckillStoreExcelVO.setCommodityCount(groupVO.getCurrentValue().getCommodityCount());
                activitySeckillStoreExcelVO.setOfferAmount(groupVO.getCurrentValue().getOfferAmount());
                activitySeckillStoreExcelVO.setOrderNumber(groupVO.getCurrentValue().getOrderNumber());
                activitySeckillStoreExcelVO.setPayAmount(groupVO.getCurrentValue().getPayAmount());
                activitySeckillStoreExcelVO.setOrderCountRate(groupVO.getCurrentValue().getOrderCountRate());
                list.add(activitySeckillStoreExcelVO);

            }
            Set<String> fields = new HashSet<>();
            fields.add("name");
            fields.add("customerCount");
            fields.add("orderNumber");
            fields.add("payAmount");
            fields.add("commodityCount");
            fields.add("offerAmount");
            fields.add("orderCountRate");
            if (!StringUtils.isEmpty(requestVO.getBeforeTimeStart())) {
                LocalDateTime startTime = requestVO.getBeforeTimeStart();
                LocalDateTime endTime = requestVO.getBeforeTimeEnd();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                String fileName = startTime.format(formatter) + "至" + endTime.format(formatter) + "活动订单导出表";
                Function<Object, List<ActivitySeckillStoreExcelVO>> queryFunction = (param) -> list;
                excelActionService.exportAsyncExcel(ActivitySeckillStoreExcelVO.class, requestVO, queryFunction, fileName, fields);
            } else {
                String fileName = "全部活动订单导出表";
                Function<Object, List<ActivitySeckillStoreExcelVO>> queryFunction = (param) -> list;
                excelActionService.exportAsyncExcel(ActivitySeckillStoreExcelVO.class, requestVO, queryFunction, fileName, fields);
            }

        }else {
            throw exception(ErrorCodeConstants.DATA_NULL);
        }

        return true;
    }

    @Override
    public PageResult<ActivitySeckillMemberVO> activityRecordsPage(List<MetricsConfig> metrics, ActicitySeckillPageRequestVO requestVO) {
        // 构建查询参数
        EsAggDTO currentRequest = requestVO.buildCurrentRequest();
        if (ObjectUtil.isNotEmpty(requestVO.getStoreId())) {
            List<Long> data = new ArrayList<>();
            data.add(requestVO.getStoreId());
            currentRequest.setStoreIds(data);
        }
        if (ObjectUtil.isNotEmpty(requestVO.getChannel())) {
            currentRequest.setChannel(requestVO.getChannel());
        }
        if (ObjectUtil.isNotEmpty(requestVO.getMemberText())) {
            currentRequest.setMemberText(requestVO.getMemberText());
        }
        if (ObjectUtil.isNotEmpty(requestVO.getCommodityId())) {
            currentRequest.setCommodityId(requestVO.getCommodityId());
        }
        List<Integer> orderList = new ArrayList<>();
        orderList.add(60);
        currentRequest.setOrderS(orderList);
        currentRequest.setActivityId(requestVO.getActivityId());

        // 查询参与人记录
        PageResult<ActivitySeckillMemberVO> currentResult = service.paginatedGroupAggregation(
                currentRequest,
                requestVO.getPageNo(),
                metrics,
                requestVO.getPageSize()
        );

        return currentResult;
    }

    /**
     * 转换渠道ID为渠道名称
     */
    private String getChannelName(Integer channelId) {
        // 实际实现需根据渠道枚举或字典转换
        if (channelId == null) {
            return "";
        }
        // 示例：假设存在渠道映射关系
        Map<Integer, String> channelMap = new HashMap<>();
        channelMap.put(1, "APP");
        channelMap.put(2, "小程序");
        channelMap.put(3, "H5");
        return channelMap.getOrDefault(channelId, "未知渠道");
    }

    private List<ActivitySeckillStorePageVO> buildResultList(List<AnalysisTopVO> currentResult) {
        List<ActivitySeckillStorePageVO> list = new ArrayList<>();
        for (AnalysisTopVO item : currentResult) {
            ActivitySeckillStorePageVO vo = new ActivitySeckillStorePageVO();
            vo.setKey(item.getKey());
            vo.setName(item.getName());
            vo.setStoreIds(item.getStoreIds());
            vo.setCurrentValue(new ActivitySeckillStoreVO(item.getValues()));
            list.add(vo);
        }
        return list;
    }

    private List<ActivitySeckillStorePageVO> buildResultMapTwo(PageResult<AnalysisTopVO> currentResult) {
        List<ActivitySeckillStorePageVO> list = new ArrayList<>();
        for (AnalysisTopVO item : currentResult.getList()) {
            ActivitySeckillStorePageVO vo = new ActivitySeckillStorePageVO();
            vo.setKey(item.getKey());
            vo.setName(item.getName());
            vo.setStoreIds(item.getStoreIds());
            vo.setCurrentValue(new ActivitySeckillStoreVO(item.getValues()));
            list.add(vo);
        }
        return list;
    }

    private List<ActivitySeckillMemberPageVO> buildResultMapMember(PageResult<AnalysisTopVO> currentResult) {
        List<ActivitySeckillMemberPageVO> list = new ArrayList<>();
        for (AnalysisTopVO item : currentResult.getList()) {
            ActivitySeckillMemberPageVO vo = new ActivitySeckillMemberPageVO();
            vo.setKey(item.getKey());
            vo.setName(item.getName());
            vo.setStoreIds(item.getStoreIds());
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
        queryDTO.setEventType(EventType.ACTIVITY);
        queryDTO.setStartTime(times[0]);
        queryDTO.setEndTime(times[1]);
        return eventService.queryUV(queryDTO);
    }


}
