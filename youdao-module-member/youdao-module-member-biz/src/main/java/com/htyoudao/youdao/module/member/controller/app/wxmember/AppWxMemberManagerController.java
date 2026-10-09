package com.htyoudao.youdao.module.member.controller.app.wxmember;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.security.core.util.SecurityFrameworkUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberRegisterRepVO;
import com.htyoudao.youdao.module.member.controller.admin.wxmember.vo.WxMemberRespVO;
import com.htyoudao.youdao.module.member.controller.app.wxmember.vo.*;
import com.htyoudao.youdao.module.member.dal.mysql.wxmember.WxMemberMapper;
import com.htyoudao.youdao.module.member.service.wxmember.WxMemberManagerService;
import com.htyoudao.youdao.module.member.service.wxmember.WxMemberService;
import com.htyoudao.youdao.module.system.api.sms.SmsCodeApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * 微信小程序用户管理Controller
 *
 * @author lbw
 * */


@Tag(name = "app - 用户")
@RestController
@RequestMapping("/member")
@Validated
public class AppWxMemberManagerController {

    private static final String HEAD_NAME = "Authorization";

    private static final String TOKEN_PARAMETER = "token";

    @Resource
    private WxMemberManagerService wxMemberManagerService;

    @Resource
    private WxMemberService wxMemberService;


    @PostMapping("/register")
    @Operation(summary = "会员注册")
    public CommonResult<WxMemberRespVO> register(@RequestBody @Valid WxMemberRegisterRepVO wxMemberRegisterRepVO, HttpServletRequest request) {
        // 请求头解析token
        String token = SecurityFrameworkUtils.obtainAuthorization(request, HEAD_NAME, TOKEN_PARAMETER);
        return wxMemberManagerService.register(wxMemberRegisterRepVO, token);
    }

    @PostMapping("/show")
    @Operation(summary = "个人中心会员详情显示")
    public CommonResult<WxMemberRespVO> show(@RequestBody WxMemberRegisterRepVO wxMemberRegisterRepVO, HttpServletRequest request) {
        return wxMemberManagerService.show(wxMemberRegisterRepVO);
    }

    @PostMapping("/updateLastLoginTime")
    @Operation(summary = "更新会员最后登录时间")
    public CommonResult<Void> updateLastLoginTime(@RequestBody WxMemberUpdateLoginTimeReqVO wxMember) {
        wxMemberService.updateLastLoginTime(wxMember);
        return CommonResult.success(null);
    }

    @PostMapping("/listByEntity")
    @Operation(summary = "查询用户信息")
    public CommonResult<WxMemberInfoRespVO> listByEntity(@RequestBody WxMemberInfoReqVO wxMember) {
        return CommonResult.success(wxMemberService.listByEntity(wxMember));
    }

    /**
     * 修改是否是第一次登录
     */
    @PutMapping("/updateFirstLogin")
    @Operation(summary = "修改是否是第一次登录")
    public CommonResult<Void> updateFirstLogin(@RequestBody WxMemberUpdateFirstLoginReqVO wxMember) {
        wxMemberService.updateFirstLogin(wxMember);
        return CommonResult.success(null);
    }

    /**
     * 保存订阅消息的会员信息
     * @param reqVO reqVO
     */
    @PostMapping("/saveNoticeReserveMember")
    public CommonResult<Boolean> saveNoticeReserveMember(@RequestBody NoticeReserveMemberReqVO reqVO) {
        return CommonResult.success(wxMemberService.saveNoticeReserveMember(reqVO));
    }



    /**
     * 设置交易密码。
     */
    @PostMapping("/tran-password/set")
    @Operation(summary = "设置交易密码")
    public CommonResult<Boolean> setTranPassword(@Valid @RequestBody SetTranPasswordReqVO reqVO) {
        boolean result = wxMemberService.setTranPassword(WebFrameworkUtils.getLoginUserId(),
                reqVO.getTranPassword(), reqVO.getConfirmPassword());
        return success(result);
    }

    /**
     * 检查是否已设置交易密码。
     */
    @GetMapping("/tran-password/check")
    @Operation(summary = "检查是否已设置交易密码")
    public CommonResult<Boolean> checkTranPasswordSet() {
        boolean result = wxMemberService.isTranPasswordSet(WebFrameworkUtils.getLoginUserId());
        return success(result);
    }

    /**
     * 通过手机号和验证码找回交易密码。
     */
    @PostMapping("/password/forget")
    @Operation(summary = "找回交易密码")
    public CommonResult<Boolean> forgetPassword(@Valid @RequestBody ForgetPasswordReqVO reqVO,
                                                HttpServletRequest request) {
        String ip = getClientIp(request);
        boolean result = wxMemberService.forgetPassword(
                reqVO.getMobile(), reqVO.getCode(),
                reqVO.getNewPassword(), reqVO.getConfirmPassword(), ip);
        return success(result);
    }

    /**
     * 修改交易密码。
     */
    @PostMapping("/password/update")
    @Operation(summary = "修改交易密码")
    public CommonResult<Boolean> updatePassword(@Valid @RequestBody UpdatePasswordReqVO reqVO) {
        boolean result = wxMemberService.updatePassword(WebFrameworkUtils.getLoginUserId(),
                reqVO.getOldPassword(), reqVO.getNewPassword(), reqVO.getConfirmPassword());
        return success(result);
    }


    /**
     * 修改交易密码。
     */
    @PostMapping("/password/check")
    @Operation(summary = "检查原密码是否正确")
    public CommonResult<Boolean> checkTranOldPassword(@Valid @RequestBody ChekPasswordReqVO reqVO) {
        boolean result = wxMemberService.checkTranOldPassword(WebFrameworkUtils.getLoginUserId(),
                reqVO.getOldPassword());
        return success(result);
    }

    /**
     * 获取客户端真实 IP。
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时，第一个 IP 通常是客户端真实 IP。
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }


}
