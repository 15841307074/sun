package com.htyoudao.youdao.module.promotion.controller.app.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "小程序 - 实物奖励填写地址 Request VO")
@Data
public class AppActivitySignAddressReqVO {

    @Schema(description = "奖励发放记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "700001")
    @NotNull(message = "奖励发放记录ID不能为空")
    private Long rewardRecordId;

    @Schema(description = "收件人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "收件人不能为空")
    private String receiveUser;

    @Schema(description = "收件联系方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "收件联系方式不能为空")
    private String receiveMobile;

    @Schema(description = "收件地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "收件地址不能为空")
    private String receiveAddress;
}
