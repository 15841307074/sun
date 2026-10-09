package com.htyoudao.youdao.module.member.controller.app.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author lqman
 */
@Schema(description = "APP - 登录 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAuthLoginRespVO {

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "访问令牌", requiredMode = Schema.RequiredMode.REQUIRED, example = "happy")
    private String accessToken;

    @Schema(description = "刷新令牌", requiredMode = Schema.RequiredMode.REQUIRED, example = "nice")
    private String refreshToken;

    @Schema(description = "过期时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime expiresTime;

    @Schema(description = "会员手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "138xxxxxxxx")
    private String mobile;

    @Schema(description = "第三方用户openid", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String openid;

    @Schema(description = "unionId", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private String unionid;

}
