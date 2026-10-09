package com.htyoudao.youdao.module.member.controller.app.wxmember.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Data
@Schema(description = "app - 用户信息更新是否是第一次登录 Response VO")
public class WxMemberUpdateFirstLoginReqVO {

    @Schema(description = "是否是第一次登录")
    private Integer isFirstLogin;

    @Schema(description = "memberId")
    private Long memberId;

    @Schema(description = "微信用户标识")
    private String openid;
}
