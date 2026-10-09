package com.htyoudao.youdao.module.member.api.wxmember.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>
 * 会员下单信息
 * </p>
 *
 * @author zhangjihe
 * @since 2025-05-11
 */
@Data
public class MemberOrderDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -8089281112137331823L;

    /**
     * 是否会员 0否 1是
     */
    private Integer isMember = 0;

    /**
     * 下单间隔天数
     */
    private Integer IntervalDays = 0;

    /**
     * 是否新客  0否 1是
     */
    private Integer isNew = 0;
}
