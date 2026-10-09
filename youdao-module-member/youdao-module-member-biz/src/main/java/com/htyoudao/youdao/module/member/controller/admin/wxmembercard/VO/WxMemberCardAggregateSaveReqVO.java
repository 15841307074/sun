package com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理后台 - 会员卡聚合保存 Request VO")
public class WxMemberCardAggregateSaveReqVO extends WxMemberCardEditReqVO {

    @Schema(description = "会员权益列表")
    @Valid
    private List<WxMemberCardBenefitItemSaveReqVO> memberBenefits = new ArrayList<>();

    @Schema(description = "会员生日礼列表")
    @Valid
    private List<WxMemberCardBenefitItemSaveReqVO> birthdayBenefits = new ArrayList<>();

    @Schema(description = "会员升级权益列表；修改时不传表示保留，传空数组表示清空")
    @Valid
    private List<WxMemberCardBenefitItemSaveReqVO> upgradeBenefits;
}
