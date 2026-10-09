package com.htyoudao.youdao.module.promotion.controller.app.usercoupon.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author dht
 */
@Data
public class AppCouponCalculateRespVO{

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "31674")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "优惠券名称", example = "赵六")
    @ExcelProperty("优惠券名称")
    private String couponName;

    @Schema(description = "优惠券类型(0-满减券,1-折扣券)", example = "2")
    @ExcelProperty("优惠券类型(0-满减券,1-折扣券)")
    private Integer couponType;

    @Schema(description = "单品id")
    @ExcelProperty("单品id")
    private String singleIds;

    @Schema(description = "优惠券图片", example = "https://www.iocoder.cn")
    @ExcelProperty("优惠券图片")
    private String couponImageUrl;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "适用门店范围 1:通用 2:门店券")
    @ExcelProperty("适用门店范围 1:通用 2:门店券")
    private Integer isCommon;

    @Schema(description = "使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛）")
    private Integer doorsillType;

    @Schema(description = "门槛金额/件数 ")
    @ExcelProperty("门槛金额/件数 ")
    private BigDecimal doorsill;

    @Schema(description = "适用商品范围 1 通用 2指定商品可用 3指定商品不可用", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("适用商品范围 1 通用 2指定商品可用 3指定商品不可用")
    private Integer isCommonStore;

    @Schema(description = "减免/折扣", example = "24242")
    @ExcelProperty("减免/折扣")
    private BigDecimal reliefOrDiscount;

    @Schema(description = "支付金额")
    @ExcelProperty("支付金额")
    private BigDecimal payAmount;

    @Schema(description = "到期时间")
    @ExcelProperty("到期时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd")
    private Date expirationTime;

    @Schema(description = "有效开始日期")
    @ExcelProperty("有效开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd")
    private Date vaildStartTime;

    @Schema(description = "用餐方式 0 全部可用 1堂食可用 2外卖可用")
    @ExcelProperty("用餐方式 0 全部可用 1堂食可用 2外卖可用")
    private Integer habit;

    @Schema(description = "优惠券背景图片", example = "https://www.iocoder.cn")
    @ExcelProperty("优惠券背景图片")
    private String couponBgImageUrl;

    @Schema(description = "用户优惠券id")
    private Long userCouponId;

    /**
     * 参与优惠的商品数量
     */
    private int goodSize;

    /**
     * 参与优惠的商品ids
     */
    private List<Long> commodityIds = new ArrayList<>();

    /**
     * 减免金额
     */
    private BigDecimal money;

    @Schema(description = "优惠券主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "8817")
    @ExcelProperty("优惠券主键")
    private Long couponId;

    /**
     * 门店ids
     */
    @Schema(description = "门店ids")
    private List<Long> storeIdList;

    /**
     * 0 无满减兑换券  1 满减兑换券
     */
    private Integer fullReduction;


    /**
     * 兑换券是否需要绑定商品 0否 1是
     */
    private Integer exchangeFlag;

    /**
     * 减少金额
     */
    @Schema(description = "n件n元/折中的元/折", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal reduceAmount;

    /**
     * 折扣
     */
    @Schema(description = "n件n元/折中的件 也是折扣的折", requiredMode = Schema.RequiredMode.REQUIRED)
    private String discount;
}
