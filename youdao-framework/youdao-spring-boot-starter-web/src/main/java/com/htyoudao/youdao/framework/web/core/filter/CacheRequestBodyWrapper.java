package com.htyoudao.youdao.framework.web.core.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *  Request Body 缓存 Wrapper
 *
 * @author 0090
 */
public class CacheRequestBodyWrapper extends HttpServletRequestWrapper {

    /**
     * 缓存的内容
     */
    private final byte[] body;
    private final Charset charset;

    public CacheRequestBodyWrapper(HttpServletRequest request, byte[] body) {
        super(request);
        this.charset = extractCharset(request);
        this.body = body;
    }

    private static Charset extractCharset(HttpServletRequest request) {
        String enc = request.getCharacterEncoding();
        return enc != null ? Charset.forName(enc) : StandardCharsets.UTF_8;
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), charset));
    }

    @Override
    public int getContentLength() {
        return body.length;
    }

    @Override
    public long getContentLengthLong() {
        return body.length;
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
        // 返回 ServletInputStream
        return new ServletInputStream() {

            @Override
            public int read() {
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // 仅支持同步场景，直接拒绝异步
                throw new IllegalStateException("Cached wrapper does not support async I/O");
            }

            @Override
            public int available() {
                return inputStream.available();
            }
        };
    }

}
