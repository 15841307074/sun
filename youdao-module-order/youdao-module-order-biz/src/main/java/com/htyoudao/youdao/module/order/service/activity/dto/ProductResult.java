package com.htyoudao.youdao.module.order.service.activity.dto;

import java.util.List;
import lombok.Data;

@Data
public class ProductResult {

    private String uId;

    private Long commodityId;

    private Long skuId;

    private String productName;

    private Integer quantity; // 购买数量

    private Double price; // 单价

    private Double discountAmount;

    private Long activityId;

    private String activityName;

    private Integer activityType;

    private List<Integer> stackableActivities;

}
