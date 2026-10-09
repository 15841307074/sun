package com.htyoudao.youdao.module.analysis.service.impl;

import cn.hutool.core.util.ObjectUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.OrderCreateTimeExcelRespVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.OrderFinishTimeExcelRespVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ReportOrderDownloadReqVO;
import com.htyoudao.youdao.module.analysis.dal.es.BzOrder;
import com.htyoudao.youdao.module.analysis.enums.OrderExportField;
import com.htyoudao.youdao.module.analysis.service.ReportDownloadService;
import com.htyoudao.youdao.module.analysis.util.ProductUtils;
import com.htyoudao.youdao.module.promotion.api.activity.ActivityApi;
import com.htyoudao.youdao.module.promotion.api.activitychannelname.ActivityChannelNameApi;
import com.htyoudao.youdao.module.system.api.org.OrgApi;
import com.htyoudao.youdao.module.system.api.org.dto.StoreOrgDTO;
import jakarta.annotation.Resource;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.analysis.api.enums.ErrorCodeConstants.*;

/**
 * @author dht
 */
@Service
@Slf4j
public class ReportDownloadServiceImpl implements ReportDownloadService {

    /**
     * 8.5.1
     */
    @Resource
    private ElasticsearchClient client;


    private static final int SCROLL_BATCH_SIZE = 1000;

    private static final String SCROLL_KEEP_ALIVE = "5m";

    @DubboReference
    private OrgApi orgApi;

    @DubboReference
    private ActivityChannelNameApi activityChannelNameApi;

    @Resource
    private ExcelActionService excelActionService;

    @DubboReference
    private ActivityApi activityApi;

    @Override
    public void orderDownload(ReportOrderDownloadReqVO requestVO){
        LocalDateTime startTime = requestVO.getStartTime();
        LocalDateTime endTime = requestVO.getEndTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        Set<String> fields = requestVO.getFields();
        if(CollectionUtils.isEmpty(fields)){
            throw exception(SELECT_AT_LEAST_ONE_OPTION);
        }

        Integer orderTime = requestVO.getOrderTime();
        String fileName = "订单下载_" + startTime.format(formatter) + "至" + endTime.format(formatter);
        //excelActionService.exportAsyncExcel(OrderExcelRespVO.class, bean, fileName, fields, firstRow);
        // 0是 订单创建时间
        if(orderTime.equals(0)){
            fields.add("createTime");
            excelActionService.exportAsyncExcel(OrderCreateTimeExcelRespVO.class,requestVO, param -> this.queryOrderByCreateTime(requestVO), fileName, fields);
        }else {
            fields.add("finishTime");
            excelActionService.exportAsyncExcel(OrderFinishTimeExcelRespVO.class,requestVO, param -> this.queryOrderByFinishTime(requestVO), fileName, fields);
        }
    }



