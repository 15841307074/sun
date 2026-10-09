package com.htyoudao.youdao.module.order.core.ylb.service;

import com.htyoudao.youdao.module.order.core.ylb.ApiRequest;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * <p>
 *
 * </p>
 *
 * @author zhangjihe
 * @since 2025-03-26
 */
@Component
public abstract class BaseService<T> {

    @Value("${ylb.source}")
    protected Integer source;
    @Value("${ylb.secret}")
    protected String secret;

    protected final String OPEN_URL = "https://open.waimai.shenbianvip.com/shop/message/open";
    protected final String VERSION = "v2.0";

    public ApiRequest<T> getApiRequest(T body) {
        long startTime = System.currentTimeMillis() / 1000;
        return new ApiRequest<>(
                Strings.EMPTY,
                this.getCmd(),
                source,
                VERSION,
                (int) startTime,
                body
        );
    }

    /**
     * 获取请求签名
     *
     * @return
     */
    abstract String getSign(ApiRequest<T> request);

    /**
     * 获取请求命令
     *
     * @return
     */
    abstract String getCmd();

}
