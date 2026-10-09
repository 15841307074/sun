package com.htyoudao.youdao.module.system.api.auth;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.validation.ValidationUtils;
import com.htyoudao.youdao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.htyoudao.youdao.module.system.dal.redis.oauth2.OAuth2AccessTokenRedisDAO;
import com.htyoudao.youdao.module.system.service.oauth2.OAuth2TokenService;
import com.htyoudao.youdao.module.system.util.string.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.error;
import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.WX_MEMBER_MOBILE_BINDING_ERROR;
import static com.htyoudao.youdao.module.system.enums.ErrorCodeConstants.WX_MEMBER_MOBILE_ERROR;

@DubboService
@Slf4j
public class AuthImplApi implements AuthApi{

    private static final String MAP_KEY_MOBILE = "mobile";
    @Resource
    private OAuth2TokenService oAuth2TokenService;

    @Resource
    private OAuth2AccessTokenRedisDAO oauth2AccessTokenRedisDAO;
    @Override
    public CommonResult<Boolean> updateToken(String token, String mobile) {
        if(StringUtils.isBlank(mobile) || StringUtils.isBlank(token)){
            log.info("用户换绑失败 {}，{}",  mobile, token);
            return error(WX_MEMBER_MOBILE_BINDING_ERROR);
        }
        // mobile 格式校验
        boolean validation = ValidationUtils.isMobile(mobile);
        if(!validation){
            log.error("手机号格式有误，{}", mobile);
            return error(WX_MEMBER_MOBILE_ERROR);
        }
        // 是否发生换绑
        OAuth2AccessTokenDO oAuth2AccessTokenDO = oauth2AccessTokenRedisDAO.get(token);
        Map<String, String> userInfo = oAuth2AccessTokenDO.getUserInfo();
        String redisMobile = userInfo.get(MAP_KEY_MOBILE);
        if( mobile.equals(redisMobile)){
            log.info("用户无需换绑 {}",  mobile);
            return success(Boolean.TRUE);
        }
        log.info("用户开始进行手机号换绑 {}>{}", redisMobile, mobile);
        // 换绑 非会员 绘有图案换绑
        userInfo.put(MAP_KEY_MOBILE, mobile);
        oAuth2AccessTokenDO.setUserInfo(userInfo);
        // redis 刷新
        oauth2AccessTokenRedisDAO.set(oAuth2AccessTokenDO);
        // 数据库更新
        oAuth2TokenService.updateAccessToken(oAuth2AccessTokenDO);
        return success(Boolean.TRUE);
    }

    @Override
    public void updateTokenFirst(String token, String mobile) {
        // 是否发生换绑
        OAuth2AccessTokenDO oAuth2AccessTokenDO = oauth2AccessTokenRedisDAO.get(token);
        Map<String, String> userInfo = oAuth2AccessTokenDO.getUserInfo();
        String redisMobile = userInfo.get(MAP_KEY_MOBILE);
        if(!ObjectUtils.isEmpty(redisMobile)){
            return;
        }
        log.info("首次注册更新token {}>{}", redisMobile, mobile);
        // 换绑 非会员 绘有图案换绑
        userInfo.put(MAP_KEY_MOBILE, mobile);
        oAuth2AccessTokenDO.setUserInfo(userInfo);
        // redis 刷新
        oauth2AccessTokenRedisDAO.set(oAuth2AccessTokenDO);
        // 数据库更新
        oAuth2TokenService.updateAccessToken(oAuth2AccessTokenDO);
    }
}
