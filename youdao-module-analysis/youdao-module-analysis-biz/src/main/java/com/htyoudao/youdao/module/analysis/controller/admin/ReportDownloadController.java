package com.htyoudao.youdao.module.analysis.controller.admin;

import static com.htyoudao.youdao.module.analysis.api.enums.LogRecordConstants.ANALYSIS_REPORT_DOWNLOAD_TYPE;
import static com.htyoudao.youdao.module.analysis.api.enums.LogRecordConstants.ANALYSIS_REPORT_MARKETING_CHANNEL_DOWNLOAD_SUB_TYPE;
import static com.htyoudao.youdao.module.analysis.api.enums.LogRecordConstants.ANALYSIS_REPORT_MARKETING_CHANNEL_DOWNLOAD_SUCCESS;
import static com.htyoudao.youdao.module.analysis.api.enums.LogRecordConstants.ANALYSIS_REPORT_MARKETING_STORE_DOWNLOAD_SUB_TYPE;
import static com.htyoudao.youdao.module.analysis.api.enums.LogRecordConstants.ANALYSIS_REPORT_MARKETING_STORE_DOWNLOAD_SUCCESS;
import static com.htyoudao.youdao.module.analysis.api.enums.LogRecordConstants.ANALYSIS_REPORT_ORDER_DOWNLOAD_SUB_TYPE;
import static com.htyoudao.youdao.module.analysis.api.enums.LogRecordConstants.ANALYSIS_REPORT_ORDER_DOWNLOAD_SUCCESS;
import static com.htyoudao.youdao.module.analysis.api.enums.LogRecordConstants.ANALYSIS_REPORT_PRODUCT_ALL_DOWNLOAD_SUB_TYPE;
import static com.htyoudao.youdao.module.analysis.api.enums.LogRecordConstants.ANALYSIS_REPORT_PRODUCT_ALL_DOWNLOAD_SUCCESS;
import static com.htyoudao.youdao.module.analysis.api.enums.LogRecordConstants.ANALYSIS_REPORT_PRODUCT_DOWNLOAD_SUB_TYPE;
import static com.htyoudao.youdao.module.analysis.api.enums.LogRecordConstants.ANALYSIS_REPORT_PRODUCT_DOWNLOAD_SUCCESS;
import static com.htyoudao.youdao.module.analysis.api.enums.LogRecordConstants.ANALYSIS_REPORT_STORE_DOWNLOAD_SUB_TYPE;
import static com.htyoudao.youdao.module.analysis.api.enums.LogRecordConstants.ANALYSIS_REPORT_STORE_DOWNLOAD_SUCCESS;

import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.MarketingChannelExcelRespVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.MarketingShopExcelRespVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.ProductExcelRespVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.StoreExcelRespVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.MarketingRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ReportOrderDownloadReqVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ReportProductDownloadReqVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ReportStoreDownloadReqVO;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.service.IAggDownService;
import com.htyoudao.youdao.module.analysis.service.IMarketingAggregationService;
import com.htyoudao.youdao.module.analysis.service.ReportDownloadService;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dht
 */
@Tag(name = "报表下载")
@RestController
@RequestMapping("/analysis/report-download")
public class ReportDownloadController {

    @Resource
    private ReportDownloadService reportDownloadService;

    @Resource
    private IAggDownService aggDownService;

    @Resource
    private ExcelActionService excelActionService;

    @Resource
    private IMarketingAggregationService marketingAggregationService;


    @Resource
    private StoreApi storeApi;


    /**
     * 下载订单报表
     */
    @LogRecord(type = ANALYSIS_REPORT_DOWNLOAD_TYPE, subType = ANALYSIS_REPORT_ORDER_DOWNLOAD_SUB_TYPE,
        bizNo = "{{#requestVO.startTime}}", success = ANALYSIS_REPORT_ORDER_DOWNLOAD_SUCCESS)
    @Operation(summary = "下载订单报表")
    @PostMapping("/order-download")
    public CommonResult<Void> orderDownload(@RequestBody @Valid ReportOrderDownloadReqVO requestVO) {
        reportDownloadService.orderDownload(requestVO);
        return CommonResult.success(null);

    }