    /**
     * 查询订单 创建时间
     * @param requestVO requestVO
     * @return OrderExcelRespVO
     */
    private List<OrderCreateTimeExcelRespVO> queryOrderByCreateTime(ReportOrderDownloadReqVO requestVO){
        Set<String> fields = requestVO.getFields();
        Query boolQuery = buildBaseQuery(requestVO);

        SearchResponse<BzOrder> initialResponse = null;
        // 2. 初始化滚动查询
        try{
            initialResponse = client.search(s -> s
                            .index("bz_order")
                            .query(q -> q.bool(b -> b.must(boolQuery)))
                            .size(SCROLL_BATCH_SIZE)
                            .scroll(sc -> sc.time(SCROLL_KEEP_ALIVE))
                            .trackTotalHits(t -> t.enabled(true))
                            .sort(sort -> sort
                                    .field(f -> f
                                            // 按创建时间排序
                                            .field("createTime")
                                            // 升序排列
                                            .order(SortOrder.Asc)
                                    )
                            )
                            .sort(sort -> sort
                                    .field(f -> f
                                            // 按店铺ID排序
                                            .field("storeId")
                                            // 升序排列
                                            .order(SortOrder.Asc)
                                    )
                            ),
                    BzOrder.class
            );
        }catch (Exception e){
            throw exception(ORDER_QUERY_ERROR);
        }


        // 3. 处理结果
        List<BzOrder> allOrders = new ArrayList<>();
        String scrollId = initialResponse.scrollId();
        assert initialResponse.hits().total() != null;
        long total = initialResponse.hits().total().value();
        log.info("开始处理订单数据，总计: {}", total);

        try {
            // 处理第一批结果
            processResponse(initialResponse, allOrders,fields.contains(OrderExportField.ACTIVITY_NAME.getFieldName()));

            // 继续滚动获取剩余结果
            while (allOrders.size() < total) {
                String finalScrollId = scrollId;
                ScrollResponse<BzOrder> scrollResponse = client.scroll(s -> s
                                .scrollId(finalScrollId)
                                .scroll(sc -> sc.time(SCROLL_KEEP_ALIVE)),
                        BzOrder.class
                );

                List<Hit<BzOrder>> hits = scrollResponse.hits().hits();
                if (hits.isEmpty()) break;

                processResponse(initialResponse, allOrders,fields.contains(OrderExportField.ACTIVITY_NAME.getFieldName()));
                scrollId = scrollResponse.scrollId();

                log.debug("已处理 {}/{} 条订单", allOrders.size(), total);
            }

            List<OrderCreateTimeExcelRespVO> bean = BeanUtils.toBean(allOrders, OrderCreateTimeExcelRespVO.class);

            boolean needOrg = fields.contains(OrderExportField.PARENT_ORG.getFieldName());


            //boolean needActivity = fields.contains(OrderExportField.ACTIVITY_NAME.getFieldName());
            Consumer<OrderCreateTimeExcelRespVO> consumer = vo -> {};

            if (needOrg) {
                List<Long> storeIds = bean.stream().map(OrderCreateTimeExcelRespVO::getStoreId).toList();
                CommonResult<Set<StoreOrgDTO>> commonResult = orgApi.getOrgListByStoreId(storeIds);
                Set<StoreOrgDTO> data = commonResult.getData();
                Map<Long, String> storeMap = data.stream()
                        .collect(Collectors.toMap(
                                StoreOrgDTO::getStoreId,
                                StoreOrgDTO::getOrgName
                        ));
                consumer = consumer.andThen(vo ->
                        vo.setOrg(storeMap.get(vo.getStoreId()))
                );
            }

            boolean needChannel = fields.contains(OrderExportField.CHANNEL.getFieldName());
            if(needChannel){
                CommonResult<Map<Long, String>> mapCommonResult = activityChannelNameApi.selectChannelNameMap();
                Map<Long, String> channelNameMap = mapCommonResult.getData();
                consumer = consumer.andThen(vo -> vo.setChannelType(ObjectUtil.isEmpty(channelNameMap.get(vo.getChannel())) ? "历史渠道数据" : channelNameMap.get(vo.getChannel())));
            }
            if(needChannel || needOrg){
                bean.forEach(consumer);
            }
//            if (needActivity) {
//                Set<Long> activityIds = bean.stream().map(OrderCreateTimeExcelRespVO::getActivityId).collect(Collectors.toSet());
//                CommonResult<Map<Long, String>> activityResult = activityApi.selectActivityByIds(activityIds);
//                Map<Long, String> activityMap = activityResult.getData();
//                consumer = consumer.andThen(vo ->
//                        vo.setActivityName(activityMap.get(vo.getActivityId()))
//                );
//            }
            return bean;
        } catch (Exception e){
            throw exception(SCROLL_ORDER_QUERY_ERROR);
        }finally {
            // 清理滚动上下文
            if (scrollId != null) {
                try {
                    String finalScrollId1 = scrollId;
                    client.clearScroll(c -> c.scrollId(finalScrollId1));
                } catch (Exception e) {
                    log.warn("清除滚动上下文失败", e);
                }
            }
            log.info("订单数据处理完成，共处理 {} 条", allOrders.size());
        }
    }


