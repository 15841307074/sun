package com.htyoudao.youdao.module.system.api.oauth2;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.object.BeanUtils;
import com.htyoudao.youdao.module.system.api.oauth2.dto.OAuth2AccessTokenCheckRespDTO;
import com.htyoudao.youdao.module.system.api.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import com.htyoudao.youdao.module.system.api.oauth2.dto.OAuth2AccessTokenRespDTO;
import com.htyoudao.youdao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.htyoudao.youdao.module.system.service.oauth2.OAuth2TokenService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

/**
 * @author lqman
 */
@DubboService
@Validated
public class OAuth2TokenApiImpl implements OAuth2TokenApi {


    @Resource
    private OAuth2TokenService oauth2TokenService;

    @Override
    public CommonResult<OAuth2AccessTokenRespDTO> createAccessToken(OAuth2AccessTokenCreateReqDTO reqDTO) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.createAccessToken(
                reqDTO.getUserId(), reqDTO.getUserType(), reqDTO.getClientId(), reqDTO.getScopes(), reqDTO.getInfo());
        return success(BeanUtils.toBean(accessTokenDO, OAuth2AccessTokenRespDTO.class));
    }

    @Override
    public CommonResult<OAuth2AccessTokenCheckRespDTO> checkAccessToken(String accessToken) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.checkAccessToken(accessToken);
        return success(BeanUtils.toBean(accessTokenDO, OAuth2AccessTokenCheckRespDTO.class));
    }

    @Override
    public CommonResult<OAuth2AccessTokenRespDTO> removeAccessToken(String accessToken) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.removeAccessToken(accessToken);
        return success(BeanUtils.toBean(accessTokenDO, OAuth2AccessTokenRespDTO.class));
    }

    @Override
    public CommonResult<OAuth2AccessTokenRespDTO> refreshAccessToken(String refreshToken, String clientId) {
        OAuth2AccessTokenDO accessTokenDO = oauth2TokenService.refreshAccessToken(refreshToken, clientId);
        return success(BeanUtils.toBean(accessTokenDO, OAuth2AccessTokenRespDTO.class));
    }

}
