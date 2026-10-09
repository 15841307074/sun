package com.htyoudao.youdao.module.infra.api.config;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "RPC 服务 - 参数配置")
public interface ConfigApi {


    @Operation(summary = "根据参数键查询参数值")
    CommonResult<String> getConfigValueByKey(@RequestParam("key") String key);

}
