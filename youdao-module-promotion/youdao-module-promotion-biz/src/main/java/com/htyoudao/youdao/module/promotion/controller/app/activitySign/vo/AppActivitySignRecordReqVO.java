package com.htyoudao.youdao.module.promotion.controller.app.activitySign.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "小程序 - 签到记录 Request VO")
@Data
public class AppActivitySignRecordReqVO {

    @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
    @NotNull(message = "活动ID不能为空")
    private Long activityId;
    @Schema(hidden = true, description = "后端调试参数：true 时跳过中间件，直接查询 MySQL")
    private Boolean forceMysql;

}
