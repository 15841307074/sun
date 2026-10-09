package com.htyoudao.youdao.module.system.api.logger;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.system.api.logger.dto.LoginLogCreateReqDTO;
import com.htyoudao.youdao.module.system.enums.ApiConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@Tag(name = "RPC 服务 - 登录日志")
public interface LoginLogApi {

    @Operation(summary = "创建登录日志")
    CommonResult<Boolean> createLoginLog(@Valid @RequestBody LoginLogCreateReqDTO reqDTO);

}
