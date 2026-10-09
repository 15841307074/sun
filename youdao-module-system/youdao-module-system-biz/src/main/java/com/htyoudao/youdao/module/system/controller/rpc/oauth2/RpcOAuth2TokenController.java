package com.htyoudao.youdao.module.system.controller.rpc.oauth2;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.api.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import com.htyoudao.youdao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.htyoudao.youdao.module.system.service.oauth2.OAuth2TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * @author lqman
 */
@Tag(name = "管理后台 - rpc OAuth2.0 令牌")
@RestController
@PermitAll
@RequestMapping("/rpc-api/system/oauth2/token")
public class RpcOAuth2TokenController {

    @Resource
    private OAuth2TokenService oauth2TokenService;


    @GetMapping("/check")
    @Operation(summary = "校验访问令牌")
    @Parameter(name = "accessToken", description = "访问令牌", required = true, example = "tudou")
    CommonResult<OAuth2AccessTokenCheckRespDTO> checkAccessToken(@RequestParam("accessToken") String accessToken){
            OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.checkAccessToken(accessToken);
            return success(BeanUtils.toBean(accessTokenDO, OAuth2AccessTokenCheckRespDTO.class));
    }

}
