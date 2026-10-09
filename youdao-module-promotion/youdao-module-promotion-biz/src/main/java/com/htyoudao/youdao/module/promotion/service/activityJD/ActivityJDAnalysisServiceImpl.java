package com.htyoudao.youdao.module.promotion.service.activityJD;

import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.module.promotion.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.ActivityJDEventReqVO;
import com.htyoudao.youdao.module.promotion.controller.admin.activityJD.vo.ActivityJdRequestVO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activity.ActivityDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.es.BzOrderPoints;
import com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.es.MetricsConfig;
import com.htyoudao.youdao.module.promotion.dal.dataobject.exchangelog.ActivityExchangeLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.goodcouponpackage.GoodCouponPackageDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotteryLogDO;
import com.htyoudao.youdao.module.promotion.dal.dataobject.lottery.LotterySettingsDO;
import com.htyoudao.youdao.module.promotion.dal.mysql.activity.ActivityMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.exchangelog.ExchangeLogMapper;
import com.htyoudao.youdao.module.promotion.dal.mysql.goodcouponpackage.GoodCouponPackageMapper;
import com.htyoudao.youdao.module.promotion.enums.EventType;
import com.htyoudao.youdao.module.promotion.service.activity.ActivityService;
import com.htyoudao.youdao.module.promotion.service.lottery.IEventService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.es.MetricsConfig.MEMBER_NUMBER;
import static com.htyoudao.youdao.module.promotion.dal.dataobject.activityJD.es.MetricsConfig.POINT_NUMBER;

@Service
public class ActivityJDAnalysisServiceImpl implements ActivityJDAnalysisService{


    @Resource
    private IEventService eventService;


    @Resource
    private ActivityService activityService;

    @Resource
    private ActivityMapper activityMapper;


    @Resource
    private GoodCouponPackageMapper goodCouponPackageMapper;

    @Resource
    private ExchangeLogMapper exchangeLogMapper;


    @Resource
    private ElasticsearchClient client;


    private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Asia/Shanghai");
    @Override
    public Map<String, Object> getLotteryLogAnalysis(String id) {

        Long businessId = BusinessContextHolder.getBusinessId();

        Map<String, Object> resultObj = new HashMap<>();
        Map<String, Long> result = new HashMap<>();
        ActivityDO activityDO = activityMapper.selectById(id);

        // 获取抽奖活动开始时间
        Date lotteryStartTime = activityDO.getStartDate();
        Date lotteryEndTime = activityDO.getEndDate();
        LocalDateTime start = convertDateToLocalDateTime(lotteryStartTime);
        LocalDateTime end = convertDateToLocalDateTime(lotteryEndTime);
        if (end != null) {
            end = end.withHour(23)
                    .withMinute(59)
                    .withSecond(59)
                    .withNano(0); // 清除纳秒，确保时间精确到秒
        }


        // ES统计总浏览量 总访客量
        buildLuckyDrawResult(result, id, lotteryStartTime, lotteryEndTime, businessId);
        // ES统计分享人数
        buildShareResult(result, id, lotteryStartTime, lotteryEndTime, businessId);
        resultObj.putAll(result);
        ActivityJdRequestVO activityJdRequestVO = new ActivityJdRequestVO();
        long number = Long.parseLong(id);
        activityJdRequestVO.setActivityId(number);
        Map<String, Double> query = this.query(activityJdRequestVO);
        resultObj.putAll(query);

        //统计优惠卷数量

        LambdaQueryWrapper<ActivityExchangeLogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ActivityExchangeLogDO::getActivityId,id);
        List<ActivityExchangeLogDO> activityExchangeLogDOS = exchangeLogMapper.selectList(wrapper);
        int count  = 0;
        if(ObjectUtil.isNotEmpty(activityExchangeLogDOS)){

            for (ActivityExchangeLogDO activityExchangeLogDO : activityExchangeLogDOS) {
                if(activityExchangeLogDO.getAwardType().equals(0)){
                    count = count +1;
                }else if(activityExchangeLogDO.getAwardType().equals(1)){
                    if(ObjectUtil.isNotEmpty(activityExchangeLogDO.getForeignId())){
                        LambdaQueryWrapper<GoodCouponPackageDO> lambdaQueryWrapper = new LambdaQueryWrapperX<>();
                        lambdaQueryWrapper.eq(GoodCouponPackageDO::getPackageId, activityExchangeLogDO.getForeignId());
                        List<GoodCouponPackageDO> goodCouponPackageDOS = goodCouponPackageMapper.selectList(lambdaQueryWrapper);
                        if(ObjectUtil.isNotEmpty(goodCouponPackageDOS)){
                            count = count + goodCouponPackageDOS.size();

                        }else{
                            count = count +1;
                        }
                    }else{
                        count = count +1;
                    }


                }
            }
            resultObj.put("couponCount",count);
        }else{
            resultObj.put("couponCount",count);
        }

