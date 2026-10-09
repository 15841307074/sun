package com.htyoudao.youdao.module.errand.controller.app.errandRunner.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设置交易密码请求VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设置交易密码请求参数")
public class SetTranPasswordReqVO {



    @NotBlank(message = "交易密码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "交易密码必须为6位数字")
    @Schema(description = "交易密码", required = true)
    private String tranPassword;

    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", required = true)
    private String confirmPassword;
}
