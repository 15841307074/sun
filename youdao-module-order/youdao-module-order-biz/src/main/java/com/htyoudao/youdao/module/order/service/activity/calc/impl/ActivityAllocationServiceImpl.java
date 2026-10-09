package com.htyoudao.youdao.module.order.service.activity.calc.impl;

import static org.apache.shardingsphere.sql.parser.autogen.FirebirdStatementParser.SCALE;

import com.htyoudao.youdao.framework.common.util.collection.BeanCopyUtils;
import com.htyoudao.youdao.framework.common.util.collection.CollectionUtils;
import com.htyoudao.youdao.module.order.service.activity.calc.Activity;
import com.htyoudao.youdao.module.order.service.activity.calc.ActivityAllocationService;
import com.htyoudao.youdao.module.order.service.activity.dto.ActivityGroup;
import com.htyoudao.youdao.module.order.service.activity.dto.AllocationResult;
import com.htyoudao.youdao.module.order.service.activity.dto.ProductItem;
import com.htyoudao.youdao.module.order.service.activity.dto.ProductResult;
import com.htyoudao.youdao.module.promotion.api.enums.activity.ActivityTypeEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ActivityAllocationServiceImpl implements ActivityAllocationService {

    @Override
    public List<ActivityGroup> groupByActivity(Map<ProductItem, Activity> allocation) {
        List<ActivityGroup> activityGroups = new ArrayList<>();
        Map<Long, ActivityGroup> groupedActivities = new HashMap<>();

        for (Map.Entry<ProductItem, Activity> entry : allocation.entrySet()) {
            ProductItem productItem = entry.getKey();
            Activity activity = entry.getValue();

            if (activity == null) {
                continue;
            }

            // 第N件N折活动不允许分组，每个商品单独创建分组
            if (activity.getAllowGroup()) {
                Long id = activity.getId();
                ActivityGroup activityGroup = groupedActivities.get(id);

                if (activityGroup == null) {
                    activityGroup = new ActivityGroup(activity, new ArrayList<>());
                    activityGroups.add(activityGroup);
                    groupedActivities.put(id, activityGroup);
                }

                activityGroup.getProductItems().add(productItem);
            } else {
                activityGroups.add(new ActivityGroup(activity, List.of(productItem)));
            }
        }

        return activityGroups;
    }

    @Override
    public List<Map<ProductItem, Activity>> generateAllocations(List<ProductItem> products) {
        List<Map<ProductItem, Activity>> results = new ArrayList<>();
        Map<ProductItem, Activity> current = new HashMap<>();
        backtrack(products, 0, current, results);
        return results;
    }


    private void backtrackNew(
        List<ProductItem> products,
        int index,
        Map<ProductItem, Activity> currentAllocation,
        OptimalSolutionHolder holder) {

        // 完整分配完成，计算并更新最优解
        if (index == products.size()) {
            AllocationResult result = calculateDiscount(new HashMap<>(currentAllocation));
            holder.updateIfBetter(result, new HashMap<>(currentAllocation));
            return;
        }

        ProductItem product = products.get(index);
        List<Activity> availableActivities = getAllAvailableActivities(product);

        if (CollectionUtils.isAnyEmpty(availableActivities)){
            currentAllocation.put(product, null);
            backtrackNew(products, index + 1, currentAllocation, holder);
            currentAllocation.remove(product);
        }

        for (Activity activity : availableActivities) {
            currentAllocation.put(product, activity);
            backtrackNew(products, index + 1, currentAllocation, holder);
            currentAllocation.remove(product);
        }
    }


    // 最优解持有器
    private static class OptimalSolutionHolder {
        AllocationResult bestResult;
        Map<ProductItem, Activity> bestAllocation;

        void updateIfBetter(AllocationResult newResult, Map<ProductItem, Activity> newAllocation) {
            if (newResult == null) return;

            if (bestResult == null) {
                bestResult = newResult;
                bestAllocation = new HashMap<>(newAllocation);
                return;
            }

            // 比较规则：先比较折扣，再比较时间
            if (newResult.getTotalDiscount() > bestResult.getTotalDiscount() ||
                (newResult.getTotalDiscount() == bestResult.getTotalDiscount() &&
                    newResult.getLatestActivityTime() < bestResult.getLatestActivityTime())) {
                bestResult = newResult;
                bestAllocation = new HashMap<>(newAllocation);
            }
        }
    }

    @Override
    public AllocationResult calculateDiscount(Map<ProductItem, Activity> allocation) {
        List<ActivityGroup> activityGroups = this.groupByActivity(allocation);
        double totalDiscount = 0.0;
        long latestActivityTime = Long.MIN_VALUE;

        for (ActivityGroup activityGroup : activityGroups) {
            Activity activity = activityGroup.getActivity();
            List<ProductItem> productItems = activityGroup.getProductItems();

            if (activity.canApply(productItems)) {
                double discount = activity.calculateDiscount(productItems);
                activityGroup.setDiscountAmount(discount);
                totalDiscount += discount;

                // 记录最新活动时间
                if (activity.getCreateTime() != null && activity.getCreateTime() > latestActivityTime) {
                    latestActivityTime = activity.getCreateTime();
                }
            }
        }

        return new AllocationResult(allocation, activityGroups, totalDiscount, latestActivityTime);
    }


    /**
     * 通过最优方案返回值。计算分摊金额
     *
     * @return
     */
    @Override
    public List<ProductResult> calculateAllocation(AllocationResult result) {
        List<ProductResult> productResults = new ArrayList<>();
        for (ActivityGroup group : result.getActivityGroups()) {
            productResults.addAll(calculatePerItemAllocation(group));
        }

        //未命中活动的商品 也返回result
        result.getAllocation().entrySet().stream()
            .filter(e -> e.getValue() == null)
            .map(Entry::getKey)
            .map(e -> createProductResult(e, null))
            .forEach(productResults::add);

        return productResults;
    }

    /**
     * 通过当前活动分组 计算分摊返回值
     *
     * @param group
     * @return
     */
    private List<ProductResult> calculatePerItemAllocation(ActivityGroup group) {
        List<ProductResult> results = new ArrayList<>();
        List<ProductItem> productItems = group.getProductItems();
        Double totalDiscount = group.getDiscountAmount();

        //无优惠金额时， 不返回命中的活动
        if (totalDiscount == null || totalDiscount <= 0.0){
            return productItems.stream().map(p -> createProductResult(p, null)).toList();
        }

        ActivityTypeEnum activityType = ActivityTypeEnum.getEnumByCode(group.getActivity().getActivityType());

        if (activityType == null) {
            log.error("{} not found", group.getActivity().getActivityType());
            return List.of();
        }

        switch (activityType) {
            case NJ_NZ: // 第N件打N折 需拆分两个商品出来 特殊处理
                results.addAll(allocateForNjnz(group));
                break;
            default:
                // 默认按比例分摊
                results.addAll(allocateProportionally(group, productItems, totalDiscount));
        }

        return results;
    }

    /**
     * N件N折 计算分摊 拆分两个商品出来
     *
     * @param group
     * @return
     */
    private List<ProductResult> allocateForNjnz(ActivityGroup group) {
        Activity activity = group.getActivity();

        Integer discountQuantity = activity.getDiscountQuantity(group.getProductItems());
        ProductItem productItem = group.getProductItems().get(0);//njnz不允许聚合

        ProductItem discountActivityItem = BeanCopyUtils.copyBean(productItem, ProductItem.class);
        discountActivityItem.setQuantity(discountQuantity);

        ProductResult productResult = createProductResult(discountActivityItem, group.getActivity());
        productResult.setDiscountAmount(group.getDiscountAmount());

        //如果是第1件x折这种 没有不命中活动的商品 不需要拆分
        if (Objects.equals(productItem.getQuantity(), discountQuantity)){
            return List.of(productResult);
        }

        ProductItem otherItem = BeanCopyUtils.copyBean(productItem, ProductItem.class);
        otherItem.setQuantity(productItem.getQuantity() - discountQuantity);
        ProductResult otherProductResult = createProductResult(otherItem, null);
        return List.of(productResult, otherProductResult);
    }

    /**
     * 正常按比例分摊
     *
     * @return
     */
    private List<ProductResult> allocateProportionally(ActivityGroup group, List<ProductItem> productItems,
        Double totalDiscount) {
        // 计算总金额
        Double totalAmount = productItems.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();

        if (totalAmount <= 0) {
            return List.of();
        }
        List<ProductResult> results = new ArrayList<>();

        // 按比例分摊
        Double allocatedTotal = 0.0;

        //保证顺序
        productItems.sort(Comparator.comparing(ProductItem::getUId));

        for (int i = 0; i < productItems.size(); i++) {
            ProductItem item = productItems.get(i);
            Double itemAmount = item.getPrice() * item.getQuantity();

            // 计算分摊比例
            Double ratio = divide(itemAmount, totalAmount, 4);
            Double itemDiscount = multiply(totalDiscount, ratio);

            // 最后一个商品需要处理精度问题，确保总优惠金额准确
            if (i == productItems.size() - 1) {
                itemDiscount = subtract(totalDiscount, allocatedTotal);
            } else {
                allocatedTotal = add(allocatedTotal, itemDiscount);
            }

            ProductResult result = createProductResult(item, group.getActivity());
            result.setDiscountAmount(itemDiscount);
            results.add(result);
        }

        return results;
    }

    /**
     * 创建商品结果对象
     */
    private ProductResult createProductResult(ProductItem item, Activity activity) {
        ProductResult result = new ProductResult();
        result.setSkuId(item.getSkuId());
        result.setCommodityId(item.getCommodityId());
        result.setProductName(item.getProductName());
        result.setQuantity(item.getQuantity());
        result.setPrice(item.getPrice());
        result.setActivityId(activity != null ? activity.getId() : null);
        result.setActivityName(activity != null ? activity.getActivityName() : null);
        result.setActivityType(activity != null ? activity.getActivityType() : null);
        result.setStackableActivities(activity != null ? activity.getStackableActivities() : List.of());
        result.setUId(item.getUId());
        return result;
    }


    /**
     * 查找最优分配方案
     */
    @Override
    public Optional<AllocationResult> findOptimalAllocation(List<ProductItem> products) {
        // 使用容器包装最优解，以便在递归中更新
        OptimalSolutionHolder holder = new OptimalSolutionHolder();

        // 开始回溯搜索
        backtrackNew(products, 0, new HashMap<>(), holder);

        return Optional.ofNullable(holder.bestResult);
    }


    private void backtrack(List<ProductItem> products, int index,
        Map<ProductItem, Activity> current,
        List<Map<ProductItem, Activity>> results) {
        if (index == products.size()) {
            results.add(new HashMap<>(current));
            return;
        }

        ProductItem product = products.get(index);
        List<Activity> availableActivities = getAllAvailableActivities(product);

        for (Activity activity : availableActivities) {
            current.put(product, activity);
            backtrack(products, index + 1, current, results);
            current.remove(product);
        }
    }

    private List<Activity> getAllAvailableActivities(ProductItem product) {
        List<Activity> activities = new ArrayList<>();

        // 添加满减满折活动
        if (product.getMjmzActivities() != null) {
            activities.addAll(product.getMjmzActivities());
        }

        // 添加N件N折活动
        if (product.getNjnzActivities() != null) {
            activities.addAll(product.getNjnzActivities());
        }

        // 添加不参加活动的选项
//        activities.add(null);
        return activities;
    }

    // 精确计算工具方法（避免浮点数精度问题）
    private Double add(Double d1, Double d2) {
        if (d1 == null) {
            d1 = 0.0;
        }
        if (d2 == null) {
            d2 = 0.0;
        }
        return BigDecimal.valueOf(d1)
            .add(BigDecimal.valueOf(d2))
            .doubleValue();
    }

    private Double subtract(Double d1, Double d2) {
        if (d1 == null) {
            d1 = 0.0;
        }
        if (d2 == null) {
            d2 = 0.0;
        }
        return BigDecimal.valueOf(d1)
            .subtract(BigDecimal.valueOf(d2))
            .doubleValue();
    }

    private Double multiply(Double d1, Double d2) {
        if (d1 == null || d2 == null) {
            return 0.0;
        }
        return BigDecimal.valueOf(d1)
            .multiply(BigDecimal.valueOf(d2))
            .setScale(SCALE, RoundingMode.HALF_UP)
            .doubleValue();
    }

    private Double divide(Double d1, Double d2, int scale) {
        if (d1 == null || d2 == null || d2 == 0.0) {
            return 0.0;
        }
        return BigDecimal.valueOf(d1)
            .divide(BigDecimal.valueOf(d2), scale, RoundingMode.HALF_UP)
            .doubleValue();
    }
}