    /**
     * 下载门店报表
     */
    @LogRecord(type = ANALYSIS_REPORT_DOWNLOAD_TYPE, subType = ANALYSIS_REPORT_STORE_DOWNLOAD_SUB_TYPE,
        bizNo = "{{#requestVO.startTime}}", success = ANALYSIS_REPORT_STORE_DOWNLOAD_SUCCESS)
    @Operation(summary = "下载门店报表")
    @PostMapping("/store-download")
    public CommonResult storeDownload(@RequestBody @Valid ReportStoreDownloadReqVO requestVO) {
        String fileName = "门店下载_" + requestVO.getStartTime() + "至" + requestVO.getEndTime();
        if (CollectionUtils.isEmpty(requestVO.getStoreIds())) {
            Set<Long> storeIds = getLoginStoreIds();
            requestVO.setStoreIds(storeIds);
        }
        Set<String> header = new HashSet<>(requestVO.getFields());
        header.add("date");
        excelActionService.exportAsyncExcel(
            StoreExcelRespVO.class,
            requestVO,
            param -> aggDownService.storeDownload(requestVO),
            fileName,
            header
        );
        return CommonResult.success(true);
    }



    /**
     * 下载门店报表
     */
    @LogRecord(type = ANALYSIS_REPORT_DOWNLOAD_TYPE, subType = ANALYSIS_REPORT_MARKETING_STORE_DOWNLOAD_SUB_TYPE,
        bizNo = "{{#requestVO.currentTimeStart}}", success = ANALYSIS_REPORT_MARKETING_STORE_DOWNLOAD_SUCCESS)
    @Operation(summary = "下载营销分析-门店报表")
    @PostMapping("/marketing/store-download")
    public CommonResult storeMarketingDownload(@RequestBody @Valid MarketingRequest requestVO) {
        String fileName = "营销分析门店下载_" + requestVO.getCurrentTimeStart() + "至" + requestVO.getCurrentTimeEnd();
        if (CollectionUtils.isEmpty(requestVO.getStoreIds())) {
            Set<Long> storeIds = getLoginStoreIds();
            requestVO.setStoreIds(new ArrayList<>(storeIds));
        }
        Set<String> header = Set.of(
            "storeName","couponReceiveCount","couponUseCount","couponUseRate","customerCount",
            "orderCount","salesVolume","customerUnitPrice","activityDiscountAmount",
            "promotionDiscountAmount","allDiscountAmount","salesAmount"
        );
        requestVO.setPageSize(3000);
        
        excelActionService.exportAsyncExcel(
            MarketingShopExcelRespVO.class,
            requestVO,
            param -> BeanCopyUtils.copyBeanList(marketingAggregationService.getMarketingShopData(requestVO).getList(),MarketingShopExcelRespVO.class) ,
            fileName,
            header
        );
        return CommonResult.success(true);
    }


