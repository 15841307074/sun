package com.htyoudao.youdao.framework.web.core.handler;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.htyoudao.youdao.framework.common.exception.ServiceException;
import com.htyoudao.youdao.framework.common.exception.util.ServiceExceptionUtil;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.common.util.collection.SetUtils;
import com.htyoudao.youdao.framework.common.util.json.JsonUtils;
import com.htyoudao.youdao.framework.common.util.monitor.TracerUtils;
import com.htyoudao.youdao.framework.common.util.servlet.ServletUtils;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import com.htyoudao.youdao.module.infra.api.logger.ApiErrorLogApi;
import com.htyoudao.youdao.module.infra.api.logger.dto.ApiErrorLogCreateReqDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.*;

/**
 * 全局异常处理器，将 Exception 翻译成 CommonResult + 对应的异常编号
 *
 * @author 0090
 */
@RestControllerAdvice
@AllArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 忽略的 ServiceException 错误提示，避免打印过多 logger
     */
    public static final Set<String> IGNORE_ERROR_MESSAGES = SetUtils.asSet("无效的刷新令牌");

    private static final ThreadLocal<Boolean> LOGGING_EXCEPTION = new TransmittableThreadLocal<>();

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String applicationName;

    private final ApiErrorLogApi apiErrorLogApi;

    /**
     * 处理所有异常，主要是提供给 Filter 使用
     * 因为 Filter 不走 SpringMVC 的流程，但是我们又需要兜底处理异常，所以这里提供一个全量的异常处理过程，保持逻辑统一。
     *
     * @param request 请求
     * @param ex      异常
     * @return 通用返回
     */
    public CommonResult<?> allExceptionHandler(HttpServletRequest request, Throwable ex) {
        if (ex instanceof MissingServletRequestParameterException) {
            return missingServletRequestParameterExceptionHandler((MissingServletRequestParameterException) ex);
        }
        if (ex instanceof MethodArgumentTypeMismatchException) {
            return methodArgumentTypeMismatchExceptionHandler((MethodArgumentTypeMismatchException) ex);
        }
        if (ex instanceof MethodArgumentNotValidException) {
            return methodArgumentNotValidExceptionExceptionHandler((MethodArgumentNotValidException) ex);
        }
        if (ex instanceof BindException) {
            return bindExceptionHandler((BindException) ex);
        }
        if (ex instanceof ConstraintViolationException) {
            return constraintViolationExceptionHandler((ConstraintViolationException) ex);
        }
        if (ex instanceof ValidationException) {
            return validationException((ValidationException) ex);
        }
        if (ex instanceof NoHandlerFoundException) {
            return noHandlerFoundExceptionHandler((NoHandlerFoundException) ex);
        }
        if (ex instanceof NoResourceFoundException) {
            return noResourceFoundExceptionHandler((NoResourceFoundException) ex);
        }
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return httpRequestMethodNotSupportedExceptionHandler((HttpRequestMethodNotSupportedException) ex);
        }
        if (ex instanceof ServiceException) {
            return serviceExceptionHandler((ServiceException) ex);
        }
        if (ex instanceof AsyncRequestNotUsableException) {
            return asyncRequestNotUsableExceptionHandler((AsyncRequestNotUsableException) ex);
        }
        if (ex instanceof RpcException) {
            return rpcExceptionHandler(request, (RpcException) ex);
        }
        if (ex instanceof AccessDeniedException) {
            return accessDeniedExceptionHandler(request, (AccessDeniedException) ex);
        }
        return defaultExceptionHandler(request, ex);
    }

    /**
     * 处理 SpringMVC 请求参数缺失
     * <p>
     * 例如说，接口上设置了 @RequestParam("xx") 参数，结果并未传递 xx 参数
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public CommonResult<?> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException ex) {
        printArgErrorLog("missingServletRequestParameterExceptionHandler", ex);
        return CommonResult.error(BAD_REQUEST.getCode(), String.format("请求参数缺失:%s", ex.getParameterName()));
    }

    /**
     * 处理 SpringMVC 请求参数类型错误
     * <p>
     * 例如说，接口上设置了 @RequestParam("xx") 参数为 Integer，结果传递 xx 参数类型为 String
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public CommonResult<?> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException ex) {
        printArgErrorLog("methodArgumentTypeMismatchExceptionHandler", ex);
        return CommonResult.error(BAD_REQUEST.getCode(), String.format("请求参数类型错误:%s", ex.getMessage()));
    }

    /**
     * 处理 SpringMVC 参数校验不正确
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<?> methodArgumentNotValidExceptionExceptionHandler(MethodArgumentNotValidException ex) {
        // 增加调试日志
        printArgErrorLog("methodArgumentNotValidExceptionExceptionHandler", ex);
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数格式错误";
        return CommonResult.error(BAD_REQUEST.getCode(), String.format("请求参数不正确:%s", msg));
    }

    /**
     * 处理 SpringMVC 参数绑定不正确，本质上也是通过 Validator 校验
     */
    @ExceptionHandler(BindException.class)
    public CommonResult<?> bindExceptionHandler(BindException ex) {
        printArgErrorLog("handleBindException", ex);

        FieldError fieldError = ex.getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数格式错误";
        return CommonResult.error(BAD_REQUEST.getCode(), String.format("请求参数不正确:%s", msg));
    }

    /**
     * 处理 SpringMVC 请求参数类型错误
     * <p>
     * 例如说，接口上设置了 @RequestBody实体中 xx 属性类型为 Integer，结果传递 xx 参数类型为 String
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public CommonResult<?> methodArgumentTypeInvalidFormatExceptionHandler(HttpMessageNotReadableException ex) {
        // 增加调试日志
        printArgErrorLog("methodArgumentTypeInvalidFormatExceptionHandler", ex);
        if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {
            return CommonResult.error(BAD_REQUEST.getCode(), String.format("请求参数类型错误:%s", invalidFormatException.getValue()));
        } else {
            return defaultExceptionHandler(Objects.requireNonNull(ServletUtils.getRequest()), ex);
        }
    }

    private void printArgErrorLog(String handler, Throwable ex) {
        ApiErrorLogCreateReqDTO errorLog = new ApiErrorLogCreateReqDTO();
        buildExceptionLog(errorLog, ServletUtils.getRequest(), ex, false);
        log.warn("[{}]参数错误调试日志：({})", handler, JSON.toJSONString(errorLog));
    }

    /**
     * 处理 Validator 校验不通过产生的异常
     */
    @ExceptionHandler(value = ConstraintViolationException.class)
    public CommonResult<?> constraintViolationExceptionHandler(ConstraintViolationException ex) {
        printArgErrorLog("constraintViolationExceptionHandler", ex);
        ConstraintViolation<?> constraintViolation = ex.getConstraintViolations().iterator().next();
        return CommonResult.error(BAD_REQUEST.getCode(), String.format("请求参数不正确:%s", constraintViolation.getMessage()));
    }

    /**
     * 处理 Dubbo Consumer 本地参数校验时，抛出的 ValidationException 异常
     */
    @ExceptionHandler(value = ValidationException.class)
    public CommonResult<?> validationException(ValidationException ex) {
        log.warn("[validationException]", ex);
        // 无法拼接明细的错误信息，因为 Dubbo Consumer 抛出 ValidationException 异常时，是直接的字符串信息，且人类不可读
        return CommonResult.error(BAD_REQUEST);
    }

    /**
     * 处理 SpringMVC 请求地址不存在
     * <p>
     * 注意，它需要设置如下两个配置项：
     * 1. spring.mvc.throw-exception-if-no-handler-found 为 true
     * 2. spring.mvc.static-path-pattern 为 /statics/**
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public CommonResult<?> noHandlerFoundExceptionHandler(NoHandlerFoundException ex) {
        log.warn("[noHandlerFoundExceptionHandler]", ex);
        return CommonResult.error(NOT_FOUND.getCode(), String.format("请求地址不存在:%s", ex.getRequestURL()));
    }

    /**
     * 处理 SpringMVC 请求地址不存在
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public CommonResult<?> noResourceFoundExceptionHandler(NoResourceFoundException ex) {
        log.warn("[noResourceFoundExceptionHandler]", ex);
        return CommonResult.error(NOT_FOUND.getCode(), String.format("请求地址不存在:%s", ex.getResourcePath()));
    }

    /**
     * 处理 SpringMVC 请求方法不正确
     * <p>
     * 例如说，A 接口的方法为 GET 方式，结果请求方法为 POST 方式，导致不匹配
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public CommonResult<?> httpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException ex) {
        log.warn("[httpRequestMethodNotSupportedExceptionHandler]", ex);
        return CommonResult.error(METHOD_NOT_ALLOWED.getCode(), String.format("请求方法不正确:%s", ex.getMessage()));
    }

    /**
     * 处理 Spring Security 权限不足的异常
     * <p>
     * 来源是，使用 @PreAuthorize 注解，AOP 进行权限拦截
     */
    @ExceptionHandler(value = AccessDeniedException.class)
    public CommonResult<?> accessDeniedExceptionHandler(HttpServletRequest req, AccessDeniedException ex) {
        log.warn("[accessDeniedExceptionHandler][userId({}) 无法访问 url({})]", WebFrameworkUtils.getLoginUserId(req),
                req.getRequestURL(), ex);
        return CommonResult.error(FORBIDDEN);
    }

    /**
     * 处理业务异常 ServiceException
     * <p>
     * 例如说，商品库存不足，用户手机号已存在。
     */
    @ExceptionHandler(value = ServiceException.class)
    public CommonResult<?> serviceExceptionHandler(ServiceException ex) {
        // 不包含的时候，才进行打印，避免 ex 堆栈过多
        if (!IGNORE_ERROR_MESSAGES.contains(ex.getMessage())) {
            // 即使打印，也只打印第一层 StackTraceElement，并且使用 warn 在控制台输出，更容易看到
            try {
                StackTraceElement[] stackTraces = ex.getStackTrace();
                for (StackTraceElement stackTrace : stackTraces) {
                    if (ObjUtil.notEqual(stackTrace.getClassName(), ServiceExceptionUtil.class.getName())) {
                        log.warn("[serviceExceptionHandler]\n\t{}", stackTrace);
                        break;
                    }
                }
            } catch (Exception ignored) {
                // 忽略日志，避免影响主流程
            }
        }
        return CommonResult.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(AsyncRequestNotUsableException.class)
    public CommonResult<Void> asyncRequestNotUsableExceptionHandler(AsyncRequestNotUsableException ex) {
        log.warn("[asyncRequestNotUsableExceptionHandler]客户端连接已断开：{}", ex.getMessage());
        // 不需要返回结果
        return new CommonResult<>();
    }

    @ExceptionHandler(RpcException.class)
    public CommonResult<Void> rpcExceptionHandler(HttpServletRequest req, RpcException e) {
        String uri = req != null ? req.getRequestURI() : "unknown";
        log.error("[rpcExceptionHandler][ URI: ({}) 发生异常]", uri, e);
        return CommonResult.error(RPC_EXCEPTION);
    }

    /**
     * 处理系统异常，兜底处理所有的一切
     */
    @ExceptionHandler(value = Exception.class)
    public CommonResult<?> defaultExceptionHandler(HttpServletRequest req, Throwable ex) {
        // 处理异常
        log.error("[defaultExceptionHandler][ URI: ({}) 发生异常]", req.getRequestURI(), ex);
        createExceptionLog(req, ex);
        // 返回 ERROR CommonResult
        return CommonResult.error(INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR.getMsg());
    }

    private void createExceptionLog(HttpServletRequest req, Throwable e) {
        // 避免递归
        if (Boolean.TRUE.equals(LOGGING_EXCEPTION.get())) {
            return;
        }
        LOGGING_EXCEPTION.set(true);

        // 插入错误日志
        ApiErrorLogCreateReqDTO errorLog = new ApiErrorLogCreateReqDTO();
        try {
            // 初始化 errorLog
            buildExceptionLog(errorLog, req, e, true);
            // 执行插入 errorLog
            apiErrorLogApi.createApiErrorLogAsync(errorLog);
        } catch (Throwable th) {
            log.error("[createExceptionLog][url({}) 记录日志失败]", req.getRequestURI(), th);
        } finally {
            LOGGING_EXCEPTION.remove();
        }
    }

    private void buildExceptionLog(ApiErrorLogCreateReqDTO errorLog, HttpServletRequest request, Throwable e, boolean detailEx) {
        // 设置异常字段
        errorLog.setExceptionMessage(ExceptionUtil.getMessage(e));
        // 是否打印详细的异常信息
        if (detailEx) {
            errorLog.setExceptionName(e.getClass().getName());
            errorLog.setExceptionRootCauseMessage(ExceptionUtil.getRootCauseMessage(e));
            errorLog.setExceptionStackTrace(ExceptionUtil.stacktraceToString(e));
            StackTraceElement[] stackTraceElements = e.getStackTrace();
            if (stackTraceElements.length > 0) {
                StackTraceElement stackTraceElement = stackTraceElements[0];
                errorLog.setExceptionClassName(stackTraceElement.getClassName());
                errorLog.setExceptionFileName(stackTraceElement.getFileName());
                errorLog.setExceptionMethodName(stackTraceElement.getMethodName());
                errorLog.setExceptionLineNumber(stackTraceElement.getLineNumber());
            }
        }
        // 设置其它字段
        errorLog.setTraceId(TracerUtils.getTraceId());
        errorLog.setApplicationName(applicationName);

        if (request != null) {
            // 处理用户信息
            errorLog.setUserId(WebFrameworkUtils.getLoginUserId(request));
            errorLog.setUserType(WebFrameworkUtils.getLoginUserType(request));
            errorLog.setRequestUrl(request.getRequestURI());

            String paramStr = "";
            try {
                Map<String, Object> requestParams = MapUtil.<String, Object>builder()
                        .put("query", JakartaServletUtil.getParamMap(request))
                        .put("body", JakartaServletUtil.getBody(request)).build();
                paramStr = JsonUtils.toJsonString(requestParams);
                int maxLength = 8000;
                if (paramStr.length() > maxLength) {
                    paramStr = paramStr.substring(0, maxLength - 1);
                }
            } catch (Exception ignore) {
            }
            errorLog.setRequestHeader(JakartaServletUtil.getHeadersMap(request));
            errorLog.setRequestParams(paramStr);
            errorLog.setRequestMethod(request.getMethod());
            errorLog.setUserAgent(ServletUtils.getUserAgent(request));
            errorLog.setUserIp(JakartaServletUtil.getClientIP(request));
        }
        errorLog.setExceptionTime(LocalDateTime.now());
    }

}
