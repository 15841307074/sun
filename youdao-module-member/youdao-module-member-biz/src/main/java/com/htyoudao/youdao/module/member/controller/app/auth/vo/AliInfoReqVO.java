package com.htyoudao.youdao.module.member.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lqman
 */
@Schema(description = "APP - ali 获取info Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AliInfoReqVO {

    @Schema(description = "签名", requiredMode = Schema.RequiredMode.REQUIRED, example = "23841878")
    @NotEmpty(message = "签名不能为空")
    private String sign;

    @Schema(description = "响应", requiredMode = Schema.RequiredMode.REQUIRED, example = "23841878")
    @NotEmpty(message = "响应不能为空")
    private String response;

}
