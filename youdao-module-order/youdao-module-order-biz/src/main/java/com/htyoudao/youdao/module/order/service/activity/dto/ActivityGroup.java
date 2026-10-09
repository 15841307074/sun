package com.htyoudao.youdao.module.order.service.activity.dto;

import com.htyoudao.youdao.module.order.service.activity.calc.Activity;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class ActivityGroup {

    private Long activityId;

    private Activity activity;

    private List<ProductItem> productItems;

    //总优惠金额
    private Double discountAmount;

    public ActivityGroup(Activity activity, List<ProductItem> productItems) {
        this.activity = activity;
        this.activityId = activity.getId();
        this.productItems = productItems;
    }
}
