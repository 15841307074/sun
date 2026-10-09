package com.htyoudao.youdao.module.errand.controller.admin.errandRunnerWithdraw.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.converters.bigdecimal.BigDecimalStringConverter;
import com.alibaba.excel.converters.date.DateDateConverter;
import com.alibaba.excel.converters.localdatetime.LocalDateTimeDateConverter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.htyoudao.youdao.module.errand.converter.WithdrawStatusConverter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ErrandRunnerWithdrawExcelVO {

    @Schema(description = "姓名")
    @ExcelProperty(value = "跑腿员姓名")
    private String name;

    @Schema(description = "跑腿员电话")
    @ExcelProperty(value = "跑腿员电话")
    private String phone;

    @Schema(description = "跑腿员认证学校")
    @ExcelProperty(value = "跑腿员认证学校")
    private String storeName;

    @Schema(description = "跑腿员学号")
    @ExcelProperty(value = "跑腿员学号")
    private String studentNo;


    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ExcelProperty(value = "提现时间")
    @Schema(description = "提现时间")
    private Date finishTime;

    @Schema(description = "提现状态 状态：状态：0待处理 1处理中 2成功 3失败")
    @ExcelProperty(value = "提现状态",converter = WithdrawStatusConverter.class)
    private Integer status;

    @Schema(description = "提现金额")
    @ExcelProperty(value = "提现金额",converter = BigDecimalStringConverter.class)
    private BigDecimal amount;

    @Schema(description = "当前余额")
    @ExcelProperty(value = "当前余额",converter = BigDecimalStringConverter.class)
    private BigDecimal balance;
}
