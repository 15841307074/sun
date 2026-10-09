package com.htyoudao.youdao.module.promotion.controller.app.wechatjump.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Data
@Schema(description = "微信跳转出参 VO")
public class WechatJumpRespVO {

    private String path;
}
