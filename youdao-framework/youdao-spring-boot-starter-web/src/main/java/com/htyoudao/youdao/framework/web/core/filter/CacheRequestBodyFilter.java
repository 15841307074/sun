package com.htyoudao.youdao.framework.web.core.filter;

import com.htyoudao.youdao.framework.common.util.servlet.ServletUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Request Body 缓存 Filter，实现它的可重复读取
 *
 * @author 0090
 */
@Slf4j
public class CacheRequestBodyFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        long len = request.getContentLengthLong();
        long maxBytes = 1024 * 1024;
        if (len > maxBytes) {
            response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }

        byte[] body;
        try {
            body = ServletUtils.getBodyBytes(request);
        } catch (Exception e) {
            log.warn("Body cache failed, uri={}", request.getRequestURI(), e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        filterChain.doFilter(new CacheRequestBodyWrapper(request, body), response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 只处理 json 请求内容
        return !ServletUtils.isJsonRequest(request);
    }

}
