package com.htyoudao.youdao.module.promotion.api.activityjk.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;

/**
 * 下单校验集卡活动请求参数
 */
@Data
@Schema(description = "下单校验集卡活动请求参数")
public class ActivityJkOrderReqDTO implements java.io.Serializable{

    @Serial
    private static final long serialVersionUID = -2794158164266381483L;

    @Schema(description = "门店ID")
    private Long storeId;

    @Schema(description = "用户ID")
    private Long memberId;

    @Schema(description = "下单商品ID集合")
    private List<Long> commodityIds;

    @Schema(description = "下单品类型 1单品， 2套餐， 3混合")
    private Integer orderProductType;

    @Schema(description = "下单实付金额")
    private BigDecimal paymentAmount;
}
