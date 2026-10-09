package com.htyoudao.youdao.module.promotion.controller.admin.activityCq.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityCqWinningExportRespVO {

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
     * 奖品类型
     */
    @ExcelProperty("奖品类型")
    @Schema(description = "奖品类型")
    private String prizeTypeName;

    /**
     * 奖品内容
     */
    @ExcelProperty("奖品内容")
    @Schema(description = "奖品内容")
    private String prizeContent;

    /**
     * 奖品图片
     */
    @ExcelProperty("奖品图片")
    @Schema(description = "奖品图片")
    private String prizeImgUrl;

    /**
     * 红包状态
     */
    @ExcelProperty("红包状态")
    @Schema(description = "红包状态")
    private String redPacketStatusName;

    /**
     * 收件人
     */
    @ExcelProperty("收件人")
    @Schema(description = "收件人")
    private String receiveUser;

    /**
     * 联系方式
     */
    @ExcelProperty("联系方式")
    @Schema(description = "联系方式")
    private String receiveMobile;

    /**
     * 收货地址
     */
    @ExcelProperty("收货地址")
    @Schema(description = "收货地址")
    private String receiveAddress;

    /**
     * 快递单号
     */
    @ExcelProperty("快递单号")
    @Schema(description = "快递单号")
    private String trackingNumber;

    /**
     * 快递公司
     */
    @ExcelProperty("快递公司")
    @Schema(description = "快递公司")
    private String expressCompany;

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
}
