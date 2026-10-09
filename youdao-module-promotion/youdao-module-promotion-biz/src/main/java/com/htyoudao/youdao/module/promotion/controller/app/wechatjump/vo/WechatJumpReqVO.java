package com.htyoudao.youdao.module.promotion.controller.app.wechatjump.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Schema(description = "微信跳转入参 VO")
@Data
public class WechatJumpReqVO {

    private String path;

    private String query;

    private Long projectOwnerShip;

    private String sign;

    private String timestamp;
}


