package com.htyoudao.youdao.module.analysis.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.analysis.service.IComplantAggregationService;
import com.htyoudao.youdao.module.system.api.complaint.ComplaintApi;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ComplantAggregationServiceImpl implements IComplantAggregationService {

    @DubboReference
    private ComplaintApi compleplantApi;

    @Override
    public Map<Integer, Map<String, Object>> query(Long storeId) {
        if (storeId == null) {
            return Collections.emptyMap();
        }
        Map<Integer, Map<String, Object>> orderSnList = new HashMap<>();
        CommonResult<Map<Integer, Map<String, Object>>> dubboResult = compleplantApi.getComplaintAggByStore(storeId);
        if (dubboResult == null) {
            return Collections.emptyMap();
        }
        if (!dubboResult.isSuccess()) {
            return Collections.emptyMap();
        }
        orderSnList = dubboResult.getData() == null ? Collections.emptyMap() : dubboResult.getData();

        return orderSnList;
    }
}