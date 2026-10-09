package com.htyoudao.youdao.gateway.sentinel;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.TOO_MANY_REQUESTS;

/**
 * 自定义gateway sentinel异常处理
 *
 * @author liuzhaowang
 */
@Slf4j
@Configuration
public class YoudaoSentinelGatewayConfiguration {

    @PostConstruct
    public void init() {
        log.info("[Sentinel Gateway] GatewayCallbackManager配置完成");
        GatewayCallbackManager.setBlockHandler((exchange, ex) -> {
            if (ex instanceof BlockException bex) {
                String blockType = determineBlockType(bex);
                log.error("[SentinelGatewayBlock][ URI: ({}) BlockType({}) Rule({}) Exception({})", exchange.getRequest().getURI(),
                        blockType, bex.getRule(), bex.getClass().getSimpleName());
            }
            return ServerResponse.status(TOO_MANY_REQUESTS.getCode()).body(Mono.just(TOO_MANY_REQUESTS.getMsg()), String.class);
        });
    }

    private String determineBlockType(BlockException ex) {
        if (ex instanceof FlowException) {
            return "限流规则";
        } else if (ex instanceof ParamFlowException) {
            return "分组限流规则";
        } else if (ex instanceof SystemBlockException sys) {
            return "系统保护规则，触发类型：" + sys.getLimitType();
        }
        return "其他规则";
    }
}
