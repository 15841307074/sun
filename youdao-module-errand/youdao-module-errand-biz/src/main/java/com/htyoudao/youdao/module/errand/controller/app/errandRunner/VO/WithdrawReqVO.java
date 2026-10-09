package com.htyoudao.youdao.module.errand.controller.app.errandRunner.VO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 提现请求VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "提现请求参数")
public class WithdrawReqVO {



    @NotNull(message = "提现金额不能为空")
    @Positive(message = "提现金额必须大于0")
    @Schema(description = "提现金额", required = true)
    private BigDecimal amount;

    @NotBlank(message = "交易密码不能为空")
    @Schema(description = "交易密码", required = true)
    private String tranPassword;

    @Schema(name = "openId", description = "用户OpenID")
    private String openId;
}
