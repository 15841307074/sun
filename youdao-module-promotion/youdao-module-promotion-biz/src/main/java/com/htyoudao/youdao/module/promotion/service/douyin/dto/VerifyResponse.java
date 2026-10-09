package com.htyoudao.youdao.module.promotion.service.douyin.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Schema(description = "核销券响应")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class VerifyResponse {

    @JsonProperty("verify_results")
    private List<VerifyResult> verifyResults;

    @JsonProperty("error_code")
    private Integer errorCode;

    @JsonProperty("description")
    private String description;




    @Data
    public static class VerifyResult {
        @JsonProperty("account_id")
        private String accountId;

        @JsonProperty("certificate_id")
        private String certificateId;

        @JsonProperty("code")
        private String code;

        @JsonProperty("msg")
        private String message;

        @JsonProperty("order_id")
        private String orderId;

        @JsonProperty("origin_code")
        private String originCode;

        @JsonProperty("result")
        private Integer result;

        @JsonProperty("verify_id")
        private String verifyId;
    }

}