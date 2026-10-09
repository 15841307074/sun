package com.htyoudao.youdao.module.member.controller.app.wxmember.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dht
 */
@Data
@Schema(description = "app - 修改用户最后登录时间入参 Request VO")
public class WxMemberUpdateLoginTimeReqVO {

    private Long memberId;

    private String openid;

    /**
     * 微信用户统一标识
     */
    private String wxUnionid;
}
