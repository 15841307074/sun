package com.htyoudao.youdao.module.analysis.controller.app;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.analysis.service.IComplantAggregationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author dht
 */

@Tag(name = "顾客")
@RestController
@RequestMapping("/analysis/complaint")
public class AppComplaintAnalysisController {

    @Resource
    private IComplantAggregationService iComplantAggregationService;

     @Operation(summary = "投诉统计")
    @GetMapping("/view")
    public CommonResult<Map<Integer, Map<String, Object>>> memberView(@RequestParam("storeId") Long storeId) {
        return CommonResult.success(iComplantAggregationService.query(storeId));
    }


}
