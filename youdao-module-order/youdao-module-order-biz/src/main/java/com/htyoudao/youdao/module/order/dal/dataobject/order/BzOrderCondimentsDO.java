package com.htyoudao.youdao.module.order.dal.dataobject.order;

import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;

@TableName("bz_order_condiments")
@Data
public class BzOrderCondimentsDO extends BusinessBaseDO {
    @Serial
    private static final long serialVersionUID = -3015603159820513546L;

    private Long condimentId;

    private String orderSn;

    private String condimentImage;

    private String condimentName;

    private String condimentDescription;

    private BigDecimal condimentPrice;

    private Integer condimentNumber;

    private String commoditySkuname;

    private Integer isSingle;

    private String commodityValue;

    private String commodityFeedingname;

    private String commodityFeedingvalue;

    private Integer commodityAmount;

    private String commodityTastename;

    private String commodityTastevalue;

    private String commodityGoodsid;
}
