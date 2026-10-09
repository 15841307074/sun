package com.htyoudao.youdao.module.promotion.controller.admin.market.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author dht
 */
@Data
@Schema(name = "营销短信发送", description = "营销短信发送")
public class PreMessageReqVO {

    @Schema(description = "短信营销id")
    private Long id;

    @Schema(description = "电话号")
    private String mobile;

    /**
     * 手机号集合 后端测试用
     */
    @Schema(description = "手机号集合 后端测试用")
    private List<String> mobiles;
}
