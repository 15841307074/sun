package com.htyoudao.youdao.module.system.service.oauth2;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.module.system.controller.admin.oauth2.vo.token.OAuth2AccessTokenPageReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;

import java.util.List;
import java.util.Map;

/**
 * OAuth2.0 Token Service 接口
 *
 * 从功能上，和 Spring Security OAuth 的 DefaultTokenServices + JdbcTokenStore 的功能，提供访问令牌、刷新令牌的操作
 *
 * @author 0090
 */
public interface OAuth2TokenService {

    /**
     * 创建访问令牌
     *
     * @param userId   用户id
     * @param userType 用户类型
     * @param clientId 客户端ID
     * @param scopes   范围
     * @param info     额外信息
     * @return {@link OAuth2AccessTokenDO }
     */
    OAuth2AccessTokenDO createAccessToken(Long userId, Integer userType, String clientId, List<String> scopes, Map<String, String> info);

    /**
     * 刷新访问令牌
     *
     * 参考 DefaultTokenServices 的 refreshAccessToken 方法
     *
     * @param refreshToken 刷新令牌
     * @param clientId 客户端编号
     * @return 访问令牌的信息
     */
    OAuth2AccessTokenDO refreshAccessToken(String refreshToken, String clientId);

    /**
     * 获得访问令牌
     *
     * 参考 DefaultTokenServices 的 getAccessToken 方法
     *
     * @param accessToken 访问令牌
     * @return 访问令牌的信息
     */
    OAuth2AccessTokenDO getAccessToken(String accessToken);

    /**
     * 校验访问令牌
     *
     * @param accessToken 访问令牌
     * @return 访问令牌的信息
     */
    OAuth2AccessTokenDO checkAccessToken(String accessToken);

    /**
     * 移除访问令牌
     * 注意：该流程中，会移除相关的刷新令牌
     *
     * 参考 DefaultTokenServices 的 revokeToken 方法
     *
     * @param accessToken 刷新令牌
     * @return 访问令牌的信息
     */
    OAuth2AccessTokenDO removeAccessToken(String accessToken);

    /**
     * 获得访问令牌分页
     *
     * @param reqVO 请求
     * @return 访问令牌分页
     */
    PageResult<OAuth2AccessTokenDO> getAccessTokenPage(OAuth2AccessTokenPageReqVO reqVO);

    /**
     * 更新用户换绑信息
     *
     * @param oAuth2AccessTokenDO 请求
     */
    void updateAccessToken(OAuth2AccessTokenDO oAuth2AccessTokenDO);

    /**
     * 清理token
     *
     * @param deleteLimit 删除限制
     * @return {@link Integer }
     */
    Integer cleanToken(Integer deleteLimit);
}