    /**
     * 查询订单 完成时间
     * @param requestVO requestVO
     * @return OrderFinishTimeExcelRespVO
     */
    private List<OrderFinishTimeExcelRespVO> queryOrderByFinishTime(ReportOrderDownloadReqVO requestVO){
        Set<String> fields = requestVO.getFields();
        Query boolQuery = buildBaseQuery(requestVO);

        SearchResponse<BzOrder> initialResponse = null;
        // 2. 初始化滚动查询
        try{
            initialResponse = client.search(s -> s
                            .index("bz_order")
                            .query(q -> q.bool(b -> b.must(boolQuery)))
                            .size(SCROLL_BATCH_SIZE)
                            .scroll(sc -> sc.time(SCROLL_KEEP_ALIVE))
                            .trackTotalHits(t -> t.enabled(true))
                            .sort(sort -> sort
                                    .field(f -> f
                                            // 按创建时间排序
                                            .field("createTime")
                                            // 升序排列
                                            .order(SortOrder.Asc)
                                    )
                            )
                            .sort(sort -> sort
                                    .field(f -> f
                                            // 按店铺ID排序
                                            .field("storeId")
                                            // 升序排列
                                            .order(SortOrder.Asc)
                                    )
                            ),
                    BzOrder.class
            );
        }catch (Exception e){
            throw exception(ORDER_QUERY_ERROR);
        }


        // 3. 处理结果
        List<BzOrder> allOrders = new ArrayList<>();
        String scrollId = initialResponse.scrollId();
        assert initialResponse.hits().total() != null;
        long total = initialResponse.hits().total().value();
        log.info("开始处理订单数据，总计: {}", total);

        try {
            // 处理第一批结果
            processResponse(initialResponse, allOrders,fields.contains(OrderExportField.ACTIVITY_NAME.getFieldName()));

            // 继续滚动获取剩余结果
            while (allOrders.size() < total) {
                String finalScrollId = scrollId;
                ScrollResponse<BzOrder> scrollResponse = client.scroll(s -> s
                                .scrollId(finalScrollId)
                                .scroll(sc -> sc.time(SCROLL_KEEP_ALIVE)),
                        BzOrder.class
                );

                List<Hit<BzOrder>> hits = scrollResponse.hits().hits();
                if (hits.isEmpty()) break;

                processResponse(initialResponse, allOrders,fields.contains(OrderExportField.ACTIVITY_NAME.getFieldName()));
                scrollId = scrollResponse.scrollId();

                log.debug("已处理 {}/{} 条订单", allOrders.size(), total);
            }

//            List<OrderFinishTimeExcelRespVO> bean = BeanUtils.toBean(allOrders, OrderFinishTimeExcelRespVO.class);
//            if(fields.contains(OrderExportField.PARENT_ORG.getFieldName()) || fields.contains(OrderExportField.ACTIVITY_NAME.getFieldName())){
//                List<Long> storeIds = bean.stream().map(OrderFinishTimeExcelRespVO::getStoreId).toList();
//                CommonResult<Set<StoreOrgDTO>> commonResult = orgApi.getOrgListByStoreId(storeIds);
//                Set<StoreOrgDTO> data = commonResult.getData();
//                Map<Long, String> storeIdToOrgNameMap = data.stream()
//                        .collect(Collectors.toMap(
//                                StoreOrgDTO::getStoreId,
//                                StoreOrgDTO::getOrgName
//                        ));
//                for (OrderFinishTimeExcelRespVO orderCreateTimeExcelRespVO : bean) {
//                    orderCreateTimeExcelRespVO.setOrg(storeIdToOrgNameMap.get(orderCreateTimeExcelRespVO.getStoreId()));
//                    if(requestVO.getOrderTime() == 1){
//                        orderCreateTimeExcelRespVO.setFinishTime2(orderCreateTimeExcelRespVO.getFinishTime());
//                    }
//                }
            List<OrderFinishTimeExcelRespVO> bean = BeanUtils.toBean(allOrders, OrderFinishTimeExcelRespVO.class);
            boolean needOrg = fields.contains(OrderExportField.PARENT_ORG.getFieldName());
            //boolean needActivity = fields.contains(OrderExportField.ACTIVITY_NAME.getFieldName());
            Consumer<OrderFinishTimeExcelRespVO> consumer = vo -> {};

            if (needOrg) {
                List<Long> storeIds = bean.stream().map(OrderFinishTimeExcelRespVO::getStoreId).toList();
                CommonResult<Set<StoreOrgDTO>> commonResult = orgApi.getOrgListByStoreId(storeIds);
                Set<StoreOrgDTO> data = commonResult.getData();
                Map<Long, String> storeMap = data.stream()
                        .collect(Collectors.toMap(
                                StoreOrgDTO::getStoreId,
                                StoreOrgDTO::getOrgName
                        ));
                consumer = consumer.andThen(vo ->
                        vo.setOrg(storeMap.get(vo.getStoreId()))
                );
            }

            boolean needChannel = fields.contains(OrderExportField.CHANNEL.getFieldName());
            if(needChannel){
                CommonResult<Map<Long, String>> mapCommonResult = activityChannelNameApi.selectChannelNameMap();
                Map<Long, String> channelNameMap = mapCommonResult.getData();
                consumer = consumer.andThen(vo -> vo.setChannelType(ObjectUtil.isEmpty(channelNameMap.get(vo.getChannel())) ? "历史渠道数据" : channelNameMap.get(vo.getChannel())));
            }

            if(needChannel || needOrg){
                bean.forEach(consumer);
            }

//            if (needActivity) {
//                Set<Long> activityIds = bean.stream().map(OrderFinishTimeExcelRespVO::getActivityId).collect(Collectors.toSet());
//                CommonResult<Map<Long, String>> activityResult = activityApi.selectActivityByIds(activityIds);
//                Map<Long, String> activityMap = activityResult.getData();
//                consumer = consumer.andThen(vo ->
//                        vo.setActivityName(activityMap.get(vo.getActivityId()))
//                );
//            }
            return bean;
        } catch (Exception e){
            throw exception(SCROLL_ORDER_QUERY_ERROR);
        }finally {
            // 清理滚动上下文
            if (scrollId != null) {
                try {
                    String finalScrollId1 = scrollId;
                    client.clearScroll(c -> c.scrollId(finalScrollId1));
                } catch (Exception e) {
                    log.warn("清除滚动上下文失败", e);
                }
            }
            log.info("订单数据处理完成，共处理 {} 条", allOrders.size());
        }
    }

