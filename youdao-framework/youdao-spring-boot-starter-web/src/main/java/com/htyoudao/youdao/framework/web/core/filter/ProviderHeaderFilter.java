package com.htyoudao.youdao.framework.web.core.filter;

import cn.hutool.core.lang.Validator;
import com.htyoudao.youdao.framework.common.enums.DubboFilterOrderEnum;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

/**
 * @author lqman
 */
@Slf4j
@Activate(group = CommonConstants.PROVIDER, order = DubboFilterOrderEnum.HEADER_FILTER)
public class ProviderHeaderFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try {
            // 1. 设置项目ID
            String businessId = RpcContext.getServerAttachment().getAttachment(WebFrameworkUtils.BUSINESS_ID);
            if (Validator.isNumber(businessId)) {
                BusinessContextHolder.setBusinessId(Long.valueOf(businessId));
            }
            // 2. 执行后续调用
            return invoker.invoke(invocation);
        } finally {
            String inner = "injvm";
            if (!inner.equals(invoker.getUrl().getProtocol())) {
                BusinessContextHolder.clear();
            }
        }
    }
}
