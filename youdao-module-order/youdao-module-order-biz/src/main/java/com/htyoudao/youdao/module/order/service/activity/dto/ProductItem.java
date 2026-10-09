package com.htyoudao.youdao.module.order.service.activity.dto;

import com.htyoudao.youdao.module.order.service.activity.calc.Activity;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ProductItem {

    private Long commodityId;

    private Long skuId;

    //唯一，解决相同sku不同副属性
    private String uId;

    private String productName;

    private Integer quantity; // 购买数量

    private Double price; // 单价

    private List<MjmzActivity> mjmzActivities;

    private List<NjnzActivity> njnzActivities;

}
