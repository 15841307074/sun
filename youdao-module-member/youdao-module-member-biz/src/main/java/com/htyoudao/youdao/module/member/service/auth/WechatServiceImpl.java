package com.htyoudao.youdao.module.member.service.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.htyoudao.youdao.framework.common.enums.CommonStatusEnum;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.common.util.string.StringUtils;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.module.member.api.enums.ErrorCodeConstants;
import com.htyoudao.youdao.module.member.controller.admin.wecom.WecomApiUtil;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.AppAuthLoginReqVO;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.AppAuthLoginRespVO;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.SyncTokenReqVO;
import com.htyoudao.youdao.module.member.controller.app.wxmember.vo.WxMemberUpdateLoginTimeReqVO;
import com.htyoudao.youdao.module.member.dal.dataobject.wxmember.WxMemberDO;
import com.htyoudao.youdao.module.member.framework.app.core.WechatAppConfig;
import com.htyoudao.youdao.module.member.service.wxmember.WxMemberService;
import com.htyoudao.youdao.module.member.util.WechatAccessTokenUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * @author lqman
 */
@Service
@Slf4j
public class WechatServiceImpl implements WechatService {

    @Resource
    private WechatAppConfig wechatAppConfig;
    @Resource
    private WxMemberService wxMemberService;
    @Resource
    private AlipayService alipayService;

    private static final String URL = "https://api.weixin.qq.com/sns/jscode2session";
    private final static String PHONE_URL = "https://api.weixin.qq.com/wxa/business/getuserphonenumber?access_token=";

    private static final String USER_INFO_URL = "https://qyapi.weixin.qq.com/cgi-bin/idconvert/unionid_to_external_userid?debug=1&access_token=";

    @Resource
    private WecomApiUtil wecomApiUtil;


    @Override
    public AppAuthLoginRespVO login(AppAuthLoginReqVO reqVO) {
        Map<String, Object> params = new HashMap<>(16);
        params.put("appid", wechatAppConfig.getAppId(String.valueOf(reqVO.getBusinessId())));

        params.put("secret", wechatAppConfig.getAppSecret(String.valueOf(reqVO.getBusinessId())));
        // 这里的code是前端通过wx.login()获取到的
        params.put("js_code", reqVO.getCode());
        // 固定参数
        params.put("grant_type", "authorization_code");
        log.info("获取openId: {}", params);
        String resultJson = HttpUtil.get(URL, params);
        log.info("返回值: {}", resultJson);

        JSONObject json = JSONObject.parseObject(resultJson);
        log.info("======参数====={}============", params);
        log.info("======返回结果====={}============", resultJson);
        boolean isError = json.containsKey("errcode");
        if (isError) {
            log.warn("微信登录失败：{}", json.getString("errmsg"));
            throw exception(ErrorCodeConstants.AUTH_LOGIN_WECHAT_FAIL);
        }
        String openid = json.getString("openid");
        String unionid = json.getString("unionid");

        WxMemberDO wxMember = new WxMemberDO();
        wxMember.setOpenid(openid);
        wxMember.setState(CommonStatusEnum.ENABLE.getStatus());
        //wxMember.setErrandFlag(0);

        wxMember.setBusinessId(reqVO.getBusinessId());

        List<WxMemberDO> wxMemberList = wxMemberService.selectList(wxMember);

        HashMap<String, Object> userInfoParams = new HashMap<>(16);
        userInfoParams.put("openid",openid);
        userInfoParams.put("unionid",unionid);
//        userInfoParams.put("subject_type",0);

//        String externalUserId = "";
//        try {
//            String accessToken = wecomApiUtil.getAccessToken();
//            //String accessToken = "CLOUD_SECRET_REQUIRED";
//            CloseableHttpClient httpClient = HttpClients.createDefault();
//            HttpPost httpPost = new HttpPost(USER_INFO_URL  + accessToken);
//            JSONObject requestBody = new JSONObject();
//            requestBody.put("openid",openid);
//            requestBody.put("unionid",unionid);
//            StringEntity entity = new StringEntity(requestBody.toString(), "UTF-8");
//            httpPost.setEntity(entity);
//            httpPost.setHeader("Content-Type", "application/json");
//            CloseableHttpResponse response = httpClient.execute(httpPost);
//            String result = EntityUtils.toString(response.getEntity());
//            JSONObject jsonObject = JSON.parseObject(result);
//
//            log.info("externalUserId: {}", externalUserId);
//        }catch (Exception e){
//
//        }

        Long memberId;
        String mobile = "";
        if (CollUtil.isEmpty(wxMemberList)) {
            wxMember.setMemberAvatar(reqVO.getAvatarUrl());
            wxMember.setWxUnionid(unionid);
            wxMember.setWxAvatarImg(reqVO.getAvatarUrl());
            wxMember.setMemberNickName("微信用户");
            wxMember.setMemberCategory(0);
            memberId = wxMemberService.createWxMemberByMiniProgram(wxMember);
        } else {
            WxMemberDO wxMemberDO = wxMemberList.get(0);
            mobile = wxMemberDO.getMemberMobile();
            memberId = wxMemberDO.getMemberId();
            WxMemberUpdateLoginTimeReqVO wxMemberVO = new WxMemberUpdateLoginTimeReqVO();
            wxMemberVO.setMemberId(memberId);
            wxMemberVO.setOpenid(openid);
            wxMemberVO.setWxUnionid(unionid);
            wxMemberService.updateLastLoginTime(wxMemberVO);
        }

        return alipayService.createTokenAfterLoginSuccess(memberId, openid, mobile,unionid);
    }

    @Override
    public String getInfo(String code) {
        String userPhone;
        // 使用前端code获取手机号码（accessToken一定要以get的方式请求）其他参数为json格式
        HashMap<String, Object> tokenParam = new HashMap<>(16);
        tokenParam.put("businessId", BusinessContextHolder.getBusinessId());
        tokenParam.put("sourceType", "0090hbgc");
        String accessTokenBody = HttpRequest.get(wechatAppConfig.getTokenUrl())
                .header(Header.AUTHORIZATION, wechatAppConfig.getTokenAuth())
                .form(tokenParam)
                .execute().body();
        @SuppressWarnings("unchecked")
        CommonResult<String> commonResult = JsonUtils.parseObject(accessTokenBody, CommonResult.class);
        assert commonResult != null;
        String accessToken = commonResult.getCheckedData();
        HashMap<String, Object> phoneParams = new HashMap<>(16);
        phoneParams.put("code", code);
        String phoneBody = HttpRequest.post(PHONE_URL + accessToken)
                .body(JsonUtils.toJsonString(phoneParams))
                .execute().body();
        JSONObject phoneJsonObject = JSONObject.parseObject(phoneBody, JSONObject.class);
        String errcode = phoneJsonObject.getString("errcode");
        String successCode = "0";
        if (!successCode.equals(errcode)) {
            throw exception(ErrorCodeConstants.AUTH_INFO_WECHAT_FAIL);
        }
        JSONObject phoneInfo = phoneJsonObject.getJSONObject("phone_info");
        // 以上都是response参数的处理 最终拿到userPhone 可以进行下一步 微信登陆了
        userPhone = phoneInfo.getString("phoneNumber");
        if (StringUtils.isEmpty(userPhone)) {
            throw exception(ErrorCodeConstants.AUTH_INFO_WECHAT_FAIL);
        }
        return userPhone;
    }

    @Override
    public void syncWechatToken(SyncTokenReqVO reqVO) {

    }
}
