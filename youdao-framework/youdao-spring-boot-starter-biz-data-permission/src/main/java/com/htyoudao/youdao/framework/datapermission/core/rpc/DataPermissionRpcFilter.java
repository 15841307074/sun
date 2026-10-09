package com.htyoudao.youdao.framework.datapermission.core.rpc;

import com.htyoudao.youdao.framework.common.enums.DubboFilterOrderEnum;
import com.htyoudao.youdao.framework.datapermission.core.annotation.DataPermission;
import com.htyoudao.youdao.framework.datapermission.core.aop.DataPermissionContextHolder;
import com.htyoudao.youdao.framework.datapermission.core.util.DataPermissionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Objects;

/**
 * @author lqman
 */
@Slf4j
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER}, order = DubboFilterOrderEnum.DATA_PERMISSION_FILTER)
public class DataPermissionRpcFilter implements Filter {

    public static final String DATA_PERMISSION_ENABLE = "data-permission-enable";

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        // 消费端：传递权限标识
        if (RpcContext.getServiceContext().isConsumerSide()) {

            DataPermission dataPermission = DataPermissionContextHolder.get();
            if (dataPermission != null && !dataPermission.enable()) {
                RpcContext.getServerAttachment().setAttachment(DATA_PERMISSION_ENABLE, Boolean.FALSE.toString());
            }
        }

        // 提供端：读取权限标识
        if (RpcContext.getServiceContext().isProviderSide()) {
            String enableFlag = RpcContext.getServerAttachment().getAttachment(DATA_PERMISSION_ENABLE);
            if (Objects.equals(enableFlag, Boolean.FALSE.toString())) {
                return DataPermissionUtils.executeRpcIgnore(() -> invoker.invoke(invocation), invoker);
            }
        }

        return invoker.invoke(invocation);
    }

}
