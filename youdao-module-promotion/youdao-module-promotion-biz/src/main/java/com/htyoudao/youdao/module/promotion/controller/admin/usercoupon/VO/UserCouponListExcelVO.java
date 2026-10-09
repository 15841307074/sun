package com.htyoudao.youdao.module.promotion.controller.admin.usercoupon.VO;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.bigdecimal.BigDecimalStringConverter;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.promotion.util.converter.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jodd.typeconverter.impl.BigDecimalConverter;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @author dht
 */
@Data
public class UserCouponListExcelVO {

    @ExcelProperty(value = "会员名称")
    private String memberName;

    @ExcelProperty(value = "联系方式")
    private String memberMobile;

    @ExcelProperty(value = "用户类型",converter = UserRestrictionsConverter.class)
    private Integer userRestrictions;

    @ExcelProperty(value = "使用状态",converter = UserCouponStatusConverter.class)
    private Integer couponStatus;

    @ExcelProperty(value = "领取门店")
    private String storeName;

    @ExcelProperty(value = "领取时间",converter = LocalDateTimeConverter.class)
    private LocalDateTime couponCreateTime;

    @ExcelProperty(value = "优惠券有效期")
    private String validityPeriod;

    @ExcelProperty(value = "下单时间",converter = LocalDateTimeConverter.class)
    private LocalDateTime couponUseTime;

    @ExcelProperty(value = "消费金额",converter = CustomBigDecimalConverter.class)
    private BigDecimal payAmount;

    @ExcelProperty(value = "领券渠道",converter = CouponSourceConverter.class)
    private Integer couponSource;
}
