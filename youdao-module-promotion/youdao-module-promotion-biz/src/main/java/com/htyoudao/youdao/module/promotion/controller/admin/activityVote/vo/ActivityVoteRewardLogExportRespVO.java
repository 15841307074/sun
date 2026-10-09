package com.htyoudao.youdao.module.promotion.controller.admin.activityVote.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ActivityVoteRewardLogExportRespVO {

    @ExcelProperty("会员昵称")
    private String memberName;

    @ExcelProperty("联系方式")
    private String memberMobile;

    @ExcelProperty("投票编号")
    private String id;

    @ExcelProperty("奖品类型")
    private String prizeTypeName;

    @ExcelProperty("奖品内容")
    private String prizeName;

    @ExcelProperty("奖品价值")
    private BigDecimal prizeValue;

    @ExcelProperty("发放时间")
    private LocalDateTime grantTime;

    @ExcelProperty("红包状态")
    private String claimStatusName;

    @ExcelProperty("收件人")
    private String receiveUser;

    @ExcelProperty("收货地址")
    private String receiveAddress;

    @ExcelProperty("快递公司")
    private String expressCompany;

    @ExcelProperty("快递单号")
    private String trackingNumber;
}
