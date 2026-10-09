package com.htyoudao.youdao.module.member.api.wxmembercard.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class WxMemberCardBenefitJobDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long memberCardId;

    private Integer benefitScene;

    private Integer couponType;

    private Long couponId;

    private Integer sendNum;

    private Integer repeatType;

    private Integer issueValue;

    private Long businessId;
}
