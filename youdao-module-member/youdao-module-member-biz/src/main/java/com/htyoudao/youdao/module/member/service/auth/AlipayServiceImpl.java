package com.htyoudao.youdao.module.member.service.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipayEncrypt;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.enums.UserTypeEnum;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.AliInfoReqVO;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.AppAuthLoginReqVO;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.AppAuthLoginRespVO;
import com.htyoudao.youdao.module.member.controller.app.wxmember.vo.WxMemberUpdateLoginTimeReqVO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.member.framework.app.core.AliAppConfig;
import com.htyoudao.youdao.module.member.service.wxmember.WxMemberService;
import com.htyoudao.youdao.module.system.api.oauth2.OAuth2TokenApi;
import com.htyoudao.youdao.module.system.api.oauth2.dto.OAuth2AccessTokenCreateReqDTO;
import com.htyoudao.youdao.module.system.api.oauth2.dto.OAuth2AccessTokenRespDTO;
import com.htyoudao.youdao.module.system.enums.oauth2.OAuth2ClientConstants;
import groovy.util.logging.Slf4j;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants.*;

/**
 * @author lqman
 */
@Service
@Slf4j
public class AlipayServiceImpl implements AlipayService {

    private static final Logger log = LoggerFactory.getLogger(AlipayServiceImpl.class);
    @Resource
    private AliAppConfig aliAppConfig;
    @Resource
    private WxMemberService wxMemberService;
    @Resource
    private OAuth2TokenApi oAuth2TokenApi;

    private static final String URL = "https://openapi.alipay.com/gateway.do";

    @Override
    @SneakyThrows
    public AppAuthLoginRespVO login(AppAuthLoginReqVO reqVO) {
        Long businessId = reqVO.getBusinessId();
        String appId = aliAppConfig.getAppId(String.valueOf(businessId));
        String privateKey = aliAppConfig.getPrivateKey(String.valueOf(businessId));
        String alipayPublicKey = aliAppConfig.getAlipayPublicKey(String.valueOf(businessId));

        if (businessId == 10L && reqVO.getNewAil() == null) {
            Long newBusinessId = 100L;
            appId = aliAppConfig.getAppId(String.valueOf(newBusinessId));
            privateKey = aliAppConfig.getPrivateKey(String.valueOf(newBusinessId));
            alipayPublicKey = aliAppConfig.getAlipayPublicKey(String.valueOf(newBusinessId));
        }

        if (businessId == 2L && reqVO.getNewAil() == null) {
            Long newBusinessId = 200L;
            appId = aliAppConfig.getAppId(String.valueOf(newBusinessId));
            privateKey = aliAppConfig.getPrivateKey(String.valueOf(newBusinessId));
            alipayPublicKey = aliAppConfig.getAlipayPublicKey(String.valueOf(newBusinessId));
        }

        AlipayClient alipayClient = new DefaultAlipayClient(URL, appId, privateKey,
                "json",
                "UTF-8",
                alipayPublicKey,
                "RSA2"
        );

        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
        request.setGrantType("authorization_code");
        request.setCode(reqVO.getCode());

        AlipaySystemOauthTokenResponse response = alipayClient.execute(request);
        if (!response.isSuccess()) {
            log.warn("支付宝登录失败：{}", response.getBody());
            throw exception(AUTH_LOGIN_ALIPAY_FAIL);
        }

        String openid = response.getUserId();
        WxMemberDO wxMember = new WxMemberDO();
        wxMember.setOpenid(openid);
        wxMember.setState(CommonStatusEnum.ENABLE.getStatus());
        //wxMember.setErrandFlag(0);

        List<WxMemberDO> wxMemberList = wxMemberService.selectList(wxMember);
        Long memberId;
        String mobile = "";
        if (CollUtil.isEmpty(wxMemberList)) {
            wxMember.setMemberAvatar(reqVO.getAvatarUrl());
            wxMember.setWxUnionid(reqVO.getWxUnionid());
            wxMember.setWxAvatarImg(reqVO.getAvatarUrl());
            wxMember.setMemberNickName("支付宝用户");
            wxMember.setMemberCategory(1);
            memberId = wxMemberService.createWxMemberByMiniProgram(wxMember);
        } else {
            WxMemberDO wxMemberDO = wxMemberList.get(0);
            memberId = wxMemberDO.getMemberId();
            mobile = wxMemberDO.getMemberMobile();
            WxMemberUpdateLoginTimeReqVO wxMemberVO = new WxMemberUpdateLoginTimeReqVO();
            wxMemberVO.setMemberId(memberId);
            wxMemberVO.setOpenid(openid);
            wxMemberService.updateLastLoginTime(wxMemberVO);
        }

        return createTokenAfterLoginSuccess(memberId, openid, mobile);
    }

