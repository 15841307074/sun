package com.htyoudao.youdao.module.order.service.activity.calc;

import com.htyoudao.youdao.module.order.service.activity.dto.ActivityGroup;
import com.htyoudao.youdao.module.order.service.activity.dto.AllocationResult;
import com.htyoudao.youdao.module.order.service.activity.dto.ProductItem;
import com.htyoudao.youdao.module.order.service.activity.dto.ProductResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ActivityAllocationService {
    /**
     * 按活动进行分组
     * 第N件打N折活动不需要聚合，每个商品单独成组
     */
     List<ActivityGroup> groupByActivity(Map<ProductItem, Activity> allocation);

    /**
     * 生成所有可能的分配方案
     * @param products
     * @return
     */
    List<Map<ProductItem, Activity>> generateAllocations(List<ProductItem> products);


    /**
     * 计算单个分配方案的总优惠金额
     */
    AllocationResult calculateDiscount(Map<ProductItem, Activity> allocation);


    /**
     * 计算分摊金额
     */
    List<ProductResult> calculateAllocation(AllocationResult result);

    /**
     * 寻找最优分配方案
     */
    Optional<AllocationResult> findOptimalAllocation(List<ProductItem> products);
}
