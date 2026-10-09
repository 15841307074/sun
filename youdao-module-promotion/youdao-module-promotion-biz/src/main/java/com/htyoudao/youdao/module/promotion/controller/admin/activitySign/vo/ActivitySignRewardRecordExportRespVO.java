package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/** 管理后台 - 奖励发放记录导出 Response VO。 */
@Data
public class ActivitySignRewardRecordExportRespVO {

  @ExcelProperty("会员昵称")
  @Schema(description = "会员昵称")
  private String memberName;

  @ExcelProperty("联系方式")
  @Schema(description = "联系方式")
  private String memberMobile;

  @ExcelProperty("奖励类型")
  @Schema(description = "奖励类型")
  private String prizeTypeName;

  @ExcelProperty("奖品内容")
  @Schema(description = "奖品内容")
  private String prizeContent;

  @ExcelProperty("奖品图片")
  @Schema(description = "奖品图片")
  private String prizeImgUrl;

  @ExcelProperty("发放时间")
  @Schema(description = "发放时间")
  private LocalDateTime issueTime;

  @ExcelProperty("发放状态")
  @Schema(description = "发放状态")
  private String issueStatusName;

  @ExcelProperty("红包状态")
  @Schema(description = "红包状态")
  private String claimStatusName;

  @ExcelProperty("收件人")
  @Schema(description = "收件人")
  private String receiveUser;

  @ExcelProperty("收件联系方式")
  @Schema(description = "收件联系方式")
  private String receiveMobile;

  @ExcelProperty("收货地址")
  @Schema(description = "收货地址")
  private String receiveAddress;

  @ExcelProperty("快递单号")
  @Schema(description = "快递单号")
  private String trackingNumber;

  @ExcelProperty("快递公司")
  @Schema(description = "快递公司")
  private String expressCompany;

  @ExcelProperty("发放门店")
  @Schema(description = "发放门店")
  private String storeName;
}
