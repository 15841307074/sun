package com.htyoudao.youdao.module.promotion.controller.admin.activityJk.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivityJkExchangeExportRespVO {


    @ExcelProperty("用户唯一标识")
    @Schema(name = "memberId", description = "会员ID")
    private String memberId;

    @ExcelProperty("会员昵称")
    @Schema(name = "memberName", description = "会员昵称")
    private String memberName;

    /** 会员手机号 */
    @ExcelProperty("联系方式")
    @Schema(name = "memberMobile", description = "会员手机号")
    private String memberMobile;


    @ExcelProperty("奖品类型")
    @Schema(name = "prizeTypeName", description = "奖品类型 1 优惠卷 2 积分 3 实物 4 无奖品")
    private String prizeTypeName;

    @ExcelProperty("奖品内容")
    @Schema(name = "prizeName", description = "奖品内容")
    private String prizeName;


    @ExcelProperty("兑换时间")
    private LocalDateTime createTime;


    @ExcelProperty("红包状态")
    @Schema(name = "claimName", description = "红包状态")
    private String claimName;

    @ExcelProperty("收件人")
    @Schema(name = "receiveUser", description = "收件人")
    private String receiveUser;


    @ExcelProperty("联系方式")
    @Schema(name = "receiveMobile", description = "联系方式")
    private String receiveMobile;

    @ExcelProperty("收货地址")
    @Schema(name = "receiveAddress", description = "收货地址")
    private String receiveAddress;

    @ExcelProperty("快递单号")
    @Schema(name = "trackingNumber", description = "快递单号")
    private String trackingNumber;

    @ExcelProperty("兑换门店")
    @Schema(name = "storeName", description = "兑换门店")
    private String storeName;


}
