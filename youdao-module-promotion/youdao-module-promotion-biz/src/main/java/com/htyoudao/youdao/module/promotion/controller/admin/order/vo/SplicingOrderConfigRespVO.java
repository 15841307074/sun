package com.htyoudao.youdao.module.promotion.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "拼单设置 - pc端拼单配置 Request VO")
@Data
public class SplicingOrderConfigRespVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 6427856317165028489L;

    @Schema(description = "banner图片")
    private String bannerUrl;

    @Schema(description = "拼单折扣")
    private String discount;
}