    @Override
    public AppAuthLoginRespVO createTokenAfterLoginSuccess(Long userId, String openid, String mobile, String unionid) {
        // 创建访问令牌
        OAuth2AccessTokenCreateReqDTO reqDTO = new OAuth2AccessTokenCreateReqDTO()
                .setClientId(OAuth2ClientConstants.CLIENT_ID_DEFAULT)
                .setUserId(userId)
                .setInfo(MapUtil.builder("openid", openid).put("unionid", unionid).put("mobile", mobile).build())
                .setUserType(UserTypeEnum.MEMBER.getValue());

        CommonResult<OAuth2AccessTokenRespDTO> accessTokenResult = oAuth2TokenApi.createAccessToken(reqDTO);
        OAuth2AccessTokenRespDTO accessTokenDTO = accessTokenResult.getCheckedData();

        // 构建返回结果
        return new AppAuthLoginRespVO()
                .setRefreshToken(accessTokenDTO.getRefreshToken())
                .setExpiresTime(accessTokenDTO.getExpiresTime())
                .setAccessToken(accessTokenDTO.getAccessToken())
                .setMobile(mobile)
                .setOpenid(openid)
                .setUserId(userId)
                .setUnionid(unionid);
    }


    @Override
    public AppAuthLoginRespVO createTokenAfterLoginSuccess(Long userId, String openid, String mobile) {
        // 创建访问令牌
        OAuth2AccessTokenCreateReqDTO reqDTO = new OAuth2AccessTokenCreateReqDTO()
                .setClientId(OAuth2ClientConstants.CLIENT_ID_DEFAULT)
                .setUserId(userId)
                .setInfo(MapUtil.builder("openid", openid).put("mobile", mobile).build())
                .setUserType(UserTypeEnum.MEMBER.getValue());

        CommonResult<OAuth2AccessTokenRespDTO> accessTokenResult = oAuth2TokenApi.createAccessToken(reqDTO);
        OAuth2AccessTokenRespDTO accessTokenDTO = accessTokenResult.getCheckedData();

        // 构建返回结果
        return new AppAuthLoginRespVO()
                .setRefreshToken(accessTokenDTO.getRefreshToken())
                .setExpiresTime(accessTokenDTO.getExpiresTime())
                .setAccessToken(accessTokenDTO.getAccessToken())
                .setMobile(mobile)
                .setOpenid(openid)
                .setUserId(userId);
    }

    @Override
    public String getInfo(AliInfoReqVO reqVO) {

        final String signType = "RSA2";
        final String charset = "UTF-8";
        final String encryptType = "AES";
        String sign = reqVO.getSign();
        String content = reqVO.getResponse();

        // 如果密文的
        boolean isDataEncrypted = !content.startsWith("{");
        boolean signCheckPass = false;

        Long businessId = BusinessContextHolder.getBusinessId();
        if (Objects.isNull(businessId)) {
            return "";
        }

        // 2. 验签
        String signContent = content;
        // 你的小程序对应的支付宝公钥（为扩展考虑建议用appId+signType做密钥存储隔离）
        // String signVeriKey = "";
        // 如果是加密的报文则需要在密文的前后添加双引号
        if (isDataEncrypted) {
            signContent = "\"" + signContent + "\"";
        }
        try {
            signCheckPass = AlipaySignature.rsaCheck(signContent, sign, aliAppConfig.getSignVeriKey(String.valueOf(businessId)), charset, signType);
        } catch (AlipayApiException e) {
            // 验签异常, 日志
            log.error("支付宝验签异常", e);
        }
        if (!signCheckPass) {
            // 验签不通过（异常或者报文被篡改），终止流程（不需要做解密）
            throw exception(AUTH_SIGN_ALIPAY_FAIL);
        }
        // 3. 解密
        String plainData;
        if (isDataEncrypted) {
            try {
                plainData = AlipayEncrypt.decryptContent(content, encryptType, aliAppConfig.getDecryptKey(String.valueOf(businessId)), charset);
            } catch (AlipayApiException e) {
                // 解密异常, 记录日志
                throw exception(AUTH_ENCRYPT_ALIPAY_FAIL);
            }
        } else {
            plainData = content;
        }
        Map<String, String> map = JSONObject.parseObject(plainData, new TypeReference<>() {
        });
        return map.get("mobile");
    }
}