        return resultObj;
    }

    @Override
    public Map<String, Object> getLotteryLogDailyAnalysis(ActivityJDEventReqVO activityJDEventReqVO) {

        Long businessId = BusinessContextHolder.getBusinessId();

        Map<String, Map<String, Long>> result = new HashMap<>();

        // 获取抽奖活动开始时间
        LocalDateTime startTime = activityJDEventReqVO.getStartTime();
        LocalDateTime endTime = activityJDEventReqVO.getEndTime();
        String id = activityJDEventReqVO.getId();

        // 若startTime与endTime均为空 默认最近30天
        if (startTime == null && endTime == null) {
            // 结束时间：当前时间的23:59:59
            endTime = LocalDateTime.now()
                    .withHour(23)
                    .withMinute(59)
                    .withSecond(59)
                    .withNano(0); // 忽略纳秒

            // 开始时间：30天前的00:00:00
            startTime = endTime.minusDays(29)
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .withNano(0);

            if (startTime.isAfter(endTime)) {
                throw exception(ErrorCodeConstants.LOTTERY_ANALYSIS_TIME_ERROR);
            }

            // 3. 计算两个时间之间的完整天数差（忽略时分秒，按日期计算）
            // 例如：2025-09-25 23:59:59 到 2025-09-26 00:00:00 算1天
            long daysDiff = ChronoUnit.DAYS.between(startTime, endTime) + 1;
            if (daysDiff > 180) {
                throw exception(ErrorCodeConstants.LOTTERY_ANALYSIS_TIME_OUT_ERROR);
            }
        }

        buildLuckyDrawDailyResult(result, id, startTime, endTime, businessId);

        return convertStatsMapToArrays(result);
    }

    /**
     * Date 转 LocalDateTime（指定业务时区，避免时区误差）
     *
     * @param date 待转换的 Date 对象（null 时返回 null）
     * @return LocalDateTime 转换后的本地时间
     */
    public static LocalDateTime convertDateToLocalDateTime(Date date) {
        if (date == null) {
            return null; // 处理 null，避免空指针
        }
        // 步骤：Date -> Instant -> ZonedDateTime（指定时区）-> LocalDateTime
        return Instant.ofEpochMilli(date.getTime())
                .atZone(BUSINESS_TIME_ZONE)
                .toLocalDateTime();
    }

    private void buildShareResult(Map<String, Long> result, String id, Date lotteryStartTime, Date lotteryEndTime, Long businessId) {


        LocalDateTime start = convertDateToLocalDateTime(lotteryStartTime);
        LocalDateTime end = convertDateToLocalDateTime(lotteryEndTime);
        if (end != null) {
            end = end.withHour(23)
                    .withMinute(59)
                    .withSecond(59)
                    .withNano(0); // 清除纳秒，确保时间精确到秒
        }
        Long count = eventService.statUv(EventType.POINT_SHARE, id, start, end, businessId);
        result.put("shareCount", count);
    }


    private void buildLuckyDrawResult(Map<String, Long> result, String id, Date lotteryStartTime, Date lotteryEndTime, Long businessId) {
        LocalDateTime start = convertDateToLocalDateTime(lotteryStartTime);
        LocalDateTime end = convertDateToLocalDateTime(lotteryEndTime);
        if (end != null) {
            end = end.withHour(23)
                    .withMinute(59)
                    .withSecond(59)
                    .withNano(0); // 清除纳秒，确保时间精确到秒
        }
        Map<String, Long> stringLongMap = eventService.statPvUv(EventType.POINT_DRAW, id, start, end, businessId);

        result.putAll(stringLongMap);
    }

    private void buildLuckyDrawDailyResult(Map<String, Map<String, Long>> result, String id, LocalDateTime start, LocalDateTime end, Long businessId) {
        Map<String, Map<String, Long>> stringLongMap = eventService.statDailyPvUv(EventType.POINT_DRAW, id, start, end, businessId);
        result.putAll(stringLongMap);
    }

    /**
     * 转换统计数据为包含数组的Map
     *
     * @param statsMap 原始统计数据，结构为:
     *                 外层Key: 日期(yyyy-MM-dd)
     *                 内层Map: Key为指标名("pv"或"uv")，Value为指标值
     * @return 转换后的Map，包含三个键:
     * - "times": 日期数组(String[])
     * - "pvValues": PV数值数组(Long[])
     * - "uvValues": UV数值数组(Long[])
     * @throws IllegalArgumentException 当原始数据为空或格式错误时抛出
     */
    public Map<String, Object> convertStatsMapToArrays(Map<String, Map<String, Long>> statsMap) {
        // 校验原始数据非空
        if (statsMap == null || statsMap.isEmpty()) {
            throw new IllegalArgumentException("原始统计数据 Map 不能为空");
        }

        // 提取外层日期并排序
        List<String> dateList = new ArrayList<>(statsMap.keySet());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 按日期顺序排序
        dateList.sort((dateStr1, dateStr2) -> {
            try {
                LocalDate date1 = LocalDate.parse(dateStr1, dateFormatter);
                LocalDate date2 = LocalDate.parse(dateStr2, dateFormatter);
                return date1.compareTo(date2);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                        "日期格式错误，需为 yyyy-MM-dd：" + dateStr1 + " / " + dateStr2, e);
            }
        });

        // 初始化目标数组
        int dataSize = dateList.size();
        String[] dateArray = new String[dataSize];
        Long[] uvArray = new Long[dataSize];
        Long[] pvArray = new Long[dataSize];

        // 填充数组数据
        for (int i = 0; i < dataSize; i++) {
            String currentDate = dateList.get(i);
            Map<String, Long> innerMap = statsMap.get(currentDate);

            // 校验内层Map非空
            if (innerMap == null) {
                throw new IllegalArgumentException(
                        "日期 " + currentDate + " 对应的内层指标 Map 不能为空");
            }

            // 填充日期数组
            dateArray[i] = currentDate;

            // 填充UV数组，缺失时补0
            uvArray[i] = innerMap.getOrDefault("uv", 0L);

            // 填充PV数组，缺失时补0
            pvArray[i] = innerMap.getOrDefault("pv", 0L);
        }

        // 构建结果Map
        Map<String, Object> resultMap = new HashMap<>(3);
        resultMap.put("times", dateArray);
        resultMap.put("pvValues", pvArray);
        resultMap.put("uvValues", uvArray);

        return resultMap;
    }



    @Override
    public Map<String, Double> query(ActivityJdRequestVO requestVO) {
        Map<String, Double> result = new HashMap<>();
        List<MetricsConfig> metrics = List.of(POINT_NUMBER,MEMBER_NUMBER);
        Long activityId = requestVO.getActivityId();

        for (MetricsConfig metric : metrics) {
            try {
                // 1. 构建查询条件
                BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
//                boolBuilder.filter(m -> m.term(
//                        t -> t.field("businessId")
//                                .value(FieldValue.of(BusinessContextHolder.getBusinessId()))
//                ));


                // activityId在ES中是字符串，需要转换为字符串查询
                boolBuilder.filter(m -> m.term(
                        t -> t.field("activityId")
                                .value(FieldValue.of(activityId.toString()))  // 确保转为字符串
                ));


                // 其他指标原有处理逻辑
//                SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
//                        .query(metric.addTermsCondition(boolBuilder))
//                        .aggregations(buildAggregationStructure(metric)));
                // 使用keyword字段进行聚合
                SearchRequest searchRequest = SearchRequest.of(s -> s.size(0)
                        .query(q -> q.bool(boolBuilder.build()))
                        .aggregations(buildAggregationStructure(metric)));
                System.out.println("Search Request: " + searchRequest);
                SearchResponse<BzOrderPoints> response = client.search(searchRequest, BzOrderPoints.class);
                processAggregationResults(response, metric, result);


            } catch (IOException e) {
//                throw exception(ErrorCodeConstants.ES_QUERY_ERROR);
            }
        }
        return result;
    }

    private Map<String, Aggregation> buildAggregationStructure(MetricsConfig metric) {
        Map<String, Aggregation> aggs = new HashMap<>();

        aggs.put(metric.getCode(), buildSimpleAggregation(metric,metric.getField()));

        return aggs;
    }

    private void processAggregationResults(SearchResponse<BzOrderPoints> response,
                                           MetricsConfig metric,
                                           Map<String, Double> result) {
        Aggregate rootAggregate = response.aggregations().get(metric.getCode());

        result.put(metric.getCode(), formatValue(getAggregationValue(rootAggregate, metric)));
    }


//    private Aggregation buildSimpleAggregation(MetricsConfig param, String fieldName) {
//        return switch (param.getAggType()) {
//            case SUM -> Aggregation.of(a -> a.sum(s -> s.field(fieldName)));
//            case AVG -> Aggregation.of(a -> a.avg(avg -> avg.field(fieldName)));
//            case COUNT -> Aggregation.of(a -> a.valueCount(vc -> vc.field(fieldName)));
//            case MAX -> Aggregation.of(a -> a.max(m -> m.field(fieldName)));
//            case MIN -> Aggregation.of(a -> a.min(m -> m.field(fieldName)));
//            case CARDINALITY -> Aggregation.of(a -> a.cardinality(m -> m.field(fieldName)));
//            case HIT -> Aggregation.of(a -> a.topHits(th -> th.size(1)
//                    .source(so -> so.filter(f -> f.includes(fieldName)))));
//            case MIN_COUNT -> Aggregation.of(a -> a.terms(t -> t.field(fieldName)
//                    .minDocCount(2).size(10000)));
//        };
//    }

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
            default -> Aggregation.of(a -> a.valueCount(vc -> vc.field(fieldName + ".keyword"))); // 默认使用keyword
        };
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

    private double formatValue(double value) {
        return value;
    }
}
