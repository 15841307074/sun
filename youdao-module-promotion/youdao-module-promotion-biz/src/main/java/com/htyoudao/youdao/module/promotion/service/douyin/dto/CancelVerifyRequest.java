package com.htyoudao.youdao.module.promotion.service.douyin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "撤销核销请求参数")
public class CancelVerifyRequest {

    @Schema(description = "代表一张券码的标识(验券时返回)", required = true, example = "ROLUhTcxyP")
    @NotBlank
    private String certificateId;

    @Schema(description = "代表券码一次核销的唯一标识(验券时返回) (次卡撤销多次时请填0)",requiredMode = Schema.RequiredMode.REQUIRED, example = "verify_123456789")
    @NotBlank
    private String verifyId;

    @Schema(description = "所属的订单id", example = "verify_123456789")
    @NotBlank
    private String shopOrderId;

    @Schema(description = "cancelToken")
    private String cancelToken;


}