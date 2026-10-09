package com.htyoudao.youdao.framework.sentinel.config;

import com.alibaba.csp.sentinel.adapter.dubbo3.config.DubboAdapterGlobalConfig;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.TOO_MANY_REQUESTS;

/**
 * Dubbo Sentinel 配置类
 * <p>
 * 特点：
 * 1. 仅在 SERVLET 环境下启用（排除 Gateway 的 REACTIVE 环境）
 * 2. 需要存在 Dubbo 相关类
 * 3. 配置 Dubbo Sentinel 全局 fallback 处理
 *
 * @author 0090
 */
@Slf4j
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = {
        "org.apache.dubbo.rpc.Filter",
        "com.alibaba.csp.sentinel.adapter.dubbo3.config.DubboAdapterGlobalConfig"
})
public class YoudaoSentinelDubboConfiguration {

    @PostConstruct
    public void init() {
        try {
            // 设置 Consumer 和 Provider 的 fallback 处理
            DubboAdapterGlobalConfig.setConsumerFallback(this::handleBlockException);
            DubboAdapterGlobalConfig.setProviderFallback(this::handleBlockException);
            log.info("[Sentinel] Dubbo Sentinel fallback 处理器设置成功");
        } catch (Throwable ex) {
            log.error("[Sentinel] Dubbo Sentinel fallback 处理器设置失败", ex);
            throw new IllegalStateException("Dubbo Sentinel 初始化失败", ex);
        }
    }

    /**
     * 处理 Sentinel Block 异常
     */
    private Result handleBlockException(Invoker<?> invoker, Invocation invocation, BlockException ex) {
        // 获取接口和方法信息
        String interfaceName = invoker.getInterface().getName();
        String methodName = invocation.getMethodName();
        Class<?>[] parameterTypes = invocation.getParameterTypes();
        String params = Arrays.stream(parameterTypes)
                .map(Class::getSimpleName)
                .collect(Collectors.joining(","));

        // 构建资源标识
        String resourceName = String.format("%s#%s(%s)", interfaceName, methodName, params);

        // 确定异常类型
        String blockType = determineBlockType(ex);

        // 确定是 Provider 还是 Consumer
        String side = RpcContext.getServiceContext().isProviderSide() ? "Provider" : "Consumer";

        // 记录日志
        log.error("[Sentinel {} Block] Resource[{}] BlockType[{}] Exception[{}]",
                side, resourceName, blockType, ex.getClass().getSimpleName());

        // 返回统一的错误响应
        return AsyncRpcResult.newDefaultAsyncResult(new ServiceException(TOO_MANY_REQUESTS), invocation);
    }

    private String determineBlockType(BlockException ex) {
        if (ex instanceof FlowException) {
            return "限流规则";
        } else if (ex instanceof DegradeException) {
            return "熔断规则";
        } else if (ex instanceof ParamFlowException) {
            return "热点参数限流规则";
        } else if (ex instanceof SystemBlockException sys) {
            return "系统保护规则，触发类型：" + sys.getLimitType();
        }
        return "其他规则";
    }
}
