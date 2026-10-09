package com.htyoudao.youdao.module.member.controller.app.address.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Schema(description = "地址管理 - MemberId 获取 个人地址信息")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WxMemberAddressReqMemberVO implements Serializable {

    @Schema(description = "MemberId", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "memberId不能为空")
    private Long memberId;
}
