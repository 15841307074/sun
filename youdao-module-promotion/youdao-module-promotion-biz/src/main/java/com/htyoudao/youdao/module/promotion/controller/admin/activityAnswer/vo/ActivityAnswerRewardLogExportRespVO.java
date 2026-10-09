package com.htyoudao.youdao.module.promotion.controller.admin.activityAnswer.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 有奖问答奖励发放记录导出响应。
 */
@Data
public class ActivityAnswerRewardLogExportRespVO {

    @ExcelProperty("会员昵称")
    private String memberName;

    @ExcelProperty("联系方式")
    private String memberMobile;

    @ExcelProperty("答题编号")
    private String answerNo;

    @ExcelProperty("奖品类型")
    private String prizeTypeName;

    @ExcelProperty("奖品内容")
    private String prizeName;

    @ExcelProperty("奖品图片")
    private String prizeImgUrl;

    @ExcelProperty("发放时间")
    private String grantTime;

    @ExcelProperty("红包状态")
    private String claimStatusName;

    @ExcelProperty("收件人")
    private String receiveUser;

    @ExcelProperty("收件人联系方式")
    private String receiveMobile;

    @ExcelProperty("收货地址")
    private String receiveAddress;

    @ExcelProperty("快递公司")
    private String expressCompany;

    @ExcelProperty("快递单号")
    private String trackingNumber;
}
