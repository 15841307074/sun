package com.htyoudao.youdao.module.promotion.service.douyin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "验券准备响应")
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrepareResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = -5851216386037790132L;

    @Schema(description = "错误描述")
    private String description;

    @Schema(description = "错误码")
    @JsonProperty("error_code")
    private Integer errorCode;

    @Schema(description = "验券标识, 在验券接口传入")
    @JsonProperty("verify_token")
    private String verifyToken;

    @JsonProperty("order_id")
    private String orderId;

    @Schema(description = "可用券列表")
    private List<Certificate> certificates;

}