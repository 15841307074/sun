package com.htyoudao.youdao.module.member.service.wxmember;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberRegisterRepVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberRespVO;
import com.htyoudao.youdao.module.member.util.AjaxResult;


/**
 * 会员 Service 接口
 *
 * @author 零零玖零
 */
public interface WxMemberManagerService {

    /**
     * 会员注册
     *
     * @param wxMemberRegisterRepVO wx成员
     * @return {@link CommonResult }
     */
    CommonResult<WxMemberRespVO> register(WxMemberRegisterRepVO wxMemberRegisterRepVO, String token);

    CommonResult<WxMemberRespVO> show(WxMemberRegisterRepVO wxMemberRegisterRepVO);
}
