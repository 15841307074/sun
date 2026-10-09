package com.htyoudao.youdao.module.promotion.controller.app.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "小程序 - 签到请求 Request VO")
@Data
public class AppActivitySignReqVO {

    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
    @NotNull(message = "活动ID不能为空")
    private Long activityId;

    @Schema(description = "门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "101")
    @NotNull(message = "门店ID不能为空")
    private Long storeId;
}
