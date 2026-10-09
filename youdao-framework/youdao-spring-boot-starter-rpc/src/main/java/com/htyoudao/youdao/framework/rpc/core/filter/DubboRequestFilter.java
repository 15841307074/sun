package com.htyoudao.youdao.framework.rpc.core.filter;

import com.htyoudao.youdao.framework.common.enums.DubboFilterOrderEnum;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.common.util.spring.SpringUtils;
import com.htyoudao.youdao.framework.rpc.core.enumd.RequestLogEnum;
import com.htyoudao.youdao.framework.rpc.core.properties.DubboCustomProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.service.GenericService;

/**
 * Dubbo 日志过滤器
 * <p>
 * 该过滤器通过实现 Dubbo 的 Filter 接口，在服务调用前后记录日志信息
 * 可根据配置开关和日志级别输出不同详细程度的日志信息
 * <p>
 * 激活条件：
 * - 在 Provider 和 Consumer 端都生效
 * - 执行顺序设置为最大值，确保在所有其他过滤器之后执行
 * <p>
 * 使用 SpringUtils 获取配置信息，根据配置决定是否记录日志及日志详细程度
 * <p>
 * 使用 Lombok 的 @Slf4j 注解简化日志记录
 *
 * @author Liu
 */
@Slf4j
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER}, order = DubboFilterOrderEnum.LOG_FILTER)
public class DubboRequestFilter implements Filter {

    /**
     * Dubbo Filter 接口实现方法，处理服务调用逻辑并记录日志
     *
     * @param invoker    Dubbo 服务调用者实例
     * @param invocation 调用的具体方法信息
     * @return 调用结果
     * @throws RpcException 如果调用过程中发生异常
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        DubboCustomProperties properties = SpringUtils.getBean(DubboCustomProperties.class);
        // 如果未开启请求日志记录，则直接执行服务调用并返回结果
        if (!properties.getRequestLog()) {
            return invoker.invoke(invocation);
        }

        // 判断是 Provider 还是 Consumer
        String client = CommonConstants.PROVIDER;
        if (RpcContext.getServiceContext().isConsumerSide()) {
            client = CommonConstants.CONSUMER;
        }

        // 构建基础日志信息
        String baseLog = "Client[" + client + "],InterfaceName=[" + invocation.getInvoker().getInterface().getSimpleName()
                + "],MethodName=[" + invocation.getMethodName() + "]";
        // 根据日志级别输出不同详细程度的日志信息
        if (properties.getLogLevel() == RequestLogEnum.INFO) {
            log.info("DUBBO - 服务调用: {}", baseLog);
        } else {
            log.info("DUBBO - 服务调用: {},Parameter={}", baseLog, invocation.getArguments());
        }

        // 记录调用开始时间
        long startTime = System.currentTimeMillis();
        // 执行接口调用逻辑
        Result result = invoker.invoke(invocation);
        // 计算调用耗时
        long elapsed = System.currentTimeMillis() - startTime;
        // 如果发生异常且调用的不是泛化服务，则记录异常日志
        if (result.hasException() && !invoker.getInterface().equals(GenericService.class)) {
            Throwable exception = result.getException();
            String message = exception.getMessage();
            // 业务异常打印warn级别日志
            if (exception instanceof ServiceException) {
                log.warn("[dubboExceptionHandler][ URI: ({}) DUBBO - 服务调用发生异常({}), 参数({})]", baseLog, message,
                        invocation.getArguments());
            } else {
                log.error("[dubboExceptionHandler][ URI: ({}) DUBBO - 服务调用发生异常({}), 参数({})]", baseLog, message,
                        invocation.getArguments(), exception);
            }
        } else {
            // 根据日志级别输出服务响应信息
            if (properties.getLogLevel() == RequestLogEnum.INFO) {
                log.info("DUBBO - 服务响应: {},SpendTime=[{}ms]", baseLog, elapsed);
            } else if (properties.getLogLevel() == RequestLogEnum.FULL) {
                log.info("DUBBO - 服务响应: {},SpendTime=[{}ms],Response={}", baseLog, elapsed,
                        JsonUtils.toJsonString(new Object[]{result.getValue()}));
            }
        }
        return result;
    }

}
