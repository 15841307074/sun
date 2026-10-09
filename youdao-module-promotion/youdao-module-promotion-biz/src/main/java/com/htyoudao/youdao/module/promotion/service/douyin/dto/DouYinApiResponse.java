package com.htyoudao.youdao.module.promotion.service.douyin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DouYinApiResponse<T> {

    private T data;

    private ResponseExtra extra;


    @Data
    public static class ResponseExtra {

        @Schema(description = "错误码")
        @JsonProperty("error_code")
        private Integer errorCode;

        @Schema(description = "描述")
        @JsonProperty("description")
        private String description;

        @Schema(description = "子错误码")
        @JsonProperty("sub_error_code")
        private Integer subErrorCode;

        @Schema(description = "子描述")
        @JsonProperty("sub_description")
        private String subDescription;

        @Schema(description = "抖音日志 id，可用于排查问题")
        @JsonProperty("logid")
        private String logid;

        @Schema(description = "调用时间，单位为s")
        @JsonProperty("now")
        private Long now;
    }
}
