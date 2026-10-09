package com.htyoudao.youdao.module.member.controller.admin.wxmember;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.htyoudao.youdao.framework.apilog.core.annotation.ApiAccessLog;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.module.member.api.wxmember.dto.MemberOrderDTO;
import com.htyoudao.youdao.module.member.api.wxmember.vo.WxMemberDataVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberRegisterRepVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberReqVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberRespVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberSendReqVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.MemberHistoryOrderRebuildReqVO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.member.service.job.JobService;
import com.htyoudao.youdao.module.member.service.wxmember.WxMemberManagerService;
import com.htyoudao.youdao.module.member.service.wxmember.WxMemberService;
import com.htyoudao.youdao.module.member.util.AccessTimeRestrictionUtil;
import com.htyoudao.youdao.module.member.util.AjaxResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.htyoudao.youdao.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * 微信小程序用户管理Controller
 *
 * @author lbw
 * */


@Tag(name = "我的 - 用户管理")
@RestController
@RequestMapping("/member/wx")
@Validated
public class WxMemberController {

    @Resource
    private WxMemberService wxMemberService;

    @Resource
    private JobService jobService;


    @GetMapping("/listPage")
    @Operation(summary = "会员pc端查询")
    public CommonResult<PageResult<WxMemberRespVO>> listPage(WxMemberReqVO wxMemberReqVO) {
        return success(wxMemberService.selectListPage(wxMemberReqVO));
    }

    @PostMapping("/sendCoupon")
    @Operation(summary = "批量发放优惠券")
    public CommonResult<Boolean> sendCoupon(@RequestBody WxMemberSendReqVO wxMemberSendReqVO) {
        return success(wxMemberService.sendCoupon(wxMemberSendReqVO));
    }

    @PostMapping("/export")
    @Operation(summary = "会员pc端导出")
    @PreAuthorize("@ss.hasPermission('system:user:export')")
    @ApiAccessLog(operateType = EXPORT)
    public CommonResult<String> exportUserList(@RequestBody WxMemberReqVO wxMemberReqVO, HttpServletRequest request, HttpServletResponse response) throws IOException {
        AccessTimeRestrictionUtil.checkAccessTime();
        wxMemberService.exportMemberList(wxMemberReqVO, request, response);
        return CommonResult.success("数据下载中,请稍后到下载管理中查看..");
    }


    /**
     * 更新最后一次下单时间
     */
    @PutMapping("/updateFinalOrderFinishTime")
    @Operation(summary = "更新最后一次下单时间")
    public CommonResult<MemberOrderDTO> updateFinalOrderFinishTime(@RequestParam(value = "memberId") Long memberId) {
        return success(wxMemberService.updateFinalOrderFinishTime(memberId));
    }


    @GetMapping("/listWechat")
    @Operation(summary = "全部微信会员列表")
    public CommonResult<List<WxMemberDataVO>> listWechat(long current,int size) {
        return wxMemberService.listWechat(current,size);
    }


//
    @PostMapping("/rebuildHistoryOrderMetricsByEs")
    @Operation(summary = "基于ES重建会员历史订单统计")
    @PermitAll
    @DataPermission(enable = false)
    public CommonResult<Boolean> rebuildHistoryOrderMetricsByEs(@Validated @RequestBody MemberHistoryOrderRebuildReqVO reqVO) throws IOException {
        jobService.rebuildHistoryOrderMetricsByEs(reqVO.getBusinessId(), Boolean.TRUE.equals(reqVO.getResetTotalOrderNum()));
        return success(true);
    }

//    @PostMapping("/nocHistoryOrderData")
//    @Operation(summary = "每晚更新用户下单历史数据 30 7 180 365 天的下单数 总金额 平均订单金额")
//    @PermitAll
//    @DataPermission(enable = false)
//    public CommonResult<Boolean> nocHistoryOrderData() {
//        BusinessContextHolder.setBusinessId(3L);
//        boolean b = wxMemberService.updateHistoryOrderData();
//        return CommonResult.success(b);
//    }
//
//    /**
//     * 每晚更新用户标签 1234567
//     */
//    @PostMapping("/updateMemberLabel")
//    @PermitAll
//    @DataPermission(enable = false)
//    @Operation(summary = "每晚更新用户标签 1234567")
//    public CommonResult<Boolean> updateMemberLabel() {
//        BusinessContextHolder.setBusinessId(3L);
//        Boolean b = wxMemberService.updateMemberLabel();
//        return CommonResult.success(b);
//    }
//
//    /**
//     * 每晚更新用户首单店铺id
//     */
//    @PostMapping("/updateFirstOrderStoreId")
//    @PermitAll
//    @DataPermission(enable = false)
//    @Operation(summary = "每晚更新用户首单店铺id")
//    public CommonResult<Boolean> updateFirstOrderStoreId() {
//        BusinessContextHolder.setBusinessId(3L);
//        wxMemberService.updateFirstOrderStoreId();
//        return CommonResult.success(true);
//    }
//
//    /**
//     * 每晚更新用户首单店铺id
//     */
//    @PostMapping("/updateMemberGroup")
//    @PermitAll
//    @Operation(summary = "测试一下是否进社群的定时任务")
//    public CommonResult<Boolean> updateMemberGroup() {
//        jobService.updateMemberGroup();
//        return CommonResult.success(true);
//    }



/*    @GetMapping("/test1")
    @Operation(summary = "全部微信会员列表")
    public CommonResult<List<Long>> listWechat1() {

        List<Long> notInCommunityList = wxMemberService.getNotInCommunityList("10");
        wxMemberService.getAllMemberId(10L);
        return CommonResult.success(wxMemberService.getCommunityList("10"));
    }*/
}
