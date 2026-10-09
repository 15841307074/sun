package com.htyoudao.youdao.module.member.controller.admin.wxmembercard;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardAggregateRespVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardAggregateSaveReqVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmembercard.VO.WxMemberCardBenefitPageReqVO;
import com.htyoudao.youdao.module.member.service.wxmembercardbenefit.IWxMemberCardBenefitRefService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member/member-card")
@Tag(name = "会员卡",description = "会员卡")
public class WxMemberCardController {

    @Resource
    private IWxMemberCardBenefitRefService wxMemberCardBenefitRefService;

    @GetMapping("/page")
    @Operation(summary = "会员卡分页")
    public CommonResult<PageResult<WxMemberCardAggregateRespVO>> page(@Valid WxMemberCardBenefitPageReqVO reqVO) {
        return CommonResult.success(wxMemberCardBenefitRefService.pageBenefits(reqVO));
    }

    @GetMapping("/getById/{memberCardId}")
    @Operation(summary = "获取会员卡详情")
    public CommonResult<WxMemberCardAggregateRespVO> getById(@PathVariable("memberCardId") Long memberCardId) {
        return CommonResult.success(wxMemberCardBenefitRefService.getCardBenefitDetail(memberCardId));
    }

    @PreAuthorize("@ss.hasPermission('member:memberCard:update')")
    @PostMapping("/create")
    @Operation(summary = "新增会员卡")
    public CommonResult<Long> create(@Valid @RequestBody WxMemberCardAggregateSaveReqVO reqVO) {
        return CommonResult.success(wxMemberCardBenefitRefService.createBenefit(reqVO));
    }

    @PreAuthorize("@ss.hasPermission('member:memberCard:update')")
    @PutMapping("/update")
    @Operation(summary = "修改会员卡")
    public CommonResult<Boolean> update(@Valid @RequestBody WxMemberCardAggregateSaveReqVO reqVO) {
        return CommonResult.success(wxMemberCardBenefitRefService.updateBenefit(reqVO));
    }

    @PreAuthorize("@ss.hasPermission('member:memberCard:update')")
    @DeleteMapping("/delete")
    @Operation(summary = "删除会员卡")
    public CommonResult<Boolean> delete(@RequestParam("id") Long memberCardId) {
        return CommonResult.success(wxMemberCardBenefitRefService.deleteMemberCard(memberCardId));
    }
}
