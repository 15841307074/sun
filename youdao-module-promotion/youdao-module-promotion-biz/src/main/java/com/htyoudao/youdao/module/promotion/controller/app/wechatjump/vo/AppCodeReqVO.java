package com.htyoudao.youdao.module.promotion.controller.app.wechatjump.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "不限制的小程序码 VO")
@Data
public class AppCodeReqVO {

    @NotNull(message = "scene不能为空")
    private String scene;

    @NotNull(message = "page不能为空")
    private String page;
}


