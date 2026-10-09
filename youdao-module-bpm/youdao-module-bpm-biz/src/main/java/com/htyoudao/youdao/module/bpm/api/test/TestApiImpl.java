package com.htyoudao.youdao.module.bpm.api.test;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
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
