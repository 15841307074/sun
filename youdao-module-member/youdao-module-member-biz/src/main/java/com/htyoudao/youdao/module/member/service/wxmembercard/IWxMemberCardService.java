package com.htyoudao.youdao.module.member.service.wxmembercard;


import com.baomidou.mybatisplus.extension.service.IService;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardEditReqVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardPageRespVO;
import com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO.WxMemberAndCardReqVO;
import com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO.WxMemberAndCardRespVO;
import com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO.WxMemberIdRespVO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmembercard.WxMemberCardDO;
import jakarta.validation.Valid;

import java.util.List;


public interface IWxMemberCardService extends IService<WxMemberCardDO> {


    PageResult<WxMemberCardPageRespVO> listPage(long pageNum, long pageSize);

    boolean edit(@Valid WxMemberCardEditReqVO wxMemberCard);

    WxMemberAndCardRespVO getMemberCardWithMemberId(WxMemberAndCardReqVO reqVO);

    List<WxMemberIdRespVO> getAllMemberCardIds();
}