    /**
     * 下载门店报表
     */
    @LogRecord(type = ANALYSIS_REPORT_DOWNLOAD_TYPE, subType = ANALYSIS_REPORT_MARKETING_CHANNEL_DOWNLOAD_SUB_TYPE,
        bizNo = "{{#requestVO.currentTimeStart}}", success = ANALYSIS_REPORT_MARKETING_CHANNEL_DOWNLOAD_SUCCESS)
    @Operation(summary = "下载营销分析-渠道报表")
    @PostMapping("/marketing/channel-download")
    public CommonResult channelMarketingDownload(@RequestBody @Valid MarketingRequest requestVO) {
        String fileName = "营销分析渠道下载_" + requestVO.getCurrentTimeStart() + "至" + requestVO.getCurrentTimeEnd();
        if (CollectionUtils.isEmpty(requestVO.getStoreIds())) {
            Set<Long> storeIds = getLoginStoreIds();
            requestVO.setStoreIds(new ArrayList<>(storeIds));
        }
        Set<String> header = Set.of(
            "channelType","linkClickCount","couponReceiveCount","couponUseCount",
            "couponUseRate","customerCount","orderCount","salesVolume","customerUnitPrice",
            "activityDiscountAmount","promotionDiscountAmount","allDiscountAmount",
            "channelOrderAmount","salesAmount","channelRate"
        );
        excelActionService.exportAsyncExcel(
            MarketingChannelExcelRespVO.class,
            requestVO,
            param -> BeanCopyUtils.copyBeanList(marketingAggregationService.getMarketingChannelData(requestVO),MarketingChannelExcelRespVO.class) ,
            fileName,
            header
        );
        return CommonResult.success(true);
    }



    /**
     * 下载商品报表
     */
    @LogRecord(type = ANALYSIS_REPORT_DOWNLOAD_TYPE, subType = ANALYSIS_REPORT_PRODUCT_DOWNLOAD_SUB_TYPE,
        bizNo = "{{#requestVO.startTime}}", success = ANALYSIS_REPORT_PRODUCT_DOWNLOAD_SUCCESS)
    @Operation(summary = "下载商品报表")
    @PostMapping("/product-download")
    public CommonResult<Boolean> productDownload(@RequestBody @Valid ReportProductDownloadReqVO requestVO) {

        String fileName = "商品下载_" + requestVO.getStartTime() + "至" + requestVO.getEndTime();
        if (CollectionUtils.isEmpty(requestVO.getStoreIds())) {
            Set<Long> storeIds = getLoginStoreIds();
            requestVO.setStoreIds(storeIds);
        }
        Set<String> header = new HashSet<>(requestVO.getFields());
        header.add("date");
        excelActionService.exportAsyncExcel(
            ProductExcelRespVO.class,
            requestVO,
            param -> aggDownService.productDownload(requestVO),
            fileName,
            header
        );
        return CommonResult.success(true);
    }


    /**
     * 下载商品报表
     */
    @LogRecord(type = ANALYSIS_REPORT_DOWNLOAD_TYPE, subType = ANALYSIS_REPORT_PRODUCT_ALL_DOWNLOAD_SUB_TYPE,
        bizNo = "{{#requestVO.startTime}}", success = ANALYSIS_REPORT_PRODUCT_ALL_DOWNLOAD_SUCCESS)
    @Operation(summary = "下载商品报表All")
    @PostMapping("/product-download-all")
    public CommonResult<Boolean> productDownloadAll() {
        ReportProductDownloadReqVO requestVO = new ReportProductDownloadReqVO();
        requestVO.setType("DAY");
        requestVO.setAllStore(1);
        requestVO.setStoreIds(getLoginStoreIds());

        //日期范围  昨天
        LocalDate yesterday = LocalDate.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        requestVO.setStartTime(yesterday.format(formatter));
        requestVO.setEndTime(yesterday.format(formatter));
        //日期 门店名称 商品分组 商品名称 商品销量
        requestVO.setFields(new HashSet<>(Set.of("storeName", "categoryName", "goodsName", "allSalesVolume")));
        LogRecordContext.putVariable("requestVO", requestVO);
        productDownload(requestVO);
        return CommonResult.success(true);
    }


    private Set<Long> getLoginStoreIds() {
        Long loginUserId = SecurityFrameworkUtils.getLoginUserId();
        Set<Long> checkedData = storeApi.getAllStoreIdByUser(loginUserId).getCheckedData();
        if (checkedData == null) {
            throw new ServiceException(ErrorCodeConstants.DATA_PERMISSION_NONE);
        }
        return checkedData;
    }

}
