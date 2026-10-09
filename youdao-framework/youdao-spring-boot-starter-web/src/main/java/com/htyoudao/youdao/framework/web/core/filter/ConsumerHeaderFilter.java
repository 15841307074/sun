package com.htyoudao.youdao.framework.web.core.filter;

import com.htyoudao.youdao.framework.common.enums.DubboFilterOrderEnum;
import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Objects;

/**
 * @author lqman
 */
@Slf4j
@Activate(group = CommonConstants.CONSUMER, order = DubboFilterOrderEnum.HEADER_FILTER)
public class ConsumerHeaderFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Long businessId = BusinessContextHolder.getBusinessId();
        if (Objects.nonNull(businessId)) {
            RpcContext.getClientAttachment().setAttachment(WebFrameworkUtils.BUSINESS_ID, businessId.toString());
        }
        return invoker.invoke(invocation);
    }
}
