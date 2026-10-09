package com.htyoudao.youdao.module.system.service.complant;

import java.util.List;
import java.util.Map;

public interface ComplaintAppService {
    /**
     * 查询三天内订单
     */
    List<String> getComplaintListByMemberId(Long memberId);

    Map<Integer, Map<String, Object>> getComplaintAggByStore(Long storeId);
}
