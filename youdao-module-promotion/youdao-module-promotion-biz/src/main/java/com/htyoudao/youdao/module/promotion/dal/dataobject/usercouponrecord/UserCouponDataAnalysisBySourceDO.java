package com.htyoudao.youdao.module.promotion.dal.dataobject.usercouponrecord;

import com.baomidou.mybatisplus.annotation.TableName;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@TableName("user_coupon_record")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Table(value = "user_coupon_record")
public class UserCouponDataAnalysisBySourceDO extends BusinessBaseDO {

    @Schema(description = "用券总成交额")
    private BigDecimal turnover;

    @Schema(description = "优惠总金额")
    private BigDecimal offerTotal;

    @Schema(description = "费效比")
    private BigDecimal cost;

    @Schema(description = "付款单数")
    private Integer orderNum;

    @Schema(description = "用券笔单价")
    private BigDecimal singlePrice;

    @Schema(description = "老客户数量")
    private Integer oldCustom;

    @Schema(description = "新客户数量")
    private Integer newCustom;

    @Schema(description = "商品数量")
    private Integer itemNum;

    @Schema(description = "使用率")
    private BigDecimal usedRate;

    @Schema(description = "优惠券渠道来源")
    private Integer couponSource;
}
