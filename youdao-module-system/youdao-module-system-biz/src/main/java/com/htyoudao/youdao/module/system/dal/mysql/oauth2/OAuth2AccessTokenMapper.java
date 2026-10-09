package com.htyoudao.youdao.module.system.dal.mysql.oauth2;

import com.htyoudao.youdao.framework.common.pojo.PageResult;
import com.htyoudao.youdao.framework.mybatis.core.mapper.BaseMapperX;
import com.htyoudao.youdao.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.htyoudao.youdao.framework.tenant.core.aop.TenantIgnore;
import com.htyoudao.youdao.module.system.controller.admin.oauth2.vo.token.OAuth2AccessTokenPageReqVO;
import com.htyoudao.youdao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OAuth2AccessTokenMapper extends BaseMapperX<OAuth2AccessTokenDO> {

    @TenantIgnore // 获取 token 的时候，需要忽略租户编号。原因是：一些场景下，可能不会传递 tenant-id 请求头，例如说文件上传、积木报表等等
    default OAuth2AccessTokenDO selectByAccessToken(String accessToken) {
        return selectOne(OAuth2AccessTokenDO::getAccessToken, accessToken);
    }

    default List<OAuth2AccessTokenDO> selectListByRefreshToken(String refreshToken) {
        return selectList(OAuth2AccessTokenDO::getRefreshToken, refreshToken);
    }

    default PageResult<OAuth2AccessTokenDO> selectPage(OAuth2AccessTokenPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OAuth2AccessTokenDO>()
                .eqIfPresent(OAuth2AccessTokenDO::getUserId, reqVO.getUserId())
                .eqIfPresent(OAuth2AccessTokenDO::getUserType, reqVO.getUserType())
                .likeIfPresent(OAuth2AccessTokenDO::getClientId, reqVO.getClientId())
                .gt(OAuth2AccessTokenDO::getExpiresTime, LocalDateTime.now())
                .orderByDesc(OAuth2AccessTokenDO::getId));
    }

    /**
     * 按过期时间删除access token
     *
     * @param expireDate 到期日
     * @param limit      限制
     * @return int
     */
    @Delete("DELETE FROM system_oauth2_access_token WHERE expires_time < #{expireDate} LIMIT #{limit}")
    int deleteByExpireDateLt(@Param("expireDate") LocalDateTime expireDate, @Param("limit") Integer limit);}
