package com.htyoudao.youdao.module.order.controller.admin.order.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.commodity.CommodityCondimentsVO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.commodity.CommodityFlavoVO;
import com.htyoudao.youdao.module.order.controller.admin.order.vo.commodity.CommoditySkusVO;
import com.htyoudao.youdao.module.order.core.submit.DTO.ActivityNjnzInfoDTO;
import com.htyoudao.youdao.module.order.dal.dataobject.order.BzOrderProductSonDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BzOrderProductVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -1687439695309799702L;

    private String orderSn;

    private Long orderProductId;

    private Long goodsId;

    private String goodsName;

    private String goodsImage;

    private Long activityId;

    private String activityName;

    private Integer activityType;

    private Long couponId;

    private Long userCouponId;

    private String couponName;

    private String specValues;

    private BigDecimal goodsShowPrice;

    private BigDecimal skuStrikePrice;

    private BigDecimal activityDiscountAmount;

    private BigDecimal promotionDiscountAmount;

    @JsonIgnore
    private String activityDiscountDetail;

    private String flavorName;

    private String flavorValue;

    private Integer goodsNum;

    private Integer isSingle;

    private String categoryName;

    private Long commodityId;

    private BigDecimal moneyAmount;

    private Integer isGetActivity;

    private Integer sendIntegral;

    private ActivityNjnzInfoDTO activityInfo;

    private List<CommodityFlavoVO> commodityFlavors = new ArrayList<>();

    private List<CommoditySkusVO> commoditySkusList = new ArrayList<>();

    private List<String> describeList = new ArrayList<>();

    private List<String> condimentNameList = new ArrayList<>();

    private List<BzOrderProductSonDO> groupBzOrderProductList = new ArrayList<>();

    private List<CommodityCondimentsVO> commodityCondiments = new ArrayList<>();

    private List<String> condimentStr = new ArrayList<>();

    private List<String> singleListStr = new ArrayList<>();
}
