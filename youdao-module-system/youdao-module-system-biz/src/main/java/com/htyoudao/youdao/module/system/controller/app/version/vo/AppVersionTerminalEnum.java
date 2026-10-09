package com.htyoudao.youdao.module.system.controller.app.version.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "版本查询端枚举")
public enum AppVersionTerminalEnum {

    @Schema(description = "小程序")
    APPLET,
    @Schema(description = "点餐机")
    ORDER_MACHINE
}
