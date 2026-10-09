package com.htyoudao.youdao.module.member.controller.app.wxmember.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码请求VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "原密码请求参数")
public class ChekPasswordReqVO {


    @NotBlank(message = "原密码不能为空")
    @Schema(description = "原密码", required = true)
    private String oldPassword;

}
