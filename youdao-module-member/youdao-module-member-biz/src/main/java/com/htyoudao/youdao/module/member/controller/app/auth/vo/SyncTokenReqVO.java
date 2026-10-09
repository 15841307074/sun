package com.htyoudao.youdao.module.member.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class SyncTokenReqVO {

    @Schema(description = "token", requiredMode = Schema.RequiredMode.REQUIRED, example = "23841878")
    @NotEmpty(message = "token不能为空")
    private String token;

    @Schema(description = "项目所有者", requiredMode = Schema.RequiredMode.REQUIRED, example = "23841878")
    @NotNull(message = "项目所有者不能为空")
    private Long projectOwnerShip;

}
