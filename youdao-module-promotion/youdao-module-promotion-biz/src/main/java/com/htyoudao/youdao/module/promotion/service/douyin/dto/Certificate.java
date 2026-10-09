package com.htyoudao.youdao.module.promotion.service.douyin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.htyoudao.youdao.module.promotion.service.douyin.constants.CertificateStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Certificate {

        @JsonProperty("certificate_id")
        @Schema(description = "加密券码", example = "I0ZwZEdxxxxx")
        private String certificateId;

        @JsonProperty("encrypted_code")
        @Schema(description = "加密券码", example = "I0ZwZEdxxxxx")
        private String encryptedCode;

        @JsonProperty("code")
        @Schema(description = "加密券码", example = "I0ZwZEdxxxxx")
        private String code;

        @JsonProperty("expire_time")
        @Schema(description = "券码有效期（截至时间）（秒时间戳）")
        private Long expireTime;

        @JsonProperty("start_time")
        @Schema(description = "券码有效期（开始时间）（秒时间戳）")
        private Long startTime;

        /**
         * 参见 {@link CertificateStatus}
         */
        @Schema(description = "券状态")
        private Integer status;

        @Schema(description = "sku")
        private Sku sku;

        @Data
        public static class Sku {

            @JsonProperty("account_id")
            private String accountId;

            @JsonProperty("voucher_type")
            private Integer voucherType;

            @JsonProperty("product_out_id")
            private String productOutId;

            @JsonProperty("product_id")
            private String productId;

            @JsonProperty("sku_id")
            private String skuId;

            @JsonProperty("sold_start_time")
            private Long soldStartTime;

            @JsonProperty("title")
            private String title;

            @JsonProperty("market_price")
            private Long marketPrice;

            @JsonProperty("sku_out_id")
            private String skuOutId;

            @JsonProperty("groupon_type")
            private Integer grouponType;

            @JsonProperty("third_sku_id")
            private String thirdSkuId;
        }

    }