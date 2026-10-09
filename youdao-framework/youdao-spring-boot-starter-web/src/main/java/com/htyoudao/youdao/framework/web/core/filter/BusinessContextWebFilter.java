package com.htyoudao.youdao.framework.web.core.filter;

import com.htyoudao.youdao.framework.context.BusinessContextHolder;
import com.htyoudao.youdao.framework.web.core.util.WebFrameworkUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


public class BusinessContextWebFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 设置
        Long businessId = WebFrameworkUtils.getBusinessId(request);
        if (businessId != null) {
            BusinessContextHolder.setBusinessId(businessId);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            // 清理
            BusinessContextHolder.clear();
        }
    }

}
