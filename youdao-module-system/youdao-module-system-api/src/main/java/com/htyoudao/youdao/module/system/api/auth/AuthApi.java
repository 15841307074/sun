package com.htyoudao.youdao.module.system.api.auth;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.business.dto.BusinessDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * @author dht
 */
@Tag(name = "RPC 服务 - token")
public interface AuthApi {



    @Operation(summary = "刷新用户token", description = "远程调用")
    CommonResult<Boolean> updateToken(String token, String mobile);


    void updateTokenFirst(String token, String mobile);
}
