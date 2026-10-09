package com.htyoudao.youdao.module.member.api.wxmembercard;

import com.htyoudao.youdao.module.member.api.wxmembercard.dto.WxMemberCardBenefitJobDTO;
import com.htyoudao.youdao.module.member.api.wxmembercard.dto.WxMemberCardJobDTO;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "RPC 服务 - 会员卡")
public interface WxMemberCardApi {

    /**
     * 查询会员卡权益发放任务使用的有效会员卡配置。
     */
    List<WxMemberCardJobDTO> listMemberCardJobCards(Long businessId);

    /**
     * 查询会员卡权益发放任务使用的权益配置。
     */
    List<WxMemberCardBenefitJobDTO> listMemberCardJobBenefits(Long businessId, List<Long> memberCardIds);
}
