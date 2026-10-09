package com.htyoudao.youdao.module.order.controller.admin.analysis;


import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.analysis.ProductSonRequest;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.analysis.ProductSonResult;
import com.htyoudao.youdao.module.order.service.order.BzOrderProductSonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理后台 - 数据分析", description = "数据分析")
@RestController
@RequestMapping("/order/analysis")
public class AnalysisController {

    @Resource
    private BzOrderProductSonService productSonService;

    @Operation(summary = "套餐子品信息")
    @PostMapping("/product/son/list")
    public CommonResult<List<ProductSonResult>> productSonList(@RequestBody @Valid ProductSonRequest singleRequest) {
        return CommonResult.success(productSonService.productSonList(singleRequest));
    }
}
