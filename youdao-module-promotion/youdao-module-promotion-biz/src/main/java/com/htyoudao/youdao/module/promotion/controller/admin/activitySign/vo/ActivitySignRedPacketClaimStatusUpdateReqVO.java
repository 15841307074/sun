package com.htyoudao.youdao.module.promotion.controller.admin.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 管理后台/内部 - 签到活动红包领取状态更新 Request VO。 */
@Data
public class ActivitySignRedPacketClaimStatusUpdateReqVO {

  @Schema(description = "商户转账单号/红包单号 outBillNo", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "商户转账单号不能为空")
  private String outBillNo;

  @Schema(description = "红包领取状态：1未领取 2已领取 3已过期", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "红包领取状态不能为空")
  private Integer claimStatus;

  @Schema(description = "失败/过期原因，可为空")
  private String failReason;
}
