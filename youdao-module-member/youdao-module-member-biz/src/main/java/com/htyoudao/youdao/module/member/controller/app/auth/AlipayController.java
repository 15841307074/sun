package com.htyoudao.youdao.module.member.controller.app.auth;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.AliInfoReqVO;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.AppAuthLoginReqVO;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.AppAuthLoginRespVO;
import com.htyoudao.youdao.module.member.service.auth.AlipayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * @author lqman
 */
@Tag(name = "APP - 支付宝")
@RestController
@RequestMapping("/member/alipay")
@Validated
public class AlipayController {

    @Resource
    private AlipayService aliPayService;


    @PostMapping("/login")
    @PermitAll
    @Operation(summary = "使用支付宝登录")
    public CommonResult<AppAuthLoginRespVO> login(@RequestBody @Valid AppAuthLoginReqVO reqVO) {
        return success(aliPayService.login(reqVO));
    }

    @PostMapping("/get-info")
    @PermitAll
    @Operation(summary = "获取支付宝信息")
    public CommonResult<String> getInfo(@RequestBody @Valid AliInfoReqVO reqVO) {
        return success(aliPayService.getInfo(reqVO));
    }
}
