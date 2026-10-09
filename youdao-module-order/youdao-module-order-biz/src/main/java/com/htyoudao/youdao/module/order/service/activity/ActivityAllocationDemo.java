package com.htyoudao.youdao.module.order.service.activity;

import com.alibaba.fastjson.JSON;
import com.htyoudao.youdao.module.order.service.activity.calc.Activity;
import com.htyoudao.youdao.module.order.service.activity.calc.impl.ActivityAllocationServiceImpl;
import com.htyoudao.youdao.module.order.service.activity.dto.AllocationResult;
import com.htyoudao.youdao.module.order.service.activity.dto.MjmzActivity;
import com.htyoudao.youdao.module.order.service.activity.dto.NjnzActivity;
import com.htyoudao.youdao.module.order.service.activity.dto.ProductItem;
import com.htyoudao.youdao.module.order.service.activity.dto.ProductResult;
import com.htyoudao.youdao.module.order.service.activity.enums.ActivityType;
import com.htyoudao.youdao.module.order.service.activity.enums.DiscountOffer;
import com.htyoudao.youdao.module.order.service.activity.enums.DiscountRules;
import com.htyoudao.youdao.module.order.service.activity.enums.DiscountType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActivityAllocationDemo {

    public static void main(String[] args) {
        // 1. 准备测试数据
        List<ProductItem> products = prepareTestData();
        String s1= "[{\n"
            + "  \"UId\": \"51ddda08-d885-4e87-9570-e2f85a4438af\",\n"
            + "  \"commodityId\": 1980103795616198657,\n"
            + "  \"mjmzActivities\": [{\n"
            + "    \"activityName\": \"满10打9.5\",\n"
            + "    \"activityRemark\": \"\",\n"
            + "    \"activityType\": 5,\n"
            + "    \"allowGroup\": true,\n"
            + "    \"discountOffer\": 2,\n"
            + "    \"discountRules\": 1,\n"
            + "    \"discountSettingMap\": {\n"
            + "      10.0: 9.5\n"
            + "    },\n"
            + "    \"discountType\": 1,\n"
            + "    \"id\": 2014147321521029122,\n"
            + "    \"stackableActivities\": []\n"
            + "  }],\n"
            + "  \"njnzActivities\": [],\n"
            + "  \"price\": 0.3,\n"
            + "  \"productName\": \"纸袋包装打包\",\n"
            + "  \"quantity\": 7,\n"
            + "  \"skuId\": 2003333499173183491\n"
            + "}, {\n"
            + "  \"UId\": \"bf083539-6965-431e-8044-4a546751ca0c\",\n"
            + "  \"commodityId\": 1946489273219506178,\n"
            + "  \"mjmzActivities\": [{\n"
            + "    \"activityName\": \"满10打9.5\",\n"
            + "    \"activityRemark\": \"\",\n"
            + "    \"activityType\": 5,\n"
            + "    \"allowGroup\": true,\n"
            + "    \"discountOffer\": 2,\n"
            + "    \"discountRules\": 1,\n"
            + "    \"discountSettingMap\": {\n"
            + "      10.0: 9.5\n"
            + "    },\n"
            + "    \"discountType\": 1,\n"
            + "    \"id\": 2014147321521029122,\n"
            + "    \"stackableActivities\": []\n"
            + "  }],\n"
            + "  \"njnzActivities\": [],\n"
            + "  \"price\": 7.0,\n"
            + "  \"productName\": \"【初遇之礼】首页领券，新人1元吃汉堡\",\n"
            + "  \"quantity\": 2,\n"
            + "  \"skuId\": 2003333480319787009\n"
            + "}, {\n"
            + "  \"UId\": \"cad91864-36aa-4a59-975f-e3361fe415da\",\n"
            + "  \"commodityId\": 1998221573048950785,\n"
            + "  \"mjmzActivities\": [{\n"
            + "    \"activityName\": \"满10打9.5\",\n"
            + "    \"activityRemark\": \"\",\n"
            + "    \"activityType\": 5,\n"
            + "    \"allowGroup\": true,\n"
            + "    \"discountOffer\": 2,\n"
            + "    \"discountRules\": 1,\n"
            + "    \"discountSettingMap\": {\n"
            + "      10.0: 9.5\n"
            + "    },\n"
            + "    \"discountType\": 1,\n"
            + "    \"id\": 2014147321521029122,\n"
            + "    \"stackableActivities\": []\n"
            + "  }],\n"
            + "  \"njnzActivities\": [],\n"
            + "  \"price\": 14.0,\n"
            + "  \"productName\": \"智选套餐E\",\n"
            + "  \"quantity\": 1,\n"
            + "  \"skuId\": 2003333459327295489\n"
            + "}, {\n"
            + "  \"UId\": \"4ee91948-acc5-4a0e-b407-9cdf21095d69\",\n"
            + "  \"commodityId\": 1993867310931804162,\n"
            + "  \"mjmzActivities\": [{\n"
            + "    \"activityName\": \"满10打9.5\",\n"
            + "    \"activityRemark\": \"\",\n"
            + "    \"activityType\": 5,\n"
            + "    \"allowGroup\": true,\n"
            + "    \"discountOffer\": 2,\n"
            + "    \"discountRules\": 1,\n"
            + "    \"discountSettingMap\": {\n"
            + "      10.0: 9.5\n"
            + "    },\n"
            + "    \"discountType\": 1,\n"
            + "    \"id\": 2014147321521029122,\n"
            + "    \"stackableActivities\": []\n"
            + "  }, {\n"
            + "    \"activityName\": \"满折多钱\",\n"
            + "    \"activityRemark\": \"\",\n"
            + "    \"activityType\": 5,\n"
            + "    \"allowGroup\": true,\n"
            + "    \"discountOffer\": 2,\n"
            + "    \"discountRules\": 1,\n"
            + "    \"discountSettingMap\": {\n"
            + "      3.0: 3.0\n"
            + "    },\n"
            + "    \"discountType\": 2,\n"
            + "    \"id\": 2011641124226117633,\n"
            + "    \"stackableActivities\": []\n"
            + "  }],\n"
            + "  \"njnzActivities\": [],\n"
            + "  \"price\": 12.6,\n"
            + "  \"productName\": \"宠粉大狂欢B\",\n"
            + "  \"quantity\": 1,\n"
            + "  \"skuId\": 2003333500532137986\n"
            + "}]";



        String s2 = "[{\n"
            + "  \"UId\": \"2f8f9915-8368-4147-a6e6-5a7617aaf3d4\",\n"
            + "  \"commodityId\": 1980103795616198657,\n"
            + "  \"mjmzActivities\": [{\n"
            + "    \"activityName\": \"满10打9.5\",\n"
            + "    \"activityRemark\": \"\",\n"
            + "    \"activityType\": 5,\n"
            + "    \"allowGroup\": true,\n"
            + "    \"discountOffer\": 2,\n"
            + "    \"discountRules\": 1,\n"
            + "    \"discountSettingMap\": {\n"
            + "      10.0: 9.5\n"
            + "    },\n"
            + "    \"discountType\": 1,\n"
            + "    \"id\": 2014147321521029122,\n"
            + "    \"stackableActivities\": []\n"
            + "  }],\n"
            + "  \"njnzActivities\": [],\n"
            + "  \"price\": 0.3,\n"
            + "  \"productName\": \"纸袋包装打包\",\n"
            + "  \"quantity\": 7,\n"
            + "  \"skuId\": 2003333499173183491\n"
            + "}, {\n"
            + "  \"UId\": \"a686e392-574e-4f90-b9b6-0741dd5cef9f\",\n"
            + "  \"commodityId\": 1946489273219506178,\n"
            + "  \"mjmzActivities\": [{\n"
            + "    \"activityName\": \"满10打9.5\",\n"
            + "    \"activityRemark\": \"\",\n"
            + "    \"activityType\": 5,\n"
            + "    \"allowGroup\": true,\n"
            + "    \"discountOffer\": 2,\n"
            + "    \"discountRules\": 1,\n"
            + "    \"discountSettingMap\": {\n"
            + "      10.0: 9.5\n"
            + "    },\n"
            + "    \"discountType\": 1,\n"
            + "    \"id\": 2014147321521029122,\n"
            + "    \"stackableActivities\": []\n"
            + "  }],\n"
            + "  \"njnzActivities\": [],\n"
            + "  \"price\": 7.0,\n"
            + "  \"productName\": \"【初遇之礼】首页领券，新人1元吃汉堡\",\n"
            + "  \"quantity\": 2,\n"
            + "  \"skuId\": 2003333480319787009\n"
            + "}, {\n"
            + "  \"UId\": \"ce8aa40a-177f-488b-a916-2ca06006213d\",\n"
            + "  \"commodityId\": 1998221573048950785,\n"
            + "  \"mjmzActivities\": [{\n"
            + "    \"activityName\": \"满10打9.5\",\n"
            + "    \"activityRemark\": \"\",\n"
            + "    \"activityType\": 5,\n"
            + "    \"allowGroup\": true,\n"
            + "    \"discountOffer\": 2,\n"
            + "    \"discountRules\": 1,\n"
            + "    \"discountSettingMap\": {\n"
            + "      10.0: 9.5\n"
            + "    },\n"
            + "    \"discountType\": 1,\n"
            + "    \"id\": 2014147321521029122,\n"
            + "    \"stackableActivities\": []\n"
            + "  }],\n"
            + "  \"njnzActivities\": [],\n"
            + "  \"price\": 14.0,\n"
            + "  \"productName\": \"智选套餐E\",\n"
            + "  \"quantity\": 1,\n"
            + "  \"skuId\": 2003333459327295489\n"
            + "}, {\n"
            + "  \"UId\": \"b4bacd0f-1ea4-4c59-bf82-6ff21d1b2f7a\",\n"
            + "  \"commodityId\": 1993867310931804162,\n"
            + "  \"mjmzActivities\": [{\n"
            + "    \"activityName\": \"满10打9.5\",\n"
            + "    \"activityRemark\": \"\",\n"
            + "    \"activityType\": 5,\n"
            + "    \"allowGroup\": true,\n"
            + "    \"discountOffer\": 2,\n"
            + "    \"discountRules\": 1,\n"
            + "    \"discountSettingMap\": {\n"
            + "      10.0: 9.5\n"
            + "    },\n"
            + "    \"discountType\": 1,\n"
            + "    \"id\": 2014147321521029122,\n"
            + "    \"stackableActivities\": []\n"
            + "  }, {\n"
            + "    \"activityName\": \"满折多钱\",\n"
            + "    \"activityRemark\": \"\",\n"
            + "    \"activityType\": 5,\n"
            + "    \"allowGroup\": true,\n"
            + "    \"discountOffer\": 2,\n"
            + "    \"discountRules\": 1,\n"
            + "    \"discountSettingMap\": {\n"
            + "      3.0: 3.0\n"
            + "    },\n"
            + "    \"discountType\": 2,\n"
            + "    \"id\": 2011641124226117633,\n"
            + "    \"stackableActivities\": []\n"
            + "  }],\n"
            + "  \"njnzActivities\": [],\n"
            + "  \"price\": 12.6,\n"
            + "  \"productName\": \"宠粉大狂欢B\",\n"
            + "  \"quantity\": 1,\n"
            + "  \"skuId\": 2003333500532137986\n"
            + "}]";

        List<ProductItem> products1 = JSON.parseArray(s1, ProductItem.class);

        List<ProductItem> products2 = JSON.parseArray(s2, ProductItem.class);


        products  = products1;
        products.addAll(products2);
        products.addAll(products2);
        products.addAll(products2);
        products.addAll(products2);

        products.addAll(products2);
        products.addAll(products2);
        products.addAll(products2);
        products.addAll(products2);   products.addAll(products2);
        products.addAll(products2);
        products.addAll(products2);
        products.addAll(products2);   products.addAll(products2);
        products.addAll(products2);
        products.addAll(products2);
        products.addAll(products2);



        // 2. 创建服务
        ActivityAllocationServiceImpl service = new ActivityAllocationServiceImpl();

        // 3. 查找最优方案
        Optional<AllocationResult> optimalResult = service.findOptimalAllocation(products);

        // 4. 处理结果
        AllocationResult result = optimalResult.get();

        log.info("最优方案总优惠金额: "  + result.getTotalDiscount());
        log.info("分配详情:");

        for (Map.Entry<ProductItem, Activity> entry : result.getAllocation().entrySet()) {
            ProductItem item = entry.getKey();
            Activity activity = entry.getValue();
            String activityName = activity == null ? "不参加活动" : activity.getActivityName();
            log.info("  商品 %s*%d (单价: %.2f) -> %s%n".formatted(item.getProductName(), item.getQuantity(), item.getPrice(), activityName));
        }


        List<ProductResult> productResults = service.calculateAllocation(result);
        for (ProductResult productResult : productResults) {
            log.info("商品：{}数量：{}单价：{}命中活动:{},优惠金额：{}", productResult.getProductName(),
                productResult.getQuantity(), productResult.getPrice(),productResult.getActivityName(), productResult.getDiscountAmount());
        }
    }

    private static List<ProductItem> prepareTestData() {
        // 创建示例商品和活动
        MjmzActivity activity1 = new MjmzActivity();
        activity1.setId(1L);
        activity1.setActivityName("满5件减3活动");
        activity1.setActivityType(ActivityType.MJ_MZ.getType());
        activity1.setDiscountType(DiscountType.MJ.getType());
        activity1.setDiscountOffer(DiscountOffer.JY.getType());
        activity1.setDiscountRules(DiscountRules.LOOP.getType());
        activity1.setDiscountSettingMap(Map.of(5.0, 3.0));

        MjmzActivity activity2 = new MjmzActivity();
        activity2.setId(1L);
        activity2.setActivityName("满5件减4活动");
        activity2.setActivityType(ActivityType.MJ_MZ.getType());
        activity2.setDiscountType(DiscountType.MJ.getType());
        activity2.setDiscountOffer(DiscountOffer.JY.getType());
        activity2.setDiscountRules(DiscountRules.LOOP.getType());
        activity2.setDiscountSettingMap(Map.of(5.0, 4.0));

        MjmzActivity mjmzActivity1 = new MjmzActivity();
        mjmzActivity1.setId(3L);
        mjmzActivity1.setActivityName("满5元减3活动");
        mjmzActivity1.setActivityType(ActivityType.MJ_MZ.getType());
        mjmzActivity1.setDiscountType(DiscountType.MJ.getType());
        mjmzActivity1.setDiscountOffer(DiscountOffer.JY.getType());
        mjmzActivity1.setDiscountRules(DiscountRules.LOOP.getType());
        mjmzActivity1.setDiscountSettingMap(Map.of(5.0, 3.0));

        NjnzActivity njnzActivity1 = new NjnzActivity();
        njnzActivity1.setId(5L);
        njnzActivity1.setActivityType(ActivityType.NJ_NZ.getType());
        njnzActivity1.setActivityName("第2件5折活动");
        njnzActivity1.setDiscountItemNum(2);
        njnzActivity1.setDiscountRate(5.0);

        ProductItem productA = new ProductItem();
        productA.setProductName("芬达");
        productA.setPrice(5.0);
        productA.setQuantity(4);
//        productA.setMjmzActivities(Arrays.asList(activity1, activity2));
        productA.setNjnzActivities(List.of(njnzActivity1));

        ProductItem productB = new ProductItem();
        productB.setProductName("可乐");
        productB.setPrice(5.0);
        productB.setQuantity(3);
        productB.setMjmzActivities(Arrays.asList(activity1));
        productB.setNjnzActivities(new ArrayList<>());

        ProductItem productC = new ProductItem();
        productC.setProductName("雪碧");
        productC.setPrice(5.0);
        productC.setQuantity(3);
        productC.setMjmzActivities(Arrays.asList(activity1));

        List<ProductItem> productItems = new ArrayList<>();
        productItems.add(productA);
        productItems.add(productB);
        productItems.add(productC);
        return productItems;
    }

}