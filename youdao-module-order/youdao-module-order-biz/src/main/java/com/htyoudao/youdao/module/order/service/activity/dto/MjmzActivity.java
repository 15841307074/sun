package com.htyoudao.youdao.module.order.service.activity.dto;

import com.htyoudao.youdao.module.order.service.activity.enums.DiscountOffer;
import com.htyoudao.youdao.module.order.service.activity.enums.DiscountRules;
import com.htyoudao.youdao.module.order.service.activity.enums.DiscountType;
import com.htyoudao.youdao.module.order.service.activity.calc.Activity;

import java.util.*;
import java.util.Map.Entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 满减满折活动
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MjmzActivity implements Activity {

    private Long createTime;

    private Long id;

    /**
     * 活动名称
     */
    private String activityName;

    /**
     * 活动类型
     */
    private Integer activityType;

    /**
     * 优惠类型
     */
    private Integer discountType;

    /**
     * 是否优惠叠加（0-否，1-是）
     */
    private Integer discountStackable;

    /**
     * 可叠加活动（存储活动标识，如1=优惠券, 2=N件N折, 3=满减满折, 4=满赠活动）
     */
    private List<Integer> stackableActivities;

    /**
     * 活动备注（最多200字）
     */
    private String activityRemark;


    /**
     * 优惠折扣（1满xx元减xx元 2满xx元减xx折）
     */
    private Integer discountOffer;

    /**
     * 优惠规则（1阶梯优惠 2循环优惠）
     */
    private Integer discountRules;

    private Map<Double, Double> discountSettingMap;


    @Override
    public Double calculateDiscount(List<ProductItem> items) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }

        double totalPrice = getTotalPrice(items);
        int totalCount = getTotalCount(items);

        DiscountType type = DiscountType.getByType(discountType);
        if (type == null){
            return 0.0;
        }

        return switch (type) {
            case MY -> calculateMYDiscount(totalPrice, totalCount); // 满元
            case MJ -> calculateMJDiscount(totalPrice, totalCount); // 满件
            default -> 0.0;
        };
    }



    @Override
    public Boolean canApply(List<ProductItem> items) {
        if (items == null || items.isEmpty() || discountSettingMap.isEmpty()) {
            return false;
        }

        double totalPrice = getTotalPrice(items);
        int totalCount = getTotalCount(items);

        DiscountType type = DiscountType.getByType(discountType);
        if (type == null) {
            return false;
        }

        return switch (type) {
            case MY -> discountSettingMap.keySet().stream()
                .anyMatch(condition -> totalPrice >= condition); //满元时，总价大于任意条件
            case MJ -> discountSettingMap.keySet().stream()
                .anyMatch(condition -> totalCount >= condition); //满元时，总件大于任意条件
            default -> false;
        };
    }

    @Override
    public Integer getDiscountQuantity(List<ProductItem> items) {
        return 0;
    }

    /**
     * 计算满元优惠
     */
    private Double calculateMYDiscount(double totalPrice, int totalCount) {
        if (discountRules.equals(DiscountRules.STEP.getType())) {
            return calculateMYStepDiscount(totalPrice);
        } else if (discountRules.equals(DiscountRules.LOOP.getType())) {
            return calculateMYLoopDiscount(totalPrice);
        }
        return 0.0;
    }

    /**
     * 计算满件优惠
     */
    private Double calculateMJDiscount(double totalPrice, int totalCount) {
        if (discountRules.equals(DiscountRules.STEP.getType())) {
            return calculateMJStepDiscount(totalCount, totalPrice);
        } else if (discountRules.equals(DiscountRules.LOOP.getType())) {
            return calculateMJLoopDiscount(totalCount, totalPrice);
        }
        return 0.0;
    }


    /**
     * 满件阶梯优惠
     */
    private Double calculateMJStepDiscount(Integer totalCount, Double totalPrice) {
        // 找到满足条件的最优惠阶梯
        Optional<Double> maxDiscount = this.discountSettingMap.entrySet().stream()
            .filter(entry -> totalCount >= entry.getKey())
            .map(entry -> calculateDiscountAmount(totalPrice, entry.getValue()))
            .max(Comparator.naturalOrder());

        return maxDiscount.orElse(0.0);
    }


    /**
     * 满件循环优惠
     */
    private Double calculateMJLoopDiscount(Integer totalCount, Double totalPrice) {
        Entry<Double, Double> firstEntry = discountSettingMap.entrySet().iterator().next();
        Double firstKey = firstEntry.getKey();
        Double firstValue = firstEntry.getValue();

        //满足阶梯的次数
        Integer stepCount = (int) (totalCount / firstKey);

        return calculateDiscountAmountByLoop(stepCount, totalPrice, firstValue);
    }

    /**
     * 满元循环优惠
     */
    private Double calculateMYLoopDiscount(Double totalPrice) {
        Entry<Double, Double> firstEntry = discountSettingMap.entrySet().iterator().next();
        Double firstKey = firstEntry.getKey();
        Double firstValue = firstEntry.getValue();

        //满足循环的次数
        Integer stepCount = (int) (totalPrice / firstKey);

        return calculateDiscountAmountByLoop(stepCount, totalPrice, firstValue);
    }


    /**
     * 满元阶梯优惠
     */
    private Double calculateMYStepDiscount(Double totalPrice) {
        // 找到满足条件的最优惠阶梯（金额最大且满足条件）
        Optional<Double> maxDiscount = discountSettingMap.entrySet().stream()
            .filter(entry -> totalPrice >= entry.getKey())
            .map(entry -> calculateDiscountAmount(totalPrice, entry.getValue()))
            .max(Comparator.naturalOrder());

        return maxDiscount.orElse(0.0);
    }


    /**
     * 计算折扣金额（统一处理减元和减折）
     */
    private Double calculateDiscountAmount(double totalPrice, double discountValue) {
        DiscountOffer offer = DiscountOffer.getByType(discountOffer);

        if (offer == null){
            return 0.0;
        }

        return switch (offer) {
            case JY -> Math.min(discountValue, totalPrice); // 减元，但不能超过总价
            case JZ -> getDiscountAmountByRate(totalPrice, discountValue);    // 减折
            default -> 0.0;
        };
    }

    /**
     * 循环 计算折扣金额
     */
    private Double calculateDiscountAmountByLoop(Integer loopCount, double totalPrice, double discountValue) {
        DiscountOffer offer = DiscountOffer.getByType(discountOffer);

        if (offer == null){
            return 0.0;
        }

        return switch (offer){
            case JZ -> getDiscountAmountByRate(totalPrice, discountValue);
            case JY -> discountValue * loopCount;
            default -> 0.0;
        };
    }

    /**
     * 转换优惠规则
     */
    public void parseDiscountSettings(String discountSettings) {
        Map<Double, Double> map = new HashMap<>();

        if (discountSettings == null || discountSettings.isBlank()) {
            this.discountSettingMap = map;
            return;
        }

        // 先按 , 分隔多组规则
        String[] items = discountSettings.split(",");

        for (String item : items) {
            String[] parts = item.split("-");
            if (parts.length != 2) {
                throw new IllegalArgumentException("折扣格式错误：" + item);
            }

            try {
                Double threshold = Double.parseDouble(parts[0]);
                Double discount = Double.parseDouble(parts[1]);
                map.put(threshold, discount);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("折扣必须为数字：" + item, e);
            }
        }

        this.discountSettingMap = map;
    }



}
