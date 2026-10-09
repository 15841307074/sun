package com.htyoudao.youdao.module.promotion.service.douyin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "撤销核销响应")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CancelVerifyResponse {

    @Schema(description = "错误描述")
    private String description;

    @Schema(description = "错误码")
    @JsonProperty("error_code")
    private Integer errorCode;

}