package com.htyoudao.youdao.module.analysis.service;

import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.CUSTOMER_COUNT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.DC_PAY_AMOUNT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.MINI_AVERAGE_PAYMENT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.MINI_CUSTOMER_COUNT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.MINI_NEW_CUSTOMER_COUNT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.MINI_PAY_AMOUNT;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.MINI_REPEAT_BUYERS;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.MINI_VALID_ORDERS;
import static com.htyoudao.youdao.module.analysis.enums.MetricsConfig.STORE_NAME;

import co.elastic.clients.elasticsearch._types.SortOrder;
import com.alibaba.fastjson.JSON;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.AnalysisServerApplication;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.base.AnalysisTopVO;
import com.htyoudao.youdao.module.analysis.enums.EsDateFormat;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import com.htyoudao.youdao.module.analysis.service.dto.AggOrgDTO;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import com.htyoudao.youdao.module.analysis.service.dto.RangeDTO;
import com.htyoudao.youdao.module.analysis.service.impl.EsAggregationServiceImpl;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AnalysisServerApplication.class)
public class BzOrderServiceTest {

    @Resource
    private EsAggregationServiceImpl aggregationService;

    private EsAggDTO getAggReq() {
        EsAggDTO request = new EsAggDTO();
        request.setTimes(new LocalDateTime[]{LocalDateTime.now().minusDays(10), LocalDateTime.now()});
        return request;
    }


    @Test // 根据 ID 编号，查询一条记录
    public void testBatchAggregateMetrics() {
        LocalDateTime[] start = {LocalDateTime.now().minusDays(10), LocalDateTime.now()};
//        List<Long> storeIds = Arrays.asList(1252629216839598080L);

        EsAggDTO request = new EsAggDTO();
        request.setTimes(start);

        List<MetricsConfig> list = Arrays.asList(MetricsConfig.ORDER_AMOUNT);
        Map<String, Double> stringDoubleMap = aggregationService.batchAggregateMetrics(request, list);

        System.out.println(stringDoubleMap);
    }


    @Test // 根据 ID 编号，查询一条记录
    public void testChart() {
        LocalDateTime[] start = {LocalDateTime.now().minusDays(10), LocalDateTime.now()};
//        List<Long> storeIds = Arrays.asList(1252629216839598080L);

        MetricsConfig alipayOrders = MetricsConfig.ORDER_AMOUNT;

        EsAggDTO request = new EsAggDTO();
        request.setTimes(start);

        Map<String, Double> stringDoubleMap = aggregationService.analyzeByTimeGranularity(
            request,
            EsDateFormat.DAY,
            alipayOrders
        );

        System.out.println(stringDoubleMap);
    }


    @Test
    public void testRangeList() {
        LocalDateTime[] start = {LocalDateTime.now().minusDays(10), LocalDateTime.now()};
        List<Long> storeIds = Arrays.asList(1252629216839598080L);

        EsAggDTO request = new EsAggDTO();
        request.setTimes(start);
        request.setStoreIds(storeIds);

        List<RangeDTO> rangeDTOS = List.of(new RangeDTO("0-7", 0.0, 7.0),
            new RangeDTO("7-14", 7.0, 14.0),
            new RangeDTO("14-21", 14.0, 21.0),
            new RangeDTO("21-28", 21.0, 28.0)
        );
        Map<String, Double> stringDoubleMap = aggregationService.rangeList(request,
            "orderAmount",
            MetricsConfig.CUSTOMER_COUNT,
            rangeDTOS);

        System.out.println(JSON.toJSONString(stringDoubleMap));
    }


    @Test
    public void testFrequencyAggGroup() {

        LocalDateTime[] start = {LocalDateTime.now().minusDays(1), LocalDateTime.now()};
        List<Long> storeIds = Arrays.asList(1252629216839598080L);

        EsAggDTO request = new EsAggDTO();
        request.setTimes(start);
        request.setStoreIds(storeIds);

        Map<Long, Double> longDoubleMap = aggregationService.frequencyAggGroup(request, CUSTOMER_COUNT);
        System.out.println(JSON.toJSONString(longDoubleMap));
    }


    @Test
    public void testPaginatedGroupAggregation() {

        EsAggDTO aggReq = getAggReq();

        final List<MetricsConfig> metrics = List.of(
            DC_PAY_AMOUNT, MINI_PAY_AMOUNT, MINI_VALID_ORDERS, MINI_AVERAGE_PAYMENT,
            MINI_CUSTOMER_COUNT, MINI_REPEAT_BUYERS, MINI_NEW_CUSTOMER_COUNT, STORE_NAME
        );

        final String groupField = "storeId";

        PageResult<AnalysisTopVO> result = aggregationService.paginatedGroupAggregation(aggReq,
            metrics,
            groupField,
            CUSTOMER_COUNT,
            SortOrder.Desc,
            1,
            10
        );
        System.out.println(JSON.toJSONString(result));
    }




    @Test
    public void testOrgPage() {

        EsAggDTO aggReq = getAggReq();

        final List<MetricsConfig> metrics = List.of(
            DC_PAY_AMOUNT, MINI_PAY_AMOUNT, MINI_VALID_ORDERS, MINI_AVERAGE_PAYMENT,
            MINI_CUSTOMER_COUNT, MINI_REPEAT_BUYERS, MINI_NEW_CUSTOMER_COUNT, STORE_NAME
        );

        final String groupField = "storeId";

        AggOrgDTO aggOrgDTO1 = new AggOrgDTO();
        aggOrgDTO1.setStoreIds(List.of(1162599097729024L,1153983570935808L));

        AggOrgDTO aggOrgDTO2 = new AggOrgDTO();
        aggOrgDTO2.setStoreIds(List.of(1162599097729024L,1162599097729024L));

        PageResult<AnalysisTopVO> result = aggregationService.paginatedRangeAggregation(aggReq,
            List.of(aggOrgDTO2,aggOrgDTO1),
            metrics,
            groupField,
            CUSTOMER_COUNT,
            SortOrder.Desc,
            1,
            10
        );
        System.out.println(JSON.toJSONString(result));
    }

}

