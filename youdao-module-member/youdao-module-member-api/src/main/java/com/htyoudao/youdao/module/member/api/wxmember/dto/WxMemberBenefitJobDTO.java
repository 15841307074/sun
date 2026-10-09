package com.htyoudao.youdao.module.member.api.wxmember.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会员卡权益任务专用会员 DTO，只保留任务链路实际使用的字段。
 */
@Data
public class WxMemberBenefitJobDTO implements Serializable {

    private Long memberId;

    private String memberMobile;

    private String memberNickName;

    private Long integralFrozen;

    private Integer memberLevel;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JSONField(format = "yyyy-MM-dd")
    private Date memberBirthday;

    private Long businessId;

    private Integer shardingValue;
}
