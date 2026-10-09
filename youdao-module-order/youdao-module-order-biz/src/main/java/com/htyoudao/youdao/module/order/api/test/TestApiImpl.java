package com.htyoudao.youdao.module.order.api.test;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.tracer.core.annotation.BizTrace;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Trace;

/**
 * details
 *
 * @author liuzhaowang
 */
@DubboService
public class TestApiImpl implements TestApi {
    @Override
    @BizTrace(id = "678", type = "tested")
    @Trace
    @Tag(key = "", value = "returnedObj")
    public CommonResult<String> test() {
        return CommonResult.success("test dubbo rpc");
    }
}
