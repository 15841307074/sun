package com.htyoudao.youdao.module.analysis.api.inventory;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.analysis.api.inventory.DTO.AggregationPageRequest;
import com.htyoudao.youdao.module.analysis.api.inventory.DTO.InventoryAggregationRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

public interface InventoryApi {



    public CommonResult<BigDecimal> storePage(@RequestBody @Valid InventoryAggregationRequest requestVO);
}
