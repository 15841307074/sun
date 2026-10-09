package com.htyoudao.youdao.module.member.service.auth;


import com.htyoudao.youdao.module.member.controller.app.auth.vo.AliInfoReqVO;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.AppAuthLoginReqVO;
import com.htyoudao.youdao.module.member.controller.app.auth.vo.AppAuthLoginRespVO;

/**
 * @author lqman
 */
public interface AlipayService {
    /**
     * 登录
     *
     * @param reqVO 请求视图对象
     * @return {@code AppAuthLoginRespVO }
     */
    AppAuthLoginRespVO login(AppAuthLoginReqVO reqVO);

    /**
     * 登录成功后创建令牌
     *
     * @param userId 用户id
     * @param openid 开放ID
     * @param mobile
     * @param unionid
     * @return {@link AppAuthLoginRespVO }
     */
    AppAuthLoginRespVO createTokenAfterLoginSuccess(Long userId, String openid, String mobile, String unionid);

    /**
     * 登录成功后创建令牌
     *
     * @param userId 用户id
     * @param openid 开放ID
     * @param mobile
     * @return {@link AppAuthLoginRespVO }
     */
    AppAuthLoginRespVO createTokenAfterLoginSuccess(Long userId, String openid, String mobile);

    /**
     * 获取信息
     *
     * @param reqVO 请求视图对象
     * @return {@code String }
     */
    String getInfo(AliInfoReqVO reqVO);
}
