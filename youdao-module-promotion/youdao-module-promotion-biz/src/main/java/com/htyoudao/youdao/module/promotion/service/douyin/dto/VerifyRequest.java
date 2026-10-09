package com.htyoudao.youdao.module.promotion.service.douyin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "核销券请求参数")
public class VerifyRequest {

    @Schema(description = "加密券码", requiredMode = Schema.RequiredMode.REQUIRED, example = "I0ZwZEZpb...")
    @NotEmpty
    private List<String> encryptedCodes;

    @Schema(description = "核销凭证令牌", requiredMode = Schema.RequiredMode.REQUIRED, example = "token123")
    @NotBlank
    private String verifyToken;

    @Schema(description = "抖音门店ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "ROLUhTcxyP")
    @NotBlank
    private String poiId;
}
