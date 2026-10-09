package com.htyoudao.youdao.module.promotion.dal.dataobject.advertising.carousel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.framework.mybatis.core.dataobject.BusinessBaseDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@TableName("good_coupon")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CouponChooseDO extends BusinessBaseDO implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String couponName;

    /**
     * 减免/折扣
     */
    private BigDecimal reliefOrDiscount;

    /**
     * 使用门槛类型 （0 无门槛 1 价格门槛 2 商品件数门槛）
     */
    private Integer doorsillType;

    /**
     * 门槛金额/件数
     */
    private BigDecimal doorsill;

    /**
     * 优惠券有效开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ExcelProperty("优惠券有效开始时间")
    private LocalDateTime couponStartTime;

    /**
     * 优惠券有效结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ExcelProperty("优惠券有效结束时间")
    private LocalDateTime couponEndTime;

    private String remark;

    private Integer isCommon;

    private Integer isCommonStore;

    private Integer couponType;

    private Integer userRestrictions;

    private Integer habit;

    private Integer useType;

    private String useTime;

    private Integer isGround;

    private Integer discount;

    private BigDecimal reduceAmount;

    private Integer fullReduction;
}
