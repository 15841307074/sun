package com.htyoudao.youdao.module.system.api.complaint;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Tag(name = "RPC 服务 - 组织")
public interface ComplaintApi {


    @Operation(summary = "根获取三天内订单")
     CommonResult<List<String>> getComplaintListByMemberId(@RequestParam("memberId") Long memberId) ;

    @Operation(summary = "投诉数据统计")
    CommonResult<Map<Integer, Map<String, Object>>> getComplaintAggByStore(@RequestParam("storeId") Long storeId) ;


}
