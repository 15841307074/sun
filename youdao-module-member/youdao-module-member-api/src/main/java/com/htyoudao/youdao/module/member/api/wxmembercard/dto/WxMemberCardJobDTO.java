package com.htyoudao.youdao.module.member.api.wxmembercard.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class WxMemberCardJobDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long memberCardId;

    private Integer memberLevel;

    private Integer minPointsThreshold;

    private Long maxPointsThreshold;

    private Long businessId;
}
