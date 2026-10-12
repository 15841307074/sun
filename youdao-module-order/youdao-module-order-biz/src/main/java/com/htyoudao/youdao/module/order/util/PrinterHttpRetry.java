package com.htyoudao.youdao.module.order.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.SocketTimeoutException;

/** 单张小票仅在连接/响应超时后重发一次。调用方关闭 HTTP 客户端。 */
@Slf4j
public final class PrinterHttpRetry {
    private PrinterHttpRetry() { }

    public static <T> T execute(CloseableHttpClient client, HttpPost request,
                                ResponseHandler<T> handler, String printer) throws IOException {
        try {
            return client.execute(request, handler);
        } catch (SocketTimeoutException | ConnectTimeoutException timeout) {
            if (timeout instanceof ConnectionPoolTimeoutException || Thread.currentThread().isInterrupted()) {
                throw timeout;
            }
            log.info("第三方小票接口超时，重试一次，printer={}, timeoutType={}", printer, timeout.getClass().getSimpleName());
            // 与第一次使用同一份请求内容、打印机及份数；第二次异常交回原有失败处理。
            return client.execute(request, handler);
        }
    }
}
