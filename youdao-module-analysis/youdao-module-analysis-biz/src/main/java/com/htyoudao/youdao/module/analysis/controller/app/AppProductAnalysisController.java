package com.htyoudao.youdao.module.analysis.controller.app;


import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.ProductPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.product.ProductResult;
import com.htyoudao.youdao.module.analysis.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.analysis.enums.ProductMetricsConfigNew;
import com.htyoudao.youdao.module.analysis.service.IProductAggerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DC - 商品")
@RestController
@RequestMapping("/analysis/app/product")
public class AppProductAnalysisController {

    @Resource
    private IProductAggerationService aggregationService;


    @Operation(summary = "商品分页")
    @PostMapping("/page")
    @PermitAll
    public CommonResult<PageResult<ProductResult>> appProductPage(@RequestBody @Valid ProductPageRequest requestVO) {
        if (CollectionUtils.isEmpty(requestVO.getStoreIds())){
            throw exception(ErrorCodeConstants.STORE_IS_EMPTY);
        }

        if (CollectionUtils.isEmpty(requestVO.getMetrics())){
            List<ProductMetricsConfigNew> baseMetrics = List.of(ProductMetricsConfigNew.COMMODITY_ID,
            ProductMetricsConfigNew.GOODS_IMAGE, ProductMetricsConfigNew.GOODS_NAME, ProductMetricsConfigNew.IS_SINGLE,
            ProductMetricsConfigNew.SINGLE_SALES_VOLUME,ProductMetricsConfigNew.ALL_SALES_VOLUME ,// 包含套餐销量
            ProductMetricsConfigNew.SALES_AMOUNT, ProductMetricsConfigNew.SALES_VOLUME);
            requestVO.setMetrics(baseMetrics.stream().map(ProductMetricsConfigNew::getCode).collect(Collectors.toSet()));
        }

        return CommonResult.success(aggregationService.realTimeProductPage(requestVO));
    }



}
