package com.htyoudao.youdao.module.analysis.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ActicitytyNjnzPageRequestVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityMzStoreExcelVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityMzStorePageRespVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.activity.ActivityNjnzStorePageVO;
import com.htyoudao.youdao.module.analysis.dal.es.BzOrderProduct;
import com.htyoudao.youdao.module.analysis.service.IActivityAggregationService;
import com.htyoudao.youdao.module.analysis.service.IActivityMzAggregationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActivityMzAggregationServiceImpl implements IActivityMzAggregationService {

    @Resource
    private ElasticsearchClient client;

    @Resource
    private IActivityAggregationService activityAggregationService;

    @Resource
    private ExcelActionService excelActionService;

    @Override
    public PageResult<ActivityMzStorePageRespVO> mzStorePage(ActicitytyNjnzPageRequestVO requestVO) {
        // 1. 从 bz_order 获取基础门店分页数据
        PageResult<ActivityNjnzStorePageVO> pageResult = activityAggregationService.activityNjnzStorePage(requestVO);

        if (pageResult == null || pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return new PageResult<>();
        }

        // 2. 提取门店ID列表
        List<Long> storeIds = pageResult.getList().stream()
                .map(vo -> Long.valueOf(vo.getKey()))
                .collect(Collectors.toList());

        // 3. 从 bz_order_product 查询赠送商品数量
        Map<Long, Long> giftCountMap = queryGiftCommodityCount(requestVO.getActivityId(), storeIds);

        // 4. 转换为扁平 VO 并合并赠送商品数量
        PageResult<ActivityMzStorePageRespVO> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        result.setList(pageResult.getList().stream().map(vo -> {
            ActivityMzStorePageRespVO resp = convertToMzStorePageResp(vo);
            Long storeId = Long.valueOf(vo.getKey());
            resp.setGiftCommodityCount(giftCountMap.getOrDefault(storeId, 0L));
            return resp;
        }).collect(Collectors.toList()));

        return result;
    }

    @Override
    public Boolean mzExportData(ActicitytyNjnzPageRequestVO requestVO) {
        // 1. 获取全量门店数据（不分页）
        PageResult<ActivityNjnzStorePageVO> allResult = activityAggregationService.activityNjnzStorePage(buildExportRequest(requestVO));

        if (allResult == null || allResult.getList() == null || allResult.getList().isEmpty()) {
            return true;
        }

        // 2. 提取门店ID列表
        List<Long> storeIds = allResult.getList().stream()
                .map(vo -> Long.valueOf(vo.getKey()))
                .collect(Collectors.toList());

        // 3. 查询赠送商品数量
        Map<Long, Long> giftCountMap = queryGiftCommodityCount(requestVO.getActivityId(), storeIds);

        // 4. 组装导出数据
        List<ActivityMzStoreExcelVO> exportList = new ArrayList<>();
        for (ActivityNjnzStorePageVO vo : allResult.getList()) {
            ActivityMzStoreExcelVO excelVO = new ActivityMzStoreExcelVO();
            excelVO.setStoreName(vo.getName());
            if (vo.getCurrentValue() != null) {
                excelVO.setCustomerCount(vo.getCurrentValue().getCustomerCount());
                excelVO.setOrderNumber(vo.getCurrentValue().getOrderNumber());
                excelVO.setPayAmount(vo.getCurrentValue().getPayAmount());
                excelVO.setCommodityCount(vo.getCurrentValue().getCommodityCount());
                excelVO.setOfferAmount(vo.getCurrentValue().getOfferAmount());
            }
            Long storeId = Long.valueOf(vo.getKey());
            excelVO.setGiftCommodityCount(giftCountMap.getOrDefault(storeId, 0L));
            exportList.add(excelVO);
        }

        // 5. 导出 Excel
        Set<String> fields = new LinkedHashSet<>();
        fields.add("storeName");
        fields.add("customerCount");
        fields.add("orderNumber");
        fields.add("payAmount");
        fields.add("commodityCount");
        fields.add("giftCommodityCount");
        fields.add("offerAmount");

        String fileName;
        if (requestVO.getBeforeTimeStart() != null && requestVO.getBeforeTimeEnd() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            fileName = requestVO.getBeforeTimeStart().format(formatter) + "至"
                    + requestVO.getBeforeTimeEnd().format(formatter) + "满赠活动数据分析导出";
        } else {
            fileName = "满赠活动数据分析导出";
        }

        excelActionService.exportAsyncExcel(ActivityMzStoreExcelVO.class, requestVO,
                param -> exportList, fileName, fields);
        return true;
    }

    @Override
    public Map<Long, Long> queryGiftCommodityCount(Long activityId, List<Long> storeIds) {
        if (activityId == null || storeIds == null || storeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<FieldValue> storeIdValues = storeIds.stream()
                    .map(FieldValue::of)
                    .collect(Collectors.toList());

            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index("bz_order_product")
                    .size(0)
                    .query(q -> q.bool(b -> b
                            .must(m -> m.term(t -> t.field("activityId").value(activityId)))
                            .must(m -> m.term(t -> t.field("isPurchase").value(0)))
                            .must(m -> m.terms(t -> t
                                    .field("storeId")
                                    .terms(tv -> tv.value(storeIdValues))
                            ))
                    ))
                    .aggregations("by_store", a -> a
                            .terms(t -> t
                                    .field("storeId")
                                    .size(storeIds.size())
                            )
                            .aggregations("gift_count", sub -> sub
                                    .sum(sum -> sum.field("goodsNum"))
                            )
                    )
            );

            SearchResponse<BzOrderProduct> response = client.search(searchRequest, BzOrderProduct.class);
            Map<Long, Long> result = new HashMap<>();

            List<LongTermsBucket> buckets = response.aggregations()
                    .get("by_store").lterms().buckets().array();
            for (LongTermsBucket bucket : buckets) {
                long storeId = bucket.key();
                long count = (long) bucket.aggregations().get("gift_count").sum().value();
                result.put(storeId, count);
            }

            return result;
        } catch (Exception e) {
            log.error("查询赠送商品数量失败, activityId={}, storeIds={}", activityId, storeIds, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 构建不分页的请求（用于导出全量数据）
     */
    private ActicitytyNjnzPageRequestVO buildExportRequest(ActicitytyNjnzPageRequestVO original) {
        ActicitytyNjnzPageRequestVO exportReq = new ActicitytyNjnzPageRequestVO();
        exportReq.setActivityId(original.getActivityId());
        exportReq.setBeforeTimeStart(original.getBeforeTimeStart());
        exportReq.setBeforeTimeEnd(original.getBeforeTimeEnd());
        exportReq.setStoreName(original.getStoreName());
        exportReq.setOrgId(original.getOrgId());
        exportReq.setPageNo(1);
        exportReq.setPageSize(10000);
        return exportReq;
    }

    /**
     * 将 ActivityNjnzStorePageVO 转换为 ActivityMzStorePageRespVO
     */
    private ActivityMzStorePageRespVO convertToMzStorePageResp(ActivityNjnzStorePageVO source) {
        ActivityMzStorePageRespVO resp = new ActivityMzStorePageRespVO();
        resp.setStoreId(Long.valueOf(source.getKey()));
        resp.setStoreName(source.getName());
        if (source.getCurrentValue() != null) {
            resp.setCustomerCount(source.getCurrentValue().getCustomerCount());
            resp.setOrderNumber(source.getCurrentValue().getOrderNumber());
            resp.setPayAmount(source.getCurrentValue().getPayAmount());
            resp.setCommodityCount(source.getCurrentValue().getCommodityCount());
            resp.setOfferAmount(source.getCurrentValue().getOfferAmount());
        }
        return resp;
    }
}
