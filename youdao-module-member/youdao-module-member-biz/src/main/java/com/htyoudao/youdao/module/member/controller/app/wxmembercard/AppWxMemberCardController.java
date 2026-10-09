package com.htyoudao.youdao.module.member.controller.app.wxmembercard;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO.WxMemberAndCardReqVO;
import com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO.WxMemberAndCardRespVO;
import com.htyoudao.youdao.module.member.controller.app.wxmembercard.VO.WxMemberIdRespVO;
import com.htyoudao.youdao.module.member.service.wxmember.WxMemberService;
import com.htyoudao.youdao.module.member.service.wxmembercard.IWxMemberCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member/member-card")
@Tag(name = "小程序会员卡",description = "小程序会员卡")
public class AppWxMemberCardController {

    @Resource
    private IWxMemberCardService wxMemberCardService;

    @Resource
    private WxMemberService wxMemberService;



    @Operation(summary = "微信小程序获取用户会员卡及信息")
    @PostMapping("/getMemberCardWithMemberId")
    public CommonResult<WxMemberAndCardRespVO> getMemberCardWithMemberId(@RequestBody WxMemberAndCardReqVO reqVO) {
        WxMemberAndCardRespVO wxMemberVO = wxMemberCardService.getMemberCardWithMemberId(reqVO);
        return CommonResult.success(wxMemberVO);
    }

    @Operation(summary = "小程序获取会员卡 idList")
    @GetMapping("/getAllMemberCardIds")
    public CommonResult<List<WxMemberIdRespVO>> getAllMemberCardIds() {
        List<WxMemberIdRespVO> allMemberCardIds = wxMemberCardService.getAllMemberCardIds();
        return CommonResult.success(allMemberCardIds);
    }



}
