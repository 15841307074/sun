package com.htyoudao.youdao.module.infra.api.logger;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.infra.api.logger.dto.ApiAccessLogCreateReqDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@Tag(name = "RPC 服务 - API 访问日志")
public interface ApiAccessLogApi {

    @Operation(summary = "创建 API 访问日志")
    CommonResult<Boolean> createApiAccessLog(@Valid @RequestBody ApiAccessLogCreateReqDTO createDTO);

    /**
     * 【异步】创建 API 访问日志
     *
     * @param createDTO 访问日志 DTO
     */
    @Async
    default void createApiAccessLogAsync(ApiAccessLogCreateReqDTO createDTO) {
        createApiAccessLog(createDTO).checkError();
    }

}
