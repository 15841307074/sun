package com.htyoudao.youdao.gateway.handler;

import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.gateway.util.WebFrameworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.INTERNAL_SERVER_ERROR;
import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;

/**
 * Gateway 的全局异常处理器，将 Exception 翻译成 CommonResult + 对应的异常编号
 * <p>
 * 在功能上，和 youdao-spring-boot-starter-web 的 GlobalExceptionHandler 类是一致的
 *
 * @author 0090
 */
@Component
@Order(-1)
@Slf4j
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @SuppressWarnings("NullableProblems")
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        // 已经 commit，则直接返回异常
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 转换成 CommonResult
        CommonResult<?> result;
        if (ex instanceof NotFoundException) {
            result = notFoundExceptionHandler(exchange, (NotFoundException) ex);
        } else if (ex instanceof ResponseStatusException) {
            result = responseStatusExceptionHandler(exchange, (ResponseStatusException) ex);
        } else {
            result = defaultExceptionHandler(exchange, ex);
        }

        // 返回给前端
        return WebFrameworkUtils.writeJSON(exchange, result);
    }

    /**
     * 处理 Spring Cloud Gateway 默认抛出的 NotFoundException 异常
     */
    private CommonResult<?> notFoundExceptionHandler(ServerWebExchange exchange, NotFoundException ex) {
        ServerHttpRequest request = exchange.getRequest();
        log.error("[notFoundExceptionHandler][ URI: ({}) 发生异常]", request.getURI(), ex);
        return CommonResult.error(NOT_IMPLEMENTED);
    }

    /**
     * 处理 Spring Cloud Gateway 默认抛出的 ResponseStatusException 异常
     */
    private CommonResult<?> responseStatusExceptionHandler(ServerWebExchange exchange, ResponseStatusException ex) {
        ServerHttpRequest request = exchange.getRequest();
        log.error("[responseStatusExceptionHandler][ URI: ({}) 发生异常]", request.getURI(), ex);
        return CommonResult.error(ex.getStatusCode().value(), ex.getReason());
    }

    /**
     * 处理系统异常，兜底处理所有的一切
     */
    @ExceptionHandler(value = Exception.class)
    public CommonResult<?> defaultExceptionHandler(ServerWebExchange exchange, Throwable ex) {
        ServerHttpRequest request = exchange.getRequest();
        log.error("[defaultExceptionHandler][uri({} [{}]) 发生异常]", request.getURI(), request.getMethod(), ex);
        // 返回 ERROR CommonResult
        return CommonResult.error(INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
    }

}
