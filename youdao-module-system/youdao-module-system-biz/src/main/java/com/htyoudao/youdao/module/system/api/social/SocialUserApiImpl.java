package com.htyoudao.youdao.module.system.api.social;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.social.dto.SocialUserBindReqDTO;
import com.htyoudao.youdao.module.system.api.social.dto.SocialUserRespDTO;
import com.htyoudao.youdao.module.system.api.social.dto.SocialUserUnbindReqDTO;
import com.htyoudao.youdao.module.system.service.social.SocialUserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

import static com.htyoudao.youdao.framework.common.pojo.CommonResult.success;

@DubboService
@Validated
public class SocialUserApiImpl implements SocialUserApi {

    @Resource
    private SocialUserService socialUserService;

    @Override
    public CommonResult<String> bindSocialUser(SocialUserBindReqDTO reqDTO) {
        return success(socialUserService.bindSocialUser(reqDTO));
    }

    @Override
    public CommonResult<Boolean> unbindSocialUser(SocialUserUnbindReqDTO reqDTO) {
        socialUserService.unbindSocialUser(reqDTO.getUserId(), reqDTO.getUserType(),
                reqDTO.getSocialType(), reqDTO.getOpenid());
        return success(true);
    }

    @Override
    public CommonResult<SocialUserRespDTO> getSocialUserByUserId(Integer userType, Long userId, Integer socialType) {
        return success(socialUserService.getSocialUserByUserId(userType, userId, socialType));
    }

    @Override
    public CommonResult<SocialUserRespDTO> getSocialUserByCode(Integer userType, Integer socialType, String code, String state) {
        return success(socialUserService.getSocialUserByCode(userType, socialType, code, state));
    }

}
