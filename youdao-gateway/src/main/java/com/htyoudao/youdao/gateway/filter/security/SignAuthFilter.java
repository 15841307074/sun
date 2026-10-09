package com.htyoudao.youdao.gateway.filter.security;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.htyoudao.youdao.framework.common.pojo.CommonResult;
import com.htyoudao.youdao.framework.redis.core.utils.RedissonUtils;
import com.htyoudao.youdao.gateway.config.SecurityProperties;
import com.htyoudao.youdao.gateway.util.SignUtils;
import com.htyoudao.youdao.gateway.util.WebFrameworkUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.htyoudao.youdao.framework.common.exception.enums.GlobalErrorCodeConstants.*;

/**
 * 签名验证和防重放攻击过滤器
 * 支持HMAC-SHA256签名算法
 * 集成了签名验证和防重放攻击防护功能
 *
 * @author lqman
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SignAuthFilter implements GlobalFilter, Ordered, InitializingBean {

    /**
     * 请求头名称常量
     */
    private static final String HEADER_TIMESTAMP = "X-Timestamp";
    private static final String HEADER_SIGN = "X-Sign";
    private static final String HEADER_NONCE = "X-Nonce";
    private static final String BUSINESS_ID = "business-id";

    /**
     * Redis缓存前缀
     */
    private static final String CACHE_KEY_PREFIX = "api:replay:";

    /**
     * 日志前缀
     */
    private static final String LOG_PREFIX = "[安全过滤] ";

    /**
     * 过滤器执行顺序
     * 在TokenAuthenticationFilter之前执行
     */
    private static final int FILTER_ORDER = -110;

    /**
     * 逗号分隔符
     */
    private static final String COMMA = ",";

    /**
     * 路径变量参数名
     */
    private static final String X_PATH_VARIABLE = "x-path-variable";

    /**
     * 默认DataBuffer工厂
     */
    private static final DataBufferFactory BUFFER_FACTORY = new DefaultDataBufferFactory();

    /**
     * 安全配置属性
     */
    private final SecurityProperties securityProperties;

    /**
     * 路径匹配器
     */
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 初始化方法，记录安全配置信息
     */
    @Override
    public void afterPropertiesSet() {
        log.info("{}安全过滤器总开关: {}", LOG_PREFIX, securityProperties.isEnabled() ? "开启" : "关闭");

        if (securityProperties.isEnabled()) {
            log.info("{}签名验证: {}", LOG_PREFIX, securityProperties.getSign().isEnabled() ? "开启" : "关闭");
            log.info("{}防重放攻击: {}", LOG_PREFIX, securityProperties.getReplay().isEnabled() ? "开启" : "关闭");

            if (!securityProperties.getWhiteList().isEmpty()) {
                log.info("{}全局白名单路径: {}", LOG_PREFIX, securityProperties.getWhiteList());
            }
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 校验businessId，app-api开头的请求必须携带
        String businessId = request.getHeaders().getFirst(BUSINESS_ID);
        if (securityProperties.isCheckBusiness() && StrUtil.isBlank(businessId) && !isPathExcluded(path)) {
            log.warn("{}缺少必要的请求头: business-id, path: {}", LOG_PREFIX, path);
            return WebFrameworkUtils.writeJSON(exchange, CommonResult.error(REQUEST_LESS));
        }

        // 1. 检查是否需要进行安全验证
        if (shouldSkipSecurityCheck(path)) {
            return chain.filter(exchange);
        }

        // 2. 获取并验证请求头
        String timestamp = request.getHeaders().getFirst(HEADER_TIMESTAMP);
        String signature = request.getHeaders().getFirst(HEADER_SIGN);
        String nonce = request.getHeaders().getFirst(HEADER_NONCE);

        if (StrUtil.hasBlank(timestamp, signature, nonce)) {
            log.warn("{}缺少必要的请求头: timestamp、signature或nonce, path: {}", LOG_PREFIX, path);
            return WebFrameworkUtils.writeJSON(exchange, CommonResult.error(REQUEST_LESS));
        }

        // 3. 验证时间戳
        if (!isTimestampValid(timestamp)) {
            log.warn("{}请求已过期, path: {}, timestamp: {}", LOG_PREFIX, path, timestamp);
            return WebFrameworkUtils.writeJSON(exchange, CommonResult.error(REQUEST_TIMEOUT));
        }

        // 4. 处理请求体
        return processRequestWithBody(exchange, chain, timestamp, signature, nonce, path);
    }

    /**
     * 检查是否应跳过安全检查
     */
    private boolean shouldSkipSecurityCheck(String path) {
        // 检查总开关是否启用
        if (!securityProperties.isEnabled()) {
            log.debug("{}安全过滤器已禁用，跳过安全检查, path: {}", LOG_PREFIX, path);
            return true;
        }

        // 如果安全功能均未启用，直接放行
        if (!securityProperties.getSign().isEnabled() && !securityProperties.getReplay().isEnabled()) {
            log.debug("{}签名验证和防重放攻击均已禁用，跳过安全检查, path: {}", LOG_PREFIX, path);
            return true;
        }

        // 检查白名单，如果在白名单中，直接放行
        if (isPathExcluded(path)) {
            log.debug("{}请求路径在白名单中，跳过安全检查, path: {}", LOG_PREFIX, path);
            return true;
        }

        return false;
    }

    /**
     * 处理请求体并进行验证
     */
    private Mono<Void> processRequestWithBody(ServerWebExchange exchange, GatewayFilterChain chain,
                                              String timestamp, String signature, String nonce, String path) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();

        // 处理OPTIONS请求 - 通常用于CORS预检，应该特殊处理
        if (method == HttpMethod.OPTIONS) {
            log.debug("{}处理OPTIONS预检请求, path: {}", LOG_PREFIX, path);
            return validateAndContinue(exchange, chain, "", timestamp, signature, nonce, path);
        }

        // 判断请求是否不包含请求体
        if (noRequestBody(method)) {
            log.debug("{}处理不包含请求体的HTTP方法: {}, path: {}", LOG_PREFIX, method, path);
            // 对于不包含请求体的方法，直接验证签名
            return validateAndContinue(exchange, chain, "", timestamp, signature, nonce, path);
        }

        // 判断请求体类型
        if (!isJsonRequest(request)) {
            log.debug("{}处理非JSON请求体, path: {}, contentType: {}",
                    LOG_PREFIX, path, request.getHeaders().getContentType());
            return validateAndContinue(exchange, chain, "", timestamp, signature, nonce, path);
        }

        // 读取并缓存请求体
        log.debug("{}处理JSON请求体, path: {}", LOG_PREFIX, path);
        return readAndCacheRequestBody(exchange, chain, timestamp, signature, nonce, path);
    }

    /**
     * 判断HTTP方法是否不包含请求体
     * 根据HTTP规范，某些方法通常不包含请求体
     *
     * @param method HTTP方法
     * @return 是否不包含请求体
     */
    private boolean noRequestBody(HttpMethod method) {
        // GET, HEAD, DELETE, TRACE 通常不包含请求体
        // 虽然HTTP规范不禁止这些方法携带请求体，但大多数客户端不会这样做
        return method == HttpMethod.GET ||
                method == HttpMethod.HEAD ||
                method == HttpMethod.DELETE ||
                method == HttpMethod.TRACE;
    }

    /**
     * 判断请求是否包含JSON请求体
     *
     * @param request HTTP请求
     * @return 是否为JSON请求
     */
    private boolean isJsonRequest(ServerHttpRequest request) {
        MediaType contentType = request.getHeaders().getContentType();
        return contentType != null && contentType.includes(MediaType.APPLICATION_JSON);
    }

    /**
     * 读取并缓存请求体
     */
    private Mono<Void> readAndCacheRequestBody(ServerWebExchange exchange, GatewayFilterChain chain,
                                               String timestamp, String signature, String nonce, String path) {
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .switchIfEmpty(Mono.just(BUFFER_FACTORY.wrap(new byte[0])))
                .flatMap(dataBuffer -> {
                    try {
                        // 读取请求体内容
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        String bodyString = new String(bytes, StandardCharsets.UTF_8);

                        // 记录请求体信息（调试级别，避免记录敏感信息）
                        if (log.isDebugEnabled()) {
                            log.debug("{}读取请求体, path: {}, bodyLength: {}", LOG_PREFIX, path, bytes.length);
                        }

                        // 创建新的DataBuffer，避免释放原始DataBuffer
                        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                        DataBuffer cachedDataBuffer = bufferFactory.wrap(bytes);

                        // 创建请求装饰器，保证请求体可以重复读取
                        ServerHttpRequest mutatedRequest = decorateRequest(exchange.getRequest(), cachedDataBuffer);

                        // 使用新的request创建新的exchange
                        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

                        // 验证签名并继续处理
                        return validateAndContinue(mutatedExchange, chain, bodyString, timestamp, signature, nonce, path);
                    } catch (Exception e) {
                        log.error("{}处理请求体异常, path: {}", LOG_PREFIX, path, e);
                        return WebFrameworkUtils.writeJSON(exchange, CommonResult.error(INTERNAL_SERVER_ERROR));
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }
                })
                .onErrorResume(e -> {
                    log.error("{}读取请求体流异常, path: {}", LOG_PREFIX, path, e);
                    return WebFrameworkUtils.writeJSON(exchange, CommonResult.error(INTERNAL_SERVER_ERROR));
                });
    }

    /**
     * 创建请求装饰器，保证请求体可以重复读取
     */
    private ServerHttpRequest decorateRequest(ServerHttpRequest originalRequest, DataBuffer cachedDataBuffer) {
        return new ServerHttpRequestDecorator(originalRequest) {
            @SuppressWarnings("NullableProblems")
            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.defer(() -> {
                    DataBufferUtils.retain(cachedDataBuffer);
                    return Flux.just(cachedDataBuffer);
                });
            }
        };
    }

    /**
     * 验证签名并继续处理请求
     */
    private Mono<Void> validateAndContinue(ServerWebExchange exchange, GatewayFilterChain chain,
                                           String body, String timestamp, String signature,
                                           String nonce, String path) {
        // 1. 验证签名
        if (securityProperties.getSign().isEnabled()) {
            boolean isSignValid = verifyRequestSignature(exchange, body, timestamp, nonce, signature, path);
            if (!isSignValid) {
                log.warn("{}签名验证失败, path: {}", LOG_PREFIX, path);
                return WebFrameworkUtils.writeJSON(exchange, CommonResult.error(SIGN_EXCEPTION));
            }
            log.debug("{}签名验证通过, path: {}", LOG_PREFIX, path);
        }

        // 2. 防重放攻击检查
        if (securityProperties.getReplay().isEnabled()) {
            boolean isNotReplay = checkReplayAttack(path, nonce, timestamp);
            if (!isNotReplay) {
                log.warn("{}防重放攻击检查失败, path: {}", LOG_PREFIX, path);
                return WebFrameworkUtils.writeJSON(exchange, CommonResult.error(REPEATED_REQUESTS));
            }
            log.debug("{}防重放攻击检查通过, path: {}", LOG_PREFIX, path);
        }

        // 所有安全检查通过，继续处理请求
        return chain.filter(exchange);
    }

    /**
     * 验证请求签名
     *
     * @return 签名是否有效
     */
    private boolean verifyRequestSignature(ServerWebExchange exchange, String body,
                                           String timestamp, String nonce, String signature, String path) {
        // 获取所有参数，包括URL参数、路径变量和请求体
        TreeMap<String, Object> allParams = new TreeMap<>(getAllRequestParams(exchange.getRequest(), body));

        // 添加时间戳和随机数
        allParams.put("timestamp", timestamp);
        allParams.put("nonce", nonce);

        // 验证签名
        String secret = securityProperties.getSign().getSecret();
        String calculatedSign = SignUtils.calculateSign(allParams, secret);

        boolean isValid = signature.equalsIgnoreCase(calculatedSign);
        if (!isValid) {
            log.warn("{}签名无效, path: {}, timestamp: {}, nonce: {}, signature: {}, calculatedSign: {}",
                    LOG_PREFIX, path, timestamp, nonce, signature, calculatedSign);
        }

        return isValid;
    }

    /**
     * 获取所有请求参数，包括URL参数、路径变量和请求体
     *
     * @param request HTTP请求
     * @param body    请求体字符串
     * @return 包含所有参数的有序Map
     */
    private SortedMap<String, Object> getAllRequestParams(ServerHttpRequest request, String body) {
        SortedMap<String, Object> result = new TreeMap<>();

        // 1. 处理路径变量（带逗号的路径参数）
        String path = request.getURI().getPath();
        String pathVariable = path.substring(path.lastIndexOf("/") + 1);
        if (pathVariable.contains(COMMA)) {
            log.debug("{}pathVariable: {}", LOG_PREFIX, pathVariable);
            try {
                String deString = URLDecoder.decode(pathVariable, StandardCharsets.UTF_8);

                // 处理双重编码的情况
                if (deString.contains("%")) {
                    try {
                        deString = URLDecoder.decode(deString, StandardCharsets.UTF_8);
                        log.debug("{}存在%情况下，执行两次解码 — pathVariable decode: {}", LOG_PREFIX, deString);
                    } catch (Exception e) {
                        // 忽略第二次解码的异常
                    }
                }
                log.debug("{}pathVariable decode: {}", LOG_PREFIX, deString);
                result.put(X_PATH_VARIABLE, deString);
            } catch (Exception e) {
                log.error("{}URL解码异常", LOG_PREFIX, e);
            }
        }

        // 2. 处理URL查询参数
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        String undefined = "undefined";
        queryParams.forEach((key, values) -> {
            if (!values.isEmpty()) {
                String value = values.get(0);
                if (!undefined.equals(value)) {
                    result.put(key, value);
                }
            }
        });

        // 3. 处理请求体参数
        if (StringUtils.hasText(body)) {
            try {
                Map<String, Object> bodyParams = JSONObject.parseObject(body);
                if (bodyParams != null) {
                    result.putAll(bodyParams);
                }
            } catch (Exception e) {
                log.warn("{}解析请求体JSON失败: {}", LOG_PREFIX, body, e);
                // 如果解析失败，作为普通字符串处理
                result.put("body", body);
            }
        }

        return result;
    }

    /**
     * 检查是否为重放攻击
     *
     * @return true表示非重放攻击，false表示检测到重放攻击
     */
    private boolean checkReplayAttack(String path, String nonce, String timestamp) {
        String cacheKey = generateCacheKey(path, nonce);
        boolean isFirstRequest = RedissonUtils.setObjectIfAbsent(
                cacheKey,
                timestamp,
                Duration.ofSeconds(securityProperties.getReplay().getWindowSeconds())
        );

        if (!isFirstRequest) {
            log.warn("{}检测到重复请求, path: {}, nonce: {}", LOG_PREFIX, path, nonce);
        }

        return isFirstRequest;
    }

    /**
     * 生成缓存键
     */
    private String generateCacheKey(String path, String nonce) {
        return CACHE_KEY_PREFIX + getPathIdentifier(path) + ":" + nonce;
    }

    /**
     * 获取路径标识符
     * 使用请求路径的哈希值作为标识符，避免路径过长
     * 同时保留路径的前缀部分，便于识别和调试
     */
    private String getPathIdentifier(String path) {
        if (StrUtil.isBlank(path)) {
            return "blank";
        }

        // 获取路径的第一部分作为前缀，例如 "/admin-api"
        String prefix = path.split("/", 3).length > 1 ?
                "/" + path.split("/", 3)[1] :
                path;

        // 计算路径的哈希值，确保唯一性
        int hashCode = path.hashCode();
        String shortHash = Integer.toUnsignedString(hashCode, 36);

        return prefix + ":" + shortHash;
    }

    /**
     * 检查路径是否排除
     */
    private boolean isPathExcluded(String path) {
        return securityProperties.getWhiteList().stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 验证时间戳是否有效
     */
    private boolean isTimestampValid(String timestamp) {
        try {
            long requestTime = Long.parseLong(timestamp);
            long currentTime = System.currentTimeMillis();
            long expireMillis = securityProperties.getSign().getExpireSeconds() * 1000L;
            return Math.abs(currentTime - requestTime) <= expireMillis;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public int getOrder() {
        // 在TokenAuthenticationFilter之前执行
        return FILTER_ORDER;
    }
}
