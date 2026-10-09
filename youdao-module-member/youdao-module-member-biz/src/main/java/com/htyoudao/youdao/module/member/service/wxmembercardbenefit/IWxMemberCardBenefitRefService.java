package com.htyoudao.youdao.module.member.service.wxmembercardbenefit;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardAggregateRespVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardAggregateSaveReqVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardBenefitPageReqVO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercardbenefit.WxMemberCardBenefitRefDO;

public interface IWxMemberCardBenefitRefService extends IService<WxMemberCardBenefitRefDO> {

    Long createBenefit(WxMemberCardAggregateSaveReqVO reqVO);

    Boolean updateBenefit(WxMemberCardAggregateSaveReqVO reqVO);

    Boolean deleteMemberCard(Long memberCardId);

    WxMemberCardAggregateRespVO getCardBenefitDetail(Long memberCardId);

    PageResult<WxMemberCardAggregateRespVO> pageBenefits(WxMemberCardBenefitPageReqVO reqVO);
}
