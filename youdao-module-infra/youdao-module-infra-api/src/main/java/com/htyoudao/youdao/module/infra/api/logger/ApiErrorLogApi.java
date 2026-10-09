package com.htyoudao.youdao.module.infra.api.logger;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.infra.api.logger.dto.ApiErrorLogCreateReqDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@Tag(name = "RPC 服务 - API 异常日志")
public interface ApiErrorLogApi {

    @Operation(summary = "创建 API 异常日志")
    CommonResult<Boolean> createApiErrorLog(@Valid @RequestBody ApiErrorLogCreateReqDTO createDTO);

    /**
     * 【异步】创建 API 异常日志
     *
     * @param createDTO 异常日志 DTO
     */
    @Async
    default void createApiErrorLogAsync(ApiErrorLogCreateReqDTO createDTO) {
        createApiErrorLog(createDTO).checkError();
    }

}
