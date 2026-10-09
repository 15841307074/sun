package com.htyoudao.youdao.module.member.controller.admin.wxmember.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "个人中心用户管理 - 会员信息 Response VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WxMemberRegisterRepVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "会员id")
    private Long memberId;

    @Schema(description = "用户名（登录名称）")
    private String memberName;

    @Schema(description = "会员昵称")
    private String memberNickName;

    @Schema(description = "手机号")
//    @Mobile
    private String memberMobile;

    @Schema(description = "用户头像")
    private String memberAvatar;

    @Schema(description = "性别：0、保密；1、男；2、女")
    private Integer gender;

    @Schema(description = "微信用户标识")
    private String openid;

    @Schema(description = "微信用户头像")
    private String wxAvatarImg;

    @Schema(description = "生日")
    private String memberBirthday;

    @Schema(description = "是否换绑: 1 换绑 2 不换绑")
    private Integer changeBinding ;

    @Schema(description = "绑定次数")
    private Integer bindingCount;
}
