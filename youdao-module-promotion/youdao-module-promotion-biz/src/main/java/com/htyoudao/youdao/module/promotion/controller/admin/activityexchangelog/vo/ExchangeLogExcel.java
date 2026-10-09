package com.htyoudao.youdao.module.promotion.controller.admin.activityexchangelog.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.converters.localdate.LocalDateDateConverter;
import com.alibaba.excel.converters.localdatetime.LocalDateTimeDateConverter;
import com.htyoudao.youdao.module.promotion.util.converter.AwardTypeConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import jodd.typeconverter.impl.LocalDateTimeConverter;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author dht
 */
@Data
public class ExchangeLogExcel {

    @Schema(description = "会员昵称", example = "赵六")
    @ExcelProperty(value = "会员昵称",index = 0)
    private String memberNickName;

    @Schema(description = "联系方式")
    @ExcelProperty(value = "联系方式",index = 1)
    private String memberMobile;

    @Schema(description = "奖品类型 0 优惠券 1券包", example = "1")
    @ExcelProperty(value = "奖品类型",index = 2,converter = AwardTypeConverter.class)
    private Integer awardType;

    @Schema(description = "奖品名称", example = "张三")
    @ExcelProperty(value = "奖品名称",index = 3)
    private String awardName;

    @Schema(description = "奖品图片")
    @ExcelProperty(value = "奖品图片",index = 4)
    private String awardPic;

    @ExcelProperty(value = "兑换时间",index = 5,converter = LocalDateTimeDateConverter.class)
    private LocalDateTime createTime;
}
