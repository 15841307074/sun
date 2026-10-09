package com.htyoudao.youdao.module.infra.controller.app.time.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 系统时间 Response VO")
@Data
public class AppTimeRespVO {

    @Schema(description = "当前系统时间戳，单位毫秒")
    private Long currentTime;

}
