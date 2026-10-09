package com.htyoudao.youdao.module.order.service.activity.dto;

import com.htyoudao.youdao.module.order.service.activity.calc.Activity;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllocationResult {

    private Map<ProductItem, Activity> allocation;
    private List<ActivityGroup> activityGroups;
    private Double totalDiscount;
    private Long latestActivityTime;
}