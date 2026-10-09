package com.htyoudao.youdao.module.analysis.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.NumberUtil;
import com.fasterxml.jackson.datatype.jsr310.DecimalUtils;
import com.htyoudao.youdao.framework.common.core.KeyValue;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.security.core.LoginUser;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.ProductExcelRespVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.StoreExcelRespVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.TimeRangeRow;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ReportProductDownloadReqVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ReportStoreDownloadReqVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.product.ProductResult;
import com.htyoudao.youdao.module.analysis.enums.DownloadModeEnum;
import com.htyoudao.youdao.module.analysis.enums.MetricsConfig;
import com.htyoudao.youdao.module.analysis.service.IAggDownService;
import com.htyoudao.youdao.module.analysis.service.IEsAggregationService;
import com.htyoudao.youdao.module.analysis.service.IProductAggerationService;
import com.htyoudao.youdao.module.analysis.service.dto.EsAggDTO;
import com.htyoudao.youdao.module.system.api.org.OrgApi;
import com.htyoudao.youdao.module.system.api.org.dto.StoreOrgDTO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AggDownloadServiceImpl implements IAggDownService {

    @Autowired
    private IEsAggregationService esAggregationService;

    @Autowired
    private IProductAggerationService productAggerationService;

    @DubboReference
    private OrgApi orgApi;



    @Override
    public List<StoreExcelRespVO> storeDownload(ReportStoreDownloadReqVO requestVO) {
        List<TimeRangeRow> timeRangeRows = formatByRange(
            requestVO.getStartTime(),
            requestVO.getEndTime(),
            DownloadModeEnum.valueOf(requestVO.getType())
        );


        List<MetricsConfig> metrics = new ArrayList<>(requestVO.getFields()
            .stream().distinct()
            .map(MetricsConfig::getEnumByCode)
            .filter(Objects::nonNull)
            .toList());


        if (requestVO.getFields().contains("repeatBuyersRate")){
            metrics.add(MetricsConfig.MINI_CUSTOMER_COUNT);
            metrics.add(MetricsConfig.MINI_REPEAT_BUYERS);
        }

        Map<Long, StoreOrgDTO> collect = orgApi.getOrgListByStoreId(requestVO.getStoreIds().stream().toList())
            .getCheckedData()
            .stream()
            .collect(Collectors.toMap(StoreOrgDTO::getStoreId, Function.identity()));


        List<StoreExcelRespVO> respVOS = new ArrayList<>();
        for (TimeRangeRow row : timeRangeRows) {
            for (Long storeId : requestVO.getStoreIds()) {
                EsAggDTO esAggDTO = new EsAggDTO();
                esAggDTO.setStoreIds(List.of(storeId));
                esAggDTO.setTimes(new LocalDateTime[]{row.getStartDate(), row.getEndDate()});
                log.info("store{}: store export;time={}", storeId, esAggDTO.getTimes());

                Map<String, Double> result = esAggregationService.batchAggregateMetrics(esAggDTO, metrics);

                StoreExcelRespVO vo = BeanUtil.toBean(result, StoreExcelRespVO.class);
                vo.setStoreId(storeId);
                vo.setDate(row.getDate());
                vo.setStartDate(row.getStartDate());
                vo.setEndDate(row.getEndDate());

                StoreOrgDTO storeOrgDTO = collect.get(vo.getStoreId());
                BeanUtil.copyProperties(storeOrgDTO, vo);

                if (vo.getCustomerCount() != null && vo.getCustomerCount() != 0){
                    vo.setRepeatBuyersRate(
                        NumberUtil.div(vo.getRepeatBuyers(), vo.getCustomerCount())
                        .doubleValue()
                    );
                }
                if (StringUtils.isNotEmpty(vo.getStoreHours())){
                    vo.setStoreHours(vo.getStoreHours().replace(",",";"));
                }
                respVOS.add(vo);
            }
        }

        return respVOS;
    }

    @Override
    public List<ProductExcelRespVO> productDownload(ReportProductDownloadReqVO requestVO) {
        List<Long> sanitizedStoreIds = requestVO.getStoreIds() == null ? List.of() : requestVO.getStoreIds().stream()
            .filter(Objects::nonNull)
            .toList();
        List<Long> sanitizedCommodityIds = requestVO.getCommodityIds() == null ? null : requestVO.getCommodityIds().stream()
            .filter(Objects::nonNull)
            .toList();

        List<TimeRangeRow> timeRangeRows = formatByRange(
            requestVO.getStartTime(),
            requestVO.getEndTime(),
            DownloadModeEnum.valueOf(requestVO.getType())
        );


        Map<Long, StoreOrgDTO> collect = orgApi.getOrgListByStoreId(sanitizedStoreIds)
            .getCheckedData()
            .stream()
            .collect(Collectors.toMap(StoreOrgDTO::getStoreId, Function.identity()));


        requestVO.getFields().add("commodityId");
        if (requestVO.getFields().contains("repurchaseRate")){
            requestVO.getFields().add("customerCount");
            requestVO.getFields().add("repurchaseUserCount");
        }
        if (requestVO.getFields().contains("newCustomerRate")) {
            requestVO.getFields().add("customerCount");
            requestVO.getFields().add("newCustomerCount");
        }


        List<ProductExcelRespVO> respVOS = new ArrayList<>();
        for (TimeRangeRow row : timeRangeRows) {
            ProductPageRequest productPageRequest = getProductPageRequest(requestVO, row, sanitizedStoreIds, sanitizedCommodityIds);
            log.info("product export; storeCount={}, time={}-{}", sanitizedStoreIds.size(), row.getStartDate(),
                row.getEndDate());
            Map<Long, List<ProductResult>> storeProductMap = productAggerationService.getProductDownloadDataByStore(
                productPageRequest);

            for (Long storeId : sanitizedStoreIds) {
                List<ProductResult> productResults = storeProductMap.getOrDefault(storeId, List.of());
                for (ProductResult productResult : productResults) {
                    ProductExcelRespVO vo = new ProductExcelRespVO();
                    BeanUtil.copyProperties(productResult, vo);
                    boolean purchaseProduct = Objects.equals(productResult.getIsPurchase(), 1);
                    vo.setIsPurchase(purchaseProduct ? "是" : "否");
                    vo.setPurchaseSalesVolume(purchaseProduct ? productResult.getAllSalesVolume() : 0);
                    vo.setPurchaseSalesAmount(purchaseProduct ? productResult.getSalesAmount() : 0D);
                    vo.setDate(row.getDate());
                    vo.setStoreId(storeId);
                    StoreOrgDTO storeOrgDTO = collect.get(storeId);
                    BeanUtil.copyProperties(storeOrgDTO, vo);


                    if (vo.getCustomerCount() != null && vo.getCustomerCount() != 0){
                        BigDecimal div = NumberUtil.div(vo.getRepurchaseUserCount(), vo.getCustomerCount());
                        vo.setRepurchaseRate(div.doubleValue());
                    }

                    if (vo.getNewCustomerCount() != null && vo.getCustomerCount() != null){
                        BigDecimal div = NumberUtil.div(vo.getNewCustomerCount(), vo.getCustomerCount());
                        vo.setNewCustomerRate(div.doubleValue());
                    }

                    respVOS.add(vo);
                }
            }
        }

        return  respVOS;
    }

    private static ProductPageRequest getProductPageRequest(ReportProductDownloadReqVO requestVO, TimeRangeRow row,
        List<Long> storeIds, List<Long> commodityIds) {
        ProductPageRequest productPageRequest = new ProductPageRequest();
        productPageRequest.setMetrics(new HashSet<>(requestVO.getFields()));
        productPageRequest.setCommodityIds(commodityIds);
        productPageRequest.setCurrentTimeStart(row.getStartDate());
        productPageRequest.setCurrentTimeEnd(row.getEndDate());
        productPageRequest.setStoreIds(new ArrayList<>(storeIds));
        productPageRequest.setRealTime(true);
        return productPageRequest;
    }


    public List<TimeRangeRow> formatByRange(String startInput, String endInput, DownloadModeEnum mode) {

        final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

        List<TimeRangeRow> result = new ArrayList<>();

        switch (mode) {
            case DAY: {
                LocalDate start = LocalDate.parse(startInput);
                LocalDate end = LocalDate.parse(endInput);
                while (!start.isAfter(end)) {
                    result.add(
                        new TimeRangeRow(
                        start.format(DATE),
                        start.atStartOfDay(),
                        start.atTime(23, 59, 59))
                    );
                    start = start.plusDays(1);
                }
                break;
            }
            case MONTH: {
                YearMonth startMonth = YearMonth.parse(startInput);
                YearMonth endMonth = YearMonth.parse(endInput);
                while (!startMonth.isAfter(endMonth)) {
                    LocalDate firstDay = startMonth.atDay(1);
                    LocalDate lastDay = startMonth.atEndOfMonth();
                    result.add(new TimeRangeRow(
                        startMonth.format(MONTH),
                        firstDay.atStartOfDay(),
                        lastDay.atTime(23, 59, 59)));
                    startMonth = startMonth.plusMonths(1);
                }
                break;
            }
            case SUMMARY: {
                LocalDate start = LocalDate.parse(startInput);
                LocalDate end = LocalDate.parse(endInput);
                result.add(new TimeRangeRow(
                    start.format(DATE) + "至" + end.format(DATE),
                    start.atStartOfDay(),
                    end.atTime(23, 59, 59)));
                break;
            }
        }

        return result;
    }

}
