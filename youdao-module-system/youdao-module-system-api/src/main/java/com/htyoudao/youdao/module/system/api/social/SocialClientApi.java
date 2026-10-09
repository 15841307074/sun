package com.htyoudao.youdao.module.system.api.social;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.social.dto.*;
import com.htyoudao.youdao.module.system.enums.ApiConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "RPC 服务 - 社交应用")
public interface SocialClientApi {

    @Operation(summary = "获得社交平台的授权 URL")
    @Parameters({
            @Parameter(name = "socialType", description = "社交平台的类型", example = "1", required = true),
            @Parameter(name = "userType", description = "用户类型", example = "1", required = true),
            @Parameter(name = "redirectUri", description = "重定向 URL", example = "https://www.iocoder.cn", required = true)
    })
    CommonResult<String> getAuthorizeUrl(@RequestParam("socialType") Integer socialType,
                                         @RequestParam("userType") Integer userType,
                                         @RequestParam("redirectUri") String redirectUri);

    @Operation(summary = "创建微信公众号 JS SDK 初始化所需的签名")
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", example = "1", required = true),
            @Parameter(name = "url", description = "访问 URL", example = "https://www.iocoder.cn", required = true)
    })
    CommonResult<SocialWxJsapiSignatureRespDTO> createWxMpJsapiSignature(@RequestParam("userType") Integer userType,
                                                                         @RequestParam("url") String url);

    @Operation(summary = "获得微信小程序的手机信息")
    @Parameters({
            @Parameter(name = "userType", description = "用户类型", example = "1", required = true),
            @Parameter(name = "phoneCode", description = "手机授权码", example = "youdao11", required = true)
    })
    CommonResult<SocialWxPhoneNumberInfoRespDTO> getWxMaPhoneNumberInfo(@RequestParam("userType") Integer userType,
                                                                        @RequestParam("phoneCode") String phoneCode);

    @Operation(summary = "获得小程序二维码")
    CommonResult<byte[]> getWxaQrcode(@SpringQueryMap SocialWxQrcodeReqDTO reqVO);

    @Operation(summary = "获得微信小程订阅模板")
    CommonResult<List<SocialWxaSubscribeTemplateRespDTO>> getWxaSubscribeTemplateList(@RequestParam("userType") Integer userType);

    @Operation(summary = "发送微信小程序订阅消息")
    CommonResult<Boolean> sendWxaSubscribeMessage(@Valid @RequestBody SocialWxaSubscribeMessageSendReqDTO reqDTO);

}
