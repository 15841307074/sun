package com.htyoudao.youdao.module.promotion.controller.admin.lottery.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "管理后台 - 原抽奖人工补发请求")
public class LotteryReissueReqVO {
    @NotNull @Positive
    @Schema(description = "原活动主表 ID，兼容抽奖配置 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long lotteryId;
    @NotNull @Positive
    @Schema(description = "原抽奖会员 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long memberId;
    @NotNull @Positive
    @Schema(description = "原抽奖门店 ID，必须与流水一致", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long storeId;
    @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{8,64}")
    @Schema(description = "原抽奖 requestId，不能生成新 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String requestId;
    @NotBlank @Size(max = 200)
    @Schema(description = "人工补发原因，记录到操作日志", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reason;
}
