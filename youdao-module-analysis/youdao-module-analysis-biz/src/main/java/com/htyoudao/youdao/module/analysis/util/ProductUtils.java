package com.htyoudao.youdao.module.analysis.util;

import cn.hutool.core.util.ObjectUtil;
import com.htyoudao.youdao.module.analysis.dal.es.BzOrder;

import java.util.List;

/**
 * @author dht
 */
public class ProductUtils {
    public static String buildProductString(List<BzOrder.Product> products) {
        StringBuilder result = new StringBuilder();
        // 用来检查是否有参与活动或优惠券的商品
        boolean hasActiveProduct = false;

        // 遍历每个商品
        for (int i = 0; i < products.size(); i++) {
            BzOrder.Product product = products.get(i);

            boolean hasActivityDiscount = product.getPromotionDiscountAmount() != null && product.getPromotionDiscountAmount() > 0;
            boolean hasCouponDiscount = product.getActivityDiscountAmount() != null && product.getActivityDiscountAmount() > 0;

            if (hasActivityDiscount || hasCouponDiscount) {
                hasActiveProduct = true;
            }

            // 商品名称、数量、单价、现价
            String productInfo = product.getGoodsName() + "*" + product.getGoodsNum() + "/售价￥" + product.getGoodsAmount() +
                    "/现价￥" + (product.getGoodsAmount() - product.getActivityDiscountAmount() - product.getPromotionDiscountAmount());

            if (hasActivityDiscount) {
                String activityTag = product.getActivityTag();
                if (ObjectUtil.isEmpty(activityTag)) {
                    activityTag = "活动";
                }
                productInfo += "【" + activityTag + "：减" + product.getPromotionDiscountAmount() + "元（" + product.getPromotionDiscountAmount() + "）】";
            }

            if (hasCouponDiscount) {
                productInfo += "【优惠券：减" + product.getActivityDiscountAmount() + "元（" + product.getCouponName() + "）】";
            }

            // 判断是否是最后一个商品，如果是最后一个就不加换行符
            if (i != products.size() - 1) {
                productInfo += "\n";
            }

            // 拼接完成后加入结果
            result.append(productInfo);
        }

        if (!hasActiveProduct) {
            return "无";
        }

        return result.toString();
    }
}