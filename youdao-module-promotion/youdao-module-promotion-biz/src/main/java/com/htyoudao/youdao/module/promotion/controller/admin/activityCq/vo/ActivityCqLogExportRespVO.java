package com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityCqLogExportRespVO {

    /**
     * 会员昵称
     */
    @ExcelProperty("会员昵称")
    @Schema(description = "会员昵称")
    private String memberName;

    /**
     * 联系方式
     */
    @ExcelProperty("联系方式")
    @Schema(description = "联系方式")
    private String memberMobile;

    /**
     * 性别
     */
    @ExcelProperty("性别")
    @Schema(description = "性别：0、保密；1、男；2、女")
    private Integer gender;

    /**
     * 用户类别
     */
    @ExcelProperty("用户类别")
    @Schema(description = "用户类别 0 微信 1支付宝")
    private Integer memberCategory;

    /**
     * 获得签码
     */
    @ExcelProperty("获得签码")
    @Schema(description = "获得签码")
    private String signCode;

    /**
     * 获取方式
     */
    @ExcelProperty("获取方式")
    @Schema(description = "获取方式")
    private String obtainTypeName;

    /**
     * 抽签时间
     */
    @ExcelProperty("抽签时间")
    @Schema(description = "抽签时间")
    private LocalDateTime drawTime;

    /**
     * 抽签门店
     */
    @ExcelProperty("抽签门店")
    @Schema(description = "抽签门店")
    private String storeName;

    /**
     * 结果状态
     */
    @ExcelProperty("结果状态")
    @Schema(description = "结果状态")
    private String resultStatusName;
}
