package com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 会员卡聚合 Response VO")
public class WxMemberCardAggregateRespVO extends WxMemberCardPageRespVO {

    @Schema(description = "会员权益列表")
    private List<WxMemberCardBenefitItemRespVO> memberBenefits = new ArrayList<>();

    @Schema(description = "会员生日礼列表")
    private List<WxMemberCardBenefitItemRespVO> birthdayBenefits = new ArrayList<>();

    @Schema(description = "会员升级权益列表")
    private List<WxMemberCardBenefitItemRespVO> upgradeBenefits = new ArrayList<>();
}
