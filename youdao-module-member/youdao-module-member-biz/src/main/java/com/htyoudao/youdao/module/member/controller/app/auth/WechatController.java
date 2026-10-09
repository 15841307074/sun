package com.htyoudao.youdao.module.member.controller.app.auth;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.AppAuthLoginReqVO;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.AppAuthLoginRespVO;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.SyncTokenReqVO;
import com.htyoudao.youdao.module.member.service.auth.WechatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * @author lqman
 */
@Tag(name = "APP - 微信")
@RestController
@RequestMapping("/member/wechat")
@Validated
public class WechatController {
    @Resource
    private WechatService wechatService;

    @PostMapping("/login")
    @PermitAll
    @Operation(summary = "使用微信登录")
    public CommonResult<AppAuthLoginRespVO> login(@RequestBody @Valid AppAuthLoginReqVO reqVO) {
        return success(wechatService.login(reqVO));
    }


    @GetMapping("/get-info")
    @Operation(summary = "获取微信信息")
    @PermitAll
    public CommonResult<String> getInfo(@RequestParam @NotEmpty String code) {
        return success(wechatService.getInfo(code));
    }

    @PostMapping("/sync-wechat-token")
    @Operation(summary = "同步微信token")
    public CommonResult<Boolean> syncWechatToken(@RequestBody @Valid SyncTokenReqVO reqVO){
        wechatService.syncWechatToken(reqVO);
        return success(true);
    }

    @GetMapping("/get-wechat-token/{projectOwnerShip}")
    @Operation(summary = "获取微信token")
    public void getWechatToken(@PathVariable("projectOwnerShip") String projectOwnerShip){
        // commonService.getWechatToken(true, projectOwnerShip);
    }
}
