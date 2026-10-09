package com.htyoudao.youdao.module.member.api.wxmember.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 小程序会员
 * @author dht
 */
@Data
public class WxMemberCrowdDTO implements Serializable {

    /**
     * 会员id
     */
    private Long memberId;

    /**
     * 手机号
     */
    private String memberMobile;
}
