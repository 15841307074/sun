package com.htyoudao.youdao.module.order.controller.order.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Data
public class BzOrderProductOrderListReqVO {

    @TableId(value = "order_product_id", type = IdType.ASSIGN_ID)
    private Long orderProductId;

    private String orderSn;

    private Long storeId;

    private String storeName;

    private Long memberId;

    private Long goodsId;

    private String goodsName;

    private String goodsImage;

    private String specValues;

    private BigDecimal goodsShowPrice;

    private Long goodsNum;

    private BigDecimal activityDiscountAmount;

    private BigDecimal platformActivityAmount;

    private BigDecimal platformVoucherAmount;

    private BigDecimal moneyAmount;

    private BigDecimal commissionRate;

    private BigDecimal commissionAmount;

    private BigDecimal attachCommissionAmount;

    private Long spellTeamId;

    private Integer isGift;

    private Integer giftId;

    private Integer returnNumber;

    private Integer isComment;

    private Date commentTime;

    private Integer sendIntegral;

    private Integer isSingle;

    private String flavorName;

    private String flavorValue;

    private Long categoryId;

    private String categoryName;

    private String activityDiscountDetail;

    private Date createTime;

    private Long projectOwnerShip;
    private List<String> orderSnList;

}
