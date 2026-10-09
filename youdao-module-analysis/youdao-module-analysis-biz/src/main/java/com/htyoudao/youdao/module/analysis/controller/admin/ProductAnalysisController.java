package com.htyoudao.youdao.module.analysis.controller.admin;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.excel.core.service.ExcelActionService;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.excel.ProductPageDownloadExcelVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductSaleQueryReq;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductSaleStatVO;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.product.ProductResult;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.enums.ProductMetricsConfigNew;
import com.htyoudao.youdao.module.analysis.service.IChProductAggregationService;
import com.htyoudao.youdao.module.analysis.service.IProductAggerationService;
import com.htyoudao.youdao.module.analysis.service.ProductSaleAnalysisService;
import com.htyoudao.youdao.module.system.api.store.StoreApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商品")
@RestController
@RequestMapping("/analysis/product")
@RefreshScope
@PermitAll
public class ProductAnalysisController {

    @Value("${clickhouseTag}")
    private String clickhouseTag;

    private static final String defaultCHTag = "1";

    private static final Set<String> EXCLUDED_DOWNLOAD_METRICS = Set.of(
        ProductMetricsConfigNew.COMMODITY_ID.getCode(),
        ProductMetricsConfigNew.CATEGORY_NAME.getCode(),
        ProductMetricsConfigNew.GOODS_IMAGE.getCode(),
        ProductMetricsConfigNew.IS_SINGLE.getCode()
    );

    @Resource
    private IProductAggerationService aggregationService;

    @Resource
    private IChProductAggregationService chProductAggregationService;

    @Resource
    private ExcelActionService excelActionService;

    @Resource
    private StoreApi storeApi;

    @Operation(summary = "商品分页")
    @PostMapping("/page")
    public CommonResult<PageResult<ProductResult>> productPage(@RequestBody @Valid ProductPageRequest requestVO) {
        if (defaultCHTag.equals(clickhouseTag)){
            return productPageNew(requestVO);
        }else {
            normalizeRequestMetrics(requestVO);
            resolveRequestStoreIds(requestVO);
            return CommonResult.success(aggregationService.realTimeProductPage(requestVO));
        }
    }

    @Operation(summary = "商品分页(ClickHouse)")
    @PostMapping("/page/new")
    public CommonResult<PageResult<ProductResult>> productPageNew(@RequestBody @Valid ProductPageRequest requestVO) {
        validateTimeRange(requestVO);
        normalizeRequestMetrics(requestVO);
        resolveRequestStoreIds(requestVO);
        return CommonResult.success(chProductAggregationService.queryProductPageFromCh(requestVO));
    }

    private void validateTimeRange(ProductPageRequest requestVO) {
        if (requestVO.getCurrentTimeStart() != null && requestVO.getCurrentTimeEnd() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(
                requestVO.getCurrentTimeStart().toLocalDate(),
                requestVO.getCurrentTimeEnd().toLocalDate()
            );
            if (days > 90) {
                throw exception(ErrorCodeConstants.TIME_RANGE_EXCEED_LIMIT);
            }
        }
    }

    @Resource
    private ProductSaleAnalysisService productSaleAnalysisService;

    @PostMapping("/rank")
    public CommonResult<List<ProductSaleStatVO>> getSaleRank(@Valid @RequestBody ProductSaleQueryReq req) {
        // 设置默认订单状态
        if (CollectionUtils.isEmpty(req.getOrderStates())) {
            req.setOrderStates(Arrays.asList("20","30","40","50","60","80","200"));
        }
        List<ProductSaleStatVO> result = productSaleAnalysisService.querySingleProductSaleRank(req);
        return CommonResult.success(result);
    }


    @Operation(summary = "商品游标分页", description = "使用游标分页查询商品数据，适用于大数据量和深度分页场景，无10000条限制")
    @PostMapping("/page-cursor")
    public CommonResult<PageResult<ProductResult>> productPageByCursor(@RequestBody @Valid ProductPageRequest requestVO) {
        normalizeRequestMetrics(requestVO);
        resolveRequestStoreIds(requestVO);
        return CommonResult.success(aggregationService.getProductPageByCursor(requestVO));
    }

    @Operation(summary = "经营分析-商品明细下载")
    @PostMapping("/page/download")
    public CommonResult<Boolean> productPageDownload(@RequestBody @Valid ProductPageRequest requestVO) {
        normalizeRequestMetrics(requestVO);
        resolveRequestStoreIds(requestVO);
        Set<String> header = buildDownloadHeaders(requestVO);

        excelActionService.exportAsyncExcel(
            ProductPageDownloadExcelVO.class,
            requestVO,
            param -> {
                List<ProductPageDownloadExcelVO> data = aggregationService.productPageDownload(requestVO);
                if (CollectionUtils.isEmpty(data)) {
                    throw exception(ErrorCodeConstants.DATA_NULL);
                }
                return data;
            },
            buildFileName(requestVO),
            header
        );
        return CommonResult.success(true);
    }

