package com.htyoudao.youdao.module.member.api.wxmember.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author dht
 */
@Data
public class WxMemberDayDTO implements Serializable {

    /**
     * 会员id
     */
    private Long memberId;

    /**
     * 手机号
     */
    private String memberMobile;

    /** 会员昵称 */
    private String memberNickName;

}
