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
@Schema(description = "APP - 登录 Request VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAuthLoginReqVO {

    @Schema(description = "微信code", requiredMode = Schema.RequiredMode.REQUIRED, example = "23841878")
    @NotEmpty(message = "微信code不能为空")
    private String code;

    @Schema(description = "微信号", requiredMode = Schema.RequiredMode.REQUIRED, example = "23841878")
    private String wxUnionid;

    @Schema(description = "微信头像", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "http://youdao.com/xx.img")
    private String avatarUrl;

    @Schema(description = "微信昵称", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "youdao")
    private String nickName;

    @Schema(description = "性别", requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "0")
    private Integer gender;

    @Schema(description = "项目所有者", requiredMode = Schema.RequiredMode.REQUIRED, example = "23841878")
    @NotNull(message = "项目所有者不能为空")
    private Long businessId;

    /**
     * 兼容老支付宝小程序
     */
    private Integer newAil;

}
