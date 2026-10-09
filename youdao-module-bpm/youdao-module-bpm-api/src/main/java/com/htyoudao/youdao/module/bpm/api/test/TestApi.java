package com.htyoudao.youdao.module.bpm.api.test;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * details
 *
 * @author liuzhaowang
 */
@Tag(name = "测试")
public interface TestApi {

    @Operation(summary = "测试")
    CommonResult<String> test();
}