    /**
     * 处理每条订单的商品
     * @param response response
     * @param resultList  resultList
     */
    private void processResponse(SearchResponse<BzOrder> response, List<BzOrder> resultList,boolean isActivityName) {
        for (Hit<BzOrder> hit : response.hits().hits()) {
            if (hit.source() != null) {
                // 计算商品总数
                BzOrder order = hit.source();
                if (order.getProduct() != null) {
                    List<BzOrder.Product> products = order.getProduct();
                    int totalGoods = products.stream()
                            .mapToInt(BzOrder.Product::getGoodsNum)
                            .sum();
                    order.setProductNum(totalGoods);
                    Map<String,Integer> productsMap = new HashMap<>(16);
                    Map<String,Double> goodsAmountMap = new HashMap<>(16);
                    String label = "/单价￥";
                    Set<String> activitySet = new LinkedHashSet<>();
                    for (BzOrder.Product product : products) {
                        //不拼接套餐内子品
                        if (Objects.equals(product.getIsSon(), 1)) {
                            continue;
                        }
                        productsMap.merge(product.getGoodsName(), product.getGoodsNum(), Integer::sum);
                        goodsAmountMap.put(product.getGoodsName(), product.getGoodsAmount());

                        String activityName = product.getActivityName();
                        if (ObjectUtil.isNotEmpty(activityName)) {
                            activitySet.add(activityName);
                        }
                    }
                    StringBuilder sb = new StringBuilder();
                    // 遍历合并后的商品映射，拼接字符串
                    int size = productsMap.size();
                    int index = 0;

                    for (Map.Entry<String, Integer> entry : productsMap.entrySet()) {
                        sb.append(entry.getKey())
                                .append("*")
                                .append(entry.getValue())
                                .append(label)
                                .append(goodsAmountMap.get(entry.getKey()));

                        // 判断当前是否是最后一个元素，如果是最后一个元素不添加换行符
                        if (++index < size) {
                            sb.append("\n");
                        }
                        sb.append(";");
                    }
                    order.setProductName(sb.toString());
                    String activityNameStr = CollectionUtils.isEmpty(activitySet)
                                    ? ""
                                    : String.join(";", activitySet);
                    order.setActivityName(activityNameStr);

                }
                if(isActivityName){
                    String activityName = ProductUtils.buildProductString(order.getProduct());
                    order.setActivityName(activityName);
                }
                resultList.add(order);
            }
        }
    }

    /**
     * 构建基础查询条件
     */
    private Query buildBaseQuery(ReportOrderDownloadReqVO reqVO) {
        List<Query> mustQueries = new ArrayList<>();

        // 时间范围条件
        mustQueries.add(buildTimeRangeQuery(reqVO));

        // 门店条件
        if (reqVO.getAllStore() == 1 && CollectionUtils.isNotEmpty(reqVO.getStoreIds())) {
            mustQueries.add(Query.of(q -> q
                    .terms(t -> t
                            .field("storeId")
                            .terms(t2 -> t2
                                    .value(reqVO.getStoreIds().stream()
                                            .map(FieldValue::of)
                                            .collect(Collectors.toList()))
                            )
                    )
            ));
        }

        return Query.of(q -> q.bool(b -> b.must(mustQueries)));
    }
//
    /**
     * 构建时间范围查询
     */
    private Query buildTimeRangeQuery(ReportOrderDownloadReqVO reqVO) {
        if (reqVO.getStartTime() == null || reqVO.getEndTime() == null) {
            return Query.of(q -> q.matchAll(m -> m));
        }

        String timeField = reqVO.getOrderTime() == 0 ? "createTime" : "finishTime";
        LocalDateTime startTime = reqVO.getStartTime();
        LocalDateTime endTime = reqVO.getEndTime();

        return Query.of(m -> m.range(
                r -> r.date(dr -> dr.field(timeField).gte(formatDateTime(startTime)).lte(formatDateTime(endTime)))));

    }

    // 辅助方法：格式化 LocalDateTime 为 ISO 格式字符串
    private String formatDateTime(LocalDateTime dateTime) {
        ZonedDateTime zoned = dateTime.atZone(ZoneId.of("Asia/Shanghai"));
        return zoned.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
