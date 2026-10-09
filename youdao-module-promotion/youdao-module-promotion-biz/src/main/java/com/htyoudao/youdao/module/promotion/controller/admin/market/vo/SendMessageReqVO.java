package com.htyoudao.youdao.module.promotion.controller.admin.market.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author dht
 */
@Data
@Schema(name = "营销短信发送", description = "营销短信发送")
public class SendMessageReqVO {

    @Schema(description = "短信营销id")
    private Long id;

    @Schema(description = "电话号")
    private String mobile;

    /**
     * 手机号集合 后端测试用
     */
    @Schema(description = "手机号集合 后端测试用")
    private List<String> mobiles;

    /**
     * 发送方式 0 立即发送 1定时发送
     */
    @Schema(description = "发送方式 0 立即发送 1定时发送")
    private Integer sendMethod;

    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
}
