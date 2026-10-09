package com.htyoudao.youdao.module.analysis.controller.admin;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.analysis.api.inventory.DTO.InventoryAggregationRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.req.AggregationPageRequest;
import com.htyoudao.youdao.module.analysis.controller.admin.vo.res.store.AnalysisStorePageVO;
import com.htyoudao.youdao.module.analysis.service.IStoreAggService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Tag(name = "进销存-订单金额")
@RestController
@RequestMapping("/analysis/inventory")
public class InventoryManagementController {

    @Resource
    private IStoreAggService aggregationService;
    @Operation(summary = "获取门店下的收入总额")
    @PostMapping("/storeTotalAmount")
    public CommonResult<BigDecimal> storeTotalAmount(InventoryAggregationRequest aggregationRequest) {
        BigDecimal storeTotalAmount= aggregationService.storeTotalAmount(aggregationRequest);

        return CommonResult.success(storeTotalAmount);
    }

}
