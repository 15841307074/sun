package com.htyoudao.youdao.module.order.service.activity.calc.impl;//package com.htyoudao.youdao.module.order.service.activity.service;
//
//import com.htyoudao.youdao.module.order.service.activity.dto.ActivityGroup;
//import com.htyoudao.youdao.module.order.service.activity.dto.MjmzActivity;
//import com.htyoudao.youdao.module.order.service.activity.dto.NjnzActivity;
//import com.htyoudao.youdao.module.order.service.activity.dto.ProductItem;
//import com.htyoudao.youdao.module.order.service.activity.enums.ActivityType;
//import com.htyoudao.youdao.module.order.service.activity.enums.DiscountOffer;
//import com.htyoudao.youdao.module.order.service.activity.enums.DiscountRules;
//import com.htyoudao.youdao.module.order.service.activity.enums.DiscountType;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//public class ActivityAllocation {
//
//    // 生成所有可能的分配方案
//    public static List<Map<ProductItem, Activity>> generateAllocations(List<ProductItem> products) {
//        List<Map<ProductItem, Activity>> results = new ArrayList<>();
//        // 用于递归的当前分配方案
//        Map<ProductItem, Activity> current = new HashMap<>();
//        backtrack(products, 0, current, results);
//        return results;
//    }
//
//    private static void backtrack(List<ProductItem> products, int index, Map<ProductItem, Activity> current,
//        List<Map<ProductItem, Activity>> results) {
//        if (index == products.size()) {
//            results.add(new HashMap<>(current));
//            return;
//        }
//
//        ProductItem product = products.get(index);
//        // 添加所有活动
//        List<Activity> availableActivities = new ArrayList<>(product.getAllActivities());
//        // 添加不参加的选项
//        availableActivities.add(null);
//
//        for (Activity activity : availableActivities) {
//            current.put(product, activity);
//            backtrack(products, index + 1, current, results);
//            current.remove(product);
//        }
//    }
//
//
//    public static void main(String[] args) {
//        // 创建示例商品和活动
//        MjmzActivity activity1 = new MjmzActivity();
//        activity1.setId(1L);
//        activity1.setActivityName("满5件减3活动");
//        activity1.setActivityType(ActivityType.MJ_MZ.getType());
//        activity1.setDiscountType(DiscountType.MJ.getType());
//        activity1.setDiscountOffer(DiscountOffer.JY.getType());
//        activity1.setDiscountRules(DiscountRules.LOOP.getType());
//        activity1.setDiscountSettingMap(Map.of(5.0, 3.0));
//
//        MjmzActivity activity2 = new MjmzActivity();
//        activity2.setId(2L);
//        activity2.setActivityName("满5件减4活动");
//        activity2.setActivityType(ActivityType.MJ_MZ.getType());
//        activity2.setDiscountType(DiscountType.MJ.getType());
//        activity2.setDiscountOffer(DiscountOffer.JY.getType());
//        activity2.setDiscountRules(DiscountRules.LOOP.getType());
//        activity2.setDiscountSettingMap(Map.of(5.0, 4.0));
//
//        MjmzActivity mjmzActivity1 = new MjmzActivity();
//        mjmzActivity1.setId(3L);
//        mjmzActivity1.setActivityName("满5元减3活动");
//        mjmzActivity1.setActivityType(ActivityType.MJ_MZ.getType());
//        mjmzActivity1.setDiscountType(DiscountType.MJ.getType());
//        mjmzActivity1.setDiscountOffer(DiscountOffer.JY.getType());
//        mjmzActivity1.setDiscountRules(DiscountRules.LOOP.getType());
//        mjmzActivity1.setDiscountSettingMap(Map.of(5.0, 3.0));
//
//        NjnzActivity njnzActivity1 = new NjnzActivity();
//        njnzActivity1.setId(4L);
//        njnzActivity1.setActivityType(ActivityType.NJ_NZ.getType());
//        njnzActivity1.setActivityName("第一件0折活动");
//        njnzActivity1.setDiscountItemNum(1);
//        njnzActivity1.setDiscountRate(0.0);
//
//        ProductItem productA = new ProductItem();
//        productA.setProductName("芬达");
//        productA.setPrice(5.0);
//        productA.setQuantity(3);
//        productA.setMjmzActivities(Arrays.asList(activity1, activity2));
//        productA.setNjnzActivities(List.of(njnzActivity1));
//
//        ProductItem productB = new ProductItem();
//        productB.setProductName("可乐");
//        productB.setPrice(5.0);
//        productB.setQuantity(3);
//        productB.setMjmzActivities(Arrays.asList(activity1));
//        productB.setNjnzActivities(new ArrayList<>());
//
//        ProductItem productC = new ProductItem();
//        productC.setProductName("雪碧");
//        productC.setPrice(5.0);
//        productC.setQuantity(3);
//        productC.setMjmzActivities(Arrays.asList(activity1));
//
//        List<ProductItem> products = Arrays.asList(productA, productB, productC);
//
//        List<Map<ProductItem, Activity>> allocations = generateAllocations(products);
//
//        for (int i = 0; i < allocations.size(); i++) {
//            Map<ProductItem, Activity> allocation = allocations.get(i);
//
//            //打印方案信息
//            System.out.println(i + "方案基础信息:");
//            printLog(allocation);
//
//
//            // 按活动进行分组
//            List<ActivityGroup> activityGroups = getActivityGroups(allocation);
//
//            //计算总优惠
//            for (ActivityGroup activityGroup : activityGroups) {
//                Double distountAmount = 0.0;
//
//                Activity activity = activityGroup.getActivity();
//                List<ProductItem> productItems = activityGroup.getProductItems();
//
//                if (activity.canApply(productItems)) {
//                    distountAmount += activity.calculateDiscount(productItems);
//                }
//
//                activityGroup.setDiscountAmount(distountAmount);
//            }
//
//            Double totalDiscountAmount = activityGroups.stream().map(ActivityGroup::getDiscountAmount)
//                .reduce(0.0, Double::sum);
//            System.out.println(i + "方案总优惠金额:" + totalDiscountAmount);
//            System.out.println();
//        }
//
//        //todo 找到最优惠的方案并且返回，如果优惠相同时 找到创建时间最晚的活动方案
//
//
//        //todo 计算分摊
//    }
//
//
//    /**
//     * 按活动进行分组 (第N件打N折 活动 不需要聚合)
//     * @param allocation
//     * @return
//     */
//    private static List<ActivityGroup> getActivityGroups(Map<ProductItem, Activity> allocation) {
//        List<ActivityGroup> activityGroups = new ArrayList<>();
//        Map<Long, ActivityGroup> groupedActivities = new HashMap<>();
//
//        for (Map.Entry<ProductItem, Activity> entry : allocation.entrySet()) {
//            ProductItem productItem = entry.getKey();
//            Activity activity = entry.getValue();
//
//            if (activity == null) {
//                continue;
//            }
//
//            if (activity.getAllowGroup()) {
//                ActivityGroup activityGroup = groupedActivities.get(activity.getId());
//
//                if (activityGroup == null) {
//                    activityGroup = new ActivityGroup(activity, new ArrayList<>());
//                    activityGroups.add(activityGroup);
//                    groupedActivities.put(activity.getId(), activityGroup);
//                }
//
//                activityGroup.getProductItems().add(productItem);
//            } else {
//                // 不允许分组时，每个产品项单独创建分组
//                activityGroups.add(new ActivityGroup(activity, List.of(productItem)));
//            }
//        }
//
//        return activityGroups;
//    }
//
//
//
//    private static void printLog(Map<ProductItem, Activity> allocation) {
//        for (Map.Entry<ProductItem, Activity> entry : allocation.entrySet()) {
//            String activityName = entry.getValue() == null ? "不参加活动" : entry.getValue().getActivityName();
//            System.out.println("  商品 " + entry.getKey().getProductName() + "*" + entry.getKey().getQuantity() + " -> "
//                + activityName);
//        }
//    }
//}