    @Operation(summary = "经营分析-商品明细下载(ClickHouse)")
    @PostMapping("/page/download/new")
    public CommonResult<Boolean> productPageDownloadNew(@RequestBody @Valid ProductPageRequest requestVO) {
        validateTimeRange(requestVO);
        normalizeRequestMetrics(requestVO);
        resolveRequestStoreIds(requestVO);
        Set<String> header = buildDownloadHeaders(requestVO);

        excelActionService.exportAsyncExcel(
            ProductPageDownloadExcelVO.class,
            requestVO,
            param -> {
                List<ProductPageDownloadExcelVO> data = chProductAggregationService.productPageDownloadFromCh(requestVO);
                if (CollectionUtils.isEmpty(data)) {
                    throw exception(ErrorCodeConstants.DATA_NULL);
                }
                return data;
            },
            buildFileName(requestVO),
            header
        );
        return CommonResult.success(true);
    }

    private void normalizeRequestMetrics(ProductPageRequest requestVO) {
        if (Integer.valueOf(3).equals(requestVO.getIsSingle())) {
            requestVO.setMetrics(new LinkedHashSet<>(List.of(
                ProductMetricsConfigNew.COMMODITY_ID.getCode(),
                ProductMetricsConfigNew.GOODS_IMAGE.getCode(),
                ProductMetricsConfigNew.GOODS_NAME.getCode(),
                ProductMetricsConfigNew.IS_PURCHASE.getCode(),
                ProductMetricsConfigNew.ALL_SALES_VOLUME.getCode(),
                ProductMetricsConfigNew.SALES_AMOUNT.getCode()
            )));
            return;
        }

        if (CollectionUtils.isEmpty(requestVO.getMetrics())) {
            List<ProductMetricsConfigNew> baseMetrics = List.of(
                ProductMetricsConfigNew.COMMODITY_ID,
                ProductMetricsConfigNew.GOODS_IMAGE,
                ProductMetricsConfigNew.GOODS_NAME,
                ProductMetricsConfigNew.IS_SINGLE,
                ProductMetricsConfigNew.SINGLE_SALES_VOLUME,
                ProductMetricsConfigNew.ALL_SALES_VOLUME,
                ProductMetricsConfigNew.SALES_AMOUNT,
                ProductMetricsConfigNew.SALES_VOLUME
            );
            requestVO.setMetrics(baseMetrics.stream().map(ProductMetricsConfigNew::getCode).collect(Collectors.toSet()));
        }

        if (requestVO.getMetrics().contains("repurchaseRate")) {
            requestVO.getMetrics().add("customerCount");
            requestVO.getMetrics().add("repurchaseUserCount");
        }
        if (requestVO.getMetrics().contains("newCustomerRate")) {
            requestVO.getMetrics().add("customerCount");
            requestVO.getMetrics().add("newCustomerCount");
        }
    }

    private Set<String> buildDownloadHeaders(ProductPageRequest requestVO) {
        Set<String> headers = new LinkedHashSet<>();
        headers.add("goodsName");
        headers.add("storeName");
        headers.add("time");
        if (Integer.valueOf(3).equals(requestVO.getIsSingle())) {
            headers.add("allSalesVolume");
            headers.add("salesAmount");
            return headers;
        }
        if (CollectionUtils.isEmpty(requestVO.getMetrics())) {
            headers.add("salesVolume");
            headers.add("singleSalesVolume");
            headers.add("allSalesVolume");
            headers.add("salesAmount");
            return headers;
        }
        requestVO.getMetrics().stream()
            .filter(metric -> !EXCLUDED_DOWNLOAD_METRICS.contains(metric))
            .forEach(headers::add);
        return headers;
    }

    private String buildFileName(ProductPageRequest requestVO) {
        return "经营分析商品明细_" + requestVO.getCurrentTimeStart().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private void resolveRequestStoreIds(ProductPageRequest requestVO) {
        if (!CollectionUtils.isEmpty(requestVO.getStoreIds())) {
            return;
        }
        Set<Long> storeIds = storeApi.getAllStoreIdByUser(SecurityFrameworkUtils.getLoginUserId()).getCheckedData();
        if (CollectionUtils.isEmpty(storeIds)) {
            throw exception(ErrorCodeConstants.DATA_PERMISSION_IS_EMPTY);
        }
        requestVO.setStoreIds(storeIds.stream().toList());
    }
}
