package com.htyoudao.youdao.module.errand.controller.app.errandRunner.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 找回密码请求VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "找回密码请求参数")
public class ForgetPasswordReqVO {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", required = true)
    private String mobile;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", required = true)
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Schema(description = "新密码", required = true)
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", required = true)
    private String confirmPassword;
}
