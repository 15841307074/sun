package com.htyoudao.youdao.module.promotion.service.douyin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "验券准备请求参数")
@Data
public class PrepareRequest {

    @Schema(description = "抖音短链(2选1)", example = "I0ZwZEZpb2...")
    private String shortLink;

    @Schema(description = "明文券码(2选1)", example = "N537Ubc4XT")
    private String code;

    @Schema(description = "抖音门店ID",requiredMode = Schema.RequiredMode.REQUIRED , example = "ROLUhTcxyP")
    @NotEmpty
    private String poiId;
}