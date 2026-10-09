package com.htyoudao.youdao.module.analysis.api.test;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.module.analysis.api.test.TestApi;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * details
 *
 * @author liuzhaowang
 */
@DubboService
public class TestApiImpl implements TestApi {
    @Override
    public CommonResult<String> test() {
        return CommonResult.success("test dubbo rpc");
    }
}
