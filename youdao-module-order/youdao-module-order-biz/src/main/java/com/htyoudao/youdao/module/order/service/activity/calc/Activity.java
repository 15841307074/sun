package com.htyoudao.youdao.module.order.service.activity.calc;

import com.htyoudao.youdao.module.order.service.activity.dto.ProductItem;
import com.htyoudao.youdao.module.order.service.activity.enums.ActivityType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// 活动接口
public interface Activity {

    Long getId();
    String getActivityName();
    Integer getActivityType();
    Long getCreateTime();
    List<Integer> getStackableActivities();


    /**
     * 计算优惠金额
     * @param items
     * @return
     */
    Double calculateDiscount(List<ProductItem> items);


    /**
     * 检查是否可以应用
     * @param items
     * @return
     */
    Boolean canApply(List<ProductItem> items);


    Integer getDiscountQuantity(List<ProductItem> items);

    /**
     * 获取商品总价格
     * @param items
     * @return
     */
    default Double getTotalPrice(List<ProductItem> items) {
        return items.stream()
            .filter(Objects::nonNull)
            .mapToDouble(item -> Optional.ofNullable(item.getPrice()).orElse(0.0)
                * Optional.ofNullable(item.getQuantity()).orElse(0))
            .sum();
    }


    /**
     * 获取商品购买总数量
     * @param items
     * @return
     */
    default Integer getTotalCount(List<ProductItem> items) {
        return items.stream()
            .filter(Objects::nonNull)
            .mapToInt(item -> Optional.ofNullable(item.getQuantity()).orElse(0))
            .sum();
    }


    /**
     * 根据 打x折 算优惠金额
     * @return discountAmount
     */
    default Double getDiscountAmountByRate(Double price , Double rate){
        return BigDecimal.valueOf(price).multiply(
            BigDecimal.valueOf((10 - rate) / 10).setScale(2, RoundingMode.HALF_UP)
        ).doubleValue();
    }


    /**
     * 获取当前活动是否允许聚合一起计算优惠
     * 如：一起凑满减的 是允许的，第N件打N折 是不允许的
     * @return
     */
    default Boolean getAllowGroup(){
        List<Integer> nowAllowGroupActivityType = List.of(ActivityType.NJ_NZ.getType());
        return !nowAllowGroupActivityType.contains(this.getActivityType());
    }

}
