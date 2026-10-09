package com.htyoudao.youdao.module.member.service.auth;


import com.htyoudao.youdao.module.member.controller.app.auth.vo.AppAuthLoginReqVO;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.AppAuthLoginRespVO;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.SyncTokenReqVO;

/**
 * @author lqman
 */
public interface WechatService {

    /**
     * 登录
     *
     * @param reqVO 请求视图对象
     * @return {@code AppAuthLoginRespVO }
     */
    AppAuthLoginRespVO login(AppAuthLoginReqVO reqVO);

    /**
     * 获取信息
     *
     * @param code 代码
     * @return {@code String }
     */
    String getInfo(String code);

    /**
     * 同步微信令牌
     *
     * @param reqVO 请求视图对象
     */
    void syncWechatToken(SyncTokenReqVO reqVO);
}
