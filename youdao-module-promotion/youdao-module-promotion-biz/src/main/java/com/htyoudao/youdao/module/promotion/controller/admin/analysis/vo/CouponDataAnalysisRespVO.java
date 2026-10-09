package com.htyoudao.youdao.module.promotion.controller.admin.analysis.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(name = "优惠券数据返回参", description = "优惠券数据返回参")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CouponDataAnalysisRespVO {

    @Schema(description = "领券总次数")
    @ExcelIgnore
    private BigDecimal received;

    @Schema(description = "门店领取次数")
    @ExcelIgnore
    private Long receiveNum;

    @ExcelIgnore
    private String createDate;

    @Schema(description = "用券总次数")
    private BigDecimal usage;

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

    @Schema(description = "商品数量")
    private Integer itemNum;

    @Schema(description = "使用率")
    @ExcelIgnore
    private BigDecimal usedRate;

    @Schema(description = "店铺id")
    @ExcelIgnore
    private Long storeId;

    @Schema(description = "店铺名称")
    private String storeName;

    @Schema(description = "优惠券渠道来源")
    @ExcelIgnore
    private Integer couponSource;

    @ExcelIgnore
    private long pv;

    @ExcelIgnore
    private long uv;

    @ExcelIgnore
    private long couponShareNum;

    public static CouponDataAnalysisRespVO initVO(){
        CouponDataAnalysisRespVO result = new CouponDataAnalysisRespVO();
        result.setReceived(new BigDecimal(0));
        result.setUsage(new BigDecimal(0));
        result.setTurnover(new BigDecimal(0));
        result.setOfferTotal(new BigDecimal(0));
        result.setCost(new BigDecimal(0));
        result.setOrderNum(0);
        result.setSinglePrice(new BigDecimal(0));
        result.setItemNum(0);
        result.setUsedRate(new BigDecimal(0));
        return result;
    }
}
